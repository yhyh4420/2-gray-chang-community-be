package kakaotech.communityBE.service;

import kakaotech.communityBE.SessionStorage;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.repository.UserRepository;
import kakaotech.communityBE.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SessionStorage sessionStorage;

    @Transactional
    public Map<String, Object> login(String email, String password) {
        // 사용자 존재 여부 확인
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            logger.warn("등록되지 않은 이메일: {}", email);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "등록되지 않은 이메일입니다.");
        });
        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("잘못된 비밀번호 입력: 이메일({})", email);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "비밀번호가 틀렸습니다.");
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

        String profileImagePath = FileUtil.getDefaultProfileImage();

        try {
            String uploadedImagePath = FileUtil.saveProfileImage(imageFile);
            if (uploadedImagePath != null) {
                profileImagePath = uploadedImagePath;
                logger.info("실제 저장된 프로필 이미지 이름 : {}", profileImagePath);
            }
        } catch (Exception e) {
            logger.error("프로필 이미지 저장 실패: {}", e.getMessage());
        }

        User newUser = new User();
        newUser.setNickname(nickname);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email);
        newUser.setProfileImage(profileImagePath);
        userRepository.save(newUser);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(()->{
                    logger.warn("유저 없음, userId : {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다.");
                });
    }

    @Transactional
    public User updateNickName(Long userId, String nickname, MultipartFile imageFile) {
        User user = findById(userId);

        FileUtil.deleteFile(user.getProfileImage());
        String imagePath = FileUtil.getDefaultProfileImage();
        try {
            String uploadedImage = FileUtil.saveProfileImage(imageFile);
            if (uploadedImage != null) {
                imagePath = uploadedImage;
            }
        } catch (Exception e) {
            logger.error("프로필 수정이미지 저장 실패: {}", e.getMessage());
        }
        user.setNickname(nickname);
        user.setProfileImage(imagePath);
        return user;
    }

    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        User user = findById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId);
        //기존 프로필사진 삭제
        FileUtil.deleteFile(user.getProfileImage());
        String deleteNickname;
        do{
            deleteNickname = "탈퇴유저_" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 5);
        } while (userRepository.findByNickname(deleteNickname).isPresent());
        user.setNickname(deleteNickname);
        user.setProfileImage(FileUtil.getResignProfileImage());
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
