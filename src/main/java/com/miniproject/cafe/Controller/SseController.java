package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Emitter.SseEmitterStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterStore emitterStore;

    // 관리자 전용 SSE 구독
    @GetMapping(value = "/sse/admin/{storeName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeAdmin(@PathVariable String storeName) {

        // 2. 타임아웃 설정 (30분)
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // 3. 저장소에 "디코딩된 이름"으로 저장
        emitterStore.addAdminEmitter(storeName, emitter);

        // 4. 연결 즉시 더미 데이터 전송 (503 에러 방지용)
        safeSend(emitter, "connect", "admin-connected");

        return emitter;
    }

    // 사용자 전용 SSE 구독
    @GetMapping(value = "/sse/user/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeUser(@PathVariable String userId) {

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitterStore.addUserEmitter(userId, emitter);

        safeSend(emitter, "connect", "user-connected");

        return emitter;
    }

    private void safeSend(SseEmitter emitter, String event, Object data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (Exception e) {
            try {
                emitter.complete();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }
}