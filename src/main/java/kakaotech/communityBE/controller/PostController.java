package kakaotech.communityBE.controller;

import kakaotech.communityBE.dto.LikeResponseDto;
import kakaotech.communityBE.dto.MessageResponseDto;
import kakaotech.communityBE.dto.PostsDto;
import kakaotech.communityBE.dto.PostsResponseDto;
import kakaotech.communityBE.service.PostService;
import kakaotech.communityBE.service.UserService;
import kakaotech.communityBE.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    Logger logger = LoggerFactory.getLogger(PostController.class);

    private final PostService postService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<PostsDto>> getAllPost() {
        List<PostsDto> postsDtoList = postService.getAllPosts();
        return ResponseUtil.ok(postsDtoList);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostsResponseDto> getPost(@PathVariable Long postId) {
        PostsDto postsDto = postService.getPost(postId);
        return ResponseUtil.ok(new PostsResponseDto("ok", postsDto));
    }

    @PostMapping
    public ResponseEntity<MessageResponseDto> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = userService.getUserBySession(sessionId).getId();
        logger.info("현재 유저 아이디 : {}, 세션 아이디 : {}", userId, sessionId);
        logger.info("받은 값 : {}, {}, {}", title, content, image);
        postService.createPost(userId, title, content, image);
        return ResponseUtil.created("ok");
    }

    @PutMapping("/{postId}")
    public ResponseEntity<MessageResponseDto> updatePost(
            @PathVariable Long postId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @CookieValue(value="sessionId", required = false) String sessionId) {
        Long userId = userService.getUserBySession(sessionId).getId();
        postService.updatePost(postId, userId, title, content, image);
        return ResponseUtil.noContent("수정 완료");
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<MessageResponseDto> deletePost(
            @PathVariable Long postId,
            @CookieValue(value="sessionId", required = false) String sessionId) {
        Long userId = userService.getUserBySession(sessionId).getId();
        postService.deletePost(userId, postId);
        return ResponseUtil.noContent("삭제 완료!");
    }

    @PutMapping("/{postId}/likes")
    public ResponseEntity<LikeResponseDto> likePost(
            @PathVariable Long postId,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = userService.getUserBySession(sessionId).getId();
        logger.info("sessionId : {}, userId : {}, postId : {}",sessionId, userId, postId);
        int likes = postService.increaseLikePost(postId, userId);
        return ResponseUtil.ok(new LikeResponseDto("좋아요 완료", likes));
    }

}
