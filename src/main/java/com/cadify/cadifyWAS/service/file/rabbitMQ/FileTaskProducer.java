package com.cadify.cadifyWAS.service.file.rabbitMQ;

import com.cadify.cadifyWAS.config.RabbitMqConfig;
import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.model.dto.files.FileTask;
import com.cadify.cadifyWAS.service.file.common.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.cadify.cadifyWAS.exception.ExceptionCode.INVALID_TYPE;

@Slf4j
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

    /**
     * 작업 타입(METAL/CNC)에 따라 결과 큐로 라우팅한다.
     */
    public void sendByResult(FileTask fileTaskResult) {
        if (fileTaskResult.getMethod() == Method.METAL) {
            sendByMetalResult(fileTaskResult);
        } else if (fileTaskResult.getMethod() == Method.CNC) {
            sendByCNCResult(fileTaskResult);
        } else {
            log.error("알 수 없는 작업 타입: {}", fileTaskResult.getMethod());
            throw new CustomLogicException(INVALID_TYPE);
        }
    }
}
