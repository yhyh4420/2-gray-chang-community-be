package kakaotech.communityBE.repository;

import jakarta.persistence.EntityManager;
import kakaotech.communityBE.entity.Comment;
import kakaotech.communityBE.entity.Posts;
import kakaotech.communityBE.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Random;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostRepositoryQueryTest {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private EntityManager em;

    private Long postId;
    private final StopWatch stopWatch = new StopWatch();
    private final Random random = new Random();

    @BeforeEach
    void setUp() {
        postId = postRepository.findAll().get(random.nextInt(1)).getId();
    }

    // todo : 쿼리 발행 개수 세기
    @Test
    void findAll_성능비교() {
        //n+1문제 발생
        System.out.println("----------------------");
        em.clear();
        stopWatch.start("일반 findAll");
        List<Posts> posts = postRepository.findAll();
        for (Posts post : posts) {
            for (Comment comment : post.getComments()) {
            }
        }
        stopWatch.stop();
        em.clear();
        System.out.println("----------------------");
        stopWatch.start("entityGraph findAll");
        List<Posts> postsEntithGraph = postRepository.findAllEntityGraph();
        for (Posts post : postsEntithGraph) {
            for (Comment comment : post.getComments()) {
            }
        }
        stopWatch.stop();
        em.clear();
        System.out.println("----------------------");
        stopWatch.start("fetch join findAll");
        List<Posts> postsfetch = postRepository.findAllFetch();
        for (Posts post : postsfetch) {
            for (Comment comment : post.getComments()) {
            }
        }
        stopWatch.stop();
        em.clear();

        System.out.println(stopWatch.prettyPrint());
    }

    /*
    ----------------------------------------------------------------
    Seconds       %       발행 쿼리 개수       Task name
    ----------------------------------------------------------------
    0.037745709   74%     1+5              일반 findAll
    0.010377709   20%     1                entityGraph findAll
    0.002919583   06%     1                fetch join findAll

    fetch join으로 post별 comment를 찾는 쿼리 수정 결과
    1. 쿼리 발행 숫자가 5+1에서 1개로 감소했다(N+1문제 해결)
    2. 일반 findAll보다 fetch join의 속도가 1,133% 향상되었다.
     */

    @Test
    void findById_성능비교(){
        System.out.println("----------------------");
        // n+1 문제 발생
        em.clear();
        stopWatch.start("일반 findById");
        Posts post = postRepository.findById(postId).isPresent() ? postRepository.findById(postId).get() : null;
        for (Comment comment : post.getComments()) {
            System.out.print(comment.getId() + " ");
        }
        System.out.println();
        stopWatch.stop();
        em.clear();
        System.out.println("----------------------");
        stopWatch.start("fetch join findById");
        Posts postfetch = postRepository.findbyIdFetch(postId).isPresent() ? postRepository.findbyIdFetch(postId).get() : null;
        for (Comment comment : postfetch.getComments()) {
            System.out.print(comment.getId() + " ");
        }
        System.out.println();
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
    }

    /*
    ----------------------------------------------------------------
    Seconds       %       발행 쿼리 개수       Task name
    ----------------------------------------------------------------
    0.005303667   44%     2             일반 findById
    0.006709583   56%     2             fetch join findById

    결과를 보면 일반 findbyId가 더 성능이 좋다. 생각해보면 당연한게, findbyId는 원래 한개의 엔티티만 조회한다.
    오히려 괜한 inner join 연산으로 성능이 떨어진 것이다.
    findByIdFetch는 테스트코드의 컴파일 에러를 방지하기 위해 로직은 남겨두되 실제 서비스로직에서는 사용하지 않을 예정
     */
}