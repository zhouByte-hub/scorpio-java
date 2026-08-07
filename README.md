# Scorpio-Java：Java 核心技术深度实践

Scorpio-Java 是一个面向 Java 开发者的系统化学习项目，以代码实践为核心，从底层原理重新构建对 Java 核心技术的理解。项目采用 Maven 多模块架构（基于 Java 21），每个模块聚焦一个核心技术领域，通过完整的代码实现配套详细的技术文档，深入剖析原理、常见陷阱与最佳实践。覆盖类加载、代理、设计模式、分布式基础、Java AI、文档处理与在线办公、Elasticsearch 检索实践，以及 Spring Cloud Stream 消息中间件集成等方向。

## 一、核心学习领域

### 1.1 类加载器机制（scorpio-classload）

深入理解 JVM 类加载体系，掌握类加载器的核心原理与实践应用。

#### 1.1.1 双亲委派模型

理解 ClassLoader 层级结构、委托机制及其设计初衷。

**核心流程：**

```mermaid
graph TD
    A[类加载请求] --> B{检查是否已加载}
    B -->|已加载| C[返回缓存Class对象]
    B -->|未加载| D[委托父加载器]
    D --> E{父加载器是否存在}
    E -->|存在| F[父加载器尝试加载]
    E -->|不存在| G[启动类加载器尝试加载]
    F --> H{父加载器是否成功}
    H -->|成功| I[返回Class对象]
    H -->|失败| J[当前加载器尝试加载]
    G --> K{启动类加载器是否成功}
    K -->|成功| I
    K -->|失败| J
    J --> L{当前加载器是否成功}
    L -->|成功| I
    L -->|失败| M[抛出ClassNotFoundException]
```

**类加载器层级：**

```
Bootstrap ClassLoader (启动类加载器)
    ↓
Platform ClassLoader (平台类加载器，Java 9+)
    ↓
App ClassLoader (应用类加载器)
    ↓
Custom ClassLoader (自定义类加载器)
```

**核心代码示例：**

```java
// 查看类加载器层级
ClassLoader classLoader = Main.class.getClassLoader();
System.out.println("当前类加载器: " + classLoader);  // AppClassLoader
System.out.println("父加载器: " + classLoader.getParent());  // PlatformClassLoader

// 加载目标类
Class<?> targetClass = classLoader.loadClass("com.zhoubyte.plugins.CalcHouseAgent");
System.out.println("目标类加载器: " + targetClass.getClassLoader());  // AppClassLoader
```

#### 1.1.2 自定义 ClassLoader 实现

从零实现 SecureClassLoader，掌握 `loadClass()` 与 `findClass()` 的协作机制。

**核心实现：**

```java
public class CalcPluginsClassLoad extends SecureClassLoader {
    
    private final Path classRootDir;
    private static final String PLUGINS_PACKAGE = "com.zhoubyte.plugins";
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // 1. 先查缓存
            Class<?> cached = findLoadedClass(name);
            if (cached != null) {
                return cached;
            }
            
            // 2. plugins包下的类打破双亲委派，直接自己加载
            if (name.startsWith(PLUGINS_PACKAGE) && !name.equals(PLUGINS_INTERFACE)) {
                return findClass(name);
            }
            
            // 3. 其他类走标准双亲委派
            return super.loadClass(name);
        }
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // 将类名转为文件路径，读取字节码
        String relativePath = name.replace('.', '/') + ".class";
        Path classFile = classRootDir.resolve(relativePath);
        byte[] classBytes = Files.readAllBytes(classFile);
        
        // 将字节码注册为Class对象
        return defineClass(name, classBytes, 0, classBytes.length);
    }
}
```

**关键点：**
- `loadClass()`：控制加载流程，决定是否委托父加载器
- `findClass()`：实际的类加载逻辑，读取字节码并定义Class对象
- `defineClass()`：将字节码转换为Class对象，JVM负责验证和链接

#### 1.1.3 打破双亲委派

实现插件化架构，理解何时以及如何安全地绕过双亲委派模型。

**应用场景：**
- 插件化系统：不同插件使用不同的ClassLoader，实现类隔离
- 热部署：重新加载修改后的类，无需重启JVM
- 依赖隔离：同一应用的不同模块依赖同一库的不同版本

**打破双亲委派的核心代码：**

```java
// 只对plugins包打破双亲委派，其他类仍走标准流程
if (name.startsWith(PLUGINS_PACKAGE) && !name.equals(PLUGINS_INTERFACE)) {
    return findClass(name);  // 直接自己加载，不委托父加载器
}
```

