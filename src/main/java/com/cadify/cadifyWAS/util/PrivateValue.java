package com.cadify.cadifyWAS.util;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

@Component
@Getter
public class PrivateValue {

    private final String fileRootDir;
    private final String serverRootDir;
    private final String dockerPath;
    private final String originDockerPath;
    private final String clusterName;
    private final String metalTaskDefinitionName;
    private final String cncTaskDefinitionName;
    private final String taskContainerName;
    private final String taskSubnetId;
    private final String taskSecurityGroupId;
    private final LocalDate POLICY_VERSION = LocalDate.of(Year.now().getValue(), Month.JUNE, 1); // 정책 버전 날짜 설정

    // @Value로 root.dir 값을 주입받기
    public PrivateValue(
            @Value("${file.root.dir}") String fileRootDir,
            @Value("${server.root.dir}") String serverRootDir,
            @Value("${docker.compose.path}") String dockerPath,
            @Value("${docker.origin.path}") String originDockerPath,
            @Value("${cluster.name}") String clusterName,
            @Value("${metal.task.definition.name}") String metalTaskDefinitionName,
            @Value("${cnc.task.definition.name}") String cncTaskDefinitionName,
            @Value("${task.container.name}") String taskContainerName,
            @Value("${task.subnet.id}") String taskSubnetId,
            @Value("${task.secruity.group.id}") String taskSecurityGroupId // 기본값 설정
    ) {
        this.fileRootDir = fileRootDir;
        this.serverRootDir = serverRootDir;
        this.dockerPath = dockerPath;
        this.originDockerPath = originDockerPath;
        this.clusterName = clusterName;
        this.metalTaskDefinitionName = metalTaskDefinitionName;
        this.cncTaskDefinitionName = cncTaskDefinitionName;
        this.taskContainerName = taskContainerName;
        this.taskSubnetId = taskSubnetId;
        this.taskSecurityGroupId = taskSecurityGroupId;
    }

    public LocalDate getPolicyVersion() {
        return POLICY_VERSION;
    }
}
