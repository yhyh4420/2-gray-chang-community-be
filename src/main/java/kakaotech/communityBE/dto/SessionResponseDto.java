package kakaotech.communityBE.dto;

public record SessionResponseDto(String message, Long userId, String sessionId, String nickname, String email, String profileImage) {
}
