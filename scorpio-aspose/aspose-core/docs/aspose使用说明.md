# Aspose 使用说明

本文档介绍 `scorpio-aspose` 模块中 Aspose.PDF 与 Aspose.Words 的常用类、API 含义、方法行为细节及典型使用场景，便于后续开发维护参考。

---

## 一、概述

### 1.1 模块定位

`scorpio-aspose` 模块基于 **Aspose.Total for Java** 提供文档处理能力，主要覆盖以下场景：

- PDF ↔ Word 互转
- PDF/Word 转图片
- PDF 表单字段枚举与填充
- PDF 文本查找替换
- Word 占位符填充（`{{xxx}}` 文本替换）
- Word 书签填充（Bookmark）
- PDF/Word 内容识别与字段检测

### 1.2 依赖情况

| 依赖 | 版本 | classifier | 用途 |
|------|------|------------|------|
| `com.aspose:aspose-pdf` | 26.6 | - | PDF 文档处理 |
| `com.aspose:aspose-words` | 26.6 | `jdk17` | Word 文档处理 |
| `com.aspose:aspose-cells` | 26.6 | - | Excel 文档处理（预留） |
| `com.aspose:aspose-ocr` | 26.5.0 | - | OCR 文字识别（预留） |

### 1.3 仓库配置

Aspose 依赖托管在私有仓库，**必须**在 `../../../pom.xml` 显式声明，否则依赖无法下载：

```xml
<repositories>
    <repository>
        <id>AsposeJavaAPI</id>
        <name>Aspose Java API</name>
        <url>https://repository.aspose.com/repo/</url>
    </repository>
</repositories>
```

> Aspose 的另一个常用仓库地址是 `https://releases.aspose.com/java/repo/`，二者均可访问相同内容。

### 1.4 aspose-words 依赖关键注意点

Aspose.Words 26.6 在仓库中**只发布带 classifier 的 jar**，不同 JDK 版本对应不同 classifier：

| classifier | 适用 JDK 版本 |
|------------|--------------|
| `jdk17` | Java 17 及以上（推荐用于 Java 21） |
| `jdk11` | Java 11 |
| `jdk8` | Java 8 |

**正确配置**（本项目使用）：

```xml
<dependency>
    <groupId>com.aspose</groupId>
    <artifactId>aspose-words</artifactId>
    <version>26.6</version>
    <classifier>jdk17</classifier>
</dependency>
```

**常见错误**：
- ❌ `<type>pom</type>` → 只会引入元数据 POM，找不到 `com.aspose.words.*` 类
- ❌ 不指定 classifier → 报 `Unresolved dependency: 'com.aspose:aspose-words:jar:26.6'`

### 1.5 评估模式限制

未加载 License 时，Aspose 进入**评估模式**，限制：

| 模块 | 评估模式限制 |
|------|------------|
| Aspose.PDF | 最多处理 **4 页**；生成的文档带水印 |
| Aspose.Words | 生成的文档带水印；部分高级 API 不可用 |

正式使用需加载 License 文件（详见 5.9 节）。

---

## 二、Aspose.PDF 常用类详解

### 2.1 文档核心类

#### `com.aspose.pdf.Document`

PDF 文档对象，是操作 PDF 的入口类。

**特性**：
- **实现 `Closeable` 接口**，支持 try-with-resources
- 加载时会完整解析 PDF 结构到内存
- 修改后必须调用 `save()` 才能持久化

**构造方法**：

| 构造方法 | 说明 |
|---------|------|
| `new Document(String filePath)` | 从文件路径加载 PDF |
| `new Document(InputStream stream)` | 从输入流加载 PDF |
| `new Document()` | 创建空白 PDF 文档 |

**核心方法**：

| 方法 | 返回值 | 作用 |
|------|--------|------|
| `getPages()` | `PageCollection` | 获取所有页面集合 |
| `getForm()` | `Form` | 获取 AcroForm 表单对象 |
| `save(String filePath)` | `void` | 保存为 PDF 格式 |
| `save(String filePath, SaveFormat format)` | `void` | 按指定格式保存（DocX、HTML、XPS 等） |
| `close()` | `void` | 关闭文档释放资源（Closeable 接口方法） |

**使用示例**：

```java
// 推荐：try-with-resources 自动释放
try (Document document = new Document(filePath)) {
    // 操作 document
    document.save(outputPath);
} catch (Exception e) {
    // 异常处理
}
```

#### `com.aspose.pdf.Page` 与 `PageCollection`

| 类 | 作用 | 关键方法 |
|----|------|---------|
| `Page` | PDF 单个页面 | `getRect()` 获取页面尺寸、`getParagraphs()` 获取段落、`accept(absorber)` 接收文本吸收器 |
| `PageCollection` | 页面集合 | `size()` 获取页数、`get_Item(int)` 按索引获取页面（**索引从 1 开始**） |

> ⚠️ **注意**：Aspose.PDF 的页面索引**从 1 开始**，与 Aspose.Words（从 0 开始）相反，使用时务必区分。

#### `com.aspose.pdf.Paragraphs`

页面段落集合，包含文本段、图片、表格等内容元素。

---

### 2.2 表单类（AcroForm）

#### `com.aspose.pdf.Form`

PDF AcroForm 表单对象，通过 `document.getForm()` 获取。

**核心方法**：

| 方法 | 返回值 | 作用 |
|------|--------|------|
| `getFields()` | `Field[]` | 获取所有表单字段 |
| `getField(String name)` | `Field` | 按字段名获取字段 |
| `fill(Font font)` | `void` | 设置表单字段的默认字体 |

#### `com.aspose.pdf.Field` 及子类

`Field` 是表单字段基类，常用子类：

| 子类 | 作用 | 设置值方式 |
|------|------|-----------|
| `TextBoxField` | 文本输入框 | `setValue(String)` |
| `CheckBoxField` | 复选框 | `setValue("Yes"/"Off")` |
| `RadioButtonField` | 单选按钮组 | `setValue(选项值)` |
| `ComboBoxField` | 下拉框 | `setValue(选项值)` |
| `ListBoxField` | 列表框 | `setValue(选项值)` |

