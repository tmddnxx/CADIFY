package com.cadify.cadifyWAS.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // Spring 설정 클래스로 등록
public class S3Configuration {

    @Value("${aws.access.key.id}") // AWS 액세스 키 ID를 application.properties에서 읽어옴
    private String awsAccessKey;

    @Value("${aws.secret.access.key}") // AWS 비밀 액세스 키를 application.properties에서 읽어옴
    private String awsSecretKey;

    @Value("${aws.region}") // AWS 리전을 application.properties에서 읽어옴
    private String region;

    @Bean // AmazonS3 객체를 Spring Bean으로 등록
    public AmazonS3 amazonS3() {
        // AWS 자격 증명 생성
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey, awsSecretKey);

        // AmazonS3 클라이언트 객체 생성
        return AmazonS3ClientBuilder.standard()
                .withRegion(region) // 리전 설정
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds)) // 자격 증명 제공
                .build(); // AmazonS3 객체 생성
    }


}