**注意事项：**
- 接口类必须由父加载器加载，否则会出现类型不兼容
- 打破双亲委派可能导致类重复加载和内存泄漏

#### 1.1.4 热加载原理

实现运行时类重载，理解 ClassLoader 生命周期、Metaspace 卸载机制与 GC 回收条件。

**热加载流程：**

```mermaid
graph LR
    A[监控.class文件变化] --> B{文件是否修改}
    B -->|是| C[创建新ClassLoader]
    B -->|否| A
    C --> D[重新加载类]
    D --> E[旧ClassLoader失去引用]
    E --> F[GC回收旧类]
    F --> G[更新插件缓存]
    G --> A
```

**核心实现：**

```java
public class HotReloadTool {
    private volatile CalcPluginsClassLoad currentLoader;
    private volatile Map<String, CalcInterface> pluginCache = new HashMap<>();
    
    public void reloadAll() {
        // 1. 创建新的ClassLoader（旧的自动失去引用）
        CalcPluginsClassLoad newLoader = new CalcPluginsClassLoad(classRootDir, parentClassLoader);
        
        // 2. 扫描并加载所有插件
        Map<String, CalcInterface> newPlugins = new HashMap<>();
        try (Stream<Path> stream = Files.list(pluginsDir)) {
            for (Path classFile : stream.filter(p -> p.toString().endsWith(".class")).toList()) {
                String className = extractClassName(classFile);
                Class<?> clazz = newLoader.loadClass(className);
                
                if (CalcInterface.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                    CalcInterface plugin = (CalcInterface) clazz.getConstructor().newInstance();
                    newPlugins.put(className, plugin);
                }
            }
        }
        
        // 3. 原子替换ClassLoader和插件缓存
        this.currentLoader = newLoader;
        this.pluginCache = newPlugins;
    }
}
```

**GC回收条件：**
1. Class对象的所有实例被回收
2. Class对象本身被回收（没有引用指向它）
3. ClassLoader对象被回收
4. Class对象的Class对象被回收（元数据）

#### 1.1.5 JAR 包动态加载

使用 URLClassLoader 和 JarFile API 实现运行时 JAR 扫描与类加载。

**核心代码：**

```java
public static Map<String, Class<?>> scanJarClasses(String jarPath) throws Exception {
    Map<String, Class<?>> classMap = new HashMap<>();
    File jarFile = new File(jarPath);
    
    try (URLClassLoader loader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});
         JarFile jar = new JarFile(jarFile)) {
        
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            
            if (name.endsWith(".class") && !name.startsWith("META-INF/")) {
                String className = name.replace('/', '.').substring(0, name.length() - 6);
                Class<?> clazz = loader.loadClass(className);
                classMap.put(className, clazz);
            }
        }
    }
    return classMap;
}
```

**使用示例：**

```java
String jarPath = "/path/to/plugin.jar";
Map<String, Class<?>> classes = scanJarClasses(jarPath);
Class<?> pluginClass = classes.get("com.example.Plugin");
Object plugin = pluginClass.getConstructor().newInstance();
Method method = pluginClass.getMethod("execute", String.class);
Object result = method.invoke(plugin, "param");
```

#### 1.1.6 类型兼容性陷阱

深入理解"同一个类由不同 ClassLoader 加载会产生不同类型"的核心问题及其解决方案。

**问题演示：**

```java
// 使用AppClassLoader加载
Class<?> class1 = Main.class.getClassLoader().loadClass("com.zhoubyte.plugins.CalcHouseAgent");
Object obj1 = class1.getConstructor().newInstance();

// 使用URLClassLoader加载
URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});
Class<?> class2 = urlClassLoader.loadClass("com.zhoubyte.plugins.CalcHouseAgent");
Object obj2 = class2.getConstructor().newInstance();

// 比较类型
System.out.println(class1 == class2);  // false - 不同类型！
System.out.println(class1.getClassLoader());  // AppClassLoader
System.out.println(class2.getClassLoader());  // URLClassLoader

// 强转会抛出ClassCastException
CalcHouseAgent agent = (CalcHouseAgent) obj2;  // ERROR!
```

**解决方案：**

1. **使用接口**：接口由父加载器加载，实现类由子加载器加载
```java
// 接口由AppClassLoader加载
CalcInterface plugin = (CalcInterface) obj2;  // OK!
```