**Field 核心方法**：

| 方法 | 返回值 | 作用 |
|------|--------|------|
| `getFullName()` | `String` | 获取字段全名（含层级路径） |
| `getPartialName()` | `String` | 获取字段短名 |
| `getValue()` / `setValue(String)` | `String` | 获取/设置字段值 |
| `getType()` | `int` | 获取字段类型常量 |
| `getPage()` | `Page` | 获取字段所在页 |

> ⚠️ **关键认知**：表单字段必须通过 Adobe Acrobat、福昕等 PDF 编辑工具预先创建，普通文本占位符 `${xxx}`、`{{xxx}}` **不会**被识别为表单字段。Word 转 PDF 后也不会自动产生 AcroForm 字段。

---

### 2.3 文本处理类

#### `com.aspose.pdf.TextFragmentAbsorber`

文本吸收器，按指定文本或正则查找页面中的文本片段。是 PDF 文本替换的核心类。

**构造方法**：

| 构造方法 | 说明 |
|---------|------|
| `new TextFragmentAbsorber(String text)` | 按精确文本查找 |
| `new TextFragmentAbsorber(String regex, TextSearchOptions options)` | 按正则查找（需 `options.setRegularExpressionUsed(true)`） |

**核心方法**：

| 方法 | 返回值 | 作用 |
|------|--------|------|
| `getTextFragments()` | `TextFragmentCollection` | 获取所有匹配的文本片段 |
| `getPattern()` | `String` | 获取查询模式 |

**使用流程**：

```java
// 1. 创建吸收器
TextFragmentAbsorber absorber = new TextFragmentAbsorber("${姓名}");
// 2. 在所有页面执行查找（页面索引从 1 开始）
document.getPages().accept(absorber);
// 3. 遍历结果替换
for (TextFragment fragment : absorber.getTextFragments()) {
    fragment.setText("张三");
}
```

#### `com.aspose.pdf.TextFragment`

文本片段，包含文本内容、位置、字体等完整信息。

**核心方法**：

| 方法 | 返回值 | 作用 |
|------|--------|------|
| `getText()` / `setText(String)` | `String` | 获取/设置文本内容 |
| `getPosition()` | `Position` | 获取文本在页面中的坐标 |
| `getTextState()` | `TextState` | 获取文本样式（字体、字号、颜色） |
| `getRectangle()` | `Rectangle` | 获取文本所占矩形区域 |

**替换时的样式保留**：
- 调用 `setText()` 替换文本时，**会保留原片段的字体、字号、颜色样式**
- 如果新文本比原文本长，**不会自动重排布局**，可能溢出原位置

#### `com.aspose.pdf.TextBuilder`

文本构建器，在指定页面位置追加新文本段，常用于在 PDF 上"盖章"式添加内容。

| 方法 | 作用 |
|------|------|
| `new TextBuilder(Page page)` | 为指定页面创建构建器 |
| `appendParagraph(TextFragment fragment)` | 在页面追加文本段 |
| `appendParagraph(TextFragment fragment, float x, float y)` | 在指定坐标追加文本段 |

---

### 2.4 设备类（Device）

设备类用于将 PDF 页面转换为其他格式（图片、打印机等），均位于 `com.aspose.pdf.devices` 包。

#### 图片设备类对比

| 类 | 输出格式 | 特点 |
|----|---------|------|
| `JpegDevice` | JPEG | 有损压缩，文件小 |
| `PngDevice` | PNG | 无损压缩，支持透明 |
| `BmpDevice` | BMP | 无损，文件大 |
| `TiffDevice` | TIFF | 支持多页合一 |

**构造方法**（以 JpegDevice 为例）：

| 构造方法 | 说明 |
|---------|------|
| `new JpegDevice()` | 默认分辨率（150x150） |
| `new JpegDevice(Resolution resolution)` | 指定分辨率 |
| `new JpegDevice(int width, int height)` | 指定输出尺寸 |
| `new JpegDevice(Resolution resolution, int quality)` | 指定分辨率和 JPEG 质量（0-100） |

**核心方法**：

| 方法 | 作用 |
|------|------|
| `process(Page page, OutputStream output)` | 将单页转换为图片输出到流 |
| `process(Document document, OutputStream output)` | 将整个文档转为多页图片（部分设备支持） |

**使用示例**：

```java
Document document = new Document(filePath);
JpegDevice device = new JpegDevice();
for (int i = 1; i <= document.getPages().size(); i++) {
    try (FileOutputStream os = new FileOutputStream("page_" + i + ".jpg")) {
        device.process(document.getPages().get_Item(i), os);
    }
}
```

#### `Resolution` 类

分辨率对象，用于图片设备构造：

```java
Resolution resolution = new Resolution(300);  // 300 DPI
JpegDevice device = new JpegDevice(resolution);
```

---

### 2.5 枚举与常量

#### `com.aspose.pdf.SaveFormat`

PDF 保存格式枚举，常用值：

| 常量 | 作用 |
|------|------|
| `SaveFormat.Pdf` | 保存为 PDF（默认） |
| `SaveFormat.DocX` | 保存为 Word DOCX |
| `SaveFormat.HTML` | 保存为 HTML |
| `SaveFormat.Xps` | 保存为 XPS |

> ⚠️ 与 `com.aspose.words.SaveFormat` 完全独立，**不要 import 错**。

#### `com.aspose.pdf.PageSize`

页面尺寸常量：

| 常量 | 尺寸 |
|------|------|
| `PageSize.A4` | 210mm × 297mm |
| `PageSize.A3` | 297mm × 420mm |
| `PageSize.LETTER` | 8.5" × 11" |
| `PageSize.LEGAL` | 8.5" × 14" |

---

### 2.6 License 类

#### `com.aspose.pdf.License`

PDF 模块许可证加载器。

**核心方法**：

| 方法 | 作用 |
|------|------|
| `setLicense(String filePath)` | 从文件路径加载 License |
| `setLicense(InputStream stream)` | 从输入流加载 License |

**加载时机**：在应用启动时加载一次即可（推荐 `@PostConstruct` 或静态代码块），全 JVM 进程生效。

---

