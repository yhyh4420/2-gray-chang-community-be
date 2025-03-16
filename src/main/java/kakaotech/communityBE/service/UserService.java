package kakaotech.communityBE.service;

import kakaotech.communityBE.SessionStorage;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SessionStorage sessionStorage;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/profile/";
    private static final String DEFAULT_IMAGE_PATH = "/uploads/profile/default-profile.jpg"; // 기본 프로필 이미지
    private static final String RESIGN_IMAGE_PATH = "/uploads/profile/resign.jpg"; // 탈퇴 회원 프로필 이미지


    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, SessionStorage sessionStorage, SessionStorage sessionStorage1) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionStorage = sessionStorage1;
    }

    @Transactional
    public Map<String, Object> login(String email, String password) {
        // 사용자 존재 여부 확인
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            logger.warn("등록되지 않은 이메일: {}", email);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "등록되지 않은 이메일입니다.");
        });
        if (user.isResigned()){
            logger.warn("탈퇴 처리된 이메일: {}", email);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "탈퇴처리된 이메일입니다.");
        }
        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("잘못된 비밀번호 입력: 이메일({})", email);
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
        logger.info("사용자 로그인 성공: {}", email);
        String sessionId = sessionStorage.createSession(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("sessionId", sessionId);
        return result;
    }

    @Transactional
    public void logout(String sessionId) {
        if (sessionId != null) {
            sessionStorage.removeSession(sessionId);
            logger.info("로그아웃 - 세션 ID 제거: {}", sessionId);
        }
    }

    @Transactional
    public void signUp(String email, String password, String nickname, MultipartFile imageFile) {
        //일단 중복되는지 확인
        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("이미 사용중인 이메일입니다.");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "중복되는 이메일입니다.");
        }
        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "중복되는 닉네임입니다.");
        }

        String encodedPassword = passwordEncoder.encode(password);

        String profileImagePath = DEFAULT_IMAGE_PATH;
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

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
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 실패");
            }
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(encodedPassword);
        newUser.setNickname(nickname);
        newUser.setProfileImage(profileImagePath);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(()->{
                    logger.warn("유저 없음, userId : {}", userId);
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다.");
                });
    }

    @Transactional
    public User updateNickName(Long userId, String nickname, MultipartFile imageFile) {
        User user = findById(userId);
        String oldImagePath = user.getProfileImage();

        logger.info("UserId: {}, OldImagePath: {}, NewNickname: {}", user.getId(), oldImagePath, nickname);

        // 기존 이미지 삭제 (이전 이미지가 기본 이미지가 아닐 때만 삭제)
        if (oldImagePath != null && !oldImagePath.equals(DEFAULT_IMAGE_PATH)) {
            File oldFile = new File(oldImagePath.substring(1)); // 앞에 '/' 제거
            if (oldFile.exists()) {
                boolean deleted = oldFile.delete();
                logger.info("Old Image Deleted: {}", deleted);
            }
        }

        String imagePath = DEFAULT_IMAGE_PATH; // 기본 프로필 이미지 경로

        // 새로운 이미지 저장
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                File destFile = new File(UPLOAD_DIR + fileName);
                imageFile.transferTo(destFile);
                imagePath = "/uploads/profile/" + fileName; // 클라이언트에서 접근할 수 있도록 경로 설정
                logger.info("New Profile Image Saved: {}", imagePath);
            } catch (IOException e) {
                logger.error("새 프로필 이미지 저장 에러 : {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 실패");
            }
        }

        user.setNickname(nickname);
        user.setProfileImage(imagePath);
        return user;
    }

    @Transactional
    public User updatePassword(Long userId, String newPassword) {
        User user = findById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        return user;
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId);
        //기존 프로필사진 삭제
        String oldProfileImage = user.getProfileImage();
        if (!Objects.equals(oldProfileImage, "/uploads/profile/default-profile.jpg")) {
            logger.info("현재 이미지 : {}", oldProfileImage);
            File oldFile = new File(oldProfileImage.substring(1));
            oldFile.delete();
        }
        user.setNickname("탈퇴한 사용자");
        user.setProfileImage("/uploads/profile/resign.png"); // todo : 이건 나중에 변수로 따로 선언
        user.setResigned(true);
        user.setEmail("deleted" + UUID.randomUUID().toString().substring(0, 35)); // 이러면 혹시모를 중복가입 안되겠지..?
        user.setPassword(passwordEncoder.encode(userId + UUID.randomUUID().toString().substring(0, 10))); // 이러면 비밀번호 추론 불가
    }

    @Transactional(readOnly = true)
    public User getUserBySession(String sessionId) {
        if (sessionId == null) {
            logger.warn("세션 없음!"); // 세션이 존재하는지 확인
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "세션이 없습니다");
        } else {
            logger.info("세션 있음 : {}", sessionId);
        }

        if (!sessionStorage.isValidSession(sessionId)) {
            logger.warn("유효하지 않은 세션ID: {}", sessionId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 세션");
        } else {
            logger.info("유효한 세션 : {}", sessionId);
        }

        Long userId = sessionStorage.getUserId(sessionId);
        if (userId == null) {
            logger.warn("세션은 유효한데 userId 없음, sessionId: {}", sessionId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "세션에 매치되는 유저아이디가 없음");
        }
        logger.info("세션 유지중 - userId : {}", userId);
        return findById(userId);
    }
}
