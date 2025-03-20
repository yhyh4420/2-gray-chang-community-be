package kakaotech.communityBE.repository;

import jakarta.persistence.EntityManager;
import kakaotech.communityBE.entity.Comment;
import kakaotech.communityBE.entity.Posts;
import kakaotech.communityBE.entity.User;
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



    @BeforeEach
    public void setUp() {
        int size = postRepository.findAll().size();
        flagPost = postRepository.findAll().get(random.nextInt(size));
    }

    @Test
    void findAllbyPost() {
        System.out.println("---------------------");
        em.clear();
        stopWatch.start("일반 findAllbyPost");
        List<Comment> comments = commentRepository.findAllByPosts(flagPost);
        for (Comment comment : comments) {

        }
        stopWatch.stop();
        em.clear();

        System.out.println("---------------------");
        em.clear();
        stopWatch.start("fetch join findAllbyPost");
        List<Comment> commentsFetch = commentRepository.findAllByPostsFetch(flagPost);
        for (Comment comment : commentsFetch) {

        }
        stopWatch.stop();
        em.clear();
        System.out.println(stopWatch.prettyPrint());
    }

    /*
    ----------------------------------------
    Seconds       %       Task name
    ----------------------------------------
    0.057457459   67%     일반 findAllbyPost
    0.028715208   33%     fetch join findAllbyPost

    fetch join으로 post별 comment를 찾는 쿼리 수정 결과 기존 쿼리 대비 약 43% 성능 향상
     */
}