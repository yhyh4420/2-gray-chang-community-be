package kakaotech.communityBE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostUpdateDto {
    private String title;
    private String content;
    private String image;
}