## 三、Aspose.Words 常用类详解

### 3.1 文档核心类

#### `com.aspose.words.Document`

Word 文档对象，是操作 Word 的入口类。

**特性**：
- **未实现 `AutoCloseable` / `Closeable`**，**不能用 try-with-resources**
- 加载时会完整解析 Word 文档的 XML 结构到内存
- 内部以"节点树"形式组织内容（段落、表格、图片等都是节点）
- 修改后必须调用 `save()` 才能持久化

**构造方法**：

| 构造方法 | 说明 |
|---------|------|
| `new Document(String filePath)` | 从文件路径加载 Word |
| `new Document(String filePath, LoadOptions options)` | 加载时指定选项（编码、密码等） |
| `new Document(InputStream stream)` | 从输入流加载 |
| `new Document()` | 创建空白文档 |

**核心方法**：

| 方法 | 返回值 | 作用 |
|------|--------|------|
| `getRange()` | `Range` | 获取整个文档范围（用于查找替换、书签访问） |
| `getFirstSection()` | `Section` | 获取第一个章节 |
| `getSections()` | `SectionCollection` | 获取所有章节 |
| `getPageCount()` | `int` | 获取页数（**会触发布局计算**，耗时操作） |
| `getChildNodes()` | `NodeCollection` | 获取所有子节点 |
| `save(String filePath)` | `void` | 保存为 DOCX |
| `save(String filePath, SaveFormat format)` | `void` | 按指定格式保存 |
| `save(String filePath, SaveOptions options)` | `void` | 按指定选项保存（控制分辨率、加密等） |

**使用示例**：

```java
// ⚠️ 不能用 try-with-resources
Document document = new Document(filePath);
try {
    // 操作 document
    document.save(outputPath);
} catch (Exception e) {
    // 异常处理
}
```

#### `com.aspose.words.DocumentBuilder`

文档构建器，提供游标式 API 在文档中插入内容。比直接操作节点树更直观。

**核心方法**：

| 方法 | 作用 |
|------|------|
| `write(String text)` | 在当前位置插入文本 |
| `writeln(String text)` | 插入文本并换行 |
| `insertImage(String imagePath)` | 插入图片 |
| `insertHtml(String html)` | 插入 HTML 内容 |
| `startTable()` / `endTable()` | 开始/结束表格 |
| `insertCell()` | 插入表格单元格 |
| `moveToDocumentStart()` / `moveToDocumentEnd()` | 移动到文档开头/结尾 |
| `moveToParagraph(int, int)` | 移动到指定段落 |
| `moveToBookmark(String name)` | 移动到指定书签位置 |

**典型场景**：在文档指定位置追加内容、生成动态表格。

#### `com.aspose.words.Section` 与 `SectionCollection`

| 类 | 作用 |
|----|------|
| `Section` | 文档章节，包含独立的页面设置（页边距、纸张大小）、页眉页脚 |
| `SectionCollection` | 章节集合 |

一个 Word 文档可包含多个 Section，每个 Section 可有不同的页面方向、页眉页脚。

---

### 3.2 范围与查找替换

#### `com.aspose.words.Range`

文档范围，提供查找替换 API。通过 `document.getRange()` 获取整个文档范围。

**核心方法**：

| 方法 | 作用 |
|------|------|
| `getBookmarks()` | 获取范围内所有书签 |
| `replace(String oldText, String newText)` | 简单文本替换 |
| `replace(String oldText, String newText, FindReplaceOptions options)` | 文本替换 + 选项 |
| `replace(Pattern regex, String replacement)` | 正则替换为字符串 |
| `replace(Pattern regex, String replacement, FindReplaceOptions options)` | 正则替换 + 选项（**支持回调**） |
| `getText()` | 获取范围内所有文本 |

> ⚠️ **重要**：Aspose.Words 26.6 中 `Range.replace` **已移除** `replace(Pattern, IReplacingCallback)` 重载。必须通过 `FindReplaceOptions.setReplacingCallback()` 包装回调。

#### `com.aspose.words.FindReplaceOptions`

查找替换选项，控制替换行为。

**核心方法**：

| 方法 | 作用 |
|------|------|
| `setReplacingCallback(IReplacingCallback callback)` | **设置替换回调（核心方法）** |
| `setMatchCase(boolean value)` | 是否区分大小写（默认 false） |
| `setWholeWords(boolean value)` | 是否整词匹配（默认 false） |
| `setDirection(int direction)` | 查找方向（`FindReplaceDirection.FORWARD` / `BACKWARD`） |
| `setIgnoreDeleted(boolean value)` | 是否忽略删除修订中的内容 |
| `setIgnoreInserted(boolean value)` | 是否忽略插入修订中的内容 |

#### `com.aspose.words.IReplacingCallback` 接口

替换回调接口，每次匹配到目标文本时触发。

**接口方法**：

```java
int replacing(ReplacingArgs e) throws Exception;
```

**返回值**：`ReplaceAction` 枚举

| 返回值 | 含义 |
|--------|------|
| `ReplaceAction.REPLACE` | 替换匹配内容 |
| `ReplaceAction.SKIP` | 跳过本次匹配，不替换 |
| `ReplaceAction.STOP` | 终止后续查找 |

#### `com.aspose.words.ReplacingArgs`

替换参数对象，在回调方法中传入。

**核心方法**：

| 方法 | 作用 |
|------|------|
| `getMatch()` | 获取 `Matcher` 对象，可访问匹配结果和捕获组 |
| `setReplacement(String value)` | 设置替换文本（**核心方法**） |
| `getReplacement()` | 获取设置的替换文本 |
| `getMatchNode()` | 获取匹配到的节点 |

**Lambda 简写示例**：

```java
FindReplaceOptions options = new FindReplaceOptions();
options.setReplacingCallback(e -> {
    String placeholder = e.getMatch().group(0);  // 完整匹配，如 {{name}}
    String key = e.getMatch().group(1);          // 捕获组，如 name
    String value = fieldValues.get(key);
    if (value != null) {
        e.setReplacement(value);
    }
    return ReplaceAction.REPLACE;
});
document.getRange().replace(Pattern.compile("\\{\\{([^}]+)\\}\\}"), "", options);
```

