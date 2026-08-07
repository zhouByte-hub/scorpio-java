package com.zhoubyte.scorpioelastic.service;

import com.zhoubyte.scorpioelastic.dto.document.BatchDeleteRequest;
import com.zhoubyte.scorpioelastic.dto.document.BatchUserDocumentRequest;
import com.zhoubyte.scorpioelastic.dto.document.DynamicDocumentRequest;
import com.zhoubyte.scorpioelastic.dto.document.DynamicSearchRequest;
import com.zhoubyte.scorpioelastic.dto.document.PageResponse;
import com.zhoubyte.scorpioelastic.dto.document.UserDocumentRequest;
import com.zhoubyte.scorpioelastic.dto.document.UserHighlightResponse;
import com.zhoubyte.scorpioelastic.dto.document.UserSearchRequest;
import com.zhoubyte.scorpioelastic.entity.UserDocEntity;

import java.util.List;
import java.util.Map;

public interface DocumentService {

    UserDocEntity saveUser(UserDocumentRequest request);

    UserDocEntity getUser(String id);

    UserDocEntity updateUser(String id, UserDocumentRequest request);

    Boolean deleteUser(String id);

    List<UserDocEntity> batchSaveUsers(BatchUserDocumentRequest request);

    Boolean batchDeleteUsers(BatchDeleteRequest request);

    PageResponse<UserDocEntity> searchUsers(UserSearchRequest request);

    PageResponse<UserHighlightResponse> highlightSearchUsers(UserSearchRequest request);

    Map<String, Object> saveDynamicDocument(DynamicDocumentRequest request);

    Map<String, Object> getDynamicDocument(String indexName, String id);

    Boolean deleteDynamicDocument(String indexName, String id);

    PageResponse<Map<String, Object>> searchDynamicDocuments(DynamicSearchRequest request);
}
