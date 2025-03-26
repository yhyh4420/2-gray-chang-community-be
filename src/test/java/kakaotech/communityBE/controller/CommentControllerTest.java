package kakaotech.communityBE.controller;

import jakarta.servlet.http.Cookie;
import kakaotech.communityBE.dto.CommentDto;
import kakaotech.communityBE.entity.Comment;
import kakaotech.communityBE.entity.Posts;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.service.CommentService;
import kakaotech.communityBE.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    User user = new User();
    Posts post = new Posts();
    Comment comment = new Comment();
    String sessionId;
    Long userId;
    Long postId;
    Long commentId;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        user.setId(200L);
        user.setEmail("test@example.com");
        user.setNickname("test");
        user.setPassword("password123");
        user.setProfileImage("test.jpg");
        user.setResigned(false);

        comment.setId(200L);
        comment.setPosts(post);
        comment.setUser(user);
        comment.setComment("testComment");
        comment.setCommentDate(LocalDateTime.now());

        post.setUser(user);
        post.setId(200L);
        post.setTitle("test");
        post.setContent("test");
        post.setImage(null);
        post.setLikes(0);
        post.setViews(0);
        post.setPostDate(LocalDateTime.now());
        post.setComments(List.of(comment));

        userId = user.getId();
        postId = post.getId();
        commentId = comment.getId();
        sessionId = "sessionId";
        when(userService.getUserBySession(sessionId)).thenReturn(user);
    }

    @Test
    @DisplayName("모든 댓글 조회에 성공하고 200을 반환한다.")
    void 모든댓글조회() throws Exception {
        Long postId = post.getId();
        when(commentService.getAllComments(postId)).thenReturn(List.of(new CommentDto(comment)));
        mockMvc.perform(get("/posts/{postId}/comment", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(comment.getId()))
                .andExpect(jsonPath("$[0].postId").value(postId))
                .andExpect(jsonPath("$[0].userId").value(user.getId()))
                .andExpect(jsonPath("$[0].comment").value(comment.getComment()));
    }

    @Test
    @DisplayName("댓글을 생성하고 201을 반환한다.")
    void 댓글쓰기() throws Exception {
        Long postId = post.getId();
        String requestBody = """
                {
                   "content": "testComment"
                }
                """;
        mockMvc.perform(post("/posts/{postId}/comment", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(new Cookie("sessionId", sessionId)))
                .andExpect(status().isOk());

        verify(commentService).createComment(user.getId(), post.getId(), "testComment");
    }

    @Test
    @DisplayName("댓글을 업데이트하고 204를 반환한다.")
    void 댓글수정() throws Exception {
        mockMvc.perform(put("/posts/{postId}/comment/{commentId}", postId, commentId)
                        .cookie(new Cookie("sessionId", sessionId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("content", "testComment"))
                .andExpect(status().isNoContent());

        verify(commentService).updateComment(userId, commentId, "testComment");
    }

    @Test
    @DisplayName("댓글을 삭제하고 204를 반환한다")
    void 댓글삭제() throws Exception {
        mockMvc.perform(delete("/posts/{postId}/comment/{commentId}", postId, commentId)
                        .cookie(new Cookie("sessionId", sessionId)))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(userId, commentId);
    }
}