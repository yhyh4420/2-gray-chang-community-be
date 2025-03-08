package kakaotech.communityBE.service;

import kakaotech.communityBE.dto.PostsDto;
import kakaotech.communityBE.entity.Posts;
import kakaotech.communityBE.entity.User;
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
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Autowired
    public PostService(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
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
                    logger.warn("유저 없음");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "유저 정보를 찾을 수 없습니다. 로그인 정보를 확인하세요");
                });
        return user;
    }

    @Transactional
    public List<PostsDto> getPosts() {
        List<Posts> posts = postRepository.findAll();
        List<PostsDto> postsDtos = posts.stream()
                .map(post -> new PostsDto(post))
                .collect(Collectors.toList());
        return postsDtos;
    }

    @Transactional
    public PostsDto getPost(Long postId) {
        Posts posts = getPostsbyId(postId);
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
    public void deletePost(Long postId) {
        Posts posts = getPostsbyId(postId);
        postRepository.delete(posts);
    }

    @Transactional
    public PostsDto editPost(Long postId, Long userId, String title, String content, String image){
        Posts posts = getPostsbyId(postId);
        if (!posts.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "직성자만 게시글을 수정할 수 있습니다.");
        }
        posts.setTitle(title);
        posts.setContent(content);
        posts.setImage(image);
        return new PostsDto(posts);
    }

    @Transactional
    public void viewPost(Long postId) {
        postRepository.increaseViews(postId);
    }

    @Transactional
    public void likePost(Long postId) {
        postRepository.increaseLikes(postId);
    }
}
