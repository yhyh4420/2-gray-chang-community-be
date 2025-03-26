package kakaotech.communityBE.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 320)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false, length = 10)
    private String nickname;

    @Column @Lob
    private String profileImage;

    @Column(nullable = false)
    private boolean isResigned = false;
}
