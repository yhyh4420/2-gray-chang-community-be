package kakaotech.communityBE.controller;

import kakaotech.communityBE.dto.CommentDto;
import kakaotech.communityBE.SessionStorage;
import kakaotech.communityBE.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("posts/{postId}/comment")
@CrossOrigin(origins = "http://localhost:5500")
@RequiredArgsConstructor
public class CommentController {

    Logger logger = LoggerFactory.getLogger(CommentController.class);
    private final CommentService commentService;
    private final SessionStorage sessionStorage;

    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable("postId") Long postId) {
        List<CommentDto> commentDtoList = commentService.getAllComments(postId);
        return ResponseEntity.ok(commentDtoList);
    }

    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @RequestBody Map<String, String> request,
            @PathVariable("postId") Long postId,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = sessionStorage.getUserId(sessionId);
        String content = request.get("content");
        logger.info("userId : {}, content : {}, postId : {}", userId, content, postId);
        CommentDto comment = commentService.createComment(userId, postId, content);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> updateComment(
            @PathVariable Long commentId,
            @RequestParam("content") String content,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = sessionStorage.getUserId(sessionId);
        logger.info("userId : {}, content : {}, commentId : {}", userId, content, commentId);
        CommentDto commentDto = commentService.updateComment(userId, commentId, content);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "수정 성공!");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @CookieValue(value = "sessionId", required = false) String sessionId){
        Long userId = sessionStorage.getUserId(sessionId);
        logger.info("userId : {}, postId : {}, commentId : {}", userId, postId, commentId);
        commentService.deleteComment(userId, commentId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "삭제 완료!");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}
