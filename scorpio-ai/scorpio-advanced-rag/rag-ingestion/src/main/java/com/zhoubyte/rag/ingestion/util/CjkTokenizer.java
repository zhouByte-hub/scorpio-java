package com.zhoubyte.rag.ingestion.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 轻量中文分词器（与 rag-pipeline 同源）：CJK 字符切 2-gram，拉丁字母/数字切整词。
 */
public final class CjkTokenizer {

    private CjkTokenizer() {
    }

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
