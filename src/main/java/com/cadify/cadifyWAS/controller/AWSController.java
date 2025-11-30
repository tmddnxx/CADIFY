package com.cadify.cadifyWAS.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AWSController {

    @RequestMapping("/health")
    public ResponseEntity<Void> awsApplicationLoadBalancerHealthCheck(){
        return ResponseEntity.ok().build();
    }
}
