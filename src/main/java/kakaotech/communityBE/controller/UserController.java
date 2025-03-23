package kakaotech.communityBE.controller;

import jakarta.servlet.http.HttpServletResponse;
import kakaotech.communityBE.dto.LoginDto;
import kakaotech.communityBE.dto.MessageResponseDto;
import kakaotech.communityBE.dto.SessionResponseDto;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.service.UserService;
import kakaotech.communityBE.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginDto loginDto,
            HttpServletResponse response) {
        Map<String, Object> login = userService.login(loginDto.getEmail(), loginDto.getPassword());
        String sessionId = (String) login.get("sessionId");
        User user = (User) login.get("user");
        ResponseCookie cookie = ResponseCookie.from("sessionId", sessionId)
                                .path("/")
                                .domain("localhost")
                                .httpOnly(true)
                                .sameSite("None")
                                .secure(true)
                                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseUtil.ok(Map.of("message", "로그인 성공", "nickname", user.getNickname()));
    }

    @PostMapping("/logout")
    public ResponseEntity logout(
            @CookieValue(value = "sessionId", required = false) String sessionId,
            HttpServletResponse response) {
        userService.logout(sessionId);

        ResponseCookie cookie = ResponseCookie.from("sessionId", "")
                .path("/")
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseUtil.ok("ok");
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponseDto> signUp(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("nickname") String nickname,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        logger.info("받은 값 : {}, {}, {}", email, nickname, imageFile);
        userService.signUp(email, password, nickname, imageFile);
        return ResponseUtil.created("회원가입 성공!");
    }

    @GetMapping("/session-user")
    public ResponseEntity<?> getSessionUser(
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        User user = userService.getUserBySession(sessionId);
        return ResponseUtil.ok(new SessionResponseDto(
                "ok",
                user.getId(),
                sessionId,
                user.getNickname(),
                user.getEmail(),
                user.getProfileImage()));
    }

    @PutMapping("/update/nickname")
    public ResponseEntity<MessageResponseDto> updateProfile(
            @RequestParam("nickName") String nickName,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        User user = userService.getUserBySession(sessionId);

        User updatedUser = userService.updateNickName(user.getId(), nickName, imageFile);
        logger.info("Updated User Info: Nickname - {}, ImagePath - {}", updatedUser.getNickname(), updatedUser.getProfileImage());
        return ResponseUtil.created("수정 완료!");
    }


    @PutMapping("/update/password")
    public ResponseEntity<MessageResponseDto> updatePassword(
            @RequestBody Map<String, String> requestBody,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = userService.getUserBySession(sessionId).getId();
        userService.updatePassword(userId, requestBody.get("password"));
        return ResponseUtil.noContent("수정 성공!");
    }

    @DeleteMapping("/resign")
    public ResponseEntity<MessageResponseDto> resign(
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = userService.getUserBySession(sessionId).getId();
        userService.deleteUser(userId);
        return ResponseUtil.noContent("탈퇴 성공");
    }
}
