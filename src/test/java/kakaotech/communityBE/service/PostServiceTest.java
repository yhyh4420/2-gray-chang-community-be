package kakaotech.communityBE.service;

import kakaotech.communityBE.dto.PostsDto;
import kakaotech.communityBE.entity.Like;
import kakaotech.communityBE.entity.Posts;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.repository.LikeRepository;
import kakaotech.communityBE.repository.PostRepository;
import kakaotech.communityBE.repository.UserRepository;
import kakaotech.communityBE.util.FileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private LikeRepository likeRepository;

    private final User user = new User();
    private final Posts post1 = new Posts();
    private final Posts post2 = new Posts();
    private final Posts postWithImage = new Posts();

    @BeforeEach
    void setUp() {
        postService = new PostService(userRepository, postRepository, likeRepository);
        user.setId(1L);
        user.setPassword("password");
        user.setNickname("nickname");
        user.setEmail("test@test.com");

        post1.setId(1L);
        post1.setTitle("제목1");
        post1.setContent("내용1");
        post1.setComments(List.of());
        post1.setUser(user);

        post2.setId(2L);
        post2.setTitle("제목2");
        post2.setContent("내용2");
        post2.setComments(List.of());
        post2.setUser(user);

        postWithImage.setId(3L);
        postWithImage.setTitle("제목3");
        postWithImage.setContent("내용3");
        postWithImage.setComments(List.of());
        postWithImage.setImage("test.jpg");
        postWithImage.setUser(user);
    }

    @Test
    void 게시글생성_유저못찾음(){
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        MultipartFile imageFile = mock(MultipartFile.class);
        //when & then
        assertThatThrownBy(() -> postService.createPost(1L, "test", "test", imageFile))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404")
                .hasMessageContaining("유저 정보를 찾을 수 없습니다.");
    }

    @Test
    void 게시글생성_이미지업로드실패() throws IOException {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        MultipartFile imageFile = mock(MultipartFile.class);
        try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
            fileUtilMock.when(() -> FileUtil.savePostImage(imageFile)).thenThrow(new IOException("업로드 실패"));
            //when & then
            assertThatThrownBy(() -> postService.createPost(1L, "test", "test", imageFile))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("500")
                    .hasMessageContaining("이미지 업로드 실패");
        }
    }

    @Test
    void 게시글생성_이미지없음() throws IOException {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        MultipartFile imageFile = mock(MultipartFile.class);
        try(MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
            fileUtilMock.when(() -> FileUtil.savePostImage(imageFile)).thenReturn(null);

            //when
            postService.createPost(1L, "test", "test", imageFile);

            //then
            ArgumentCaptor<Posts> captor = ArgumentCaptor.forClass(Posts.class);
            verify(postRepository).save(captor.capture());
            Posts posts = captor.getValue();
            assertThat(posts.getTitle()).isEqualTo("test");
            assertThat(posts.getContent()).isEqualTo("test");
            assertThat(posts.getImage()).isNull();
        }
    }

    @Test
    void 게시글생성_이미지있음() throws IOException {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        MultipartFile imageFile = mock(MultipartFile.class);
        try(MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
            fileUtilMock.when(() -> FileUtil.savePostImage(imageFile)).thenReturn("testImage");

            //when
            postService.createPost(1L, "test", "test", imageFile);

            //then
            ArgumentCaptor<Posts> captor = ArgumentCaptor.forClass(Posts.class);
            verify(postRepository).save(captor.capture());
            Posts posts = captor.getValue();
            assertThat(posts.getTitle()).isEqualTo("test");
            assertThat(posts.getContent()).isEqualTo("test");
            assertThat(posts.getImage()).isEqualTo("testImage");
        }
    }

    @Test
    void 모든_게시글_조회() {
        // given
        List<Posts> fakePosts = List.of(post1, post2);

        when(postRepository.findAllFetch()).thenReturn(fakePosts);

        // when
        List<PostsDto> result = postService.getAllPosts();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("제목1");
        assertThat(result.get(1).getTitle()).isEqualTo("제목2");
    }

    @Test
    void 상세_게시글_조회_조회성공() {
        //given
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        //when
        PostsDto result = postService.getPost(1L);
        //then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("제목1");
        assertThat(result.getViews()).isEqualTo(1);
    }

    @Test
    void 상세_게시글_조회_조회실패(){
        //given
        when(postRepository.findById(4L)).thenReturn(Optional.empty());
        //when & then
        assertThatThrownBy(() -> {PostsDto post = postService.getPost(4L);})
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 게시글삭제_성공(){
        //givne
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        //when & then
        ArgumentCaptor<Posts> captor = ArgumentCaptor.forClass(Posts.class);
        postService.deletePost(1L, 1L);
        verify(postRepository).delete(captor.capture());
        Posts posts = captor.getValue();
        assertThat(posts.getTitle()).isEqualTo("제목1");
        assertThat(posts.getContent()).isEqualTo("내용1");
    }

    @Test
    void 게시글삭제_실패(){
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        assertThatThrownBy(() -> postService.deletePost(2L,1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }

    @Test
    void 게시글업데이트_성공_기존이미지삭제(){
        //given
        when(postRepository.findById(3L)).thenReturn(Optional.of(postWithImage));
        MultipartFile imageFile = mock(MultipartFile.class);
        when(imageFile.getOriginalFilename()).thenReturn("updateImage.jpg");

        //when
        postService.updatePost(3L, 1L, "test", "test", imageFile);

        //then
        assertThat(postWithImage.getImage()).contains("updateImage.jpg");
    }

    @Test
    void 게시글업데이트_성공_기존이미지없음(){
        //given
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        MultipartFile imageFile = mock(MultipartFile.class);
        when(imageFile.getOriginalFilename()).thenReturn("updateImage.jpg");

        //when
        postService.updatePost(1L, 1L, "test", "test", imageFile);

        //then
        assertThat(post1.getImage()).contains("updateImage.jpg");
    }

    @Test
    void 게시글업데이트_실패_작성자아님(){
        //given
        when(postRepository.findById(1L)).thenReturn(Optional.of(postWithImage));
        //when & then
        assertThatThrownBy(() -> postService.updatePost(1L, 2L, "test", "test", null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401")
                .hasMessageContaining("작성자만 게시글을 수정할 수 있습니다.");
    }

    @Test
    void 게시글업데이트_실패_이미지저장실패(){
        //given
        when(postRepository.findById(3L)).thenReturn(Optional.of(postWithImage));
        MultipartFile imageFile = mock(MultipartFile.class);
        try(MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
            fileUtilMock.when(() -> FileUtil.savePostImage(imageFile)).thenThrow(IOException.class);

            //when & then
            assertThatThrownBy(() -> postService.updatePost(3L, 1L, "test", "test", imageFile))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("500")
                    .hasMessageContaining("이미지 저장 실패");
        }
    }

    @Test
    void 좋아요누르기_성공(){
        //givne
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        when(likeRepository.findByUserAndPost(user, post1)).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            post1.setLikes(post1.getLikes() + 1);
            return null;
        }).when(postRepository).increaseLikes(1L);

        //when
        int likeCount = postService.increaseLikePost(1L, 1L);
        //then
        assertThat(likeCount).isEqualTo(1);
    }

    @Test
    void 좋아요누르기_실패(){
        //givne
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        when(likeRepository.findByUserAndPost(user, post1)).thenReturn(Optional.of(new Like(user, post1)));

        //when & then
        assertThatThrownBy(() -> postService.increaseLikePost(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400")
                .hasMessageContaining("이미 좋아요를 눌렀습니다.");
    }


}