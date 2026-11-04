package com.zhoubyte.rag.pipeline.pipeline.preprocess;

import com.zhoubyte.rag.pipeline.model.MetadataInfo;
import com.zhoubyte.rag.pipeline.model.PreprocessResult;
import jakarta.annotation.PostConstruct;
import org.roaringbitmap.RoaringBitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 【前置处理层】元数据倒排位图过滤（RoaringBitmap）。
 *
 * <p>每个“字段:取值”对应一个 RoaringBitmap，位图下标为 chunk 的 seq。
 * 查询时按抽取出的过滤条件做集合运算：
 * <ul>
 *   <li>同字段多个取值（如密级“允许 internal 及以下”）→ 位图并集（OR）；</li>
 *   <li>不同字段之间 → 位图交集（AND）。</li>
 * </ul>
 * 最终得到允许命中的 seq 白名单，作为 ES terms 过滤条件下推到每一路召回。
 * 位图通过 Redis 持久化，应用重启后不丢失。</p>
 */
@Component
public class MetadataFilterResolver {

    private static final Logger log = LoggerFactory.getLogger(MetadataFilterResolver.class);
    private static final String KEY_PREFIX = "rag:rb:";
    private static final List<String> LEVEL_ORDER = List.of("public", "internal", "secret");

    private final StringRedisTemplate redisTemplate;
    private final Map<String, RoaringBitmap> cache = new ConcurrentHashMap<>();

    public MetadataFilterResolver(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 启动时从 Redis 恢复位图缓存。
     */
    @PostConstruct
    public void loadFromRedis() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys == null) {
            return;
        }
        for (String key : keys) {
            String encoded = redisTemplate.opsForValue().get(key);
            if (encoded == null) {
                continue;
            }
            RoaringBitmap bitmap = new RoaringBitmap();
            try (DataInputStream in = new DataInputStream(
                    new ByteArrayInputStream(Base64.getDecoder().decode(encoded)))) {
                bitmap.deserialize(in);
            } catch (Exception e) {
                log.warn("位图恢复失败，忽略: {}", key, e);
                continue;
            }
            cache.put(key.substring(KEY_PREFIX.length()), bitmap);
        }
        log.info("RoaringBitmap 元数据位图加载完成，共 {} 个键", cache.size());
    }

    /**
     * 文档 chunk 入库时登记元数据位图。
     */
    public void onChunkIndexed(int seq, MetadataInfo metadata) {
        index("department", metadata.department(), seq);
        index("level", metadata.level(), seq);
        index("docType", metadata.docType(), seq);
        index("year", yearOf(metadata.docDate()), seq);
    }

    /**
     * 解析过滤条件为允许命中的 seq 白名单；无过滤条件时返回 empty（不限制）。
     */
    public Optional<Set<Integer>> resolve(PreprocessResult.Filters filters) {
        if (filters == null) {
            return Optional.empty();
        }
        List<RoaringBitmap> conjunctions = new ArrayList<>();
        if (notBlank(filters.department())) {
            conjunctions.add(bitmapOf("department", filters.department()));
        }
        if (notBlank(filters.docType())) {
            conjunctions.add(bitmapOf("docType", filters.docType()));
        }
        if (notBlank(filters.year())) {
            conjunctions.add(bitmapOf("year", filters.year()));
        }
        if (notBlank(filters.level())) {
            conjunctions.add(levelsUpTo(filters.level().trim().toLowerCase()));
        }
        if (conjunctions.isEmpty()) {
            return Optional.empty();
        }
        RoaringBitmap result = conjunctions.get(0).clone();
        for (int i = 1; i < conjunctions.size(); i++) {
            result.and(conjunctions.get(i));
        }
        Set<Integer> allowed = result.stream().boxed().collect(Collectors.toSet());
        return Optional.of(allowed);
    }

    private RoaringBitmap bitmapOf(String field, String value) {
        RoaringBitmap bitmap = cache.get(field + ":" + value.trim());
        return bitmap == null ? new RoaringBitmap() : bitmap;
    }

    /**
     * 密级“取该级别及以下”：public < internal < secret，取并集。
     */
    private RoaringBitmap levelsUpTo(String level) {
        int bound = LEVEL_ORDER.indexOf(level);
        if (bound < 0) {
            return new RoaringBitmap();
        }
        RoaringBitmap union = new RoaringBitmap();
        for (int i = 0; i <= bound; i++) {
            union.or(bitmapOf("level", LEVEL_ORDER.get(i)));
        }
        return union;
    }

    private void index(String field, String value, int seq) {
        if (!notBlank(value)) {
            return;
        }
        String key = field + ":" + value.trim();
        RoaringBitmap bitmap = cache.computeIfAbsent(key, k -> loadOrDefault(k));
        synchronized (bitmap) {
            bitmap.add(seq);
        }
        persist(key, bitmap);
    }

    private RoaringBitmap loadOrDefault(String key) {
        String encoded = redisTemplate.opsForValue().get(KEY_PREFIX + key);
        if (encoded != null) {
            RoaringBitmap bitmap = new RoaringBitmap();
            try (DataInputStream in = new DataInputStream(
                    new ByteArrayInputStream(Base64.getDecoder().decode(encoded)))) {
                bitmap.deserialize(in);
                return bitmap;
            } catch (Exception e) {
                log.warn("位图恢复失败，新建: {}", key, e);
            }
        }
        return new RoaringBitmap();
    }

    private void persist(String key, RoaringBitmap bitmap) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(buffer)) {
            bitmap.serialize(out);
        } catch (Exception e) {
            log.warn("位图序列化失败: {}", key, e);
            return;
        }
        redisTemplate.opsForValue().set(KEY_PREFIX + key, Base64.getEncoder().encodeToString(buffer.toByteArray()));
    }

    private String yearOf(String docDate) {
        return docDate != null && docDate.length() >= 4 ? docDate.substring(0, 4) : "";
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
