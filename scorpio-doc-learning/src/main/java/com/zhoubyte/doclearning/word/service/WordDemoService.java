package com.zhoubyte.doclearning.word.service;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Word（XWPF）学习示例。
 * <p>
 * 核心概念：
 * <ul>
 *   <li>XWPFDocument 对应一个 .docx 文件（OOXML 的 wordprocessingml 格式）。</li>
 *   <li>文档内容层级：Document → Paragraph（段落）/ Table（表格），段落内是 Run（文本片段）。</li>
 *   <li>Run 是 Word 里最小的格式单元：同一加粗、同一字体的一串文字是一个 Run，
 *       所以“一段话内部分加粗”要拆成多个 Run 分别设置格式。</li>
 *   <li>高级样式（标题级别、编号、目录）依赖 styles.xml，POI 能通过 setStyle("Heading1")
 *       引用文档内置样式，但新建空文档时需要先用 addStyle 或依赖 Word 打开时识别。</li>
 * </ul>
 */
@Service
public class WordDemoService {

    /**
     * 创建演示 Word 文档：标题段落、富文本段落、内置样式标题、表格、图片、页眉。
     */
    public void createDemoDocument(OutputStream output) throws Exception {
        try (XWPFDocument document = new XWPFDocument()) {

            // ---------- 1. 居中大标题 ----------
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER); // 段落级对齐
            XWPFRun titleRun = title.createRun();
            titleRun.setText("POI XWPF 文档学习示例");
            titleRun.setBold(true);
            titleRun.setFontSize(22);
            titleRun.setFontFamily("微软雅黑");
            titleRun.setColor("1F4E79"); // 颜色是 RRGGBB 十六进制串，不带 #

            // ---------- 2. 富文本段落：同一段内不同格式的多个 Run ----------
            XWPFParagraph rich = document.createParagraph();
            rich.setFirstLineIndent(480); // 首行缩进，单位 twip（1/20 磅），480 ≈ 2 字符
            rich.createRun().setText("这一段演示同段落内的多种格式：");
            XWPFRun bold = rich.createRun();
            bold.setText("加粗文本");
            bold.setBold(true);
            XWPFRun italic = rich.createRun();
            italic.setText("、斜体文本");
            italic.setItalic(true);
            XWPFRun strike = rich.createRun();
            strike.setText("、删除线文本");
            strike.setStrikeThrough(true);
            XWPFRun underlined = rich.createRun();
            underlined.setText("、下划线文本");
            underlined.setUnderline(UnderlinePatterns.SINGLE);

            // ---------- 3. 内置样式标题（Heading 1） ----------
            // styleId 取决于文档 styles.xml 中是否注册了该样式；
            // POI 新建文档会预置少量样式，Word 打开后能看到对应大纲级别
            XWPFParagraph heading = document.createParagraph();
            heading.setStyle("Heading1");
            heading.createRun().setText("第一章 表格示例");

            // ---------- 4. 表格 ----------
            // createTable(rows, cols) 先建好框架，再逐格填充
            XWPFTable table = document.createTable(3, 3);
            // 表格宽度：底层 CTTblWidth 是 OOXML 的 XML 映射对象，单位 dxa（1/20 磅）
            // 5000 dxa ≈ 8.8 cm；也可以用 STTblWidth.PCT 百分比
            CTTblWidth width = table.getCTTbl().addNewTblPr().addNewTblW();
            width.setType(STTblWidth.DXA);
            width.setW(BigInteger.valueOf(5000));

            String[][] data = {
                    {"姓名", "部门", "职位"},
                    {"张三", "研发部", "Java 工程师"},
                    {"李四", "产品部", "产品经理"},
            };
            for (int r = 0; r < data.length; r++) {
                XWPFTableRow row = table.getRow(r);
                for (int c = 0; c < data[r].length; c++) {
                    XWPFTableCell cell = row.getCell(c);
                    // 表格单元格内也是段落，getText() 拿不到新写入的内容，
                    // 必须操作 cell 内部的 Paragraph/Run
                    cell.getParagraphs().get(0).createRun().setText(data[r][c]);
                    if (r == 0) { // 表头行加粗
                        cell.getParagraphs().get(0).getRuns().get(0).setBold(true);
                    }
                }
            }

            // ---------- 5. 插入图片 ----------
            XWPFParagraph picPara = document.createParagraph();
            picPara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun picRun = picPara.createRun();
            byte[] png;
            try (var in = getClass().getResourceAsStream("/demo-image.png")) {
                if (in == null) {
                    throw new IllegalStateException("classpath 下找不到 /demo-image.png");
                }
                png = in.readAllBytes();
            }
            // addPicture(图片类型, 字节流, 文件名, 宽, 高)：宽高单位是 EMU，
            // 用 Units.EMU_PER_CENTIMETER 换算成 12cm x 4cm（原图 240x80 px 会被拉伸）
            try (var in = new java.io.ByteArrayInputStream(png)) {
                picRun.addPicture(in, XWPFDocument.PICTURE_TYPE_PNG, "demo.png",
                        Units.EMU_PER_CENTIMETER * 12, Units.EMU_PER_CENTIMETER * 4);
            }

            // ---------- 6. 页眉 ----------
            // 页眉页脚按“节”（Section）组织，HeaderFooterType 在 org.apache.poi.wp.usermodel 包
            XWPFHeader header = document.createHeader(
                    org.apache.poi.wp.usermodel.HeaderFooterType.DEFAULT);
            XWPFParagraph headerPara = header.createParagraph();
            headerPara.setAlignment(ParagraphAlignment.RIGHT);
            headerPara.createRun().setText("scorpio-doc-learning 内部文档");

            document.write(output);
        }
    }

    /**
     * 读取 Word 文档：按文档顺序遍历段落与表格。
     * <p>
     * 关键点：document.getParagraphs() 与 getTables() 是分开的两个列表，
     * 如果要按“文档实际顺序”遍历，需要用 getBodyElements() 判断元素类型。
     * </p>
     */
    public List<String> readDocument(byte[] bytes) throws Exception {
        List<String> lines = new ArrayList<>();
        try (XWPFDocument document = new XWPFDocument(new java.io.ByteArrayInputStream(bytes))) {
            for (var element : document.getBodyElements()) {
                switch (element.getElementType()) {
                    case PARAGRAPH -> {
                        XWPFParagraph p = (XWPFParagraph) element;
                        // 一个段落所有 Run 的文本拼接才是完整段落文本
                        lines.add("[段落] " + p.getText());
                    }
                    case TABLE -> {
                        XWPFTable t = (XWPFTable) element;
                        for (XWPFTableRow row : t.getRows()) {
                            // getCellText() 直接返回单元格文本（含所有段落）
                            lines.add("[表格行] " + String.join(" | ",
                                    row.getTableCells().stream().map(XWPFTableCell::getText).toList()));
                        }
                    }
                    default -> lines.add("[其他] " + element.getElementType());
                }
            }
            // 页眉页脚读取示例
            document.getHeaderList().forEach(h ->
                    h.getParagraphs().forEach(p -> lines.add("[页眉] " + p.getText())));
        }
        return lines;
    }
}
