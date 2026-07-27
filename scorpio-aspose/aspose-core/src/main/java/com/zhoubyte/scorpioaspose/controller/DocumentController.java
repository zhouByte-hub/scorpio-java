package com.zhoubyte.scorpioaspose.controller;

import com.zhoubyte.scorpioaspose.dto.DocumentDto;
import com.zhoubyte.scorpioaspose.dto.EditorConfigDto;
import com.zhoubyte.scorpioaspose.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 文档管理Controller
 * 提供文档列表、下载、OnlyOffice编辑器配置接口
 */
@RestController
@RequestMapping(value = "/documents")
public class DocumentController {

    private static final String UPLOAD_FILE_PATH = System.getProperty("user.dir") + "/scorpio-aspose/src/main/resources/";

    @Value("${server.port:9527}")
    private int serverPort;

    @Value("${server.servlet.context-path:/aspose}")
    private String contextPath;

    /**
     * 获取文档列表
     */
    @GetMapping
    public Result<List<DocumentDto>> list() {
        List<DocumentDto> documents = new ArrayList<>();
        File baseDir = new File(UPLOAD_FILE_PATH);

        if (!baseDir.exists()) {
            return Result.success(documents);
        }

        // 支持的文档扩展名
        List<String> supportedExtensions = Arrays.asList("docx", "doc", "pdf");

        // 遍历资源目录下的所有子目录
        scanDirectory(baseDir, supportedExtensions, documents);

        // 按修改时间倒序
        documents.sort(Comparator.comparing(DocumentDto::getUpdatedAt).reversed());

        return Result.success(documents);
    }

    /**
     * 下载文档
     */
    @GetMapping(value = "/{fileId}/download")
    public void download(@PathVariable("fileId") String fileId, HttpServletResponse response) {
        // 在上传目录中查找包含该fileId的文件
        File baseDir = new File(UPLOAD_FILE_PATH);
        File targetFile = findFileById(baseDir, fileId);

        if (targetFile == null || !targetFile.exists()) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + targetFile.getName() + "\"");
        response.setContentLengthLong(targetFile.length());

        try (FileInputStream fis = new FileInputStream(targetFile);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    /**
     * 获取OnlyOffice编辑器配置
     */
    @GetMapping(value = "/{docId}/editor-config")
    public Result<EditorConfigDto> editorConfig(@PathVariable("docId") String docId) {
        File baseDir = new File(UPLOAD_FILE_PATH);
        File targetFile = findFileById(baseDir, docId);

        if (targetFile == null || !targetFile.exists()) {
            Result<EditorConfigDto> errorResult = Result.success(null);
            errorResult.setCode(HttpStatus.BAD_REQUEST.value());
            errorResult.setMessage("文件不存在");
            return errorResult;
        }

        String fileName = targetFile.getName();
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() : "docx";

        EditorConfigDto config = new EditorConfigDto();

        // 文档配置
        EditorConfigDto.DocumentConfig docConfig = new EditorConfigDto.DocumentConfig();
        docConfig.setFileType(ext);
        docConfig.setKey(docId + "-" + System.currentTimeMillis());
        docConfig.setTitle(fileName);
        docConfig.setUrl(getServerUrl() + contextPath + "/documents/" + docId + "/download");

        EditorConfigDto.PermissionConfig permConfig = new EditorConfigDto.PermissionConfig();
        docConfig.setPermissions(permConfig);

        config.setDocument(docConfig);

        // 文档类型
        if (Arrays.asList("doc", "docx", "odt", "rtf", "txt").contains(ext)) {
            config.setDocumentType("word");
        } else if (Arrays.asList("xls", "xlsx", "ods", "csv").contains(ext)) {
            config.setDocumentType("cell");
        } else if (Arrays.asList("ppt", "pptx", "odp").contains(ext)) {
            config.setDocumentType("slide");
        } else {
            config.setDocumentType("word");
        }

        // 编辑器配置
        EditorConfigDto.EditorConfig editorCfg = new EditorConfigDto.EditorConfig();
        editorCfg.setCallbackUrl(getServerUrl() + contextPath + "/documents/" + docId + "/callback");
        editorCfg.setLang("zh-CN");
        editorCfg.setMode("edit");
        editorCfg.setUser(new EditorConfigDto.UserConfig("1", "用户"));

        EditorConfigDto.CustomizationConfig customCfg = new EditorConfigDto.CustomizationConfig();
        editorCfg.setCustomization(customCfg);

        config.setEditorConfig(editorCfg);

        return Result.success(config);
    }

    /**
     * OnlyOffice回调接口（保存文档）
     */
    @GetMapping(value = "/{docId}/callback")
    public Result<String> callback(@PathVariable("docId") String docId) {
        // OnlyOffice的回调通常为POST请求，此处提供GET占位
        return Result.success("ok");
    }

    /**
     * 递归扫描目录，查找支持的文档文件
     */
    private void scanDirectory(File dir, List<String> supportedExtensions, List<DocumentDto> documents) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, supportedExtensions, documents);
            } else {
                String fileName = file.getName();
                String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() : "";
                if (supportedExtensions.contains(ext) && !fileName.contains("已填充")) {
                    documents.add(DocumentDto.fromFile(file, contextPath));
                }
            }
        }
    }

    /**
     * 根据文件ID查找文件（文件名去掉扩展名即为ID）
     */
    private File findFileById(File dir, String fileId) {
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                File found = findFileById(file, fileId);
                if (found != null) {
                    return found;
                }
            } else {
                String name = file.getName();
                String id = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
                if (id.equals(fileId)) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * 获取服务器地址
     */
    private String getServerUrl() {
        try {
            return "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + serverPort;
        } catch (UnknownHostException e) {
            return "http://localhost:" + serverPort;
        }
    }
}
