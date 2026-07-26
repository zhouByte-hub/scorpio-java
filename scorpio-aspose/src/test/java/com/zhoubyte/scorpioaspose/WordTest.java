package com.zhoubyte.scorpioaspose;

import com.aspose.words.Bookmark;
import com.aspose.words.BookmarkCollection;
import com.aspose.words.Document;
import com.aspose.words.FindReplaceOptions;
import com.aspose.words.ReplaceAction;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@SpringBootTest
public class WordTest {

    private static final Logger log = LoggerFactory.getLogger(WordTest.class);

    @Test
    public void fillWord() {
        String filePath = "/Users/zhoubyte/project/java/scorpio-java/scorpio-aspose/src/main/resources/templates/录用通知书_placeholder.docx";
        File wordFile = new File(filePath);
        if (!wordFile.exists()) {
            throw new RuntimeException("文件不存在：" + filePath);
        }

        // 准备占位符与实际值的映射（请根据实际模板补充）
        Map<String, String> fieldValues = new HashMap<>();
        fieldValues.put("name", "张三");
        fieldValues.put("position", "Java开发工程师");
        fieldValues.put("salary", "20000元/月");
        fieldValues.put("date", "2026-08-01");

        // 输出文件路径（同目录生成 _已填充.docx）
        String fileNameWithoutExt = wordFile.getName().substring(0, wordFile.getName().lastIndexOf('.'));
        File filledFile = new File(wordFile.getParentFile(), fileNameWithoutExt + "_已填充.docx");

        try {
            // 加载 Word 文档（com.aspose.words.Document 未实现 AutoCloseable，不能用 try-with-resources）
            Document document = new Document(filePath);

            // 使用正则匹配所有 {{xxx}} 占位符，通过 FindReplaceOptions 设置回调动态替换
            // 正则支持英文和中文占位符，如 {{name}}、{{姓名}}
            String placeholderPattern = "\\{\\{([^}]+)\\}\\}";
            FindReplaceOptions options = new FindReplaceOptions();
            options.setReplacingCallback(e -> {
                String placeholder = e.getMatch().group(0);  // 完整匹配，如 {{name}}
                String key = e.getMatch().group(1);           // 捕获组，如 name
                String value = fieldValues.get(key);
                if (value != null) {
                    log.info("替换占位符：{} -> {}", placeholder, value);
                    e.setReplacement(value);
                } else {
                    log.warn("未找到占位符对应的值：{}", placeholder);
                }
                return ReplaceAction.REPLACE;
            });
            // 第二个参数为默认替换值，会被回调中的 setReplacement 覆盖，这里传空字符串即可
            document.getRange().replace(Pattern.compile(placeholderPattern), "", options);

            document.save(filledFile.getAbsolutePath());
            log.info("Word填充完成：{}", filledFile.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Word填充失败：" + e.getMessage(), e);
        }
    }

    @Test
    public void listBookmarks() {
        String filePath = "/Users/zhoubyte/project/java/scorpio-java/scorpio-aspose/src/main/resources/templates/录用通知书_bookmark.docx";
        File wordFile = new File(filePath);
        if (!wordFile.exists()) {
            throw new RuntimeException("文件不存在：" + filePath);
        }
        try {
            Document document = new Document(filePath);
            BookmarkCollection bookmarks = document.getRange().getBookmarks();
            log.info("========== Word书签列表 ==========");
            for (Bookmark bookmark : bookmarks) {
                log.info("书签名: {}, 内容: {}", bookmark.getName(), bookmark.getText());
            }
            log.info("共 {} 个书签", bookmarks.getCount());
            log.info("==================================");
        } catch (Exception e) {
            throw new RuntimeException("获取书签失败：" + e.getMessage(), e);
        }
    }
}
