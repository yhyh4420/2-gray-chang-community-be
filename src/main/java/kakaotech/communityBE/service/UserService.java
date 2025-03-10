package kakaotech.communityBE.service;

import jakarta.servlet.http.HttpSession;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User login(String email, String password) {
        // 사용자 존재 여부 확인
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            logger.warn("등록되지 않은 이메일: {}", email);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "등록되지 않은 이메일입니다.");
        });
        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("잘못된 비밀번호 입력: 이메일({})", email);
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
        logger.info("사용자 로그인 성공: {}", email);
        return user;
    }

    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
            logger.info("로그아웃 성공 : 세션이 종료되었습니다.");
        } else {
            logger.warn("로그아웃 실패 : 세션이 존재하지 않습니다.");
        } // else를 통해 세션이 있을 때만 로그아웃하도록 함
    }

    public User signUp(String email, String password, String nickname, String profileImage) {
        //일단 중복되는지 확인
        Optional<User> userfinByemail = userRepository.findByEmail(email);
        Optional<User> userfindBynickname = userRepository.findByNickname(nickname);
        if (userfinByemail.isPresent()) {
            logger.warn("이미 사용중인 이메일입니다.");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "중복되는 이메일입니다.");
        }
        if (userfindBynickname.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "중복되는 닉네임입니다.");
        }

        String encodedPassword = passwordEncoder.encode(password);

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(encodedPassword);
        newUser.setNickname(nickname);
        newUser.setProfileImage(profileImage);

        return userRepository.save(newUser);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
