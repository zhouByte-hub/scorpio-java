package com.zhoubyte.doclearning.ppt.service;

import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.springframework.stereotype.Service;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * PPT（XSLF）学习示例。
 * <p>
 * 核心概念：
 * <ul>
 *   <li>XMLSlideShow 对应 .pptx（OOXML 的 presentationml 格式）。</li>
 *   <li>层级结构：SlideShow → Slide（页）→ Shape（形状），文本在 TextBox / 占位符（placeholder）里，
 *       再往下是 Paragraph → Run，和 Word 的段落/文本片段概念一致。</li>
 *   <li>坐标单位是“点”（point，1/72 英寸），页面尺寸默认 10x7.5 英寸（4:3），
 *       新版常为 13.33x7.5 英寸（16:9），可用 setPageSize 自定义。</li>
 *   <li>POI 的绘图模块（org.apache.poi.sl）对 XSLF 和 HSLF（.ppt）做了统一抽象，
 *       所以 TextParagraph.TextAlign 等枚举是两个格式通用的。</li>
 * </ul>
 */
@Service
public class PptDemoService {

    /**
     * 创建演示 PPT：标题页、项目符号列表页、表格页（用文本框模拟）、图片页。
     */
    public void createDemoPresentation(OutputStream output) throws Exception {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            // 设置 16:9 页面尺寸（单位：点）
            ppt.setPageSize(new Dimension(960, 540));

            // ---------- 1. 标题页 ----------
            XSLFSlide slide1 = ppt.createSlide();
            // 新建的空白页没有任何形状，文本框需要手动创建并定位
            XSLFTextBox title = slide1.createTextBox();
            title.setAnchor(new Rectangle(60, 180, 840, 80)); // x, y, width, height（点）
            title.setText("POI XSLF 学习示例");
            // 对齐/字号等段落属性都在 TextParagraph 上设置，XSLFTextShape 本身不提供
            title.getTextParagraphs().get(0).setTextAlign(TextParagraph.TextAlign.CENTER);
            // 通过 Run 精确控制字体
            title.getTextParagraphs().get(0).getTextRuns().get(0).setFontSize(40d);
            title.getTextParagraphs().get(0).getTextRuns().get(0).setFontColor(
                    new java.awt.Color(0x1F, 0x4E, 0x79));

            // ---------- 2. 项目符号列表页 ----------
            XSLFSlide slide2 = ppt.createSlide();
            XSLFTextBox bullets = slide2.createTextBox();
            bullets.setAnchor(new Rectangle(80, 60, 800, 400));
            // 一个 TextBox 可包含多个 Paragraph，每个 Paragraph 可包含多个 Run；
            // setText 写入第一段，后续段落用 addNewTextParagraph() 追加
            bullets.setText("POI 核心概念一：SlideShow 是整个文档");
            var p2 = bullets.addNewTextParagraph();
            // 段落里通过 Run 写入文字（XSLFTextParagraph 没有 setText 方法）
            p2.addNewTextRun().setText("POI 核心概念二：Slide 是单页");
            p2.setBullet(true); // 打开项目符号
            p2.setLeftMargin(20d); // 缩进让符号与文字分开
            var p3 = bullets.addNewTextParagraph();
            p3.addNewTextRun().setText("POI 核心概念三：Shape 是页面元素");
            p3.setBullet(true);
            p3.setLeftMargin(20d);

            // ---------- 3. 图片页 ----------
            XSLFSlide slide3 = ppt.createSlide();
            byte[] png;
            try (var in = getClass().getResourceAsStream("/demo-image.png")) {
                if (in == null) {
                    throw new IllegalStateException("classpath 下找不到 /demo-image.png");
                }
                png = in.readAllBytes();
            }
            // 图片先注册进 SlideShow 的媒体库，得到 PictureData，再往页面上贴
            XSLFPictureData pictureData = ppt.addPicture(png, PictureData.PictureType.PNG);
            XSLFPictureShape picture = slide3.createPicture(pictureData);
            // createPicture 后默认按图片原始尺寸放置，这里手动指定位置和大小
            picture.setAnchor(new Rectangle(180, 120, 600, 200));

            // ---------- 4. 写出 ----------
            ppt.write(output);
        }
    }

    /**
     * 读取 PPT：逐页提取所有形状里的文本。
     * <p>
     * 关键点：getShapes() 返回页面上所有形状（文本框、图片、表格、占位符等），
     * 只有 TextShape 类型的形状才有文本，需要 instanceof 判断。
     * </p>
     */
    public List<String> readPresentation(byte[] bytes) throws Exception {
        List<String> result = new ArrayList<>();
        try (XMLSlideShow ppt = new XMLSlideShow(new ByteArrayInputStream(bytes))) {
            result.add("页面尺寸: " + ppt.getPageSize().width + " x " + ppt.getPageSize().height + " pt");
            List<XSLFSlide> slides = ppt.getSlides();
            for (int i = 0; i < slides.size(); i++) {
                result.add("--- 第 " + (i + 1) + " 页 ---");
                for (var shape : slides.get(i).getShapes()) {
                    if (shape instanceof org.apache.poi.xslf.usermodel.XSLFTextShape textShape) {
                        // getText() 汇总该形状内所有段落文本
                        String text = textShape.getText();
                        if (text != null && !text.isBlank()) {
                            result.add(text);
                        }
                    } else {
                        result.add("[非文本形状] " + shape.getShapeName());
                    }
                }
            }
        }
        return result;
    }
}
