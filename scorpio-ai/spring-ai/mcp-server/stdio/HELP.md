# Stdio MCP Server

STDIO MCP：进程间通过 stdin/stdout 走 JSON-RPC，**stderr 专门打日志，千万不要 println 到 stdout，会破坏协议报文**。

MCP 3 大核心能力：
- **Tool**：可被模型调用的函数
- **Resource**：可通过 URI 读取的上下文资源（文件、知识库片段等）
- **Prompt**：预定义提示词模板，客户端可传入参数渲染消息列表
