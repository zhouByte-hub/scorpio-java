package com.zhoubyte.doclearning.pdf.service;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * PDFBox 学习示例。
 * <p>
 * 核心概念：
 * <ul>
 *   <li>PDDocument 对应一个 PDF 文件，PDF 内部是“对象树”：Document → Page → Content Stream。</li>
 *   <li>内容流（Content Stream）是类 PostScript 的绘图指令序列：beginText → 设置字体/坐标 →
 *       showText → endText，坐标原点在页面左下角（与 Office 系的左上角原点不同）。</li>
 *   <li>字体是 PDF 最容易踩坑的点：PDType1Font 只内置 14 种西文字体（Helvetica 等），
 *       写中文必须嵌入 TTF/OTF（PDType0Font.load），且字体文件要有中文 glyph。</li>
 *   <li>PDFBox 2.x 用 PDDocument.load() 加载；3.x 改成了 Loader.loadPDF()，迁移时注意。</li>
 *   <li>PDDocument 实现了 Closeable，必须关闭（内部会释放临时文件与句柄）。</li>
 * </ul>
 */
@Service
public class PdfDemoService {

    /** macOS 常见的可用中文字体路径，用于演示“嵌入 TTF 写中文”。 */
    private static final List<String> CJK_FONT_CANDIDATES = List.of(
            // 优先单文件 TTF：加载最简单，覆盖中文且无 TTC 关闭时序问题
            "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
            // 备选 TTC 集合字体
            "/System/Library/Fonts/STHeiti Light.ttc",
            "/System/Library/Fonts/Hiragino Sans GB.ttc"
    );

