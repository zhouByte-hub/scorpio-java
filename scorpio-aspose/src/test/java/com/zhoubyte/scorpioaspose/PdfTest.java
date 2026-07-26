package com.zhoubyte.scorpioaspose;

import com.aspose.pdf.Document;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
public class PdfTest {

    private static final Logger log = LoggerFactory.getLogger(PdfTest.class);

    @Test
    public void fillTest() {
        String filePath = "/Users/zhoubyte/project/java/scorpio-java/scorpio-aspose/src/main/resources/templates/录用通知书.pdf";
        File pdfFile = new File(filePath);
        if (!pdfFile.exists()) {
            throw new RuntimeException("文件不存在：" + filePath);
        }
        try(Document document = new Document(filePath)) {
            // 获取表单
            com.aspose.pdf.Form form = document.getForm();
            // 输出所有表单字段名（便于调试）
            log.info("========== PDF表单字段列表 ==========");
            for (com.aspose.pdf.Field field : form.getFields()) {
                log.info("字段名: {}, 类型: {}", field.getFullName(), field.getClass().getSimpleName());
            }
            log.info("=====================================");

            // 填充表单字段示例：
            // form.getFields().get("textField1").setValue("填写内容1");
            // form.getFields().get("textField2").setValue("填写内容2");
            // form.getFields().get("checkBox1").setValue("Yes");  // 复选框
            // form.getFields().get("radioButton1").setValue("Option1");  // 单选按钮
        } catch (Exception e) {
            throw new RuntimeException("PDF表单填充失败：" + e.getMessage(), e);
        }
    }
}
