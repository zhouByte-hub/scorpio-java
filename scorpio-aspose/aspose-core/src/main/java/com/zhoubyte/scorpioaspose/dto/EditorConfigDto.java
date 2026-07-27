package com.zhoubyte.scorpioaspose.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * OnlyOffice编辑器配置DTO
 */
public class EditorConfigDto {

    private DocumentConfig document;
    private String documentType;
    private EditorConfig editorConfig;

    public DocumentConfig getDocument() {
        return document;
    }

    public void setDocument(DocumentConfig document) {
        this.document = document;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public EditorConfig getEditorConfig() {
        return editorConfig;
    }

    public void setEditorConfig(EditorConfig editorConfig) {
        this.editorConfig = editorConfig;
    }

    /**
     * 文档配置
     */
    public static class DocumentConfig {
        private String fileType;
        private String key;
        private String title;
        private String url;
        private PermissionConfig permissions;

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public PermissionConfig getPermissions() {
            return permissions;
        }

        public void setPermissions(PermissionConfig permissions) {
            this.permissions = permissions;
        }
    }

    /**
     * 权限配置
     */
    public static class PermissionConfig {
        private boolean edit = true;
        private boolean download = true;
        private boolean print = true;

        public boolean isEdit() {
            return edit;
        }

        public void setEdit(boolean edit) {
            this.edit = edit;
        }

        public boolean isDownload() {
            return download;
        }

        public void setDownload(boolean download) {
            this.download = download;
        }

        public boolean isPrint() {
            return print;
        }

        public void setPrint(boolean print) {
            this.print = print;
        }
    }

    /**
     * 编辑器配置
     */
    public static class EditorConfig {
        private String callbackUrl;
        private String lang = "zh-CN";
        private String mode = "edit";
        private UserConfig user;
        private CustomizationConfig customization;

        public String getCallbackUrl() {
            return callbackUrl;
        }

        public void setCallbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public UserConfig getUser() {
            return user;
        }

        public void setUser(UserConfig user) {
            this.user = user;
        }

        public CustomizationConfig getCustomization() {
            return customization;
        }

        public void setCustomization(CustomizationConfig customization) {
            this.customization = customization;
        }
    }

    /**
     * 用户配置
     */
    public static class UserConfig {
        private String id;
        private String name;

        public UserConfig(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * 自定义配置
     */
    public static class CustomizationConfig {
        private boolean autosave = true;
        private boolean forcesave = true;
        private String uiTheme = "theme-light";

        public boolean isAutosave() {
            return autosave;
        }

        public void setAutosave(boolean autosave) {
            this.autosave = autosave;
        }

        public boolean isForcesave() {
            return forcesave;
        }

        public void setForcesave(boolean forcesave) {
            this.forcesave = forcesave;
        }

        public String getUiTheme() {
            return uiTheme;
        }

        public void setUiTheme(String uiTheme) {
            this.uiTheme = uiTheme;
        }
    }
}
