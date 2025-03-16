package kakaotech.communityBE.repository;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStorage {
    private final Map<String, Long> sessionStore = new ConcurrentHashMap<>();

    // 새로운 세션 생성 (로그인 시)
    public String createSession(Long userId) {
        String sessionId = UUID.randomUUID().toString(); // 고유한 세션 ID 생성
        sessionStore.put(sessionId, userId);
        return sessionId;
    }

    // 세션에서 사용자 ID 가져오기
    public Long getUserId(String sessionId) {
        return sessionStore.get(sessionId);
    }

    // 세션 삭제 (로그아웃 시)
    public void removeSession(String sessionId) {
        sessionStore.remove(sessionId);
    }

    // 세션 존재 여부 확인
    public boolean isValidSession(String sessionId) {
        return sessionStore.containsKey(sessionId);
    }
}
