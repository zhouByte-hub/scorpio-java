package com.zhoubyte.scorpioaspose.service;

import java.io.File;
import java.util.List;

/**
 * 文档存储服务：统一管理存储路径与文件查找
 */
public interface DocumentStorageService {

    /**
     * 获取文档存储根路径（以 / 结尾）
     */
    String getStoragePath();

    /**
     * 按类型获取上传目录，例如 docx、pdf
     */
    String getTypeDir(String type);

    /**
     * 根据文档 ID 查找文件
     */
    File findFileById(String fileId);

    /**
     * 扫描支持的文档列表
     */
    List<File> listDocumentFiles();

    /**
     * 删除文档（文件及所属目录）
     */
    boolean deleteById(String fileId);
}
