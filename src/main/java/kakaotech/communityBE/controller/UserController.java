package kakaotech.communityBE.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kakaotech.communityBE.dto.LoginDto;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.repository.SessionStorage;
import kakaotech.communityBE.service.UserService;
import org.apache.coyote.Response;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5500")
public class UserController {

    private final UserService userService;
    private final SessionStorage sessionStorage;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/profile/";
    private static final String DEFAULT_IMAGE_PATH = "/uploads/profile/default-profile.jpg"; // 기본 프로필 이미지

    public UserController(UserService userService, SessionStorage sessionStorage) {
        this.userService = userService;
        this.sessionStorage = sessionStorage;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginDto loginDto, HttpServletResponse response) {
        User user = userService.login(loginDto.getEmail(), loginDto.getPassword());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "이메일이나 비밀번호가 잘못되었습니다."));
        }

        String sessionId = sessionStorage.createSession(user.getId());
        logger.info("로그인 성공 - 세션 ID: {}", sessionId);

        ResponseCookie cookie = ResponseCookie.from("sessionId", sessionId)
                                .path("/")
                                .domain("localhost")
                                .httpOnly(true)
                                .sameSite("None")
                                .secure(true)
                                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());;

        return ResponseEntity.ok(Map.of("message", "로그인 성공",
                "sessionId", sessionId,
                "nickname", user.getNickname(),
                "profileImage", user.getProfileImage()));
    }

    @PostMapping("/logout")
    public ResponseEntity logout(@CookieValue(value = "sessionId", required = false) String sessionId,
                                 HttpServletResponse response) {
        if (sessionId != null) {
            sessionStorage.removeSession(sessionId);
            logger.info("로그아웃 - 세션 ID 제거: {}", sessionId);
        }

        ResponseCookie cookie = ResponseCookie.from("sessionId", "")
                .path("/")
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .maxAge(0) // ✅ 쿠키 즉시 만료
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
        String profileImagePath = DEFAULT_IMAGE_PATH;

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 파일 저장 처리
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                File destFile = new File(UPLOAD_DIR + fileName);
                logger.info("업로드 디렉터리 존재 여부: " + new File(UPLOAD_DIR).exists());
                logger.info("파일 경로: " + destFile.getAbsolutePath());
                logger.info("파일 크기: " + imageFile.getSize() + " bytes");
                imageFile.transferTo(destFile);
                profileImagePath = "/uploads/profile/" + fileName; // 저장된 이미지 경로
            } catch (IOException e) {
                logger.error(e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "이미지 업로드 실패"));
            }
        }

        User newUser = userService.signUp(email, password, nickname, profileImagePath);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "회원가입 성공", "profileImage", profileImagePath));
    }

    @GetMapping("/session-user")
    public ResponseEntity<?> getSessionUser(@CookieValue(value = "sessionId", required = false) String sessionId) {
        if (sessionId == null) {
            logger.warn("세션 없음!"); // ✅ 세션이 존재하는지 확인
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "세션 없음"));
        } else {
            logger.info("세션 있음 : {}", sessionId);
        }

        if (!sessionStorage.isValidSession(sessionId)) {
            logger.warn("유효하지 않은 세션ID: {}", sessionId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","유효하지 않은 세션"));
        } else {
            logger.info("유효한 세션 : {}", sessionId);
        }

        Object userId = sessionStorage.getUserId(sessionId);
        if (userId == null) {
            logger.warn("세션은 유효한데 userId 없음 : {}", userId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "세션에 매치되는 유저아이디 없음"));
        }
        logger.info("세션 유지중 - userId : {}", userId);
        User user = userService.findById((Long) userId);
        return ResponseEntity.ok(Map.of(
                "message", "ok",
                "userId", userId,
                "sessionId", sessionId,
                "nickname", user.getNickname(),
                "profileImage", user.getProfileImage()));
    }
}
