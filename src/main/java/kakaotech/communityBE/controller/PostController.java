package kakaotech.communityBE.controller;

import jakarta.servlet.http.HttpSession;
import kakaotech.communityBE.dto.PostEditDto;
import kakaotech.communityBE.dto.PostsDto;
import kakaotech.communityBE.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<List<PostsDto>> getAllPost() {
        List<PostsDto> postsDtoList = postService.getPosts();
        return ResponseEntity.ok(postsDtoList);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostsDto> getPost(@PathVariable Long postId) {
        PostsDto postsDto = postService.getPost(postId);
        //todo: Comment List로 받아오는 것도 여기서 해야함(아마 메서드명 getAllComment(postId)?)
        return ResponseEntity.status(HttpStatus.OK).body(postsDto);
    }

    @PostMapping
    public ResponseEntity<PostsDto> createPost(@RequestBody PostsDto postsDto, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        PostsDto post = postService.createPost(userId, postsDto.getTitle(), postsDto.getContent(), postsDto.getImage());
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PutMapping("/{postId}")
    public ResponseEntity updatePost(@PathVariable Long postId, @RequestBody PostEditDto editDto, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        PostsDto post = postService.editPost(userId, postId, editDto);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "수정 성공!");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity deletePost(@PathVariable Long postId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        postService.deletePost(userId, postId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "삭제 완료!");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @PutMapping("/{postId}/likes")
    public ResponseEntity likePost(@PathVariable Long postId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        int likes = postService.increaseLikePost(userId, postId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "좋아요 완료!");
        response.put("likes", likes);

        return ResponseEntity.ok(response);
    }


    /*
    todo : 게시글 수정, 삭제, 좋아요
     */
}