> 第二个参数 `""` 是默认替换值，会被回调中的 `setReplacement()` 覆盖，传空字符串即可。

---

### 3.3 书签类

#### `com.aspose.words.Bookmark`

Word 书签对象，对应 Word 文档中通过"插入 → 书签"创建的命名区域。

**核心方法**：

| 方法 | 返回值 | 作用 |
|------|--------|------|
| `getName()` | `String` | 获取书签名称 |
| `getText()` | `String` | 获取书签范围内的当前文本 |
| `setText(String value)` | `void` | **替换书签范围内的文本** |
| `remove()` | `void` | 删除书签本身（不影响文档内容） |
| `getBookmarkStart()` / `getBookmarkEnd()` | `BookmarkStart` / `BookmarkEnd` | 获取书签起止节点 |

**`setText()` 的行为细节**：

- ✅ 替换书签范围内**所有内容**（多行也支持）
- ✅ **保留书签本身**，可重复填充
- ✅ 保留书签位置的**字符格式**（字体、字号、颜色、加粗等）
- ⚠️ 如果新文本过长，**不会自动撑开布局**（与 Word 编辑时不同）

#### `com.aspose.words.BookmarkCollection`

书签集合，通过 `document.getRange().getBookmarks()` 获取。

**核心方法**：

| 方法 | 返回值 | 作用 |
|------|--------|------|
| `getCount()` | `int` | 获取书签总数 |
| `get(int index)` | `Bookmark` | 按索引获取书签 |
| `get(String name)` | `Bookmark` | 按名称获取书签（找不到返回 null） |
| `iterator()` | `Iterator<Bookmark>` | 获取迭代器 |

**遍历示例**：

```java
BookmarkCollection bookmarks = document.getRange().getBookmarks();
for (Bookmark bookmark : bookmarks) {
    // 跳过 Word 内部书签（_GoBack、_Toc... 等）
    if (bookmark.getName().startsWith("_")) {
        continue;
    }
    String value = fieldValues.get(bookmark.getName());
    if (value != null) {
        bookmark.setText(value);
    }
}
```

> ⚠️ Word 会自动生成 `_GoBack`（标记上次光标位置）、`_Toc123456789`（目录项）等内部书签，**业务填充时必须过滤**，否则可能误填或日志报错。

---

### 3.4 图片保存类

#### `com.aspose.words.ImageSaveOptions`

图片保存选项，控制 Word 转图片的输出参数。

**构造方法**：

```java
new ImageSaveOptions(SaveFormat.JPEG)  // 必须传入 SaveFormat.JPEG 或 PNG
```

**核心方法**：

| 方法 | 作用 |
|------|------|
| `setPageSet(PageSet pageSet)` | **设置输出页面（核心方法）** |
| `setResolution(float resolution)` | 设置水平/垂直分辨率（DPI） |
| `setHorizontalResolution(float)` | 单独设置水平分辨率 |
| `setVerticalResolution(float)` | 单独设置垂直分辨率 |
| `setJpegQuality(int quality)` | 设置 JPEG 质量（0-100，默认 100） |
| `setImageBrightness(int)` | 设置图片亮度（-255 ~ 255） |
| `setImageColorMode(int)` | 设置颜色模式（彩色/灰度/黑白） |

#### `com.aspose.words.PageSet`

页面集合，指定要输出的页面范围。

**构造方法**：

| 构造方法 | 作用 |
|---------|------|
| `new PageSet(int pageIndex)` | 输出单页（**索引从 0 开始**） |
| `new PageSet(int[] pageIndices)` | 输出多个指定页 |
| `new PageSet(Range pageRange)` | 输出页面范围 |
| `new PageSet(PageSet[] pageSets)` | 组合多个 PageSet |

**内置常量**：

| 常量 | 含义 |
|------|------|
| `PageSet.ALL` | 所有页面 |
| `PageSet.EVEN` | 仅偶数页 |
| `PageSet.ODD` | 仅奇数页 |

**逐页输出示例**：

```java
Document document = new Document(filePath);
ImageSaveOptions options = new ImageSaveOptions(SaveFormat.JPEG);
int pageCount = document.getPageCount();  // 触发布局计算
for (int i = 0; i < pageCount; i++) {
    options.setPageSet(new PageSet(i));
    document.save("page_" + (i + 1) + ".jpg", options);
}
```

> ⚠️ Aspose.Words 的页面索引**从 0 开始**，与 Aspose.PDF（从 1 开始）相反。

---

### 3.5 枚举与常量

#### `com.aspose.words.SaveFormat`

Word 保存格式枚举，常用值：

| 常量 | 作用 |
|------|------|
| `SaveFormat.DOCX` | 保存为 Word DOCX |
| `SaveFormat.DOC` | 保存为 Word DOC（旧格式） |
| `SaveFormat.PDF` | 保存为 PDF |
| `SaveFormat.JPEG` | 保存为 JPEG 图片 |
| `SaveFormat.PNG` | 保存为 PNG 图片 |
| `SaveFormat.HTML` | 保存为 HTML |
| `SaveFormat.XPS` | 保存为 XPS |

> ⚠️ 与 `com.aspose.pdf.SaveFormat` 完全独立，**两个类同名但独立**，同文件使用时需用全限定名。

#### `com.aspose.words.LoadOptions`

Word 文档加载选项：

| 方法 | 作用 |
|------|------|
| `setPassword(String)` | 设置打开密码（加密文档） |
| `setLoadFormat(int)` | 指定加载格式 |

---

### 3.6 License 类

#### `com.aspose.words.License`

Word 模块许可证加载器，使用方式与 PDF License 一致。

```java
com.aspose.words.License license = new com.aspose.words.License();
license.setLicense("/path/to/Aspose.Words.Java.lic");
```

---

## 四、核心方法含义速查表

### 4.1 PDF 模块方法速查

#### Document 类

