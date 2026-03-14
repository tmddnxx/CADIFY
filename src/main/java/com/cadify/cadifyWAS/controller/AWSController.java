package com.cadify.cadifyWAS.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AWSController {

    // AWS ALB 헬스 체크 엔드포인트
    @RequestMapping("/health")
    public ResponseEntity<Void> awsApplicationLoadBalancerHealthCheck() {
        return ResponseEntity.ok().build();
    }
}
