package com.zhoubyte.doclearning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 文档处理学习模块启动类。
 * <p>
 * 本模块用于学习两个主流 Java 文档处理库：
 * <ul>
 *   <li>Apache POI（poi-ooxml）：操作 Office 2007+ 的 .xlsx / .docx / .pptx 文档</li>
 *   <li>Apache PDFBox：操作 PDF（创建、文本提取、渲染图片、合并拆分、加解密）</li>
 * </ul>
 * 每个学习主题对应一个 Controller + Service，通过 REST 接口触发演示方法，
 * 生成或解析的文件统一放在 target/output 目录下。
 */
@SpringBootApplication
public class DocLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocLearningApplication.class, args);
    }
}
