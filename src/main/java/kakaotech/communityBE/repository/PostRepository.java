package kakaotech.communityBE.repository;

import kakaotech.communityBE.entity.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PostRepository extends JpaRepository<Posts, Long> {

    @Modifying
    @Query("update Posts p set p.likes = p.likes+1 where p.id = :postId")
    int increaseLikes(@Param("postId") Long postId);

    @Query("select p from Posts p left join fetch p.comments")
    List<Posts> findAllFetch();

    @Query("select p from Posts p left join fetch p.comments where p.id = :postId")
    Optional<Posts> findbyIdFetch(@Param("postId") Long postId);
}
