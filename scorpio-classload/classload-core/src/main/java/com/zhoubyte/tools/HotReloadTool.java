package com.zhoubyte.tools;

import com.zhoubyte.classloads.CalcPluginsClassLoad;
import com.zhoubyte.plugins.CalcInterface;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * 类热加载工具：监控 .class 文件变化，自动卸载旧类并重新加载。
 * 
 * 核心原理：
 * 1. 每次重新加载时创建新的 CalcPluginsClassLoad
 * 2. 旧的 ClassLoader 失去引用 → GC 回收 → 旧类从 Metaspace 卸载
 * 3. 新的 ClassLoader 加载新版本的类
 */
public class HotReloadTool {

    private final String classRootDir;
    private final ClassLoader parentClassLoader;
    
    // 记录每个 .class 文件的最后修改时间
    private final Map<String, FileTime> fileTimestamps = new HashMap<>();
    
    // 当前活跃的 ClassLoader（只持有最新的一个，旧的会被 GC）
    private volatile CalcPluginsClassLoad currentLoader;
    
    // 已加载的插件实例缓存
    private volatile Map<String, CalcInterface> pluginCache = new HashMap<>();
    
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread monitorThread;

    public HotReloadTool(String classRootDir, ClassLoader parentClassLoader) {
        this.classRootDir = classRootDir;
        this.parentClassLoader = parentClassLoader;
    }

    /**
     * 初始加载所有插件
     */
    public void initialLoad() throws Exception {
        System.out.println("=== 初始加载插件 ===");
        reloadAll();
    }

    /**
     * 重新加载所有插件（核心方法）
     */
    public void reloadAll() {
        // 1. 创建新的 ClassLoader（旧的自动失去引用）
        CalcPluginsClassLoad newLoader = new CalcPluginsClassLoad(classRootDir, parentClassLoader);
        
        // 2. 扫描并加载所有插件
        Path pluginsDir = Paths.get(classRootDir, "com/zhoubyte/plugins");
        Map<String, CalcInterface> newPlugins = new HashMap<>();
        
        if (!Files.exists(pluginsDir)) {
            return;
        }
        try (Stream<Path> stream = Files.list(pluginsDir).filter(p -> p.toString().endsWith(".class"))) {
            for(Path classFile : stream.toList()) {
                String fileName = classFile.getFileName().toString();
                String className = fileName.replace(".class", "");
                String fullClassName = "com.zhoubyte.plugins." + className;

                Class<?> clazz = newLoader.loadClass(fullClassName);

                // 只加载实现了 CalcInterface 的类（排除接口本身）
                if (CalcInterface.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                    CalcInterface plugin = (CalcInterface) clazz.getConstructor().newInstance();
                    newPlugins.put(className, plugin);

                    // 记录文件时间戳
                    fileTimestamps.put(classFile.toString(), Files.getLastModifiedTime(classFile));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // 3. 替换为新的 ClassLoader 和插件缓存（原子操作）
        this.currentLoader = newLoader;
        this.pluginCache = newPlugins;
        
        System.out.println("加载完成，共 " + newPlugins.size() + " 个插件");
        newPlugins.forEach((name, plugin) -> System.out.println("  [" + name + "] ClassLoader: " + plugin.getClass().getClassLoader()));
    }

    /**
     * 调用指定插件
     */
    public Object invokePlugin(String pluginName, Double baseMoney) {
        CalcInterface plugin = pluginCache.get(pluginName);
        if (plugin == null) {
            return "插件不存在: " + pluginName;
        }
        return plugin.calc(baseMoney);
    }

    /**
     * 启动文件监控（后台线程）
     * @param intervalMs 检查间隔（毫秒）
     */
    public void startWatch(long intervalMs) {
        running.set(true);
        monitorThread = new Thread(() -> {
            System.out.println("=== 启动热加载监控，间隔 " + intervalMs + "ms ===");
            while (running.get()) {
                try {
                    boolean changed = checkFileChanges();
                    if (changed) {
                        System.out.println("\n>>> 检测到文件变化，开始热重载 <<<");
                        reloadAll();
                        System.out.println(">>> 热重载完成 <<<\n");
                    }
                    Thread.sleep(intervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("监控异常: " + e.getMessage());
                }
            }
        }, "HotReload-Monitor");
        monitorThread.setDaemon(true); // 守护线程，JVM 退出时自动停止
        monitorThread.start();
    }

    /**
     * 检查 .class 文件是否有变化
     */
    private boolean checkFileChanges() throws Exception {
        Path pluginsDir = Paths.get(classRootDir, "com/zhoubyte/plugins");
        if (!Files.exists(pluginsDir)) {
            return false;
        }

        // 检查是否有新文件或文件被修改
        try (var stream = Files.list(pluginsDir).filter(p -> p.toString().endsWith(".class"))) {
            for (Path classFile : stream.toList()) {
                FileTime currentTime = Files.getLastModifiedTime(classFile);
                FileTime lastTime = fileTimestamps.get(classFile.toString());
                
                if (lastTime == null || !lastTime.equals(currentTime)) {
                    return true; // 新文件或已修改
                }
            }
        }
        
        // 检查是否有文件被删除
        if (!fileTimestamps.isEmpty()) {
            try (Stream<Path> stream = Files.list(pluginsDir).filter(p -> p.toString().endsWith(".class"))) {
                long currentCount = stream.count();
                if (currentCount != fileTimestamps.size()) {
                    return true; // 文件数量变化
                }
            }
        }
        
        return false;
    }

    /**
     * 停止监控
     */
    public void stopWatch() {
        running.set(false);
        if (monitorThread != null) {
            monitorThread.interrupt();
        }
    }

    /**
     * 获取当前加载的插件数量
     */
    public int getPluginCount() {
        return pluginCache.size();
    }

    /**
     * 获取当前 ClassLoader（用于调试）
     */
    public CalcPluginsClassLoad getCurrentLoader() {
        return currentLoader;
    }
}
