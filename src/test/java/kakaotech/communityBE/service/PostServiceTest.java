package kakaotech.communityBE.service;

import kakaotech.communityBE.dto.PostEditDto;
import kakaotech.communityBE.dto.PostsDto;
import kakaotech.communityBE.entity.Posts;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.repository.PostRepository;
import kakaotech.communityBE.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    private User user;
    private Posts post1;
    private Posts post2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setNickname("testUser");

        post1 = new Posts();
        post1.setId(1L);
        post1.setUser(user);
        post1.setTitle("Test Title");
        post1.setContent("Test Content");
        post1.setImage("test.jpg");
        post1.setLikes(0);
        post1.setViews(0);

        post2 = new Posts();
        post2.setId(2L);
        post2.setUser(user);
        post2.setTitle("Test Title2");
        post2.setContent("Test Content2");
        post2.setImage("test.jpg2");
        post2.setLikes(0);
        post2.setViews(0);
    }

    @Test
    void testCreatePost() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Posts.class))).thenReturn(post1);

        PostsDto result = postService.createPost(1L, "Test Title", "Test Content", "test.jpg");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Title");
        assertThat(result.getContent()).isEqualTo("Test Content");
        assertThat(result.getImage()).isEqualTo("test.jpg");

        verify(postRepository, times(1)).save(any(Posts.class));
    }

    @Test
    void testGetAllPost() {
        when(postRepository.findAll()).thenReturn(Arrays.asList(post1, post2));

        List<PostsDto> result = postService.getPosts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Title");
    }

    @Test
    void testGetPost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));

        PostsDto result = postService.getPost(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Title");
    }

    @Test
    void testEditPost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));

        PostEditDto editDto = new PostEditDto( "Updated Title", "Updated Content", "updated.jpg");
        PostsDto result = postService.editPost(1L, 1L, editDto);

        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getContent()).isEqualTo("Updated Content");
        assertThat(result.getImage()).isEqualTo("updated.jpg");
    }

    @Test
    void testDeletePost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));

        postService.deletePost(1L,  1L);

        verify(postRepository, times(1)).delete(post1);
    }

    @Test
    void testGetPostNotFound() {
        when(postRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPost(2L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(HttpStatus.NOT_FOUND.toString());
    }

    @Test
    void testEditPostUnauthorized() {
        PostEditDto editDto = new PostEditDto( "Updated Title", "Updated Content", "updated.jpg");
        User anotherUser = new User();
        anotherUser.setId(2L);
        post1.setUser(anotherUser);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));

        assertThatThrownBy(() -> postService.editPost(1L, 1L, editDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(HttpStatus.UNAUTHORIZED.toString());
    }
}