| 方法签名 | 作用 | 备注 |
|---------|------|------|
| `new Document(String filePath)` | 从路径加载 PDF | |
| `new Document(InputStream stream)` | 从流加载 PDF | |
| `getPages()` → `PageCollection` | 获取页面集合 | |
| `getForm()` → `Form` | 获取 AcroForm 表单 | 没有表单时返回空 Form |
| `save(String filePath)` | 保存为 PDF | |
| `save(String filePath, SaveFormat format)` | 按指定格式保存 | 可转 DocX、HTML 等 |
| `close()` | 关闭文档 | 实现 Closeable，可用 try-with-resources |

#### Form 类

| 方法签名 | 作用 |
|---------|------|
| `getFields()` → `Field[]` | 获取所有表单字段 |
| `getField(String name)` → `Field` | 按名获取字段 |
| `getType()` → `int` | 表单类型 |

#### Field 类

| 方法签名 | 作用 |
|---------|------|
| `getFullName()` → `String` | 字段全名 |
| `getPartialName()` → `String` | 字段短名 |
| `getValue()` / `setValue(String)` | 获取/设置字段值 |
| `getPage()` → `Page` | 字段所在页 |

#### TextFragmentAbsorber 类

| 方法签名 | 作用 |
|---------|------|
| `new TextFragmentAbsorber(String text)` | 创建文本查找吸收器 |
| `getTextFragments()` → `TextFragmentCollection` | 获取匹配结果 |

#### TextFragment 类

| 方法签名 | 作用 |
|---------|------|
| `getText()` / `setText(String)` | 获取/设置文本 |
| `getPosition()` → `Position` | 获取坐标 |
| `getTextState()` → `TextState` | 获取文本样式 |

#### JpegDevice 类

| 方法签名 | 作用 |
|---------|------|
| `new JpegDevice()` | 默认分辨率 |
| `new JpegDevice(Resolution resolution)` | 指定分辨率 |
| `process(Page page, OutputStream output)` | 单页转 JPEG |

### 4.2 Word 模块方法速查

#### Document 类

| 方法签名 | 作用 | 备注 |
|---------|------|------|
| `new Document(String filePath)` | 从路径加载 Word | |
| `getRange()` → `Range` | 获取整个文档范围 | |
| `getFirstSection()` → `Section` | 获取第一个章节 | |
| `getPageCount()` → `int` | 获取页数 | **会触发布局计算，耗时** |
| `save(String filePath)` | 保存为 DOCX | |
| `save(String filePath, SaveFormat format)` | 按指定格式保存 | |
| `save(String filePath, SaveOptions options)` | 按选项保存 | |

#### Range 类

| 方法签名 | 作用 |
|---------|------|
| `getBookmarks()` → `BookmarkCollection` | 获取所有书签 |
| `replace(String, String)` | 简单文本替换 |
| `replace(String, String, FindReplaceOptions)` | 文本替换 + 选项 |
| `replace(Pattern, String)` | 正则替换为字符串 |
| `replace(Pattern, String, FindReplaceOptions)` | 正则替换 + 选项（支持回调） |
| `getText()` → `String` | 获取范围内所有文本 |

#### FindReplaceOptions 类

| 方法签名 | 作用 |
|---------|------|
| `setReplacingCallback(IReplacingCallback)` | 设置替换回调（核心） |
| `setMatchCase(boolean)` | 区分大小写 |
| `setWholeWords(boolean)` | 整词匹配 |
| `setDirection(int)` | 查找方向 |

#### Bookmark 类

| 方法签名 | 作用 |
|---------|------|
| `getName()` → `String` | 获取书签名 |
| `getText()` → `String` | 获取书签内容 |
| `setText(String)` | 替换书签内容（保留书签） |
| `remove()` | 删除书签 |

#### BookmarkCollection 类

| 方法签名 | 作用 |
|---------|------|
| `getCount()` → `int` | 获取书签数量 |
| `get(int index)` → `Bookmark` | 按索引获取 |
| `get(String name)` → `Bookmark` | 按名获取 |

#### ImageSaveOptions 类

| 方法签名 | 作用 |
|---------|------|
| `new ImageSaveOptions(SaveFormat.JPEG)` | 创建 JPEG 选项 |
| `setPageSet(PageSet)` | 设置输出页面 |
| `setResolution(float)` | 设置分辨率 |
| `setJpegQuality(int)` | 设置 JPEG 质量 |

#### PageSet 类

| 构造方法 | 作用 |
|---------|------|
| `new PageSet(int pageIndex)` | 单页（索引从 0 开始） |
| `new PageSet(int[] pageIndices)` | 多页 |
| `new PageSet(Range pageRange)` | 页面范围 |

---

## 五、常用场景示例

### 5.1 PDF 转图片（按页输出）

```java
/**
 * 将 PDF 每一页转为 JPG 图片，输出到 pictures 子目录
 * @param filePath PDF 文件路径
 * @return 所有生成的图片路径列表
 */
public List<String> toPictures(String filePath) {
    List<String> picturePaths = new ArrayList<>();
    File pdfFile = new File(filePath);
    if (!pdfFile.exists()) {
        throw new RuntimeException("文件不存在：" + filePath);
    }

    String fileNameWithoutExt = pdfFile.getName().substring(0, pdfFile.getName().lastIndexOf('.'));
    File picturesDir = new File(pdfFile.getParentFile(), "pictures");
    if (!picturesDir.exists()) {
        picturesDir.mkdirs();
    }

    // 注意：Aspose.PDF 的 Document 实现了 Closeable，可以用 try-with-resources
    try (Document document = new Document(filePath)) {
        JpegDevice jpegDevice = new JpegDevice();
        // ⚠️ PDF 页面索引从 1 开始
        for (int i = 1; i <= document.getPages().size(); i++) {
            String pictureName = fileNameWithoutExt + "_page_" + i + ".jpg";
            File pictureFile = new File(picturesDir, pictureName);
            try (FileOutputStream outputStream = new FileOutputStream(pictureFile)) {
                jpegDevice.process(document.getPages().get_Item(i), outputStream);
            }
            picturePaths.add(pictureFile.getAbsolutePath());
        }
    } catch (Exception e) {
        throw new RuntimeException("PDF转图片失败：" + e.getMessage(), e);
    }
    return picturePaths;
}
```

### 5.2 PDF 转 Word

