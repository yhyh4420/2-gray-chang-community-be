package kakaotech.communityBE.controller;

import kakaotech.communityBE.dto.PostUpdateDto;
import kakaotech.communityBE.dto.PostsDto;
import kakaotech.communityBE.SessionStorage;
import kakaotech.communityBE.service.PostService;
import kakaotech.communityBE.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        return ResponseEntity.ok(postsDtoList);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> getPost(@PathVariable Long postId) {
        PostsDto postsDto = postService.getPost(postId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "ok");
        response.put("post", postsDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = userService.getUserBySession(sessionId).getId();
        logger.info("현재 유저 아이디 : {}, 세션 아이디 : {}", userId, sessionId);
        logger.info("받은 값 : {}, {}, {}", title, content, image);
        postService.createPost(userId, title, content, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "ok"));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> updatePost(
            @PathVariable Long postId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @CookieValue(value="sessionId", required = false) String sessionId) {
        Long userId = userService.getUserBySession(sessionId).getId();
        postService.updatePost(postId, userId, title, content, image);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("message", "수정 성공!"));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(
            @PathVariable Long postId,
            @CookieValue(value="sessionId", required = false) String sessionId) {
        Long userId = userService.getUserBySession(sessionId).getId();
        postService.deletePost(userId, postId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "삭제 완료!");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @PutMapping("/{postId}/likes")
    public ResponseEntity<Map<String, Object>> likePost(
            @PathVariable Long postId,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = userService.getUserBySession(sessionId).getId();
        logger.info("sessionId : {}, userId : {}, postId : {}",sessionId, userId, postId);
        int likes = postService.increaseLikePost(postId, userId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "좋아요 완료!");
        response.put("likes", likes);
        return ResponseEntity.ok(response);
    }

}
