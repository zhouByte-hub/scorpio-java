package com.zhoubyte.scorpioelastic.controller;

import com.zhoubyte.scorpioelastic.common.Result;
import com.zhoubyte.scorpioelastic.dto.document.BatchDeleteRequest;
import com.zhoubyte.scorpioelastic.dto.document.BatchUserDocumentRequest;
import com.zhoubyte.scorpioelastic.dto.document.DynamicDocumentRequest;
import com.zhoubyte.scorpioelastic.dto.document.DynamicSearchRequest;
import com.zhoubyte.scorpioelastic.dto.document.PageResponse;
import com.zhoubyte.scorpioelastic.dto.document.UserDocumentRequest;
import com.zhoubyte.scorpioelastic.dto.document.UserHighlightResponse;
import com.zhoubyte.scorpioelastic.dto.document.UserSearchRequest;
import com.zhoubyte.scorpioelastic.entity.UserDocEntity;
import com.zhoubyte.scorpioelastic.service.DocumentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping(value = "/documents")
public class ElasticDocumentController {

    private final DocumentService documentService;

    public ElasticDocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/users")
    public Result<UserDocEntity> saveUser(@Valid @RequestBody UserDocumentRequest request) {
        return Result.success(documentService.saveUser(request));
    }

    @GetMapping("/users/{id}")
    public Result<UserDocEntity> getUser(@PathVariable @NotBlank(message = "文档 ID 不能为空") String id) {
        return Result.success(documentService.getUser(id));
    }

    @PutMapping("/users/{id}")
    public Result<UserDocEntity> updateUser(
            @PathVariable @NotBlank(message = "文档 ID 不能为空") String id,
            @Valid @RequestBody UserDocumentRequest request) {
        return Result.success(documentService.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    public Result<Boolean> deleteUser(@PathVariable @NotBlank(message = "文档 ID 不能为空") String id) {
        return Result.success(documentService.deleteUser(id));
    }

    @PostMapping("/users/batch")
    public Result<List<UserDocEntity>> batchSaveUsers(@Valid @RequestBody BatchUserDocumentRequest request) {
        return Result.success(documentService.batchSaveUsers(request));
    }

    @DeleteMapping("/users/batch")
    public Result<Boolean> batchDeleteUsers(@Valid @RequestBody BatchDeleteRequest request) {
        return Result.success(documentService.batchDeleteUsers(request));
    }

    @PostMapping("/users/search")
    public Result<PageResponse<UserDocEntity>> searchUsers(@Valid @RequestBody UserSearchRequest request) {
        return Result.success(documentService.searchUsers(request));
    }

    @PostMapping("/users/search/highlight")
    public Result<PageResponse<UserHighlightResponse>> highlightSearchUsers(
            @Valid @RequestBody UserSearchRequest request) {
        return Result.success(documentService.highlightSearchUsers(request));
    }

    @PostMapping("/dynamic")
    public Result<Map<String, Object>> saveDynamicDocument(@Valid @RequestBody DynamicDocumentRequest request) {
        return Result.success(documentService.saveDynamicDocument(request));
    }

    @GetMapping("/dynamic/{indexName}/{id}")
    public Result<Map<String, Object>> getDynamicDocument(
            @PathVariable @NotBlank(message = "索引名称不能为空") String indexName,
            @PathVariable @NotBlank(message = "文档 ID 不能为空") String id) {
        return Result.success(documentService.getDynamicDocument(indexName, id));
    }

    @DeleteMapping("/dynamic/{indexName}/{id}")
    public Result<Boolean> deleteDynamicDocument(
            @PathVariable @NotBlank(message = "索引名称不能为空") String indexName,
            @PathVariable @NotBlank(message = "文档 ID 不能为空") String id) {
        return Result.success(documentService.deleteDynamicDocument(indexName, id));
    }

    @PostMapping("/dynamic/search")
    public Result<PageResponse<Map<String, Object>>> searchDynamicDocuments(
            @Valid @RequestBody DynamicSearchRequest request) {
        return Result.success(documentService.searchDynamicDocuments(request));
    }
}