2. **使用反射**：不依赖具体类型，通过反射调用方法
```java
Method method = class2.getMethod("calc", Double.class);
Object result = method.invoke(obj2, 1200D);
```

### 1.2 代理模式（scorpio-proxy）

掌握 Java 动态代理的核心原理与应用场景。

#### 1.2.1 静态代理 vs 动态代理

理解两种代理模式的本质区别与适用场景。

**静态代理：**
- 代理类在编译期确定，每个目标类都需要一个对应的代理类
- 优点：简单易懂，性能好
- 缺点：代码冗余，维护成本高

**动态代理：**
- 代理类在运行时动态生成，无需为每个目标类编写代理类
- 优点：灵活，代码简洁，易于扩展
- 缺点：性能略低于静态代理，调试困难

#### 1.2.2 JDK 动态代理

深入 Proxy 类与 InvocationHandler 接口的实现原理。

**核心流程：**

```mermaid
graph TD
    A[调用代理对象方法] --> B[InvocationHandler.invoke]
    B --> C[方法前置处理]
    C --> D[反射调用目标方法]
    D --> E[方法后置处理]
    E --> F[返回结果]
    
    G[Proxy.newProxyInstance] --> H[生成代理类字节码]
    H --> I[加载代理类]
    I --> J[创建代理实例]
```

**核心代码：**

```java
// 定义InvocationHandler
public class LoggingHandler implements InvocationHandler {
    private Object target;
    
    public LoggingHandler(Object target) {
        this.target = target;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 前置处理
        System.out.println("[LOG] 调用方法: " + method.getName());
        
        // 调用目标方法
        Object result = method.invoke(target, args);
        
        // 后置处理
        System.out.println("[LOG] 返回结果: " + result);
        
        return result;
    }
}

// 创建代理对象
CalcInterface realObject = new CalcHouseAgent();
CalcInterface proxy = (CalcInterface) Proxy.newProxyInstance(
    realObject.getClass().getClassLoader(),
    realObject.getClass().getInterfaces(),
    new LoggingHandler(realObject)
);

// 使用代理
Double result = proxy.calc(1200D);
```

**关键点：**
- JDK动态代理只能代理接口，不能代理类
- 代理类继承Proxy类，实现目标接口
- 所有方法调用都转发到InvocationHandler.invoke()

#### 1.2.3 CGLIB 代理

理解基于继承的代理机制及其与 JDK 动态代理的差异。

**CGLIB vs JDK动态代理：**

| 特性 | JDK动态代理 | CGLIB代理 |
|------|------------|-----------|
| 代理方式 | 基于接口 | 基于继承 |
| 代理目标 | 必须实现接口 | 可以是普通类 |
| 性能 | 较低（反射调用） | 较高（字节码生成） |
| 限制 | 不能代理final方法 | 不能代理final类和方法 |
| 适用场景 | 接口代理 | 类代理 |

**核心代码：**

```java
// 定义MethodInterceptor
public class CglibProxy implements MethodInterceptor {
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        // 前置处理
        System.out.println("[CGLIB] 调用方法: " + method.getName());
        
        // 调用目标方法（不使用反射）
        Object result = proxy.invokeSuper(obj, args);
        
        // 后置处理
        System.out.println("[CGLIB] 返回结果: " + result);
        
        return result;
    }
}

// 创建代理对象
Enhancer enhancer = new Enhancer();
enhancer.setSuperclass(CalcHouseAgent.class);
enhancer.setCallback(new CglibProxy());
CalcHouseAgent proxy = (CalcHouseAgent) enhancer.create();

// 使用代理
Double result = proxy.calc(1200D);
```

#### 1.2.4 代理在框架中的应用

Spring AOP、MyBatis Mapper 代理等核心机制解析。

**Spring AOP：**
- 默认使用JDK动态代理（如果目标类实现接口）
- 可配置使用CGLIB代理（如果目标类未实现接口）
- 通过代理实现事务管理、日志记录、权限控制等横切关注点

**MyBatis Mapper代理：**
- Mapper接口无需实现类，由动态代理生成实现
- 代理对象拦截方法调用，转换为SQL执行
- 核心机制：MapperProxyFactory + MapperProxy

### 1.3 设计模式实践

通过实际场景理解经典设计模式的应用。

#### 1.3.1 策略模式

插件化计算器架构，实现运行时算法切换。

**核心代码：**

