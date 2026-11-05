package com.zhoubyte.rag.ingestion.parser;

import com.zhoubyte.rag.ingestion.block.Block;
import com.zhoubyte.rag.ingestion.block.ParsedDocument;
import com.zhoubyte.rag.ingestion.util.MarkdownTables;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel（.xlsx）解析器（POI XSSF）：每个 sheet 整表转 Markdown 表格块，永不跨块切碎。
 *
 * <p>财务标准、参数配置这类"答案就在表里"的文档，整表入库是检索准确率的关键。</p>
 */
@Component
public class ExcelParser implements DocumentParser {

    private static final int MAX_ROWS = 800;
    private static final int MAX_COLUMNS = 40;

    private final DataFormatter formatter = new DataFormatter();

    @Override
    public boolean supports(String extension) {
        return extension.equals("xlsx") || extension.equals("xlsm");
    }

    @Override
    public ParsedDocument parse(byte[] data, String fileName) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(data))) {
            List<Block> blocks = new ArrayList<>();
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                XSSFSheet sheet = workbook.getSheetAt(s);
                String markdown = sheetToMarkdown(sheet);
                if (markdown != null) {
                    blocks.add(Block.table("### Sheet: " + sheet.getSheetName() + "\n\n" + markdown, null));
                }
            }
            String title = fileName.replaceAll("\\.[^.]+$", "");
            return new ParsedDocument(title, blocks);
        }
    }

    private String sheetToMarkdown(XSSFSheet sheet) {
        int lastRow = Math.min(sheet.getLastRowNum(), MAX_ROWS);
        List<String> headers = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();

        for (int r = 0; r <= lastRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            List<String> cells = new ArrayList<>();
            for (int c = 0; c < Math.min(row.getLastCellNum(), MAX_COLUMNS); c++) {
                cells.add(formatter.formatCellValue(row.getCell(c)).trim());
            }
            if (cells.stream().allMatch(String::isEmpty)) {
                continue;
            }
            if (headers.isEmpty()) {
                headers.addAll(cells);
            } else {
                rows.add(cells);
            }
        }
        if (headers.isEmpty()) {
            return null;
        }
        return MarkdownTables.toMarkdown(headers, rows);
    }
}
