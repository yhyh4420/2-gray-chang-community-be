package kakaotech.communityBE.controller;

import kakaotech.communityBE.dto.CommentDto;
import kakaotech.communityBE.dto.MessageResponseDto;
import kakaotech.communityBE.service.CommentService;
import kakaotech.communityBE.service.UserService;
import kakaotech.communityBE.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("posts/{postId}/comment")
@RequiredArgsConstructor
public class CommentController {

    private final UserService userService;
    private final CommentService commentService;
    Logger logger = LoggerFactory.getLogger(CommentController.class);

    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable("postId") Long postId) {
        List<CommentDto> commentDtoList = commentService.getAllComments(postId);
        return ResponseUtil.ok(commentDtoList);
    }

    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @RequestBody Map<String, String> request,
            @PathVariable("postId") Long postId,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = userService.getUserBySession(sessionId).getId();
        String content = request.get("content");
        logger.info("userId : {}, content : {}, postId : {}", userId, content, postId);
        CommentDto comment = commentService.createComment(userId, postId, content);
        return ResponseUtil.ok(comment);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<MessageResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestParam("content") String content,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = userService.getUserBySession(sessionId).getId();
        logger.info("userId : {}, content : {}, commentId : {}", userId, content, commentId);
        CommentDto comments = commentService.updateComment(userId, commentId, content);
        return ResponseUtil.noContent("수정 성공!");
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<MessageResponseDto> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        Long userId = userService.getUserBySession(sessionId).getId();
        logger.info("userId : {}, postId : {}, commentId : {}", userId, postId, commentId);
        commentService.deleteComment(userId, commentId);
        return ResponseUtil.noContent("삭제 완료");
    }
}
