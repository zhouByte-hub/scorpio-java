package com.zhoubyte.scorpioaspose.service.impl;

import com.aspose.pdf.Document;
import com.aspose.pdf.SaveFormat;
import com.aspose.pdf.devices.JpegDevice;
import com.zhoubyte.scorpioaspose.service.PdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfServiceImpl implements PdfService {
    
    private static final Logger log = LoggerFactory.getLogger(PdfServiceImpl.class);

    @Override
    public List<String> toPictures(String filePath) {
        List<String> picturePaths = new ArrayList<>();
        File pdfFile = new File(filePath);
        if (!pdfFile.exists()) {
            throw new RuntimeException("文件不存在：" + filePath);
        }
        // 获取文件名（不含后缀）
        String fileName = pdfFile.getName();
        String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));

        // 创建 pictures 目录
        File parentDir = pdfFile.getParentFile();
        File picturesDir = new File(parentDir, "pictures");
        if (!picturesDir.exists()) {
            picturesDir.mkdirs();
        }
        try {
            // 加载 PDF 文档
            Document document = new Document(filePath);
            // 遍历每一页并转换为图片
            for (int i = 1; i <= document.getPages().size(); i++) {
                String pictureName = fileNameWithoutExt + "_page_" + i + ".jpg";
                log.info("正在解析：{}", pictureName);
                File pictureFile = new File(picturesDir, pictureName);

                // 使用 JpegDevice 将页面转换为图片
                JpegDevice jpegDevice = new JpegDevice();
                FileOutputStream outputStream = new FileOutputStream(pictureFile);
                jpegDevice.process(document.getPages().get_Item(i), outputStream);
                outputStream.close();

                picturePaths.add(pictureFile.getAbsolutePath());
            }
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("PDF转图片失败：" + e.getMessage(), e);
        }
        return picturePaths;
    }

    @Override
    public String toWord(String filePath) {
        File pdfFile = new File(filePath);
        if (!pdfFile.exists()) {
            throw new RuntimeException("文件不存在：" + filePath);
        }
        // 获取文件名（不含后缀）
        String fileName = pdfFile.getName();
        String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));

        // Word 文件保存在同级目录
        File parentDir = pdfFile.getParentFile();
        File wordFile = new File(parentDir, fileNameWithoutExt + ".docx");
        try {
            // 加载 PDF 文档
            Document document = new Document(filePath);
            log.info("正在转换PDF为Word：{}", wordFile.getName());
            // 保存为 Word 文档
            document.save(wordFile.getAbsolutePath(), SaveFormat.DocX);
            document.close();
            log.info("PDF转Word完成：{}", wordFile.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("PDF转Word失败：" + e.getMessage(), e);
        }
        return wordFile.getAbsolutePath();
    }

    @Override
    public String fill(String filePath) {
        return "不能修改PDF文件";
    }

}