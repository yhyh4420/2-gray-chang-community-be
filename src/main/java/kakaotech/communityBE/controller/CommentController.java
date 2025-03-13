package kakaotech.communityBE.controller;

import kakaotech.communityBE.dto.CommentDto;
import kakaotech.communityBE.entity.Comment;
import kakaotech.communityBE.repository.SessionStorage;
import kakaotech.communityBE.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.h2.engine.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/comment")
@CrossOrigin(origins = "http://localhost:5500")
@RequiredArgsConstructor
public class CommentController {

    Logger logger = LoggerFactory.getLogger(CommentController.class);
    private final CommentService commentService;
    private final SessionStorage sessionStorage;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @RequestParam("content") String content,
            @RequestParam("postId") Long postId,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = (Long) sessionStorage.getUserId(sessionId);
        logger.info("userId : {}, content : {}, postId : {}", userId, content, postId);
        CommentDto comment = commentService.createComment(userId, postId, content);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> updateComment(
            @PathVariable Long commentId,
            @RequestParam("content") String content,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = (Long) sessionStorage.getUserId(sessionId);
        logger.info("userId : {}, content : {}, commentId : {}", userId, content, commentId);
        CommentDto commentDto = commentService.updateComment(userId, commentId, content);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "수정 성공!");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}
