package com.zhoubyte.scorpioaspose.service.impl;

import com.zhoubyte.scorpioaspose.service.DocumentStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DocumentStorageServiceImpl implements DocumentStorageService {

    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList("docx", "doc", "pdf");

    @Value("${document.storage.path:}")
    private String documentStoragePath;

    @Override
    public String getStoragePath() {
        if (documentStoragePath != null && !documentStoragePath.isEmpty()) {
            String path = documentStoragePath;
            return path.endsWith("/") || path.endsWith("\\") ? path : path + "/";
        }
        String userDir = System.getProperty("user.dir");
        File dir = new File(userDir);
        for (int i = 0; i < 4; i++) {
            File candidate = new File(dir, "scorpio-aspose/src/main/resources");
            if (candidate.exists()) {
                return candidate.getAbsolutePath() + "/";
            }
            File candidate2 = new File(dir, "src/main/resources");
            if (candidate2.exists()) {
                return candidate2.getAbsolutePath() + "/";
            }
            dir = dir.getParentFile();
            if (dir == null) {
                break;
            }
        }
        return userDir + "/scorpio-aspose/src/main/resources/";
    }

    @Override
    public String getTypeDir(String type) {
        return getStoragePath() + type.toLowerCase();
    }

    @Override
    public File findFileById(String fileId) {
        if (fileId == null || fileId.isBlank()) {
            return null;
        }
        File baseDir = new File(getStoragePath());
        if (!baseDir.exists()) {
            return null;
        }
        // 优先按目录名匹配（上传约定：{type}/{uuid}/{originalName.ext}）
        File byDir = findByDirectoryName(baseDir, fileId);
        if (byDir != null) {
            return byDir;
        }
        // 兼容旧约定：文件名去扩展名即为 ID
        return findByFileNameId(baseDir, fileId);
    }

    @Override
    public List<File> listDocumentFiles() {
        List<File> result = new ArrayList<>();
        File baseDir = new File(getStoragePath());
        if (!baseDir.exists()) {
            return result;
        }
        scanDirectory(baseDir, result);
        return result;
    }

    @Override
    public boolean deleteById(String fileId) {
        File target = findFileById(fileId);
        if (target == null || !target.exists()) {
            return false;
        }
        File parent = target.getParentFile();
        boolean deleted = target.delete();
        if (deleted && parent != null && parent.isDirectory()) {
            File[] remain = parent.listFiles();
            if (remain == null || remain.length == 0) {
                parent.delete();
            }
        }
        return deleted;
    }

    private File findByDirectoryName(File dir, String fileId) {
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                if (fileId.equals(file.getName())) {
                    File document = firstSupportedFile(file);
                    if (document != null) {
                        return document;
                    }
                }
                File found = findByDirectoryName(file, fileId);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private File findByFileNameId(File dir, String fileId) {
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                File found = findByFileNameId(file, fileId);
                if (found != null) {
                    return found;
                }
            } else {
                String name = file.getName();
                String id = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
                if (fileId.equals(id) && isSupported(name)) {
                    return file;
                }
            }
        }
        return null;
    }

    private File firstSupportedFile(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (file.isFile() && isSupported(file.getName()) && !file.getName().contains("已填充")) {
                return file;
            }
        }
        return null;
    }

    private void scanDirectory(File dir, List<File> documents) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, documents);
            } else if (isSupported(file.getName()) && !file.getName().contains("已填充")) {
                documents.add(file);
            }
        }
    }

    private boolean isSupported(String fileName) {
        String ext = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
                : "";
        return SUPPORTED_EXTENSIONS.contains(ext);
    }
}
