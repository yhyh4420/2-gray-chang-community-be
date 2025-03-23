package kakaotech.communityBE.service;

import kakaotech.communityBE.SessionStorage;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.repository.UserRepository;
import kakaotech.communityBE.util.FileUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito 환경 설정
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private SessionStorage sessionStorage;

    @Mock
    private MultipartFile imageFile;

    private final User user = new User();
    private String email;
    private String password;
    private String nickname;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, sessionStorage);
        email = "test@test.com";
        nickname = "test";
        password = "test";
        user.setId(1L);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPassword(password);
    }

    @Test
    void 회원가입_성공_프로필사진없음() throws IOException {
        // given
        String defaultProfileImage = "uploads/profile/default-profile.png";

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(password);

        MockedStatic<FileUtil> fileUtilMockStatic = mockStatic(FileUtil.class);
        fileUtilMockStatic.when(FileUtil::getDefaultProfileImage).thenReturn(defaultProfileImage);

        userService.signUp(email, password, nickname, imageFile);
        verify(userRepository, times(1)).save(argThat(user ->
                user.getNickname().equals(nickname) &&
                        user.getEmail().equals(email)&&
                        user.getPassword().equals(password)&&
                        user.getProfileImage().equals(defaultProfileImage)));
    }

    @Test
    void 회원가입_실패_중복이메일() {
        // given
        String email = "test@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));
        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                userService.signUp(email, "password", "nickname", null));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(userRepository, never()).save(any());
        // then
    }

    @Test
    void 회원가입_실패_중복닉네임() {
        // given
        String nickname = "test";
        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(new User()));
        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                userService.signUp("test@email.com", "password", "test", null));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(userRepository, never()).save(any());
        // then
    }

    @Test
    void 로그인_성공() {
        // given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
        when(sessionStorage.createSession(user.getId())).thenReturn("mockSessionId");

        // when
        Map<String, Object> result = userService.login(user.getEmail(), "password");

        // then
        assertThat(result).containsKeys("user", "sessionId");
        assertThat(result.get("sessionId")).isEqualTo("mockSessionId");
    }

    @Test
    void 로그인_실패_잘못된비밀번호() {
        // given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(user.getEmail(), "wrongPassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 틀렸습니다.");
    }

    @Test
    void 닉네임_및_프로필이미지_변경() throws IOException {
        // given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        MultipartFile imageFileUpdate = mock(MultipartFile.class);

        // when
        User updatedUser = userService.updateNickName(user.getId(), "newNickname", imageFileUpdate);

        // then
        assertThat(updatedUser.getNickname()).isEqualTo("newNickname");
        verify(userRepository).findById(user.getId());
    }

    @Test
    void 비밀번호_변경() {
        // given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        // when
        userService.updatePassword(user.getId(), "newPassword");

        // then
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    void 회원_탈퇴() {
        // given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // when
        userService.deleteUser(user.getId());

        // then
        assertThat(user.isResigned()).isTrue();
        assertThat(user.getProfileImage()).isEqualTo(FileUtil.getResignProfileImage());
        assertThat(user.getEmail()).startsWith("deleted");
    }

    @Test
    void 세션으로_사용자_조회_성공() {
        // given
        when(sessionStorage.isValidSession("mockSessionId")).thenReturn(true);
        when(sessionStorage.getUserId("mockSessionId")).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // when
        User result = userService.getUserBySession("mockSessionId");

        // then
        assertThat(result.getId()).isEqualTo(user.getId());
    }

    @Test
    void 세션으로_사용자_조회_실패_잘못된세션() {
        // given
        when(sessionStorage.isValidSession("invalidSessionId")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.getUserBySession("invalidSessionId"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(HttpStatus.UNAUTHORIZED.toString());
    }
}
