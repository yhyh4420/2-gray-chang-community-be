package kakaotech.communityBE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class PostEditDto {
    private String title;
    private String content;
    private String image;
}