```java
// 策略接口
public interface CalcInterface {
    Double calc(Double baseMoney);
}

// 具体策略：房屋计算
public class CalcHouseAgent implements CalcInterface {
    @Override
    public Double calc(Double baseMoney) {
        return (baseMoney + 200) * 1.2;
    }
}

// 具体策略：交通计算
public class CalcTransportationAgent implements CalcInterface {
    @Override
    public Double calc(Double baseMoney) {
        return baseMoney * 0.8;
    }
}

// 策略上下文
public class CalcContext {
    private CalcInterface strategy;
    
    public void setStrategy(CalcInterface strategy) {
        this.strategy = strategy;
    }
    
    public Double executeCalc(Double baseMoney) {
        return strategy.calc(baseMoney);
    }
}
```

**应用场景：**
- 插件化架构：运行时动态加载不同的计算策略
- 支付系统：支持多种支付方式（支付宝、微信、银行卡）
- 排序算法：根据数据规模选择不同的排序策略

#### 1.3.2 工厂模式

Class 对象的动态创建与管理。

**简单工厂：**

```java
public class PluginFactory {
    public static CalcInterface createPlugin(String pluginName) {
        try {
            Class<?> clazz = Class.forName("com.zhoubyte.plugins." + pluginName);
            return (CalcInterface) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("创建插件失败: " + pluginName, e);
        }
    }
}
```

**应用场景：**
- 数据库连接池：根据配置创建不同类型的连接
- 日志框架：根据配置创建不同的日志实现

#### 1.3.3 模板方法模式

ClassLoader 的 loadClass 流程设计。

**核心流程：**

```java
// 抽象模板
public abstract class AbstractClassLoader extends ClassLoader {
    
    // 模板方法：定义加载流程骨架
    @Override
    public final Class<?> loadClass(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // 1. 检查缓存
            Class<?> cached = findLoadedClass(name);
            if (cached != null) {
                return cached;
            }
            
            // 2. 委托父加载器（可由子类决定是否打破）
            if (shouldDelegateToParent(name)) {
                try {
                    return super.loadClass(name);
                } catch (ClassNotFoundException e) {
                    // 父加载器加载失败，继续
                }
            }
            
            // 3. 自己加载（由子类实现）
            return findClass(name);
        }
    }
    
    // 钩子方法：子类决定是否委托父加载器
    protected boolean shouldDelegateToParent(String name) {
        return true;  // 默认遵循双亲委派
    }
    
    // 抽象方法：由子类实现具体加载逻辑
    @Override
    protected abstract Class<?> findClass(String name) throws ClassNotFoundException;
}
```

**应用场景：**
- 类加载器：loadClass定义流程，findClass由子类实现
- Servlet生命周期：init → service → destroy
- Spring JdbcTemplate：定义流程，具体SQL由子类实现

#### 1.3.4 观察者模式

文件监控与热加载通知机制。

**核心代码：**

```java
// 观察者接口
public interface FileChangeListener {
    void onFileChanged(Path file);
}

// 被观察者：文件监控器
public class FileWatcher {
    private List<FileChangeListener> listeners = new ArrayList<>();
    
    public void addListener(FileChangeListener listener) {
        listeners.add(listener);
    }
    
    public void startWatch(Path directory) {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        
        while (true) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                Path file = (Path) event.context();
                // 通知所有观察者
                for (FileChangeListener listener : listeners) {
                    listener.onFileChanged(file);
                }
            }
            key.reset();
        }
    }
}

// 具体观察者：热加载器
public class HotReloader implements FileChangeListener {
    @Override
    public void onFileChanged(Path file) {
        System.out.println("检测到文件变化: " + file);
        reloadAll();  // 触发热加载
    }
}
```

### 1.4 分布式系统基础

为后续分布式系统学习奠定基础。

#### 1.4.1 类隔离机制

理解容器化环境（如 Tomcat、OSGi）中的类加载隔离原理。

**Tomcat类加载架构：**

```
Bootstrap ClassLoader
    ↓
System ClassLoader
    ↓
Common ClassLoader (Tomcat核心类)
    ↓
    ├── Catalina ClassLoader (Tomcat内部类)
    └── WebApp ClassLoader (每个Web应用独立)
            ├── WebApp1 ClassLoader
            ├── WebApp2 ClassLoader
            └── ...
```

**隔离原理：**
- 每个WebApp使用独立的ClassLoader
- WebAppClassLoader打破双亲委派，优先加载WEB-INF/classes和WEB-INF/lib
- 不同WebApp可以依赖同一库的不同版本

**实现示例：**

