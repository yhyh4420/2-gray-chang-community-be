package kakaotech.communityBE.service;

import kakaotech.communityBE.dto.PostUpdateDto;
import kakaotech.communityBE.dto.PostsDto;
import kakaotech.communityBE.entity.Like;
import kakaotech.communityBE.entity.Posts;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.repository.LikeRepository;
import kakaotech.communityBE.repository.PostRepository;
import kakaotech.communityBE.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;

    @Autowired
    public PostService(UserRepository userRepository, PostRepository postRepository, LikeRepository likeRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
    }

    @Transactional
    public PostsDto createPost(Long userId, String title, String content, String image){
        User user = getUser(userId);

        Posts posts = new Posts();
        posts.setUser(user);
        posts.setTitle(title);
        posts.setContent(content);
        posts.setImage(image);
        posts.setLikes(0);
        posts.setViews(0);

        Posts savedPosts = postRepository.save(posts);
        return new PostsDto(savedPosts);
    }

    private User getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> {
                    logger.warn("유저 없음 : {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "유저 정보를 찾을 수 없습니다. 로그인 정보를 확인하세요");
                });
        return user;
    }

    @Transactional(readOnly = true)
    public List<PostsDto> getAllPosts() {
        List<Posts> posts = postRepository.findAll();
        List<PostsDto> postsDtos = posts.stream()
                .map(PostsDto::new)
                .toList();
        return postsDtos;
    }

    @Transactional
    public PostsDto getPost(Long postId) {
        Posts posts = getPostsbyId(postId);
        posts.setViews(posts.getViews() + 1);// 조회수 오르는거 반영
        return new PostsDto(posts);
    }

    private Posts getPostsbyId(Long postId) {
        Posts posts = postRepository.findById(postId).orElseThrow(()->{
            logger.warn("postId 조회 안됨 : {}", postId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        });
        return posts;
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        Posts posts = getPostsbyId(postId);
        if (userId != posts.getUser().getId()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "게시글 작성자만 글을 지울 수 있습니다.");
        } else{
            postRepository.delete(posts);
        }
    }

    @Transactional
    public PostsDto updatePost(Long postId, Long userId, PostUpdateDto postUpdateDto) {
        Posts posts = getPostsbyId(postId);
        if (!posts.getUser().getId().equals(userId)) {
            logger.warn("작성자 아님");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "직성자만 게시글을 수정할 수 있습니다.");
        }
        posts.setTitle(postUpdateDto.getTitle());
        posts.setContent(postUpdateDto.getContent());
        posts.setImage(postUpdateDto.getImage());
        return new PostsDto(posts);
    }

    @Transactional
    public int increaseLikePost(Long postId, Long userId) {
        User user = getUser(userId);
        Posts posts = getPostsbyId(postId);
        Optional<Like> like = likeRepository.findByUserAndPost(user, posts);
        if (like.isPresent()) {
            logger.warn("이미 좋아요함");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 좋아요를 눌렀습니다.");
        }
        Like newLike = new Like(user, posts);
        likeRepository.save(newLike);
        postRepository.increaseLikes(postId);
        return getPostsbyId(postId).getLikes();
    }
}
