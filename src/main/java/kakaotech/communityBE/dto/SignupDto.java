package kakaotech.communityBE.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupDto {
    private String email;
    private String password;
    private String nickname;
    private String profileImage;
}
