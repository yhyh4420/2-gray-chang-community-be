package kakaotech.communityBE.controller;

import jakarta.servlet.http.Cookie;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    User mockUser = new User();

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        mockUser.setId(200L);
        mockUser.setEmail("test@example.com");
        mockUser.setNickname("test");
        mockUser.setPassword("password123");
        mockUser.setProfileImage("test.jpg");
        mockUser.setResigned(false);
    }

    @Test
    void login_shouldReturnSuccessWithCookie() throws Exception {
        String email = "test@example.com";
        String password = "password123";
        String sessionId = "session-abc";

        User mockUser = new User();
        mockUser.setNickname("테스트유저");

        Map<String, Object> loginResult = Map.of(
                "sessionId", sessionId,
                "user", mockUser
        );

        when(userService.login(email, password)).thenReturn(loginResult);

        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.nickname").value("테스트유저"));
    }

    @Test
    void 로그아웃() throws Exception {
        String sessionId = "sessionId";

        mvc.perform(post("/auth/logout")
                        .cookie(new Cookie("sessionId", sessionId)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("sessionId=")));

        verify(userService).logout(sessionId);
    }

    @Test
    void 회원가입() throws Exception {
        String email = "test@example.com";
        String password = "password123";
        String nickName = "test";
        mvc.perform(post("/auth/signup")
                        .param("email", email)
                        .param("password", password)
                        .param("nickname", nickName))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입 성공!"));

        verify(userService).signUp(email, password, nickName, null);
    }

    @Test
    void 세션확인() throws Exception {
        String sessionId = "sessionId";
        when(userService.getUserBySession(sessionId)).thenReturn(mockUser);
        mvc.perform(get("/auth/session-user")
                        .cookie(new Cookie("sessionId", sessionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.userId").value(200L))
                .andExpect(jsonPath("$.nickname").value("test"))
                .andExpect(jsonPath("$.profileImage").value("test.jpg"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUserBySession(sessionId);
    }

    @Test
    void 닉네임변경() throws Exception {
        String nickname = "updateTest";
        String sessionId = "sessionId";
        when(userService.getUserBySession(sessionId)).thenReturn(mockUser);
        System.out.println(mockUser.getId());
        mockUser.setNickname(nickname);
        when(userService.updateNickName(mockUser.getId(), nickname, null)).thenReturn(mockUser);
        mvc.perform(put("/auth/update/nickname")
                        .param("nickName", nickname)
                        .cookie(new Cookie("sessionId", sessionId)))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("수정 성공!"));

        verify(userService).updateNickName(mockUser.getId(), nickname, null);
        verify(userService).getUserBySession(sessionId);
    }

    @Test
    void 비밀번호변경() throws Exception {
        String sessionId = "sessionId";
        String requestBody = """
                {
                   "password": "password123"
                }
                """;
        when(userService.getUserBySession(sessionId)).thenReturn(mockUser);
        mvc.perform(put("/auth/update/password")
                        .cookie(new Cookie("sessionId", sessionId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("수정 성공!"));

        verify(userService).getUserBySession(sessionId);
        verify(userService).updatePassword(mockUser.getId(), "password123");
    }

    @Test
    void 회원탈퇴() throws Exception {
        String sessionId = "sessionId";
        when(userService.getUserBySession(sessionId)).thenReturn(mockUser);
        mvc.perform(delete("/auth/resign")
                        .cookie(new Cookie("sessionId", sessionId)))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("탈퇴 성공"));

        verify(userService).getUserBySession(sessionId);
        verify(userService).deleteUser(mockUser.getId());
    }
}