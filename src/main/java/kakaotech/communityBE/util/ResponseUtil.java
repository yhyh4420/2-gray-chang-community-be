package kakaotech.communityBE.util;

import kakaotech.communityBE.dto.MessageResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    public static <T> ResponseEntity<T> ok(T entity) {
        return ResponseEntity.ok(entity);
    }

    public static ResponseEntity<MessageResponseDto> ok(String message) {
        return ResponseEntity.ok(new MessageResponseDto(message));
    }

    public static ResponseEntity<MessageResponseDto> created(String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponseDto(message));
    }

    public static ResponseEntity<MessageResponseDto> noContent(String message) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new MessageResponseDto(message));
    }
}