```java
public class WebAppClassLoader extends URLClassLoader {
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // 1. 检查缓存
            Class<?> cached = findLoadedClass(name);
            if (cached != null) {
                return cached;
            }
            
            // 2. 对于Web应用自己的类，优先自己加载（打破双亲委派）
            if (name.startsWith("com.myapp")) {
                return findClass(name);
            }
            
            // 3. 对于JDK核心类，委托父加载器
            if (name.startsWith("java.") || name.startsWith("javax.")) {
                return super.loadClass(name);
            }
            
            // 4. 其他类，先自己尝试加载，失败再委托父加载器
            try {
                return findClass(name);
            } catch (ClassNotFoundException e) {
                return super.loadClass(name);
            }
        }
    }
}
```

#### 1.4.2 插件化架构

构建可扩展的插件系统，理解模块化设计的核心思想。

**插件化架构设计：**

```mermaid
graph TD
    A[主应用] --> B[插件管理器]
    B --> C[插件ClassLoader]
    B --> D[插件注册表]
    C --> E[插件1]
    C --> F[插件2]
    C --> G[插件3]
    E --> H[接口契约]
    F --> H
    G --> H
```

**核心组件：**

1. **插件接口契约**：定义插件必须实现的接口
2. **插件管理器**：负责加载、卸载、管理插件生命周期
3. **插件ClassLoader**：实现插件隔离，每个插件使用独立的ClassLoader
4. **插件注册表**：维护插件元数据和实例

**应用场景：**
- IDE插件系统（Eclipse、IntelliJ IDEA）
- 微服务框架（Spring Boot Starter）
- 大数据平台（Flink、Spark）

#### 1.4.3 动态服务发现

基于反射和 ClassLoader 实现运行时服务注册与发现。

**服务注册与发现流程：**

```mermaid
graph LR
    A[扫描服务类] --> B[加载Class对象]
    B --> C[实例化服务]
    C --> D[注册到服务注册表]
    D --> E[客户端查询服务]
    E --> F[返回服务实例]
```

**核心代码：**

```java
// 服务注册表
public class ServiceRegistry {
    private Map<String, Object> services = new ConcurrentHashMap<>();
    
    // 注册服务
    public void register(String serviceName, Object service) {
        services.put(serviceName, service);
    }
    
    // 发现服务
    public <T> T getService(String serviceName) {
        return (T) services.get(serviceName);
    }
    
    // 扫描并注册服务
    public void scanAndRegister(String basePackage, ClassLoader classLoader) {
        // 扫描包下所有类
        Set<Class<?>> classes = scanClasses(basePackage, classLoader);
        
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Service.class)) {
                Object instance = clazz.getConstructor().newInstance();
                String serviceName = clazz.getSimpleName();
                register(serviceName, instance);
            }
        }
    }
}
```

**应用场景：**
- 微服务架构：服务注册中心（Eureka、Consul、Nacos）
- RPC框架：动态代理生成客户端存根
- 依赖注入：Spring IoC容器

### 1.5 Java AI 应用实践（java-ai）

探索 Java 在 AI 应用开发中的实践，掌握主流 AI 框架的使用方法。

#### 1.5.1 Spring AI

Spring 官方 LLM 集成框架，为 Spring 应用提供统一的 AI 接入抽象。

**核心特性：**
- 统一的 API 抽象：ChatClient、EmbeddingClient、ImageClient 等
- 配置驱动：通过 application.yml 配置模型供应商和参数
- Spring Boot 深度集成：自动配置、Bean 管理、环境抽象
- 工具调用支持：Function Calling、工具编排

**应用场景：**
- 在现有 Spring 微服务中集成 LLM 能力
- 简单的问答、摘要、分类、RAG 应用
- 利用 Spring 生态（配置、监控、安全、云原生）

#### 1.5.2 Spring AI Alibaba

面向国内模型生态的 Spring AI 增强，支持阿里云通义等国内大模型服务。

**核心特性：**
- 国内模型优先支持：阿里云通义、百度文心、腾讯混元等
- 本地化配置：针对国内网络环境、鉴权方式的适配
- Spring 风格保持：与 Spring AI 编程模型一致
- 中文文档和示例：完善的本地化支持

**应用场景：**
- 使用国内云厂商大模型服务
- 需要本地化配置和文档支持
- 解决国内网络/环境的细节问题

#### 1.5.3 LangChain4j

Java 版 LangChain，提供 Chain、Agent、Memory、Tool 等高级编排能力。

**核心特性：**
- 链式编排：Chain、Agent、Memory、Tool 一等公民
- 多步骤推理：支持复杂工作流和多步推理
- 框架独立：不依赖 Spring，可在任意 Java 环境使用
- 多模型支持：覆盖国际/国内主流模型

