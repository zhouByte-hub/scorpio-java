package com.zhoubyte.stdio.dto;

import java.util.List;
import java.util.Map;

public class DbQueryResult {

    private List<Map<String, Object>> rows;
    private Integer size;

    public DbQueryResult(List<Map<String, Object>> rows, Integer size) {
        this.rows = rows;
        this.size = size;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