    /**
     * 创建演示 PDF：两页内容，包含英文 + 中文（嵌入系统字体）。
     */
    public void createDemoPdf(OutputStream output) throws Exception {
        // TTC 集合字体的句柄要活到 document.save() 之后才能关（子集嵌入是延迟到 save 时读字形的），
        // 所以这里不能用 try-with-resources 包住 ttc，改用 finally 统一关闭
        org.apache.fontbox.ttf.TrueTypeCollection ttc = null;
        try (PDDocument document = new PDDocument()) {
            // ---------- 第 1 页 ----------
            PDPage page1 = new PDPage(PDRectangle.A4); // 内置 A4/Letter 等纸张常量
            document.addPage(page1);

            // 中文：必须嵌入 TTF 字体（PDType0Font，CID 字体 + 子集嵌入，只嵌入用到的字形）。
            // 单文件 TTF 直接 load；.ttc 集合需要 TrueTypeCollection 展开，并把句柄保留到 save 后
            PDFont cjk = null;
            for (String path : CJK_FONT_CANDIDATES) {
                File fontFile = new File(path);
                if (!fontFile.exists()) {
                    continue;
                }
                if (path.endsWith(".ttc")) {
                    try {
                        var collection = new org.apache.fontbox.ttf.TrueTypeCollection(fontFile);
                        var holder = new org.apache.fontbox.ttf.TrueTypeFont[1];
                        // fontbox 2.x 没有按索引取字体的 API，用 processAllFonts 遍历取第一个
                        collection.processAllFonts(ttf -> {
                            if (holder[0] == null) {
                                holder[0] = ttf;
                            }
                        });
                        if (holder[0] == null) {
                            collection.close();
                            continue;
                        }
                        ttc = collection;
                        cjk = PDType0Font.load(document, holder[0], true);
                        break;
                    } catch (Exception ignored) {
                        // 尝试下一个候选字体
                    }
                } else {
                    // 普通 .ttf：load(document, File) 内部会持有文件句柄，随 PDDocument 关闭而释放
                    cjk = PDType0Font.load(document, fontFile);
                    break;
                }
            }

            // ContentStream 必须指定“写到哪一页”，appendContent=true 表示追加而非覆盖
            try (PDPageContentStream cs = new PDPageContentStream(document, page1)) {
                PDFont latin = PDType1Font.HELVETICA;

                // 英文：直接用内置字体。坐标 newLineAtOffset(x, y)，原点在左下角
                cs.beginText();
                cs.setFont(latin, 18);
                cs.newLineAtOffset(50, 780);
                cs.showText("PDFBox Learning Demo - Page 1");
                cs.endText();

                cs.beginText();
                cs.setFont(latin, 12);
                cs.newLineAtOffset(50, 750);
                cs.showText("Created by Apache PDFBox 2.0.x (PDPageContentStream).");
                cs.endText();

                if (cjk != null) {
                    cs.beginText();
                    cs.setFont(cjk, 14);
                    cs.newLineAtOffset(50, 700);
                    cs.showText("第一页：这是嵌入字体渲染的中文文本。");
                    cs.endText();
                } else {
                    cs.beginText();
                    cs.setFont(latin, 12);
                    cs.newLineAtOffset(50, 700);
                    cs.showText("(CJK font not found on this machine, Chinese demo skipped)");
                    cs.endText();
                }
            }

            // ---------- 第 2 页（供后续拆分/合并演示用） ----------
            PDPage page2 = new PDPage(PDRectangle.A4);
            document.addPage(page2);
            try (PDPageContentStream cs = new PDPageContentStream(document, page2)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 18);
                cs.newLineAtOffset(50, 780);
                cs.showText("PDFBox Learning Demo - Page 2");
                cs.endText();
            }

            document.save(output); // 写入输出流；save(File) 则直接落盘
        } finally {
            // 关键时序：必须在 document.save() 之后关闭 TTC，否则子集嵌入时读不到字形表
            if (ttc != null) {
                ttc.close();
            }
        }
    }

    /**
     * 文本提取：PDFTextStripper 是 PDFBox 的文本抽取引擎。
     * <p>
     * 关键点：
     * <ul>
     *   <li>setSortByPosition(true)：PDF 内部文字是按“绘制指令顺序”存储的，
     *       不一定是阅读顺序，开启排序可修正大多数乱序场景；</li>
     *   <li>扫描版 PDF（图片）没有文本层，提取结果是空的，需要 OCR，PDFBox 不负责 OCR；</li>
     *   <li>setStartPage/setEndPage 支持 1 基页码范围提取。</li>
     * </ul>
     */
    public String extractText(byte[] bytes, Integer startPage, Integer endPage) throws Exception {
        try (PDDocument document = PDDocument.load(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            if (startPage != null) {
                stripper.setStartPage(startPage); // 页码从 1 开始
            }
            if (endPage != null) {
                stripper.setEndPage(endPage);
            }
            return stripper.getText(document);
        }
    }

    /**
     * 页面渲染：把第 pageIndex 页（0 基）渲染成 PNG 图片。
     * <p>PDFRenderer 是纯 Java 渲染器，支持字体嵌入渲染；DPI 越高越清晰也越耗时，
     * 常用 96（屏幕预览）/ 150 / 300（打印）。</p>
     *
     * @return 渲染出的图片对象（由 Controller 写出为 PNG）
     */
    public BufferedImage renderPageToImage(byte[] bytes, int pageIndex, float dpi) throws Exception {
        try (PDDocument document = PDDocument.load(bytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            // renderImageWithDPI 页码从 0 开始；第二个参数是 DPI
            return renderer.renderImageWithDPI(pageIndex, dpi);
        }
    }

    /** 把 BufferedImage 编码为 PNG 写出。 */
    public void writePng(BufferedImage image, OutputStream output) throws Exception {
        ImageIO.write(image, "png", output);
    }

    /**
     * 合并多个 PDF：PDFMergerUtility 是官方提供的合并工具。
     * 这里演示“同一份文档复制两份再合并”，实际用时长传入多个不同文件字节。
     */
    public void mergePdf(byte[] bytes, OutputStream output) throws Exception {
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.addSource(new ByteArrayInputStream(bytes));
        merger.addSource(new ByteArrayInputStream(bytes)); // 复制一份演示
        merger.setDestinationStream(output);
        // MemoryUsageSetting 控制合并过程的内存策略：
        // setupMainMemoryOnly() 全内存（快）；setupTempFileOnly() 走临时文件（省内存，适合大文件）
        merger.mergeDocuments(org.apache.pdfbox.io.MemoryUsageSetting.setupMainMemoryOnly());
    }

    /**
     * 拆分 PDF：Splitter 可以按“每 N 页一个文档”拆分（setSplitAtPages），默认每页一个。
     * 注意：拆出来的每个 PDDocument 都是独立资源，这里立即转成字节数组并关闭。
     */
    public List<byte[]> splitPdf(byte[] bytes) throws Exception {
        List<byte[]> result = new ArrayList<>();
        try (PDDocument document = PDDocument.load(bytes)) {
            Splitter splitter = new Splitter();
            List<PDDocument> pages = splitter.split(document);
            for (PDDocument page : pages) {
                try (page) {
                    // 2.x 没有 saveAsBytes()（3.x 才有），先 save 到内存流再取出
                    var buffer = new java.io.ByteArrayOutputStream();
                    page.save(buffer);
                    result.add(buffer.toByteArray());
                }
            }
        }
        return result;
    }

    /**
     * 加密 PDF：StandardProtectionPolicy 设置所有者密码/用户密码与权限位。
     * <p>
     * 概念区分：
     * <ul>
     *   <li>ownerPassword：拥有者密码，拥有全部权限（可改密码、改权限）；</li>
     *   <li>userPassword：用户密码，打开文档用的密码（可空 = 只限制权限不限制打开）；</li>
     *   <li>AccessPermission：权限位（是否可打印/复制文本/修改等），依赖加密生效。</li>
     * </ul>
     */
    public byte[] encryptPdf(byte[] bytes, String ownerPassword, String userPassword) throws Exception {
        try (PDDocument document = PDDocument.load(bytes)) {
            AccessPermission ap = new AccessPermission();
            ap.setCanPrint(false);     // 禁止打印
            ap.setCanExtractContent(false); // 禁止复制文本
            StandardProtectionPolicy policy =
                    new StandardProtectionPolicy(ownerPassword, userPassword, ap);
            policy.setEncryptionKeyLength(128); // AES-128（也可 256，取决于 PDF 版本兼容性）
            document.protect(policy);
            // 2.x 没有 saveAsBytes()（3.x 才有），先 save 到内存流再取出
            var buffer = new java.io.ByteArrayOutputStream();
            document.save(buffer);
            return buffer.toByteArray();
        }
    }

    /**
     * 打开加密 PDF：带密码加载即可正常解析/提取。
     * 密码错误会抛 InvalidPasswordException。
     */
    public String extractTextFromEncrypted(byte[] bytes, String password) throws Exception {
        try (PDDocument document = PDDocument.load(bytes, password)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }
}