```java
public String toWord(String filePath) {
    File pdfFile = new File(filePath);
    String fileNameWithoutExt = pdfFile.getName().substring(0, pdfFile.getName().lastIndexOf('.'));
    File wordFile = new File(pdfFile.getParentFile(), fileNameWithoutExt + ".docx");

    try (Document document = new Document(filePath)) {
        // 注意：这里使用 com.aspose.pdf.SaveFormat
        document.save(wordFile.getAbsolutePath(), com.aspose.pdf.SaveFormat.DocX);
    } catch (Exception e) {
        throw new RuntimeException("PDF转Word失败：" + e.getMessage(), e);
    }
    return wordFile.getAbsolutePath();
}
```

### 5.3 PDF 表单字段枚举（调试用）

```java
/**
 * 打印 PDF 中所有 AcroForm 表单字段，用于调试
 */
public void listFormFields(String filePath) {
    try (Document document = new Document(filePath)) {
        Form form = document.getForm();
        Field[] fields = form.getFields();
        System.out.println("========== PDF表单字段列表 ==========");
        for (Field field : fields) {
            System.out.printf("字段名: %s, 类型: %s%n",
                field.getFullName(),
                field.getClass().getSimpleName());
        }
        System.out.println("共 " + fields.length + " 个字段");
        System.out.println("====================================");
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
```

### 5.4 PDF 文本替换（处理 `${xxx}` 占位符）

```java
/**
 * 替换 PDF 中的 ${xxx} 文本占位符
 * 注意：PDF 中的占位符必须连续不跨行，否则匹配失败
 */
public void replacePdfText(String filePath, Map<String, String> fieldValues) {
    try (Document document = new Document(filePath)) {
        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
            // 创建文本吸收器查找占位符
            TextFragmentAbsorber absorber = new TextFragmentAbsorber(entry.getKey());
            // 在所有页面执行查找
            document.getPages().accept(absorber);
            // 遍历结果替换（保留原片段的字体样式）
            for (TextFragment fragment : absorber.getTextFragments()) {
                fragment.setText(entry.getValue());
            }
        }
        document.save(filePath);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
```

### 5.5 Word 转图片（逐页输出）

```java
/**
 * 将 Word 每一页转为 JPG 图片
 * @param filePath Word 文件路径
 * @return 所有生成的图片路径列表
 */
public List<String> toPictures(String filePath) {
    List<String> picturePaths = new ArrayList<>();
    File wordFile = new File(filePath);
    String fileNameWithoutExt = wordFile.getName().substring(0, wordFile.getName().lastIndexOf('.'));

    File picturesDir = new File(wordFile.getParentFile(), "pictures");
    if (!picturesDir.exists()) {
        picturesDir.mkdirs();
    }

    // ⚠️ Aspose.Words 的 Document 未实现 AutoCloseable，不能用 try-with-resources
    try {
        Document document = new Document(filePath);
        ImageSaveOptions options = new ImageSaveOptions(SaveFormat.JPEG);
        // getPageCount() 会触发布局计算
        int pageCount = document.getPageCount();
        // ⚠️ Word 页面索引从 0 开始
        for (int i = 0; i < pageCount; i++) {
            String pictureName = fileNameWithoutExt + "_page_" + (i + 1) + ".jpg";
            File pictureFile = new File(picturesDir, pictureName);
            // 每次保存前指定输出页
            options.setPageSet(new PageSet(i));
            document.save(pictureFile.getAbsolutePath(), options);
            picturePaths.add(pictureFile.getAbsolutePath());
        }
    } catch (Exception e) {
        throw new RuntimeException("Word转图片失败：" + e.getMessage(), e);
    }
    return picturePaths;
}
```

### 5.6 Word 转 PDF

```java
public String toPdf(String filePath) {
    File wordFile = new File(filePath);
    String fileNameWithoutExt = wordFile.getName().substring(0, wordFile.getName().lastIndexOf('.'));
    File pdfFile = new File(wordFile.getParentFile(), fileNameWithoutExt + ".pdf");

    try {
        Document document = new Document(filePath);
        // 注意：这里使用 com.aspose.words.SaveFormat
        document.save(pdfFile.getAbsolutePath(), SaveFormat.PDF);
    } catch (Exception e) {
        throw new RuntimeException("Word转PDF失败：" + e.getMessage(), e);
    }
    return pdfFile.getAbsolutePath();
}
```

### 5.7 Word 占位符填充（`{{xxx}}` → 实际值）

```java
/**
 * 基于 {{xxx}} 文本占位符填充 Word 文档
 * @param filePath Word 模板路径
 * @param fieldValues 字段映射（如 name -> 张三）
 * @return 填充后的 Word 文件路径
 */
public String fillByPlaceholder(String filePath, Map<String, String> fieldValues) {
    File wordFile = new File(filePath);
    String fileNameWithoutExt = wordFile.getName().substring(0, wordFile.getName().lastIndexOf('.'));
    File filledFile = new File(wordFile.getParentFile(), fileNameWithoutExt + "_已填充.docx");

    try {
        Document document = new Document(filePath);

        // 正则匹配 {{xxx}}，支持英文和中文占位符
        String placeholderPattern = "\\{\\{([^}]+)\\}\\}";
        FindReplaceOptions options = new FindReplaceOptions();
        options.setReplacingCallback(e -> {
            String placeholder = e.getMatch().group(0);  // 完整匹配 {{name}}
            String key = e.getMatch().group(1);           // 捕获组 name
            String value = fieldValues.get(key);
            if (value != null) {
                // 命中字段，设置替换值
                e.setReplacement(value);
            } else {
                // 未命中字段，日志警告（保留原占位符或清空？这里默认清空）
                log.warn("未找到占位符对应的值：{}", placeholder);
            }
            return ReplaceAction.REPLACE;
        });

        // 第二个参数是默认替换值，会被回调中的 setReplacement 覆盖
        document.getRange().replace(Pattern.compile(placeholderPattern), "", options);
        document.save(filledFile.getAbsolutePath());
    } catch (Exception e) {
        throw new RuntimeException("Word填充失败：" + e.getMessage(), e);
    }
    return filledFile.getAbsolutePath();
}
```

