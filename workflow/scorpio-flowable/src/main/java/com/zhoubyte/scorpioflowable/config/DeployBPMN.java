package com.zhoubyte.scorpioflowable.config;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
@Slf4j
public class DeployBPMN implements ApplicationRunner {

    private final static String BPMN_XML_FILE = ".bpmn20.xml";
    private final static String BPMN_PARENT_FILE_NAME = "bpmn/";

//    @Resource
//    private ProcessEngine processEngine;

    @Resource
    private RepositoryService repositoryService;


    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        URL bpmn = this.getClass().getClassLoader().getResource("bpmn");
        if(bpmn == null) {
            log.warn("BPMN 文件目录为空，没有流程信息");
            return;
        }
        Path bpmnPath = Path.of(bpmn.toURI());
        try (Stream<Path> walk = Files.walk(bpmnPath)) {
            // 收集所有 BPMN 文件
            List<Path> bpmnFiles = walk
                    .filter(path -> path.getFileName().toString().endsWith(BPMN_XML_FILE))
                    .toList();
            if (bpmnFiles.isEmpty()) {
                log.warn("未找到 BPMN 流程文件");
            } else {
                // 智能部署：检查文件变化和部署状态
                List<Path> needDeployFiles = new ArrayList<>();

                for (Path bpmnFile : bpmnFiles) {
                    String fileName = bpmnFile.getFileName().toString();
                    String resourcePath = BPMN_PARENT_FILE_NAME + fileName;

                    if (shouldDeploy(bpmnFile, resourcePath)) {
                        needDeployFiles.add(bpmnFile);
                    }
                }

                if (!needDeployFiles.isEmpty()) {
                    DeploymentBuilder builder = repositoryService
                            .createDeployment()
                            .name("bpmn-deployment-" + System.currentTimeMillis());
                    for (Path item : needDeployFiles) {
                        String fileName = item.getFileName().toString();
                        builder.addClasspathResource(BPMN_PARENT_FILE_NAME + fileName);
                        log.info("准备部署流程文件: {}", fileName);
                    }
                    Deployment deploy = builder.deploy();
                    log.info("批量部署完成，共 {} 个流程文件，deploymentId: {}", needDeployFiles.size(), deploy.getId());
                } else {
                    log.info("所有流程文件均无变化，跳过部署");
                }
            }
        } catch (Exception e) {
            log.error("部署 BPMN 文件失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断是否需要部署
     * @param bpmnFile BPMN文件路径
     * @param resourcePath 资源路径
     * @return true-需要部署，false-不需要部署
     */
    private boolean shouldDeploy(Path bpmnFile, String resourcePath) {
        try {
            // 读取文件内容
            byte[] fileContent = Files.readAllBytes(bpmnFile);
            String fileMd5 = calculateMD5(fileContent);

            // 从BPMN文件中提取流程KEY
            String processKey = extractProcessKey(fileContent);
            if (processKey == null) {
                log.warn("无法从文件 {} 中提取流程KEY，将进行部署", bpmnFile.getFileName());
                return true;
            }

            // 查询该流程KEY的最新版本
            ProcessDefinition latestDefinition = repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionKey(processKey)
                    .latestVersion()
                    .singleResult();

            if (latestDefinition == null) {
                log.info("流程 {} 未部署过，将进行部署", processKey);
                return true;
            }

            // 获取已部署的文件内容
            InputStream deployedStream = repositoryService
                    .getResourceAsStream(latestDefinition.getDeploymentId(), latestDefinition.getResourceName());
            byte[] deployedContent = deployedStream.readAllBytes();
            String deployedMd5 = calculateMD5(deployedContent);

            // 比较MD5
            if (!fileMd5.equals(deployedMd5)) {
                log.info("流程 {} 文件内容有变化，将进行部署", processKey);
                return true;
            } else {
                log.info("流程 {} 文件内容无变化，跳过部署", processKey);
                return false;
            }

        } catch (Exception e) {
            log.warn("检查流程文件 {} 时发生异常，默认进行部署: {}", bpmnFile.getFileName(), e.getMessage());
            return true;
        }
    }

    /**
     * 计算MD5哈希值
     * @param data 字节数组
     * @return MD5哈希字符串
     */
    private String calculateMD5(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 从BPMN XML文件中提取流程KEY
     * @param fileContent 文件内容
     * @return 流程KEY，如果提取失败返回null
     */
    private String extractProcessKey(byte[] fileContent) {
        try {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            ByteArrayInputStream in = new ByteArrayInputStream(fileContent);
            XMLStreamReader xtr = xif.createXMLStreamReader(in);

            while (xtr.hasNext()) {
                xtr.next();
                if (xtr.isStartElement() && "process".equals(xtr.getLocalName())) {
                    String key = xtr.getAttributeValue(null, "id");
                    xtr.close();
                    return key;
                }
            }
            xtr.close();
            return null;
        } catch (Exception e) {
            log.warn("提取流程KEY失败: {}", e.getMessage());
            return null;
        }
    }
}
