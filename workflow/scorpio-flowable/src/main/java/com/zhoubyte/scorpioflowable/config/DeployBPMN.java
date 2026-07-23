package com.zhoubyte.scorpioflowable.config;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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
//        RepositoryService repositoryService = processEngine.getRepositoryService();
        URL bpmn = this.getClass().getClassLoader().getResource("bpmn");
        if(bpmn == null) {
            log.warn("BPMN 文件目录为空，没有流程信息");
            return ;
        }
        Path bpmnPath = Path.of(bpmn.toURI());
        try (Stream<Path> walk = Files.walk(bpmnPath)) {
            // 收集所有 BPMN 文件
            List<Path> bpmnFiles = walk
                    .filter(path -> path.getFileName().toString().endsWith(BPMN_XML_FILE))
                    .toList();
            if (bpmnFiles.isEmpty()) {
                log.warn("未找到 BPMN 流程文件");
            }else {
                // 批量部署
                DeploymentBuilder builder = repositoryService
                        .createDeployment()
                        .name("bpmn-deployment-" + System.currentTimeMillis());
                for (Path item : bpmnFiles) {
                    builder.addClasspathResource(BPMN_PARENT_FILE_NAME + item.getFileName().toString());
                }
                Deployment deploy = builder.deploy();
                log.info("批量部署完成，共 {} 个流程文件，deploymentId: {}", bpmnFiles.size(), deploy.getId());
            }
        } catch (Exception e) {
            log.error("部署 BPMN 文件失败", e);
            throw new RuntimeException(e);
        }
    }
}