**应用场景：**
- 构建复杂 LLM 应用：Agent、多步推理、工具调用编排
- 复杂 RAG 和工作流
- 需要与 Python LangChain 相近的 Java 能力

#### 1.5.4 LangGraph

基于 LangChain4j 的图式工作流框架，用于构建复杂的有状态 Agent。

**核心特性：**
- 图式编排：用节点和边描述复杂对话流、决策树
- 有状态对话：支持分支、循环、人机协同
- 可观测性：复杂逻辑可视化、可拆分
- 多 Agent 协作：支持多 Agent 协作场景

**应用场景：**
- 复杂对话流程：客服流转、审批流、跨系统编排
- 多 Agent 协作：需要多个 Agent/工具/RAG 流程组织
- 可观测工作流：希望后续能可视化、可观测

#### 1.5.5 MCP（Model Context Protocol）

模型上下文协议，实现 AI 模型与外部工具的标准化交互。

**核心特性：**
- 标准化协议：统一的工具描述和调用协议
- 工具隔离：工具与模型解耦，便于扩展
- 双向通信：支持模型调用工具和工具返回结果
- 多语言支持：Java、Python 等多语言实现

**应用场景：**
- AI 模型与外部系统集成：数据库、API、业务逻辑
- 工具调用标准化：降低模型与工具的耦合度
- 多模型复用：同一工具可被不同模型使用

**框架对比总结：**

| 框架 | 定位 | 依赖 Spring | 适用场景 |
|------|------|------------|----------|
| Spring AI | Spring 官方 LLM 集成 | 是 | Spring 微服务简单 AI 集成 |
| Spring AI Alibaba | 国内模型增强 | 是 | 国内模型生态 Spring 应用 |
| LangChain4j | Java 版 LangChain | 否（可选） | 复杂 LLM 应用编排 |
| LangGraph | 图式工作流 | 否 | 复杂有状态 Agent 编排 |
| MCP | 工具调用协议 | 否 | AI 模型与外部系统集成 |

### 1.6 文档处理与在线办公（scorpio-aspose）

理解办公文档自动化与「在线文档」背后的能力分层，掌握用文档引擎完成转换、填充与浏览器编辑的核心思路。

#### 1.6.1 在线文档的能力分层

市面上的在线 WPS / Google Docs，表面上是「浏览器里打开文档」，本质上至少分三层，学习时不要混为一谈：

| 能力 | 含义 | 学习重点 |
|------|------|----------|
| **Preview（预览）** | 只读渲染，不能改、不能存 | PDF 渲染、DOCX 只读展示、缩放翻页 |
| **Edit（编辑）** | 可改内容并落盘 | 编辑引擎选型、保存闭环、格式保真 |
| **Co-authoring（协同）** | 多人实时共编 | 冲突合并、OT/CRDT、会话与权限 |

**关键点：**
- 「能打开」≠「能编辑」≠「能协同」
- 预览可用轻量前端库完成；真正的编辑几乎总要依赖文档引擎或专用 Document Server
- 先想清楚目标落在哪一层，再谈技术选型，否则容易做成「看起来像编辑器、实际存不回去」

#### 1.6.2 文档引擎能解决什么问题

以 Aspose 一类商业文档库为代表，学习重点不在 API 清单，而在**业务能力模型**：

**格式转换：**
- Word ↔ PDF、文档 → 图片（分页渲染）
- 关注点：版式保真、字体缺失、分页差异、大文件性能

**模板填充：**
- 占位符替换（如 `{{name}}`）：实现简单，适合纯文本字段
- 书签（Bookmark）填充：定位更稳，适合复杂版式中的插入点
- 关注点：字段映射、空值策略、重复块/表格行扩展

**内容识别与表单：**
- PDF 表单域枚举与填写、文本查找替换
- 关注点：扫描件 vs 文本 PDF、表单域与普通文本的区别

**学习重点：**
- 把「文档」当成领域对象：输入格式、输出格式、填充模型、失败边界
- 模板设计与代码解耦：占位符/书签是契约，业务只填数据
- 评估模式与正式授权的差异（页数限制、水印）会直接影响联调结论

#### 1.6.3 浏览器里如何「编辑 Word」

浏览器本身不能原生编辑 DOCX，业界常见三条路：

