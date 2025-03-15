package kakaotech.communityBE.controller;

import jakarta.servlet.http.HttpSession;
import kakaotech.communityBE.dto.CommentDto;
import kakaotech.communityBE.dto.PostUpdateDto;
import kakaotech.communityBE.dto.PostsDto;
import kakaotech.communityBE.repository.SessionStorage;
import kakaotech.communityBE.service.CommentService;
import kakaotech.communityBE.service.PostService;
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
@CrossOrigin(origins = "http://localhost:5500")
@RequiredArgsConstructor
public class PostController {

    Logger logger = LoggerFactory.getLogger(PostController.class);

    private final PostService postService;
    private final SessionStorage sessionStorage;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/postImage/";

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
    public ResponseEntity<PostsDto> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = (Long) sessionStorage.getUserId(sessionId);
        System.out.println("현재 세션 아이디 : " + userId);

        logger.info("받은 값 : {}, {}, {}", title, content, image);
        String imagePath = null;
        if (image != null && !image.isEmpty()) {
            try {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs(); // 폴더가 없으면 생성
                }

                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                File destFile = new File(UPLOAD_DIR + fileName);
                logger.info("업로드 디렉터리 존재 여부: " + new File(UPLOAD_DIR).exists());
                logger.info("파일 경로: " + destFile.getAbsolutePath());
                logger.info("파일 크기: " + image.getSize() + " bytes");
                image.transferTo(destFile);
                // 이미지 URL 저장
                imagePath = "/uploads/postImage/" + fileName; // 저장된 이미지 경로
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }
        PostsDto post = postService.createPost(userId, title, content, imagePath);
        return ResponseEntity.ok(post);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> updatePost(
            @PathVariable Long postId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @CookieValue(value="sessionId", required = false) String sessionId) {
        Long userId = (Long) sessionStorage.getUserId(sessionId);

        PostsDto existingPost = postService.getPost(postId);
        String imagePath = existingPost.getImage(); // 기존 이미지 유지
        logger.info(imagePath);
        if (imagePath != null && !imagePath.isEmpty()) {
            File oldFile = new File(imagePath.substring(1));
            logger.info("삭제할 파일 존재 여부: " + oldFile.exists());
            try{
                oldFile.delete();
            } catch (Exception e){
                logger.warn("기존 이미지 삭제 실패 : " + e.getMessage());
            }
        }
        if (image != null && !image.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                File destFile = new File(UPLOAD_DIR + fileName);
                image.transferTo(destFile);
                imagePath = "/uploads/postImage/" + fileName;
                PostUpdateDto updateDto = new PostUpdateDto(title, content, imagePath);
                PostsDto post = postService.updatePost(postId, userId, updateDto);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "이미지 저장 실패"));
            }
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("message", "수정 성공!"));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(
            @PathVariable Long postId,
            @CookieValue(value="sessionId", required = false) String sessionId) {
        Long userId = (Long) sessionStorage.getUserId(sessionId);
        postService.deletePost(userId, postId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "삭제 완료!");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @PutMapping("/{postId}/likes")
    public ResponseEntity<Map<String, Object>> likePost(
            @PathVariable Long postId,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = (Long) sessionStorage.getUserId(sessionId);
        logger.info("sessionId : {}, userId : {}, postId : {}",sessionId, userId, postId);
        int likes = postService.increaseLikePost(postId, userId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "좋아요 완료!");
        response.put("likes", likes);
        return ResponseEntity.ok(response);
    }

}
