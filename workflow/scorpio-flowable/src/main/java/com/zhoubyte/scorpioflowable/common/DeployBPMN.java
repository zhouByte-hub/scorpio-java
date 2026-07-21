package com.zhoubyte.scorpioflowable.common;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.ProcessEngine;
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
import java.util.stream.Stream;

@Component
@Slf4j
public class DeployBPMN implements ApplicationRunner {

    private final static String BPMN_XML_FILE = ".xml";
    private final static String BPMN_PARENT_FILE_NAME = "bpmn/";

    @Resource
    private ProcessEngine processEngine;


    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        URL bpmn = this.getClass().getClassLoader().getResource("bpmn");
        if(bpmn == null) {
            log.warn("BPMN 文件目录为空，没有流程信息");
            return ;
        }
        Path bpmnPath = Path.of(bpmn.toURI());
        try(Stream<Path> walk = Files.walk(bpmnPath)){
            walk.filter(path -> path.endsWith(BPMN_XML_FILE)).forEach(item -> {
                String relativePath = BPMN_PARENT_FILE_NAME + item.getFileName().toString();
                repositoryService.createDeployment()
                        .addClasspathResource(relativePath)
                        .name(item.getFileName().toString())
                        .deploy();
                log.info("deployed: {}", relativePath);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
