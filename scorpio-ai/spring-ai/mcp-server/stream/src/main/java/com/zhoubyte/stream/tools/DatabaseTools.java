package com.zhoubyte.stream.tools;

import com.zhoubyte.stream.dto.DbQueryResult;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseTools {

    @McpTool(name = "query_database", description = "在数据库中执行只读查询，返回查询结果和行数。")
    public DbQueryResult queryDatabase(@McpToolParam(description = "需要执行的Query SQL") String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("sql 不能为空");
        }

        String normalized = sql.trim().toLowerCase();
        if (!normalized.startsWith("select")) {
            throw new IllegalArgumentException("仅允许 SELECT 查询");
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", i);
            row.put("name", "示例数据:" + i);
            row.put("created_at", LocalDateTime.now().toString());
            rows.add(row);
        }
        return new DbQueryResult(rows, rows.size());
    }
}
