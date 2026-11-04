package com.zhoubyte.rag.pipeline.model;

/**
 * 文档元数据：用于权限/范围过滤与溯源展示。
 *
 * @param department 所属部门，如“研发部”“财务部”
 * @param level      密级，如 public / internal / secret
 * @param docType    文档类型，如 规章制度 / 技术方案 / 会议纪要
 * @param docDate    文档日期，格式 yyyy-MM-dd
 */
public record MetadataInfo(String department, String level, String docType, String docDate) {

    public MetadataInfo {
        if (department == null) department = "";
        if (level == null) level = "public";
        if (docType == null) docType = "";
        if (docDate == null) docDate = "";
    }
}
