package kakaotech.communityBE.repository;

import jakarta.persistence.EntityManager;
import kakaotech.communityBE.entity.Comment;
import kakaotech.communityBE.entity.Posts;
import kakaotech.communityBE.entity.User;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Random;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentRepositoryQueryTest {

    @Autowired private CommentRepository commentRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private EntityManager em;

    private Posts flagPost;
    private final StopWatch stopWatch = new StopWatch();
    private final Random random = new Random();

    private Statistics statistics;



    @BeforeEach
    public void setUp() {
        flagPost = postRepository.findById(1L).orElse(null);
        Session session = em.unwrap(Session.class);
        statistics = session.getSessionFactory().getStatistics();
        statistics.clear();
        em.clear();
    }

    @Test
    void findAllbyPost() {
        System.out.println("---------------------");
        stopWatch.start("일반 findAllbyPost");
        List<Comment> comments = commentRepository.findAllByPosts(flagPost);
        for (Comment comment : comments) {
            String commentContent = comment.getUser().getNickname();
        }
        stopWatch.stop();
        System.out.println("Query Count : " + statistics.getQueryExecutionCount());
        statistics.clear();
        em.clear();

        System.out.println("---------------------");
        stopWatch.start("fetch join findAllbyPost");
        List<Comment> commentsFetch = commentRepository.findAllByPostsFetch(flagPost);
        for (Comment comment : commentsFetch) {
            String commentContent = comment.getUser().getNickname();
        }
        stopWatch.stop();
        System.out.println("Query Count : " + statistics.getQueryExecutionCount());
        statistics.clear();
        em.clear();
        System.out.println(stopWatch.prettyPrint());
    }
}
    /*
    ----------------------------------------------------------------
    Seconds       %       발행 쿼리 개수       Task name
    ----------------------------------------------------------------
    0.057457459   67%     10+1          일반 findAllbyPost
    0.028715208   33%     1           fetch join findAllbyPost

    fetch join으로 comment별 user를 찾는 쿼리 수정 결과
    1. 발행쿼리개수 감소 확인(N+1문제 해결)
    2. 속도 측면에서 기존 쿼리 대비 약 103% 성능 향상
     */
