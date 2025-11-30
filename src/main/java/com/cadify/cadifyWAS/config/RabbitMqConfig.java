package com.cadify.cadifyWAS.config;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@Configuration
public class RabbitMqConfig {
    public static final String METAL_UPLOAD_QUEUE_NAME = "metal-file-upload-queue";
    public static final String CNC_UPLOAD_QUEUE_NAME = "cnc-file-upload-queue";
    public static final String METAL_RESULT_QUEUE_NAME = "metal-file-result-queue";
    public static final String CNC_RESULT_QUEUE_NAME = "cnc-file-result-queue";

    @Bean
    public Queue metalFileQueue() {
        return new Queue(METAL_UPLOAD_QUEUE_NAME, true);
    }

    @Bean
    public Queue cncFileQueue() {
        return new Queue(CNC_UPLOAD_QUEUE_NAME, true);
    }

    @Bean // metal 후처리
    public Queue metalFileResultQueue() {
        return new Queue(METAL_RESULT_QUEUE_NAME, true);
    }

    @Bean // metal 후처리
    public Queue cncFileResultQueue() {
        return new Queue(CNC_RESULT_QUEUE_NAME, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean(name = "metalFileUploadRabbitListener")// 업로드 용 큐
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactoryByMetal(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(5);  // 동시 소비자 수
        factory.setMaxConcurrentConsumers(10);  // 최대 동시 소비자 수
        factory.setPrefetchCount(1); // 소비자당 1개씩 처리
        factory.setTaskExecutor(fileExecuter()); // 스레드 풀 사용
        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(1)
                        .backOffOptions(1000, 2.0, 10000)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean(name = "metalFileResultRabbitListener") // 후처리 용 큐
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactoryByMetalResult(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);  // 동시 소비자 수
        factory.setMaxConcurrentConsumers(5);  // 최대 동시 소비자 수
        factory.setPrefetchCount(1); // 소비자당 1개씩 처리
        factory.setTaskExecutor(fileResultExecuter()); // 스레드 풀 사용
        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(1)
                        .backOffOptions(1000, 2.0, 10000)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean(name = "cncFileUploadRabbitListener") // 업로드 용 큐
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactoryByCnc(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(5);  // 동시 소비자 수
        factory.setMaxConcurrentConsumers(10);  // 최대 동시 소비자 수
        factory.setPrefetchCount(1); // 소비자당 1개씩 처리
        factory.setTaskExecutor(fileExecuter()); // 스레드 풀 사용
        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(1)
                        .backOffOptions(1000, 2.0, 10000)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean(name = "cncFileResultRabbitListener") // 후처리 용 큐
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactoryByCNCResult(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);  // 동시 소비자 수
        factory.setMaxConcurrentConsumers(5);  // 최대 동시 소비자 수
        factory.setPrefetchCount(1); // 소비자당 1개씩 처리
        factory.setTaskExecutor(fileResultExecuter()); // 스레드 풀 사용
        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(1)
                        .backOffOptions(1000, 2.0, 10000)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean(name = "fileExecuter")
    public TaskExecutor fileExecuter() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("upload-thread-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "fileResultExecuter")
    public TaskExecutor fileResultExecuter() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("processing-thread-");
        executor.initialize();
        return executor;
    }
}
