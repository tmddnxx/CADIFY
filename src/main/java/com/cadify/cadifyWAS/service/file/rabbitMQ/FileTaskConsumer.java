package com.cadify.cadifyWAS.service.file.rabbitMQ;

import com.cadify.cadifyWAS.config.RabbitMqConfig;
import com.cadify.cadifyWAS.config.SseEmitters;
import com.cadify.cadifyWAS.controller.files.TestController;
import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.model.dto.files.FileTask;
import com.cadify.cadifyWAS.service.file.FileLogService;
import com.cadify.cadifyWAS.service.file.FilesService;
import com.cadify.cadifyWAS.service.file.FilesTaskService;
import com.cadify.cadifyWAS.service.file.TestService;
import com.cadify.cadifyWAS.service.file.common.Method;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class FileTaskConsumer {

    private final FilesService filesService;
    private final SseEmitters sseEmitters;
    private final FilesTaskService filesTaskService;
    private final FileLogService fileLogService;
    
    // 판금 업로드
    @RabbitListener(queues = RabbitMqConfig.METAL_UPLOAD_QUEUE_NAME, containerFactory = "metalFileUploadRabbitListener")
    public void receiveByMetalTest(FileTask task) {
        System.out.println("🟢 판금 업로드 시작: " + task.getOriginFileName());
        filesService.executeTask(task, Method.METAL); // ecs 태스크 실행요청
    }

    // 판금 후처리
    @RabbitListener(queues = RabbitMqConfig.METAL_RESULT_QUEUE_NAME, containerFactory = "metalFileResultRabbitListener")
    public void receiveByMetalResult(FileTask taskResult) throws IOException {
        try {
            System.out.println("🟢 판금 후처리 시작: " + taskResult.getOriginFileName());
            long startTime = System.currentTimeMillis();

            EstimateDTO.StatusResponse statusResponse = filesService.processingMetalResult(taskResult); // 판금 결과 처리
            sendSseResponseSafely(taskResult.getMemberKey(), statusResponse);

            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;

            System.out.println("🟢 판금 처리 끝: " + taskResult.getOriginFileName() + " (소요 시간: " + elapsedTime + "ms)");

        } catch (Exception e) {
            System.err.println("❌ 판금 처리 실패: " + e.getMessage());
            sendSseResponseSafely(taskResult.getMemberKey(), createFailureResponse(taskResult, e));
            fileLogService.saveFileUploadFailedLog(taskResult, e.getMessage()); // 실패 로그 저장
        } finally {
            filesService.cleanUpDirectory(taskResult);
            filesTaskService.removeTempKey(taskResult.getMemberKey(), taskResult.getTempKey()); // 처리 완료된 tempKey 제거
        }
    }

    // 절삭
    @RabbitListener(queues = RabbitMqConfig.CNC_UPLOAD_QUEUE_NAME, containerFactory = "cncFileUploadRabbitListener")
    public void receiveByCnC(FileTask task) throws IOException {
        System.out.println("🟢 절삭 업로드 시작: " + task.getOriginFileName());
        filesService.executeTask(task, Method.CNC); // ecs 태스크 실행요청
    }

    // 절삭 후처리
    @RabbitListener(queues = RabbitMqConfig.CNC_RESULT_QUEUE_NAME, containerFactory = "cncFileResultRabbitListener")
    public void receiveByCNCResult(FileTask taskResult) throws IOException {
        try {
            System.out.println("🟢 절삭 후처리 시작: " + taskResult.getOriginFileName());
            long startTime = System.currentTimeMillis();

            EstimateDTO.StatusResponse statusResponse = filesService.processingCNCResult(taskResult); // 판금 결과 처리
            sendSseResponseSafely(taskResult.getMemberKey(), statusResponse);

            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;

            System.out.println("🟢 절삭 처리 끝: " + taskResult.getOriginFileName() + " (소요 시간: " + elapsedTime + "ms)");

        } catch (Exception e) {
            System.err.println("❌ 절삭 처리 실패: " + e.getMessage());
            sendSseResponseSafely(taskResult.getMemberKey(), createFailureResponse(taskResult, e));
            fileLogService.saveFileUploadFailedLog(taskResult, e.getMessage()); // 실패 로그 저장
        } finally {
            filesService.cleanUpDirectory(taskResult);
            filesTaskService.removeTempKey(taskResult.getMemberKey(), taskResult.getTempKey()); // 처리 완료된 tempKey 제거
        }
    }

    // 성공 시 응답
    private void sendSseResponseSafely(String memberKey, EstimateDTO.StatusResponse response) {
        try {
            sseEmitters.sendToClient(memberKey, response);
        } catch (Exception e) {
            System.out.println("⚠️ SSE 전송 실패 (무시): " + e.getMessage());
        }
    }

    // 실패 시 응답
    private EstimateDTO.StatusResponse createFailureResponse(FileTask task, Exception e) {
        return EstimateDTO.StatusResponse.builder()
                .fileName(task.getOriginFileName())
                .isSuccess(false)
                .message("파일 처리 중 오류 발생: " + e.getMessage())
                .data(EstimateDTO.Response.builder()
                        .tempKey(task.getTempKey())
                        .build())
                .build();
    }
}