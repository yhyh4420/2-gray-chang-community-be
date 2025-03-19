package kakaotech.communityBE.repository;

import kakaotech.communityBE.entity.Comment;
import kakaotech.communityBE.entity.Posts;
import org.hibernate.annotations.Comments;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPosts(Posts posts);

    @Query("select c from Comment c left join fetch c.user where c.posts = :posts")
    List<Comment> findAllByPostsFetch(@Param("posts") Posts posts);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT c FROM Comment c WHERE c.posts = :posts")
    List<Comment> findAllByPostsEntityGraph(@Param("posts") Posts posts);
}
