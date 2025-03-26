package kakaotech.communityBE.service;

import kakaotech.communityBE.dto.CommentDto;
import kakaotech.communityBE.entity.Comment;
import kakaotech.communityBE.entity.Posts;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.repository.CommentRepository;
import kakaotech.communityBE.repository.PostRepository;
import kakaotech.communityBE.repository.UserRepository;
import org.mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    private final User user = new User();
    private final Posts post = new Posts();
    private final Comment comment = new Comment();

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, userRepository, postRepository);
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setNickname("nickname");
        user.setPassword("password");

        post.setId(1L);
        post.setTitle("제목");
        post.setContent("내용");
        post.setUser(user);

        comment.setId(1L);
        comment.setComment("test");
        comment.setPosts(post);
        comment.setUser(user);
    }

    @Test
    void 댓글작성_성공() {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        //when
        commentService.createComment(1L, 1L, "댓글");
        //then
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        Comment comment = captor.getValue();
        assertThat(comment.getComment()).isEqualTo("댓글");
    }

    @Test
    void 댓글작성_실패_유저없음(){
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        //when & then
        assertThatThrownBy(() -> commentService.createComment(1L, 1L, "test"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404")
                .hasMessageContaining("유저 정보를 찾을 수 없습니다.");
    }

    @Test
    void 댓글작성_실패_게시글없음(){
        //givne
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        //when & then
        assertThatThrownBy(() -> commentService.createComment(1L, 1L, "test"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404")
                .hasMessageContaining("게시글을 찾을 수 없습니다.");
    }

    @Test
    void 모든댓글가져오기_성공(){
        //givne
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findAllByPostsFetch(post)).thenReturn(List.of(comment));

        //when
        List<CommentDto> result = commentService.getAllComments(1L);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getComment()).isEqualTo("test");
    }

    @Test
    void 댓글업데이트_성공(){
        //given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        //when
        CommentDto commentDto = commentService.updateComment(1L, 1L, "수정테스트");
        //then
        assertThat(commentDto).isNotNull();
        assertThat(commentDto.getComment()).isEqualTo("수정테스트");
    }

    @Test
    void 댓글업데이트_실패_댓글못찾음(){
        //givne
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> commentService.updateComment(1L, 1L, "test"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void 댓글업데이트_실패_작성자아님(){
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        assertThatThrownBy(() -> commentService.updateComment(2L, 1L, "test"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }

    @Test
    void 댓글삭제_성공(){
        //givne
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        //when & then
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        commentService.deleteComment(1L, 1L);
        verify(commentRepository).delete(captor.capture());
        Comment comment = captor.getValue();
        assertThat(comment.getComment()).isEqualTo("test");
    }

    @Test
    void 댓글삭제_실패_작성자아님(){
        //givne
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        //when & then
        assertThatThrownBy(()->commentService.deleteComment(2L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }
}