### 5.8 Word 书签填充

```java
/**
 * 基于 Word 书签填充文档
 * @param filePath Word 模板路径（需预先在 Word 中创建书签）
 * @param fieldValues 字段映射（书签名 -> 值）
 * @return 填充后的 Word 文件路径
 */
public String fillByBookmark(String filePath, Map<String, String> fieldValues) {
    File wordFile = new File(filePath);
    String fileNameWithoutExt = wordFile.getName().substring(0, wordFile.getName().lastIndexOf('.'));
    File filledFile = new File(wordFile.getParentFile(), fileNameWithoutExt + "_已填充.docx");

    try {
        Document document = new Document(filePath);
        BookmarkCollection bookmarks = document.getRange().getBookmarks();
        for (Bookmark bookmark : bookmarks) {
            // 跳过 Word 内部书签（_GoBack、_Toc 等）
            if (bookmark.getName().startsWith("_")) {
                continue;
            }
            String value = fieldValues.get(bookmark.getName());
            if (value != null) {
                // setText 会保留书签本身，可重复填充
                bookmark.setText(value);
            }
        }
        document.save(filledFile.getAbsolutePath());
    } catch (Exception e) {
        throw new RuntimeException("Word填充失败：" + e.getMessage(), e);
    }
    return filledFile.getAbsolutePath();
}
```

### 5.9 License 加载（解除评估限制）

未加载 License 时会进入评估模式，需在应用启动时加载一次：

```java
/**
 * 初始化 Aspose License
 * 推荐放在 @PostConstruct 或 @Configuration 类的静态代码块中
 */
public class AsposeLicenseInitializer {

    @PostConstruct
    public void init() {
        try {
            // 加载 PDF License
            com.aspose.pdf.License pdfLicense = new com.aspose.pdf.License();
            try (InputStream pdfIs = getClass().getResourceAsStream("/licenses/Aspose.Pdf.Java.lic")) {
                if (pdfIs != null) {
                    pdfLicense.setLicense(pdfIs);
                }
            }

            // 加载 Words License
            com.aspose.words.License wordsLicense = new com.aspose.words.License();
            try (InputStream wordsIs = getClass().getResourceAsStream("/licenses/Aspose.Words.Java.lic")) {
                if (wordsIs != null) {
                    wordsLicense.setLicense(wordsIs);
                }
            }
        } catch (Exception e) {
            log.error("Aspose License 加载失败", e);
        }
    }
}
```

License 文件存放路径：`src/main/resources/licenses/`。

### 5.10 PDF 表单填充（AcroForm 字段）

```java
/**
 * 填充 PDF AcroForm 表单字段
 * 前提：PDF 必须通过 Adobe Acrobat 等工具预先创建表单字段
 */
public void fillForm(String filePath, Map<String, String> fieldValues) {
    try (Document document = new Document(filePath)) {
        Form form = document.getForm();
        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
            Field field = form.getField(entry.getKey());
            if (field != null) {
                field.setValue(entry.getValue());
            }
        }
        document.save(filePath);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
```

---

## 六、Aspose.PDF vs Aspose.Words 详细对比

### 6.1 核心差异

| 对比项 | Aspose.PDF | Aspose.Words |
|-------|------------|--------------|
| 包路径 | `com.aspose.pdf.*` | `com.aspose.words.*` |
| `Document` 是否可关闭 | ✅ 实现 `Closeable` | ❌ 未实现 AutoCloseable |
| try-with-resources | ✅ 支持 | ❌ 不支持 |
| 页面索引 | **从 1 开始** | **从 0 开始** |
| 转 PDF | `save(path, SaveFormat.PDF)` | `save(path, SaveFormat.PDF)` |
| 转图片 | `JpegDevice.process()` | `ImageSaveOptions + PageSet` |
| 表单填充 | `Form.getFields()` 操作 AcroForm 字段 | `Bookmark.setText()` |
| 文本替换 | `TextFragmentAbsorber` | `Range.replace(Pattern, String, FindReplaceOptions)` |
| Maven 依赖 | 默认 jar | **必须** `<classifier>jdk17</classifier>` |
| License 类 | `com.aspose.pdf.License` | `com.aspose.words.License` |

### 6.2 SaveFormat 同名问题

两个库都有 `SaveFormat` 类，但**完全独立**：

```java
com.aspose.pdf.SaveFormat.DocX       // 用于 PDF 转 Word
com.aspose.words.SaveFormat.PDF      // 用于 Word 转 PDF
```

**建议**：同一文件同时操作 PDF 和 Word 时，使用**全限定名**避免混淆：

```java
// 同时引入会冲突
// import com.aspose.pdf.SaveFormat;
// import com.aspose.words.SaveFormat;

// 解决方案：使用全限定名
document.save(path, com.aspose.pdf.SaveFormat.DocX);
document.save(path, com.aspose.words.SaveFormat.PDF);
```

### 6.3 文档加载与关闭对比

```java
// PDF：try-with-resources
try (com.aspose.pdf.Document doc = new com.aspose.pdf.Document(path)) {
    // 操作
}

// Word：普通 try-catch
try {
    com.aspose.words.Document doc = new com.aspose.words.Document(path);
    // 操作
} catch (Exception e) {
    // 异常处理
}
```

---

## 七、常见坑与最佳实践

### 7.1 aspose-words 依赖找不到

**症状**：编译报错 `程序包 com.aspose.words 不存在`

**原因**：
- ❌ 使用 `<type>pom</type>` → 只引入元数据 POM
- ❌ 不指定 classifier → 仓库里找不到默认 jar

**解决**：使用 `<classifier>jdk17</classifier>`：

```xml
<dependency>
    <groupId>com.aspose</groupId>
    <artifactId>aspose-words</artifactId>
    <version>26.6</version>
    <classifier>jdk17</classifier>
</dependency>
```

### 7.2 com.aspose.words.Document 不能用 try-with-resources

**症状**：编译报错 `the resource type Document does not implement java.lang.AutoCloseable`

**原因**：Aspose.Words 的 `Document` 类没有实现 `AutoCloseable` / `Closeable` 接口。

