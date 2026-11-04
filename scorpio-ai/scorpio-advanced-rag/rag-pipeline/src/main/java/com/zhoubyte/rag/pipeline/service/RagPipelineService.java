package com.zhoubyte.rag.pipeline.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.zhoubyte.rag.pipeline.config.RagProperties;
import com.zhoubyte.rag.pipeline.model.Citation;
import com.zhoubyte.rag.pipeline.model.DocumentChunk;
import com.zhoubyte.rag.pipeline.model.FusedCandidate;
import com.zhoubyte.rag.pipeline.model.PipelineTrace;
import com.zhoubyte.rag.pipeline.model.PreprocessResult;
import com.zhoubyte.rag.pipeline.model.RagAnswer;
import com.zhoubyte.rag.pipeline.model.RecallResult;
import com.zhoubyte.rag.pipeline.pipeline.assemble.AssembledContext;
import com.zhoubyte.rag.pipeline.pipeline.assemble.ContextAssembler;
import com.zhoubyte.rag.pipeline.pipeline.fusion.RrfFusionService;
import com.zhoubyte.rag.pipeline.pipeline.preprocess.MetadataFilterResolver;
import com.zhoubyte.rag.pipeline.pipeline.preprocess.QueryPreProcessor;
import com.zhoubyte.rag.pipeline.pipeline.preprocess.QueryRewriter;
import com.zhoubyte.rag.pipeline.pipeline.recall.RecallChannel;
import com.zhoubyte.rag.pipeline.pipeline.rerank.LlmRerankService;
import com.zhoubyte.rag.pipeline.pipeline.verify.AnswerVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 【编排】RAG 流水线主服务。
 *
 * <pre>
 * Query → ①前置处理（清洗/意图/RoaringBitmap过滤） → ②Query改写
 *       → ③BM25/稀疏/稠密 多路并行召回 → ④RRF融合 → ⑤LLM重排
 *       → ⑥上下文组装+溯源锚定 → ⑦受约束生成 → ⑧LLM事实校验 → 带引用答案
 * </pre>
 * 任一环节判断“无法基于资料回答”都会短路返回兜底话术，宁可不答不给错答。
 */
@Service
public class RagPipelineService {

    private static final Logger log = LoggerFactory.getLogger(RagPipelineService.class);
    private static final String REFUSED_ANSWER = "资料不足，无法根据现有知识库回答该问题。";
    private static final int FUSION_LIMIT = 30;
    private static final String GENERATION_SYSTEM = """
            你是企业知识库问答助手，必须严格遵守：
            1. 只依据【参考资料】回答，禁止使用资料之外的任何知识；
            2. 答案中引用某条资料时，必须在该论断后标注其锚点编号，如 [1]、[2]；
            3. 禁止跨文档拼接推断、禁止推断原文没有的信息；
            4. 若参考资料不足以完整回答，必须只回答：“资料不足，无法根据现有知识库回答该问题。”""";

    private final QueryPreProcessor preProcessor;
    private final MetadataFilterResolver filterResolver;
    private final QueryRewriter queryRewriter;
    private final List<RecallChannel> recallChannels;
    private final RrfFusionService rrfFusionService;
    private final LlmRerankService rerankService;
    private final ContextAssembler contextAssembler;
    private final AnswerVerifier answerVerifier;
    private final ChatClient generationClient;
    private final ChatClient utilityClient;
    private final ElasticsearchClient elasticsearchClient;
    private final RagProperties properties;

    private final ExecutorService recallExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public RagPipelineService(QueryPreProcessor preProcessor, MetadataFilterResolver filterResolver,
                              QueryRewriter queryRewriter, List<RecallChannel> recallChannels,
                              RrfFusionService rrfFusionService, LlmRerankService rerankService,
                              ContextAssembler contextAssembler, AnswerVerifier answerVerifier,
                              ChatClient generationClient, ChatClient utilityClient,
                              ElasticsearchClient elasticsearchClient, RagProperties properties) {
        this.preProcessor = preProcessor;
        this.filterResolver = filterResolver;
        this.queryRewriter = queryRewriter;
        this.recallChannels = recallChannels;
        this.rrfFusionService = rrfFusionService;
        this.rerankService = rerankService;
        this.contextAssembler = contextAssembler;
        this.answerVerifier = answerVerifier;
        this.generationClient = generationClient;
        this.utilityClient = utilityClient;
        this.elasticsearchClient = elasticsearchClient;
        this.properties = properties;
    }