| 路线 | 思路 | 优点 | 代价 |
|------|------|------|------|
| Document Server（OnlyOffice / Collabora 等） | 专用编辑服务渲染并保存 | 排版接近桌面 Office | 必须额外部署组件 |
| 中间格式往返（如 DOCX ↔ HTML） | 引擎转 HTML，前端富文本编辑，再写回 | 只跑业务前后端即可 | 复杂版式易失真 |
| 纯预览 | 前端只读渲染 | 实现最轻 | 没有真正编辑 |

**学习重点：**
- 保存闭环：打开 → 修改 → 写回原格式，缺任何一环都不是完整编辑
- HTML 往返适合「正文为主」的文档；页眉页脚、多栏、严密表格、嵌入对象最容易丢
- `scale` 一类参数在 PDF 组件里可能只影响清晰度，真正控制显示大小的往往是宽度/缩放策略——读文档比猜参数重要

#### 1.6.4 模板填充的设计取舍

```mermaid
graph TD
    A[业务数据] --> B{填充方式}
    B -->|文本占位符| C[全局查找替换]
    B -->|书签| D[按书签定位写入]
    C --> E[生成结果文档]
    D --> E
    E --> F{后续用途}
    F -->|归档下载| G[保持原格式]
    F -->|在线再编辑| H[进入编辑链路]
    F -->|审批流转| I[转 PDF 固化版式]
```

**学习重点：**
- 占位符：实现快，但要注意半匹配、样式断裂、表格单元格内替换
- 书签：结构更清晰，适合固定版式通知书、合同条款插入
- 「填充」与「在线编辑」是两条链路：前者是批处理生成，后者是交互式修改
- 需要固化不可改时，优先转 PDF；需要继续改内容时，保留可编辑源格式

#### 1.6.5 常见陷阱与判断标准

1. **把预览当编辑**：能渲染不等于能保存；验收标准应包含「改完再打开内容仍在」
2. **忽视保真度**：转换/往返后版式变化是常态，要提前定义可接受损失，而不是事后惊讶
3. **模板契约不清**：字段名、书签名、空值表现没有约定，前后端会对不齐
4. **授权与评估模式**：未授权环境的页数/水印限制，会让「功能坏了」和「评估限制」难以区分
5. **选型与部署目标冲突**：既要桌面级排版，又拒绝部署 Document Server，两者很难同时满足

**学完应能回答：**
- 预览、编辑、协同分别需要什么能力？
- 占位符填充和书签填充各自适合什么模板？
- 为什么 DOCX ↔ HTML 能编辑，却仍不如专业在线 Office？
- 什么时候该转 PDF 固化，什么时候该保留可编辑源文件？

### 1.7 Elasticsearch 检索实践（scorpio-elastic）

掌握 Elasticsearch 在 Java 应用中的常见使用方式，从索引管理、文档 CRUD 到搜索、高亮、动态文档和聚合统计，建立完整的 ES 学习闭环。

#### 1.7.1 索引管理

通过 `ElasticsearchTemplate` 和 ES Java Client 理解索引生命周期管理。

**核心能力：**
- 创建索引并写入 mapping
- 删除索引、判断索引是否存在
- 查询索引 mapping 和索引列表
- 动态追加字段 mapping

**学习重点：**
- index、mapping、document 三者的职责边界
- `text`、`keyword`、`integer` 等字段类型的差异
- analyzer 与 search analyzer 对搜索结果的影响

#### 1.7.2 文档 CRUD 与批量操作

以 `UserDocEntity` 作为固定实体示例，理解类型化文档的写入、查询、更新和删除。

**核心接口能力：**
- 单条用户文档保存、按 ID 查询、更新、删除
- 批量保存与批量删除
- 分页、排序、关键词搜索
- 搜索结果高亮返回

**学习重点：**
- 固定实体适合结构稳定的业务数据
- 批量操作要关注失败边界与幂等性
- 搜索接口返回结构应包含 `total`、`page`、`size` 和 `content`

#### 1.7.3 动态文档与聚合统计

动态文档适合学习管理类场景：请求传入 `indexName` 和字段 Map，运行时决定写入哪个索引。

**动态能力：**
- 动态文档保存、查询、删除
- 动态索引关键词搜索
- 用户年龄区间聚合
- 动态字段 terms 统计

**常见陷阱：**
1. **把 mapping 当数据库表结构随意改**：字段类型一旦写入，后续修改会受到 ES 限制。
2. **忽略 text 和 keyword 差异**：全文检索和精确聚合不是同一种字段模型。
3. **查询条件写死**：学习模块应保留动态查询入口，方便观察不同字段类型的行为。
4. **错误直接暴露底层异常**：Controller 层应统一返回结构，异常信息要适合接口调用者理解。

