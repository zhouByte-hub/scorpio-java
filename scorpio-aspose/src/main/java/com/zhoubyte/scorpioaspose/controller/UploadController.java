package com.zhoubyte.scorpioaspose.controller;

import com.zhoubyte.scorpioaspose.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class UploadController<T> {

    @Resource
    protected T service;

    private final String UPLOAD_FILE_TYPE;
    private String UPLOAD_FILE_PATH = System.getProperty("user.dir") + "/scorpio-aspose/src/main/resources/";

    public UploadController(String type) {
        UPLOAD_FILE_TYPE = type;
    }

    @PostMapping(value = "/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if(file == null) {
            throw new RuntimeException("file can't is null");
        }
        String[] split = Objects.requireNonNull(file.getOriginalFilename()).split("\\.");
        if(split.length < 2) {
            throw new RuntimeException("文件缺少相应的后缀，支持【Word|PDF】");
        }
        String suffix = split[split.length - 1];
        String uploadPath;
        if(UPLOAD_FILE_TYPE.equals(suffix.toUpperCase(Locale.ROOT))){
            uploadPath = UPLOAD_FILE_PATH + UPLOAD_FILE_TYPE.toLowerCase(Locale.ROOT);
        }else{
            throw new RuntimeException("格式错误，所需格式：" + UPLOAD_FILE_TYPE);
        }
        String dir = UUID.randomUUID().toString().replace("-", "");
        String targetPath = uploadPath + "/" + dir + "/" + (dir + "." + suffix);
        File targetFile = new File(targetPath);
        try {
            File parentDir = targetFile.getParentFile();
            if (!parentDir.exists()) {
                Files.createDirectories(parentDir.toPath());
            }
            if(!targetFile.exists()) {
                Files.createFile(targetFile.toPath());
            }
            file.transferTo(targetFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Result.success(targetPath);
    }
}