    /**
     * 执行完整 RAG 流水线。
     */
    public RagAnswer ask(String rawQuery) {
        PipelineTrace trace = new PipelineTrace();

        // ① 前置处理：清洗 + 意图分类 + 过滤条件抽取
        long start = System.currentTimeMillis();
        PreprocessResult pre = preProcessor.process(rawQuery);
        trace.add("1.前置处理", cost(start), "意图=%s, 清洗=%s".formatted(pre.intent(), pre.cleanedQuery()));

        // 寒暄/通用常识：跳过检索直接回答
        if ("CHITCHAT".equalsIgnoreCase(pre.intent())) {
            String answer = utilityClient.prompt()
                    .user("友好、简短地回答用户（无需检索知识库）：%s".formatted(rawQuery))
                    .call().content();
            return new RagAnswer(rawQuery, answer, List.of(), false, trace);
        }

        // ② 元数据过滤：RoaringBitmap 集合运算得到 seq 白名单
        start = System.currentTimeMillis();
        Optional<Set<Integer>> allowed = filterResolver.resolve(pre.filters());
        trace.add("2.元数据过滤", cost(start), allowed
                .map(seqs -> "RoaringBitmap 白名单命中 %d 个 chunk".formatted(seqs.size()))
                .orElse("无过滤条件"));

        // ③ Query 改写：提升召回覆盖率
        start = System.currentTimeMillis();
        List<String> queries = queryRewriter.rewrite(pre.cleanedQuery());
        trace.add("3.Query改写", cost(start), queries.toString());

        // ④ 多路并行召回（改写Query × 通道 全组合，虚拟线程并行）
        start = System.currentTimeMillis();
        List<CompletableFuture<List<RecallResult>>> futures = new ArrayList<>();
        for (String query : queries) {
            for (RecallChannel channel : recallChannels) {
                futures.add(CompletableFuture.supplyAsync(
                        () -> channel.recall(query, allowed, properties.channelTopK()), recallExecutor));
            }
        }
        List<List<RecallResult>> channelResults = futures.stream().map(CompletableFuture::join).toList();
        long totalHits = channelResults.stream().mapToLong(List::size).sum();
        trace.add("4.多路并行召回", cost(start), "%d 路(通道%s) × %d query，共命中 %d 条"
                .formatted(channelResults.size(), channelNames(), queries.size(), totalHits));

        // ⑤ RRF 融合：合并去重、统一打分
        start = System.currentTimeMillis();
        List<FusedCandidate> fused = rrfFusionService.fuse(channelResults, properties.rrfK(), FUSION_LIMIT);
        if (fused.isEmpty()) {
            return refuse(rawQuery, trace, "5.RRF融合", "融合后无候选");
        }
        trace.add("5.RRF融合", cost(start), "融合后 %d 个候选，Top1 RRF分=%.4f"
                .formatted(fused.size(), fused.get(0).score()));

        // 拉取候选 chunk 原文
        Map<Integer, DocumentChunk> chunkMap = fetchChunks(fused.stream().map(FusedCandidate::chunkSeq).toList());
        if (chunkMap.isEmpty()) {
            return refuse(rawQuery, trace, "6.候选拉取", "候选 chunk 均不存在");
        }

        // ⑥ LLM 重排：剔除语义相似但答非所问的垃圾 chunk
        start = System.currentTimeMillis();
        List<FusedCandidate> reranked = rerankService.rerank(pre.cleanedQuery(), fused, chunkMap);
        if (reranked.isEmpty()) {
            return refuse(rawQuery, trace, "6.LLM重排", "全部候选相关性低于阈值 %.1f".formatted(properties.rerankThreshold()));
        }
        List<DocumentChunk> topChunks = reranked.stream()
                .map(c -> chunkMap.get(c.chunkSeq()))
                .filter(Objects::nonNull)
                .toList();
        trace.add("6.LLM重排", cost(start), "重排后保留 %d/%d 条，分数：%s"
                .formatted(topChunks.size(), fused.size(), rerankScores(reranked)));

        // ⑦ 上下文组装 + 溯源锚定
        start = System.currentTimeMillis();
        AssembledContext context = contextAssembler.assemble(topChunks);
        trace.add("7.上下文组装", cost(start), "注入 %d 个带锚点 chunk，共 %d 字"
                .formatted(topChunks.size(), context.contextText().length()));

        // ⑧ 受约束生成：强制引用锚点、无依据必须明说
        start = System.currentTimeMillis();
        String answer = generationClient.prompt()
                .system(GENERATION_SYSTEM)
                .user("用户问题：%s\n\n【参考资料】\n%s".formatted(pre.cleanedQuery(), context.contextText()))
                .call().content();
        trace.add("8.受约束生成", cost(start), "生成长度 %d 字".formatted(answer == null ? 0 : answer.length()));

        // ⑨ LLM 事实校验：答案必须全部能在 chunk 中找到依据
        if (properties.verifyEnabled()) {
            start = System.currentTimeMillis();
            AnswerVerifier.Verdict verdict = answerVerifier.verify(pre.cleanedQuery(), answer, context.contextText());
            trace.add("9.LLM校验", cost(start), "grounded=%s%s"
                    .formatted(verdict.grounded(), verdict.reason().isEmpty() ? "" : "，" + verdict.reason()));
            if (!verdict.grounded()) {
                return new RagAnswer(rawQuery, REFUSED_ANSWER + "（校验未通过：" + verdict.reason() + "）",
                        List.of(), true, trace);
            }
        } else {
            trace.add("9.LLM校验", 0, "已关闭(scorpio.rag.verify-enabled=false)");
        }

        // ⑩ 解析引用来源
        start = System.currentTimeMillis();
        List<Citation> citations = contextAssembler.extractCitations(answer, context.chunks());
        trace.add("10.引用溯源", cost(start), "答案引用 %d 个来源".formatted(citations.size()));

        return new RagAnswer(rawQuery, answer, citations, false, trace);
    }