### 1.8 Spring Cloud Stream 消息中间件实践（scorpio-cloud-stream）

学习 Spring Cloud Stream 的 Binder 抽象，理解应用代码如何通过统一模型对接 RabbitMQ 和 Kafka。

#### 1.8.1 模块拆分

`scorpio-cloud-stream` 是父聚合模块，内部拆分为两个可独立启动的子模块：

```text
scorpio-cloud-stream
├── scorpio-cloud-stream-rabbit
└── scorpio-cloud-stream-kafka
```

**拆分原因：**
- RabbitMQ 和 Kafka 的 broker 模型不同，配置项差异明显
- 独立子模块能避免 binder 依赖和 binding 配置互相干扰
- 每个应用都能单独启动、调试和观察消息流转

#### 1.8.2 RabbitMQ Binder 示例

RabbitMQ 子模块聚焦 exchange、队列、分组消费、延迟消息和死信处理。

**核心能力：**
- REST 接口发送普通消息
- 分组消费示例
- 批量消息发送
- 延迟消息发送
- 死信队列演示

**学习重点：**
- destination 与 exchange 的对应关系
- group 对队列命名和消费语义的影响
- 延迟消息依赖 RabbitMQ delayed exchange 能力
- 消费异常如何进入 DLQ

#### 1.8.3 Kafka Binder 示例

Kafka 子模块聚焦 topic、consumer group、partition key、批量发送和错误处理。

**核心能力：**
- REST 接口发送普通消息
- 分组消费示例
- 分区 key 发送示例
- 批量消息发送
- 错误处理与 DLQ 示例

**学习重点：**
- topic 与 consumer group 决定消费隔离关系
- partition key 决定同 key 消息的分区归属
- Kafka 更适合日志型、可回放、顺序性可控的消息场景
- RabbitMQ 更偏队列路由和任务分发模型

#### 1.8.4 StreamBridge 与函数式消费者

两个子模块都使用 `StreamBridge` 发送消息，并通过 `Consumer<Message<String>>` 定义消费者。

**设计取舍：**
- Controller 只接收请求，不直接操作 binder
- Service 统一封装 bindingName、headers 和发送结果
- Consumer 函数只关注消息消费逻辑
- binder 差异放在 `application.yaml` 中配置

**学完应能回答：**
- Spring Cloud Stream 的 binder 解决了什么问题？
- RabbitMQ 与 Kafka 的消息模型差异是什么？
- `destination`、`group`、`bindingName` 分别表示什么？
- 什么时候使用分区 key，什么时候使用死信队列？

## 二、学习特色

### 2.1 原理驱动

每个知识点从 JVM 规范和 JDK 源码层面深入剖析，不仅知其然，更知其所以然。

**示例：类加载器为什么使用双亲委派？**
- 安全性：避免核心类被篡改（如自定义java.lang.String）
- 唯一性：保证类的全局唯一性，避免重复加载
- 性能：父加载器加载的类可被所有子加载器共享

### 2.2 实践验证

所有概念配套可运行的代码示例，通过实际运行验证理论。

**验证方法：**
- 编写单元测试验证每个知识点
- 使用日志输出观察类加载过程
- 通过调试器跟踪代码执行流程
- 使用JVM参数观察类加载行为（-verbose:class）

### 2.3 陷阱警示

标注常见错误与性能陷阱，提供最佳实践指导。

**常见陷阱：**
1. **类加载器泄漏**：自定义ClassLoader未正确关闭导致内存泄漏
2. **类型转换异常**：同一类由不同ClassLoader加载导致ClassCastException
3. **死锁问题**：并行类加载时未正确使用getClassLoadingLock()
4. **性能问题**：频繁创建ClassLoader导致Metaspace溢出

### 2.4 渐进式学习

从基础概念到高级应用，构建完整的知识体系。

**学习路径：**
1. **基础篇**：理解类加载器层级、双亲委派模型
2. **进阶篇**：自定义ClassLoader、打破双亲委派、热加载
3. **高级篇**：类隔离、插件化架构、动态服务发现
4. **实战篇**：Tomcat类加载机制、Spring AOP原理、MyBatis Mapper代理
5. **应用篇**：Java AI 集成、文档自动化与在线办公能力分层（预览 / 编辑 / 协同）、Elasticsearch 检索实践、Spring Cloud Stream 消息中间件集成