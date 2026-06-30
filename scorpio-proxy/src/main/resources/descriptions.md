# JDK 动态代理 与 CGLIB 代理

## 一、什么是代理

代理是一种设计模式，通过一个"代理对象"控制对"目标对象"的访问。调用者不直接操作目标对象，而是经过代理对象，从而可以在调用前后插入拦截逻辑（如日志、权限、事务等）。

---

## 二、JDK 动态代理

### 2.1 定义

JDK 动态代理是 Java 标准库提供的代理机制，基于**接口**实现，在运行时动态生成代理类。

核心类：
- `java.lang.reflect.Proxy`：负责创建代理对象
- `java.lang.reflect.InvocationHandler`：定义拦截逻辑，所有方法调用都会路由到其 `invoke` 方法

### 2.2 使用步骤

1. 定义目标接口
2. 创建目标类，实现该接口
3. 实现 `InvocationHandler`，在 `invoke` 方法中编写拦截逻辑
4. 调用 `Proxy.newProxyInstance(classLoader, interfaces, handler)` 生成代理对象

### 2.3 限制

- **目标对象必须实现至少一个接口**，否则无法使用
- 代理对象只能代理接口中声明的方法

---

## 三、CGLIB 代理

### 3.1 定义

CGLIB（Code Generation Library）是一个第三方字节码生成库，通过**继承**实现代理，在运行时动态生成目标类的子类作为代理类。

核心类：
- `net.sf.cglib.proxy.Enhancer`：配置并创建代理对象
- `net.sf.cglib.proxy.MethodInterceptor`：定义拦截逻辑，所有方法调用都会路由到其 `intercept` 方法

### 3.2 使用步骤

1. 创建目标类（无需实现接口）
2. 实现 `MethodInterceptor`，在 `intercept` 方法中编写拦截逻辑
3. 使用 `Enhancer` 设置父类（`setSuperclass`）和回调（`setCallback`），调用 `create()` 生成代理对象

### 3.3 限制

- 无法代理 `final` 类或 `final` 方法（因为基于继承）
- 需要额外引入 CGLIB 依赖

---

## 四、两者对比

| 对比维度 | JDK 动态代理 | CGLIB 代理 |
|---|---|---|
| 依赖 | Java 标准库，无需额外依赖 | 需引入 CGLIB 第三方库 |
| 代理方式 | 基于**接口**，生成接口的实现类 | 基于**继承**，生成目标类的子类 |
| 是否需要接口 | **必须**有接口 | 不需要接口，可直接代理普通类 |
| 底层实现 | 使用 `java.lang.reflect.Proxy` 反射生成 | 使用 ASM 字节码框架，在运行时生成子类字节码 |
| 性能 | 早期版本较慢，JDK 8+ 已优化，接口场景下性能更优 | 生成代理类耗时较长，但方法调用性能略高（直接调用，无需反射） |
| final 限制 | 无此限制（接口方法本身不能是 final） | 无法代理 `final` 类和 `final` 方法 |
| 典型使用场景 | Spring 中目标对象实现了接口时默认使用 | Spring 中目标对象未实现接口时自动切换使用 |

---

## 五、Spring 中的选择策略

Spring AOP 默认遵循以下规则：
- 目标对象实现了接口 → 使用 JDK 动态代理
- 目标对象没有实现接口 → 使用 CGLIB 代理

Spring Boot 2.x 起默认将 `spring.aop.proxy-target-class` 设为 `true`，即强制使用 CGLIB 代理，以保证行为一致性。