    private Map<Integer, DocumentChunk> fetchChunks(List<Integer> seqs) {
        try {
            SearchResponse<DocumentChunk> response = elasticsearchClient.search(s -> s
                            .index(properties.indexName())
                            .query(q -> q.terms(t -> t.field("seq").terms(tv -> tv.value(
                                    seqs.stream().map(FieldValue::of).toList()))))
                            .size(seqs.size()),
                    DocumentChunk.class);
            Map<Integer, DocumentChunk> map = new HashMap<>();
            response.hits().hits().forEach(hit -> {
                if (hit.source() != null) {
                    map.put(hit.source().seq(), hit.source());
                }
            });
            return map;
        } catch (IOException e) {
            throw new IllegalStateException("候选 chunk 拉取失败", e);
        }
    }

    private RagAnswer refuse(String question, PipelineTrace trace, String stage, String reason) {
        trace.add(stage, 0, reason);
        log.info("流水线兜底返回：{}", reason);
        return new RagAnswer(question, REFUSED_ANSWER, List.of(), true, trace);
    }

    private String channelNames() {
        return String.join("/", recallChannels.stream().map(RecallChannel::name).toList());
    }

    private String rerankScores(List<FusedCandidate> reranked) {
        return String.join(",", reranked.stream().map(c -> "%.0f".formatted(c.score())).toList());
    }

    private long cost(long startMillis) {
        return System.currentTimeMillis() - startMillis;
    }
}
