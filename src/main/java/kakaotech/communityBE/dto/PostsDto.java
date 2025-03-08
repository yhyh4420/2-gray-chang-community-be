package kakaotech.communityBE.dto;

import kakaotech.communityBE.entity.Posts;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
public class PostsDto {

    private Long id;
    private String title;
    private String content;
    private String image;
    private int likes;
    private int views;
    private LocalDateTime postDate;
    private List<CommentDto> comments;

    public PostsDto(Posts posts) {
        this.id = posts.getId();
        this.title = posts.getTitle();
        this.content = posts.getContent();
        this.image = posts.getImage();
        this.likes = posts.getLikes();
        this.views = posts.getViews();
        this.comments = posts.getComments().stream()
                .map(CommentDto::new)
                .collect(Collectors.toList());
    }

}
