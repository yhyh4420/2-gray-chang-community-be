package kakaotech.communityBE.repository;

import kakaotech.communityBE.entity.Comment;
import kakaotech.communityBE.entity.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    public List<Comment> findByPost(Posts posts);
}
