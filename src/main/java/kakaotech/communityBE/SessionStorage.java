package kakaotech.communityBE;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class SessionStorage {

    private final StringRedisTemplate redisTemplate;
    private static final int SESSION_EXPIRY = 60 * 30;

    // 새로운 세션 생성 (로그인 시)
    public String createSession(Long userId) {
        String sessionId = UUID.randomUUID().toString(); // 고유한 세션 ID 생성
        redisTemplate.opsForValue().set(sessionId, String.valueOf(userId), SESSION_EXPIRY, TimeUnit.SECONDS);
        return sessionId;
    }

    // 세션에서 사용자 ID 가져오기
    public Long getUserId(String sessionId) {
        String userId = redisTemplate.opsForValue().get(sessionId);
        return (userId != null) ? Long.parseLong(userId) : null;
    }

    // 세션 삭제 (로그아웃 시)
    public void removeSession(String sessionId) {
        redisTemplate.delete(sessionId);
    }

    // 세션 존재 여부 확인
    public boolean isValidSession(String sessionId) {
        return redisTemplate.hasKey(sessionId);
    }
}
