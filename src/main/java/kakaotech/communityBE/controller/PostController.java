package kakaotech.communityBE.controller;

import jakarta.servlet.http.HttpSession;
import kakaotech.communityBE.dto.CommentDto;
import kakaotech.communityBE.dto.PostUpdateDto;
import kakaotech.communityBE.dto.PostsDto;
import kakaotech.communityBE.service.CommentService;
import kakaotech.communityBE.service.PostService;
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
@CrossOrigin(origins = "http://localhost:5500")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    private static final String UPLOAD_DIR = "uploads/postImage/";

    public PostController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<PostsDto>> getAllPost() {
        List<PostsDto> postsDtoList = postService.getAllPosts();
        return ResponseEntity.ok(postsDtoList);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> getPost(@PathVariable Long postId) {
        PostsDto postsDto = postService.getPost(postId);
        List<CommentDto> comments = commentService.getAllComments(postId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "ok");
        response.put("post", postsDto);
        response.put("comments", comments);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<PostsDto> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        System.out.println("현재 세션 아이디 : " + userId);

        String imagePath = null;
        if (image != null && !image.isEmpty()) {
            try {
                // 이미지 저장 경로 설정
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs(); // 폴더가 없으면 생성
                }

                // 파일 저장
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                File destFile = new File(UPLOAD_DIR + fileName);
                image.transferTo(destFile);

                // 이미지 URL 저장
                imagePath = "/" + UPLOAD_DIR + fileName; // 저장된 이미지 경로
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }
        PostsDto post = postService.createPost(userId, title, content, imagePath);
        return ResponseEntity.ok(post);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> updatePost(@PathVariable Long postId, @RequestBody PostUpdateDto editDto, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        PostsDto post = postService.updatePost(userId, postId, editDto);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "수정 성공!");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable Long postId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        postService.deletePost(userId, postId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "삭제 완료!");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @PutMapping("/{postId}/likes")
    public ResponseEntity<Map<String, Object>> likePost(@PathVariable Long postId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        int likes = postService.increaseLikePost(userId, postId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "좋아요 완료!");
        response.put("likes", likes);

        return ResponseEntity.ok(response);
    }

}
