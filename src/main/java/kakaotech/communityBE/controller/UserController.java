package kakaotech.communityBE.controller;

import jakarta.servlet.http.HttpServletResponse;
import kakaotech.communityBE.dto.LoginDto;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.SessionStorage;
import kakaotech.communityBE.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5500")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/profile/";
    private static final String DEFAULT_IMAGE_PATH = "/uploads/profile/default-profile.jpg"; // 기본 프로필 이미지

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

        return ResponseEntity.ok(Map.of("message", "로그인 성공",
                "nickname", user.getNickname()));
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

        return ResponseEntity.ok(Map.of("message", "로그아웃되었습니다."));
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signUp(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("nickname") String nickname,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        logger.info("받은 값 : {}, {}, {}", email, nickname, imageFile);
        userService.signUp(email, password, nickname, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "회원가입 성공"));
    }

    @GetMapping("/session-user")
    public ResponseEntity<?> getSessionUser(
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        User user = userService.getUserBySession(sessionId);
        return ResponseEntity.ok(Map.of(
                "message", "ok",
                "userId", user.getId(),
                "sessionId", sessionId,
                "nickname", user.getNickname(),
                "email", user.getEmail(),
                "profileImage", user.getProfileImage()));
    }

    @PutMapping("/update/nickname")
    public ResponseEntity<Map<String, String>> updateProfile(
            @RequestParam("nickName") String nickName,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        User user = userService.getUserBySession(sessionId);

        // ✅ 닉네임 & 이미지 업데이트
        User updatedUser = userService.updateNickName(user.getId(), nickName, imageFile);
        logger.info("Updated User Info: Nickname - {}, ImagePath - {}", updatedUser.getNickname(), updatedUser.getProfileImage());

        return ResponseEntity.ok(Map.of("message", "수정 성공!", "profileImage", updatedUser.getProfileImage()));
    }


    @PutMapping("/update/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            @RequestBody Map<String, String> requestBody,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = userService.getUserBySession(sessionId).getId();
        User newUser = userService.updatePassword(userId, requestBody.get("password"));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("message", "수정 성공!"));
    }

    @DeleteMapping("/resign")
    public ResponseEntity<Map<String, String>> resign(
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = userService.getUserBySession(sessionId).getId();
        userService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("message", "탈퇴 성공!"));
    }
}
