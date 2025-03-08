package kakaotech.communityBE.repository;

import kakaotech.communityBE.entity.Like;
import kakaotech.communityBE.entity.Posts;
import kakaotech.communityBE.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndPost(User user, Posts post);
}
