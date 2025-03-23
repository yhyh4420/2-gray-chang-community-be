package kakaotech.communityBE.dto;

import kakaotech.communityBE.entity.Comment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CommentDto {
    private Long id;
    private String comment;
    private Long postId;
    private Long userId;
    private String userNickname;
    private LocalDateTime commentDate;

    // Entity -> DTO 변환
    public CommentDto(Comment comment) {
        this.id = comment.getId();
        this.comment = comment.getComment();
        this.postId = comment.getPosts().getId();
        this.userId = comment.getUser().getId();
        this.userNickname = comment.getUser().getNickname();
        this.commentDate = comment.getCommentDate();
    }
}
