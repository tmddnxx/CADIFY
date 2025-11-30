package com.cadify.cadifyWAS.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
@Log4j2
public class SseEmitters {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    // 공유된 스케줄러 (하트비트 전송을 위한 스레드 풀)
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SseEmitter add(String clientId, SseEmitter emitter) {
        emitters.put(clientId, emitter);
        emitter.onCompletion(() -> emitters.remove(clientId));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(clientId);
        });
        emitter.onError(e -> emitters.remove(clientId));
        return emitter;
    }

    public void sendToClient(String clientId, Object data) throws IOException {
        SseEmitter emitter = emitters.get(clientId);
        if (emitter != null) {
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(data));
        }
    }

    // 하트비트를 보내는 메서드
    public static void sendHeartbeat(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("heartbeat").data(":keep-alive"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    // 하트비트를 주기적으로 보내는 메서드
    public void startHeartbeat(SseEmitter emitter) {
        // 30초마다 하트비트 전송
        scheduler.scheduleAtFixedRate(() -> sendHeartbeat(emitter), 0, 30, TimeUnit.SECONDS);
    }

}