**解决**：使用普通 try-catch：

```java
// ❌ 错误
try (Document document = new Document(filePath)) { ... }

// ✅ 正确
try {
    Document document = new Document(filePath);
    // ...
} catch (Exception e) {
    // ...
}
```

> 注意：Aspose.PDF 的 `Document` **可以**用 try-with-resources（实现了 Closeable），两个库设计不同。

### 7.3 Range.replace 回调签名变化

**症状**：编译报错 `对于 replace(java.util.regex.Pattern, IReplacingCallback), 找不到合适的方法`

**原因**：Aspose.Words 26.6 移除了 `replace(Pattern, IReplacingCallback)` 重载。

**解决**：通过 `FindReplaceOptions` 包装回调：

```java
// ❌ 旧版本（26.6 之前）
document.getRange().replace(Pattern.compile(pattern), new IReplacingCallback() { ... });

// ✅ 新版本（26.6+）
FindReplaceOptions options = new FindReplaceOptions();
options.setReplacingCallback(callback);
document.getRange().replace(Pattern.compile(pattern), "", options);
```

### 7.4 Word 内部书签过滤

**症状**：日志出现 `未找到书签对应的值：_GoBack` 警告。

**原因**：Word 自动生成内部书签：
- `_GoBack`：标记上次光标位置
- `_Toc123456789`：目录项书签
- `_Hlt...`：高亮书签

**解决**：业务填充时过滤以 `_` 开头的书签：

```java
if (bookmark.getName().startsWith("_")) {
    continue;
}
```

### 7.5 SaveFormat 类不要混淆

**症状**：方法找不到或类型不匹配。

**原因**：`com.aspose.pdf.SaveFormat` 和 `com.aspose.words.SaveFormat` 是两个独立的类，import 错会编译失败。

**解决**：同文件使用时用全限定名：

```java
com.aspose.pdf.SaveFormat.DocX       // PDF 转 Word
com.aspose.words.SaveFormat.PDF      // Word 转 PDF
```

### 7.6 PDF 表单字段 vs 文本占位符

| 形式 | 来源 | Aspose.PDF 识别 |
|------|------|----------------|
| AcroForm 字段 | Adobe Acrobat 等工具创建 | ✅ `form.getFields()` 可获取 |
| `${xxx}` / `{{xxx}}` | Word 模板中的纯文本 | ❌ 不被识别为表单字段 |
| PDF 文本占位符 | Word 转换后保留的文本 | ✅ 通过 `TextFragmentAbsorber` 查找替换 |

**关键认知**：Word 中的 `${xxx}` 占位符在转 PDF 后**只是普通文本**，必须通过 `TextFragmentAbsorber` 处理，不能用表单 API。

### 7.7 长文本占位符跨行问题

**Word 模板设计要点**：
- ✅ **Word**：基于 XML 节点，`Range.replace` 能处理跨行占位符
- ❌ **PDF**：基于文本匹配，占位符跨行会被拆分为多个 `TextFragment`，**匹配失败**

**最佳实践**：Word 模板设计占位符时，确保 `${xxx}` 或 `{{xxx}}` 在**同一行内**，避免被自动换行拆分。

### 7.8 PDF 页面索引从 1，Word 从 0

```java
// Aspose.PDF：页面索引从 1 开始
for (int i = 1; i <= document.getPages().size(); i++) {
    Page page = document.getPages().get_Item(i);
}

// Aspose.Words：页面索引从 0 开始
for (int i = 0; i < document.getPageCount(); i++) {
    options.setPageSet(new PageSet(i));
}
```

### 7.9 getPageCount() 触发布局计算

Aspose.Words 的 `document.getPageCount()` **不是简单读取属性**，而是会触发**完整的文档布局计算**：
- 首次调用较慢（大文档可能数秒）
- 计算结果会被缓存，后续调用快

**优化建议**：
- 一次操作中只需调用一次
- 如果只是简单修改不需要页数信息，避免调用

### 7.10 反射构建字段映射的最佳实践

当 DTO 字段较多时，使用反射避免硬编码：

```java
Map<String, String> fieldValues = new HashMap<>();
for (Field field : dto.getClass().getDeclaredFields()) {
    field.setAccessible(true);
    Object value = field.get(dto);
    if (value != null) {
        // Integer、Double 等通过 String.valueOf 自动转字符串
        fieldValues.put(field.getName(), String.valueOf(value));
    }
}
```

**优点**：DTO 字段增减无需修改填充代码。

**注意点**：
- `getDeclaredFields()` 只获取本类声明的字段，**不包括父类字段**
- `field.setAccessible(true)` 绕过 private 访问限制
- `null` 值跳过，避免把未设置的字段替换为 "null" 字符串

### 7.11 PDF 文本替换的样式保留

`TextFragment.setText()` 替换文本时：
- ✅ 保留原片段的字体、字号、颜色样式
- ⚠️ 如果新文本比原文本长，**不会自动重排布局**，可能溢出原位置

**建议**：
- PDF 文本替换适合**短文本**（姓名、日期、金额等）
- 长文本（如段落描述）建议从 Word 模板填充后再转 PDF

### 7.12 bookmark.setText() 的行为细节

- ✅ 替换书签范围内**所有内容**（包括多段落）
- ✅ **保留书签本身**，可重复填充
- ✅ 保留书签位置的字符格式
- ⚠️ 如果新文本过长，**不会自动撑开布局**（与 Word 编辑时不同）

**注意**：书签范围必须正确设置（在 Word 中选中目标文本再创建书签），否则 `setText()` 可能替换到错误位置。

---

## 八、参考资源

- Aspose.Words for Java 官方文档：https://docs.aspose.com/words/java/
- Aspose.PDF for Java 官方文档：https://docs.aspose.com/pdf/java/
- Aspose Maven 仓库：https://repository.aspose.com/repo/
- Aspose.Words 26.6 发布说明：https://releases.aspose.com/words/java/26-6/
- Aspose 论坛（提问/查 issue）：https://forum.aspose.com/
- Aspose GitHub 示例代码：https://github.com/aspose-words/Aspose.Words-for-Java
