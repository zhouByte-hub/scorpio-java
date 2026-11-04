package com.zhoubyte.rag.pipeline.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 轻量中文分词器：CJK 字符切 2-gram（bigram），拉丁字母/数字切整词。
 *
 * <p>ES 标准分词器会把中文切成单字，检索噪声大；本地环境不部署 IK 插件时，
 * 索引侧与查询侧统一使用本分词器（配合 whitespace 分词器）即可获得高质量的中文 BM25，
 * 同时 bigram 词项也是稀疏通道（rank_features）的词表基础。</p>
 */
public final class CjkTokenizer {

    private CjkTokenizer() {
    }

    /**
     * 分词：返回词元列表（已小写）。
     */
    public static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return tokens;
        }
        String input = text.toLowerCase(Locale.ROOT);
        StringBuilder cjkRun = new StringBuilder();
        StringBuilder wordRun = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (isCjk(c)) {
                flushWord(wordRun, tokens);
                cjkRun.append(c);
            } else if (Character.isLetterOrDigit(c)) {
                flushCjk(cjkRun, tokens);
                wordRun.append(c);
            } else {
                flushWord(wordRun, tokens);
                flushCjk(cjkRun, tokens);
            }
        }
        flushWord(wordRun, tokens);
        flushCjk(cjkRun, tokens);
        return tokens;
    }

    /**
     * 分词并按空格拼接，用于索引 text 字段（whitespace 分词器）。
     */
    public static String preTokenize(String text) {
        return String.join(" ", tokenize(text));
    }

    private static void flushCjk(StringBuilder run, List<String> tokens) {
        int len = run.length();
        if (len == 1) {
            tokens.add(run.toString());
        } else {
            for (int i = 0; i < len - 1; i++) {
                tokens.add(run.substring(i, i + 2));
            }
        }
        run.setLength(0);
    }

    private static void flushWord(StringBuilder run, List<String> tokens) {
        if (!run.isEmpty()) {
            tokens.add(run.toString());
            run.setLength(0);
        }
    }

    private static boolean isCjk(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS;
    }
}
