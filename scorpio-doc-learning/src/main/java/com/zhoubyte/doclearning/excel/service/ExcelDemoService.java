package com.zhoubyte.doclearning.excel.service;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel（XSSF）学习示例。
 * <p>
 * 核心概念：
 * <ul>
 *   <li>POI 用统一的 Workbook 接口抽象 Excel 文件，XSSFWorkbook 是 .xlsx（OOXML）实现，
 *       HSSFWorkbook 是 .xls（老二进制格式）实现，日常推荐 XSSF。</li>
 *   <li>层级结构：Workbook → Sheet → Row → Cell，行号列号都从 0 开始。</li>
 *   <li>OOXML 本质是一个 zip 包（内部是若干 XML），所以 Workbook 用完必须 close()，
 *       推荐用 try-with-resources 释放临时文件。</li>
 *   <li>样式（CellStyle/Font）是“工作簿级”对象，同一个 Workbook 内应复用样式对象，
 *       而不是每个单元格 new 一个（Excel 有样式数量上限，约 64000 个）。</li>
 * </ul>
 */
@Service
public class ExcelDemoService {

    /**
     * 创建一个演示用 Excel，覆盖最常用的能力：
     * 表头样式、数据填充、公式、合并单元格、下拉列表、插入图片。
     *
     * @param output 输出流（由 Controller 层打开并负责关闭）
     */
    public void createDemoWorkbook(OutputStream output) throws Exception {
        // XSSFWorkbook 构造出来是一个“内存中的新工作簿”，写出到流后才落盘
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            // ---------- 1. 准备样式（工作簿级，务必复用） ----------
            // 表头样式：加粗白字 + 蓝色背景 + 居中
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            // 背景色必须配合填充模式才生效，SOLID_FOREGROUND 是纯色填充
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // 数据行样式：细边框
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 金额样式：货币格式 + 边框。数据格式用 Excel 内置格式串表达（￥#,##0.00）
            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.cloneStyleFrom(dataStyle);
            moneyStyle.setDataFormat(workbook.createDataFormat().getFormat("￥#,##0.00"));

            // ---------- 2. 创建 Sheet 与表头 ----------
            Sheet sheet = workbook.createSheet("订单明细");
            String[] headers = {"订单号", "客户", "数量", "单价", "金额", "状态"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                // 设置列宽：POI 的单位是 1/256 个字符宽度，252 大约对应 8.8 个字符
                sheet.setColumnWidth(i, 14 * 256);
            }

            // ---------- 3. 填充数据行 + 公式 ----------
            // 公式写法与 Excel 内一致，POI 只负责写入公式串，
            // 用 Excel/WPS 打开时会自动计算；如果想在 Java 里拿到计算结果，
            // 需要用 FormulaEvaluator（见下方 readWorkbook 方法）。
            Object[][] data = {
                    {"SO-2026-001", "北京科技", 100, 25.5, "SUM 公式", "已付款"},
                    {"SO-2026-002", "上海贸易", 200, 12.8, "SUM 公式", "待付款"},
                    {"SO-2026-003", "广州制造", 150, 40.0, "SUM 公式", "已发货"},
            };
            int rowIndex = 1;
            for (Object[] rowData : data) {
                Row row = sheet.createRow(rowIndex);
                row.createCell(0).setCellValue((String) rowData[0]);
                row.createCell(1).setCellValue((String) rowData[1]);
                row.createCell(2).setCellValue((Integer) rowData[2]);
                row.createCell(3).setCellValue((Double) rowData[3]);
                // 金额列用公式：数量 * 单价
                Cell amountCell = row.createCell(4);
                amountCell.setCellFormula("C" + (rowIndex + 1) + "*D" + (rowIndex + 1));
                amountCell.setCellStyle(moneyStyle);
                row.createCell(5).setCellValue((String) rowData[4]);
                for (int c = 0; c < headers.length; c++) {
                    if (c != 4) { // 金额列已有样式
                        row.getCell(c).setCellStyle(dataStyle);
                    }
                }
                rowIndex++;
            }

            // ---------- 4. 合并单元格 ----------
            // 在数据下面一行做合计：合并前 3 列，写“合计”
            Row totalRow = sheet.createRow(rowIndex);
            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("合计");
            totalLabel.setCellStyle(headerStyle);
            // CellRangeAddress 参数：firstRow, lastRow, firstCol, lastCol（都含边界，0 基）
            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 2));
            // 合计公式：SUM 整个金额列的数据区间
            Cell totalAmount = totalRow.createCell(4);
            totalAmount.setCellFormula("SUM(E2:E" + rowIndex + ")");
            totalAmount.setCellStyle(moneyStyle);

            // ---------- 5. 下拉列表（数据校验） ----------
            // 状态列添加下拉框，限制可填的值。显式列表最长 255 字符，
            // 更长的选项应写到隐藏 Sheet 里用名称引用。
            DataValidationHelper helper = sheet.getDataValidationHelper();
            DataValidationConstraint constraint =
                    helper.createExplicitListConstraint(new String[]{"已付款", "待付款", "已发货"});
            // 校验范围覆盖状态列的数据区（F2:F4）
            // 注意：createValidation 需要的是 CellRangeAddressList，构造参数为 firstRow/lastRow/firstCol/lastCol
            CellRangeAddressList range = new CellRangeAddressList(1, data.length, 5, 5);
            DataValidation validation = helper.createValidation(constraint, range);
            // 出错时的提示行为
            validation.setShowErrorBox(true);
            validation.createErrorBox("输入无效", "请从下拉列表中选择状态");
            sheet.addValidationData(validation);

            // ---------- 6. 插入图片 ----------
            // POI 插图三步：Drawing（画布）→ ClientAnchor（在哪个矩形区域）→ addPicture
            XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
            // anchor 坐标单位是 EMU（英制单位，1 像素 ≈ 9525 EMU），Units 类提供了换算常量
            XSSFClientAnchor anchor = new XSSFClientAnchor(
                    0, 0, 0, 0,
                    0, rowIndex + 2, 2, rowIndex + 6); // 占据 A 列到 B 列、合计下方 4 行的区域
            anchor.setAnchorType(XSSFClientAnchor.AnchorType.MOVE_AND_RESIZE);
            // 读取演示图片（来自 classpath），实际项目里图片通常来自上传或对象存储
            byte[] png;
            try (var in = getClass().getResourceAsStream("/demo-image.png")) {
                if (in == null) {
                    throw new IllegalStateException("classpath 下找不到 /demo-image.png");
                }
                png = in.readAllBytes();
            }
            int pictureIdx = workbook.addPicture(png, Workbook.PICTURE_TYPE_PNG);
            drawing.createPicture(anchor, pictureIdx);

            // ---------- 7. 写出 ----------
            workbook.write(output);
            // try-with-resources 会自动调用 workbook.close()，清理 OOXML 的临时解包文件
        }
    }

    /**
     * 读取一个 Excel 文件，把每个 Sheet 的内容解析成结构化数据。
     * 演示了读取时的三个关键问题：
     * <ol>
     *   <li>单元格类型多样（数字/字符串/公式/布尔/空），用 getCellType() 判断或用 DataFormatter 统一转文本；</li>
     *   <li>公式单元格默认拿到的是公式串，需要 FormulaEvaluator 才能拿到计算结果；</li>
     *   <li>可能存在合并单元格，只有左上角单元格有值，其余为 null。</li>
     * </ol>
     *
     * @param bytes 上传的 Excel 文件内容
     * @return key 为 Sheet 名，value 为该 Sheet 逐行单元格的文本表示
     */
    public Map<String, List<List<String>>> readWorkbook(byte[] bytes) throws Exception {
        // XSSFWorkbook 支持从 InputStream 构造，用于解析已有文件
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {

            // FormulaEvaluator：负责执行公式。注意它计算的是 POI 内置支持的公式，
            // 个别复杂函数（少数新函数）可能抛 NotImplementedException，需要自行降级
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            // DataFormatter：按“Excel 里显示的样子”把任意类型单元格转成字符串，
            // 是读取场景最省心的工具（例如日期 2026-01-01 不会变成数字 46023）
            DataFormatter formatter = new DataFormatter();

            Map<String, List<List<String>>> result = new LinkedHashMap<>();
            for (Sheet sheet : workbook) { // Workbook 实现了 Iterable<Sheet>
                List<List<String>> rowsData = new ArrayList<>();
                // 遍历行：rowIterator() 返回的是物理存在的行，
                // 如果 Sheet 有删过的空行，行对象可能为 null，需要判空
                for (Row row : sheet) {
                    List<String> cellsData = new ArrayList<>();
                    // 按列号 0..lastCellNum 顺序取，避免中间空单元格导致列错位
                    for (int c = 0; c < row.getLastCellNum(); c++) {
                        Cell cell = row.getCell(c);
                        if (cell == null) {
                            cellsData.add("");
                            continue;
                        }
                        String text;
                        if (cell.getCellType() == CellType.FORMULA) {
                            // 公式单元格：先评估再按结果格式化
                            // evaluate() 返回计算后的“值单元格”，再交给 DataFormatter 输出
                            text = formatter.formatCellValue(cell, evaluator);
                        } else {
                            text = formatter.formatCellValue(cell);
                        }
                        cellsData.add(text);
                    }
                    rowsData.add(cellsData);
                }
                result.put(sheet.getSheetName(), rowsData);
            }
            return result;
        }
    }
}
