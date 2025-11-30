package com.cadify.cadifyWAS.service.file.rabbitMQ;

import com.cadify.cadifyWAS.config.RabbitMqConfig;
import com.cadify.cadifyWAS.controller.files.TestController;
import com.cadify.cadifyWAS.model.dto.files.FileTask;
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;


@Service
public class FileTaskProducer {

    private final RabbitTemplate rabbitTemplate;

    public FileTaskProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendByMetal(FileTask task) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.METAL_UPLOAD_QUEUE_NAME, task);
    }

    public void sendByCnc(FileTask task) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.CNC_UPLOAD_QUEUE_NAME, task);
    }

    public void sendByMetalResult(FileTask fileTaskResult) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.METAL_RESULT_QUEUE_NAME, fileTaskResult);
    }

    public void sendByCNCResult(FileTask fileTaskResult) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.CNC_RESULT_QUEUE_NAME, fileTaskResult);
    }
}