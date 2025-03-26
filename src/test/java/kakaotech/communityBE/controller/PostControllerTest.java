package kakaotech.communityBE.controller;

import jakarta.servlet.http.Cookie;
import kakaotech.communityBE.dto.PostsDto;
import kakaotech.communityBE.entity.Comment;
import kakaotech.communityBE.entity.Posts;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.service.PostService;
import kakaotech.communityBE.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    User user = new User();
    Posts post = new Posts();
    Comment comment = new Comment();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

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
    }

    @Test
    void 모든_게시물_가져오기() throws Exception {
        when(postService.getAllPosts()).thenReturn(List.of(new PostsDto(post)));
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(post.getId()))
                .andExpect(jsonPath("$[0].title").value(post.getTitle()))
                .andExpect(jsonPath("$[0].content").value(post.getContent()))
                .andExpect(jsonPath("$[0].image").value(post.getImage()))
                .andExpect(jsonPath("$[0].likes").value(post.getLikes()))
                .andExpect(jsonPath("$[0].views").value(post.getViews()))
                .andExpect(jsonPath("$[0].comments[0].comment").value("testComment"));

        verify(postService).getAllPosts();
    }

    @Test
    void 게시물_가져오기() throws Exception {
        Long id = 200L;
        when(postService.getPost(id)).thenReturn(new PostsDto(post));
        mockMvc.perform(get("/posts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.post.id").value(post.getId()))
                .andExpect(jsonPath("$.post.title").value(post.getTitle()))
                .andExpect(jsonPath("$.post.content").value(post.getContent()));

        verify(postService).getPost(id);
    }

    @Test
    void 게시글생성() throws Exception {
        String sessionId = "sessionId";
        when(userService.getUserBySession(sessionId)).thenReturn(user);
        mockMvc.perform(post("/posts")
                        .cookie(new Cookie("sessionId", sessionId))
                        .param("title", "test")
                        .param("content", "contentTest"))
                .andExpect(status().isCreated());

        verify(userService).getUserBySession(sessionId);
        verify(postService).createPost(user.getId(), "test", "contentTest", null);
    }

    @Test
    void 게시글수정() throws Exception {
        String sessionId = "sessionId";
        Long id = 200L;
        when(userService.getUserBySession(sessionId)).thenReturn(user);
        mockMvc.perform(put("/posts/{id}", id)
                        .cookie(new Cookie("sessionId", sessionId))
                        .param("title", "test")
                        .param("content", "contentTest"))
                .andExpect(status().isNoContent());

        verify(userService).getUserBySession(sessionId);
        verify(postService).updatePost(post.getId(), user.getId(), "test", "contentTest", null);
    }

    @Test
    void 게시글삭제() throws Exception {
        String sessionId = "sessionId";
        Long id = 200L;
        when(userService.getUserBySession(sessionId)).thenReturn(user);
        mockMvc.perform(delete("/posts/{id}", id)
                        .cookie(new Cookie("sessionId", sessionId)))
                .andExpect(status().isNoContent());

        verify(userService).getUserBySession(sessionId);
        verify(postService).deletePost(user.getId(), id);
    }

    @Test
    void 좋아요() throws Exception {
        String sessionId = "sessionId";
        Long id = 200L;
        when(userService.getUserBySession(sessionId)).thenReturn(user);
        mockMvc.perform(put("/posts/{id}/likes", id)
                        .cookie(new Cookie("sessionId", sessionId)))
                .andExpect(status().isOk());

        verify(userService).getUserBySession(sessionId);
        verify(postService).increaseLikePost(post.getId(), user.getId());
    }
}