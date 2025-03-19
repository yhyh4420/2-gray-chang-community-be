package kakaotech.communityBE.repository;

import jakarta.persistence.EntityManager;
import kakaotech.communityBE.controller.PostController;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private EntityManager em;

    private static final Logger log = LoggerFactory.getLogger(PostRepositoryTest.class);
    private Long postId;

    @BeforeEach
    void setUp() {
        em.clear();
        User user = new User();
        user.setNickname("nickname");
        user.setEmail("email");
        user.setPassword("password");
        em.persist(user);

        Posts posts = new Posts();
        posts.setUser(user);
        posts.setTitle("title");
        posts.setContent("content");
        em.persist(posts);
        postId = posts.getId();

        for (int i = 0; i < 10; i++) {
            Comment comment = new Comment();
            comment.setPosts(posts);
            comment.setUser(user);
            comment.setComment("comment" + i);
            em.persist(comment);
        }

        em.flush();
        em.clear();
    }

    @Test
    void fetchjoin없는_일반findAll() {
        //n+1문제 발생
        System.out.println("----------------------");
        List<Posts> posts = postRepository.findAll();
        for (Posts post : posts) {
            System.out.println(post.getTitle());

            for (Comment comment : post.getComments()) {
                System.out.print(comment.getId() + "  ");
            }
        }
    }

    @Test
    void fetchjoin_findAll() {
        //n+1문제 발생
        System.out.println("----------------------");
        List<Posts> posts = postRepository.findAllFetch();
        for (Posts post : posts) {
            System.out.println(post.getTitle());

            for (Comment comment : post.getComments()) {
                System.out.print(comment.getId() + "  ");
            }
        }
    }

    @Test
    void 일반_findById(){
        System.out.println("----------------------");
        // n+1 문제 발생
        Posts post = postRepository.findById(postId).get();
        for (Comment comment : post.getComments()) {
            System.out.print(comment.getId() + " ");
        }
        System.out.println();
    }

    @Test
    void fetchJoin_findbyId(){
        System.out.println("----------------------");
        // n+1 문제 발생
        Posts post = postRepository.findbyIdFetch(postId).get();
        for (Comment comment : post.getComments()) {
            System.out.print(comment.getId() + " ");
        }
        System.out.println();
    }
}