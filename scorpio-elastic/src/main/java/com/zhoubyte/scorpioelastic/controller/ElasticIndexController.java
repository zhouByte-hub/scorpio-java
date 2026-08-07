package com.zhoubyte.scorpioelastic.controller;

import com.zhoubyte.scorpioelastic.common.Result;
import com.zhoubyte.scorpioelastic.dto.index.CreateIndexRequest;
import com.zhoubyte.scorpioelastic.dto.index.IndexFieldRequest;
import com.zhoubyte.scorpioelastic.dto.index.IndexInfoResponse;
import com.zhoubyte.scorpioelastic.service.IndexService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
@RequestMapping(value = "/indices")
public class ElasticIndexController {

    private final IndexService indexService;

    public ElasticIndexController(IndexService indexService) {
        this.indexService = indexService;
    }

    @PostMapping
    public Result<Boolean> createIndex(@Valid @RequestBody CreateIndexRequest request) {
        return Result.success(indexService.createIndex(request));
    }

    @DeleteMapping("/{indexName}")
    public Result<Boolean> deleteIndex(@PathVariable @NotBlank(message = "索引名称不能为空") String indexName) {
        return Result.success(indexService.deleteIndex(indexName));
    }

    @GetMapping("/{indexName}/exists")
    public Result<Boolean> exists(@PathVariable @NotBlank(message = "索引名称不能为空") String indexName) {
        return Result.success(indexService.exists(indexName));
    }

    @GetMapping("/{indexName}")
    public Result<IndexInfoResponse> getIndexInfo(
            @PathVariable @NotBlank(message = "索引名称不能为空") String indexName) {
        return Result.success(indexService.getIndexInfo(indexName));
    }

    @PutMapping("/{indexName}/mapping")
    public Result<Map<String, Object>> putMapping(
            @PathVariable @NotBlank(message = "索引名称不能为空") String indexName,
            @Valid @NotEmpty(message = "索引字段不能为空") @RequestBody List<IndexFieldRequest> fields) {
        return Result.success(indexService.putMapping(indexName, fields));
    }

    @GetMapping
    public Result<List<String>> listIndices() {
        return Result.success(indexService.listIndices());
    }
}
