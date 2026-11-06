package com.zhoubyte.doclearning.common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 演示输出目录工具：所有生成的文件统一放到 target/output 下，
 * 保证每次 mvn clean 后目录也会被清空，不会残留脏文件。
 */
public final class OutputFiles {

    private OutputFiles() {
    }

    /** 解析输出文件路径，并自动创建父目录。 */
    public static Path resolve(String fileName) {
        try {
            Path path = Paths.get("target", "output").resolve(fileName);
            Files.createDirectories(path.getParent());
            return path;
        } catch (Exception e) {
            throw new IllegalStateException("创建输出目录失败", e);
        }
    }
}
