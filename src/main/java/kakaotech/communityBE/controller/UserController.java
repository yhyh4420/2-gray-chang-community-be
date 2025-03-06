package kakaotech.communityBE.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kakaotech.communityBE.dto.LoginDto;
import kakaotech.communityBE.dto.SignupDto;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginDto loginDto, HttpSession session ) {
        User user = userService.login(loginDto.getEmail(), loginDto.getPassword());
        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            return ResponseEntity.status(401).body("이메일이나 비밀번호가 잘못되었습니다");
        }
        session.setAttribute("user", user);
        response.put("message", "로그인 성공!");
        response.put("email", user.getEmail());
        return ResponseEntity.status(200).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity logout(HttpSession session, HttpServletRequest request) {
        userService.logout(session);
        return ResponseEntity.ok("로그아웃되었습니다");
    }

    @PostMapping("/signup")
    public ResponseEntity signUp(@RequestBody SignupDto signupDto) {
        User newUser = userService.signUp(
                signupDto.getEmail(),
                signupDto.getPassword(),
                signupDto.getNickname(),
                signupDto.getProfileImage()
        );
        return ResponseEntity.status(201).body("success");
    }
}
