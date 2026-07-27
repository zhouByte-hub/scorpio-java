package com.zhoubyte.scorpioaspose.service.impl;

import com.aspose.words.Bookmark;
import com.aspose.words.BookmarkCollection;
import com.aspose.words.Document;
import com.aspose.words.FindReplaceOptions;
import com.aspose.words.ImageSaveOptions;
import com.aspose.words.PageSet;
import com.aspose.words.ReplaceAction;
import com.aspose.words.SaveFormat;
import com.zhoubyte.scorpioaspose.dto.OfferDto;
import com.zhoubyte.scorpioaspose.service.WordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class WordServiceImpl implements WordService {

    private static final Logger log = LoggerFactory.getLogger(WordServiceImpl.class);

    // 使用标签占位符进行填充 {{name}} -> 张三
    @Override
    public String fillByPlaceholder(String filePath, OfferDto offerDto) {
        File wordFile = new File(filePath);
        if (!wordFile.exists()) {
            throw new RuntimeException("文件不存在：" + filePath);
        }
        if (offerDto == null) {
            throw new RuntimeException("OfferDto 不能为空");
        }

        // 通过反射将 OfferDto 字段转为 Map<String, String>，null 值跳过
        Map<String, String> fieldValues = new HashMap<>();
        try {
            for (Field field : offerDto.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(offerDto);
                if (value != null) {
                    fieldValues.put(field.getName(), String.valueOf(value));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("OfferDto 字段读取失败：" + e.getMessage(), e);
        }

        String fileNameWithoutExt = wordFile.getName().substring(0, wordFile.getName().lastIndexOf('.'));
        File filledFile = new File(wordFile.getParentFile(), fileNameWithoutExt + "_已填充.docx");
        try {
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
        return filledFile.getAbsolutePath();
    }

    @Override
    public String fillByBookmark(String filePath, OfferDto offerDto) {
        File wordFile = new File(filePath);
        if (!wordFile.exists()) {
            throw new RuntimeException("文件不存在：" + filePath);
        }
        if (offerDto == null) {
            throw new RuntimeException("OfferDto 不能为空");
        }

        // 通过反射将 OfferDto 字段转为 Map<String, String>，null 值跳过
        Map<String, String> fieldValues = new HashMap<>();
        try {
            for (Field field : offerDto.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(offerDto);
                if (value != null) {
                    fieldValues.put(field.getName(), String.valueOf(value));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("OfferDto 字段读取失败：" + e.getMessage(), e);
        }

        String fileNameWithoutExt = wordFile.getName().substring(0, wordFile.getName().lastIndexOf('.'));
        File filledFile = new File(wordFile.getParentFile(), fileNameWithoutExt + "_已填充.docx");
        try {
            Document document = new Document(filePath);
            BookmarkCollection bookmarks = document.getRange().getBookmarks();
            for (Bookmark bookmark : bookmarks) {
                // 跳过 Word 内部书签（如 _GoBack，以下划线开头）
                if (bookmark.getName().startsWith("_")) {
                    continue;
                }
                String value = fieldValues.get(bookmark.getName());
                if (value != null) {
                    log.info("填充书签：{} -> {}", bookmark.getName(), value);
                    bookmark.setText(value);
                } else {
                    log.warn("未找到书签对应的值：{}", bookmark.getName());
                }
            }
            document.save(filledFile.getAbsolutePath());
            log.info("Word填充完成：{}", filledFile.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Word填充失败：" + e.getMessage(), e);
        }
        return filledFile.getAbsolutePath();
    }

    @Override
    public List<String> toPictures(String filePath) {
        List<String> picturePaths = new ArrayList<>();
        File wordFile = new File(filePath);
        if (!wordFile.exists()) {
            throw new RuntimeException("文件不存在：" + filePath);
        }
        String fileNameWithoutExt = wordFile.getName().substring(0, wordFile.getName().lastIndexOf('.'));

        // 创建 pictures 目录
        File picturesDir = new File(wordFile.getParentFile(), "pictures");
        if (!picturesDir.exists()) {
            picturesDir.mkdirs();
        }
        try {
            // com.aspose.words.Document 未实现 AutoCloseable，不能用 try-with-resources
            Document document = new Document(filePath);
            ImageSaveOptions options = new ImageSaveOptions(SaveFormat.JPEG);
            int pageCount = document.getPageCount();
            for (int i = 0; i < pageCount; i++) {
                String pictureName = fileNameWithoutExt + "_page_" + (i + 1) + ".jpg";
                log.info("正在解析：{}", pictureName);
                File pictureFile = new File(picturesDir, pictureName);
                // 仅输出第 i 页（索引从 0 开始）
                options.setPageSet(new PageSet(i));
                document.save(pictureFile.getAbsolutePath(), options);
                picturePaths.add(pictureFile.getAbsolutePath());
            }
            log.info("Word转图片完成，共 {} 页", pageCount);
        } catch (Exception e) {
            throw new RuntimeException("Word转图片失败：" + e.getMessage(), e);
        }
        return picturePaths;
    }

    @Override
    public String toPdf(String filePath) {
        File wordFile = new File(filePath);
        if (!wordFile.exists()) {
            throw new RuntimeException("文件不存在：" + filePath);
        }
        String fileNameWithoutExt = wordFile.getName().substring(0, wordFile.getName().lastIndexOf('.'));
        File pdfFile = new File(wordFile.getParentFile(), fileNameWithoutExt + ".pdf");
        try {
            Document document = new Document(filePath);
            log.info("正在转换Word为PDF：{}", pdfFile.getName());
            document.save(pdfFile.getAbsolutePath(), SaveFormat.PDF);
            log.info("Word转PDF完成：{}", pdfFile.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Word转PDF失败：" + e.getMessage(), e);
        }
        return pdfFile.getAbsolutePath();
    }
}
