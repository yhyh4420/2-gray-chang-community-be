package kakaotech.communityBE.service;

import kakaotech.communityBE.dto.CommentDto;
import kakaotech.communityBE.dto.PostsDto;
import kakaotech.communityBE.entity.Comment;
import kakaotech.communityBE.entity.Posts;
import kakaotech.communityBE.entity.User;
import kakaotech.communityBE.repository.CommentRepository;
import kakaotech.communityBE.repository.PostRepository;
import kakaotech.communityBE.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final Logger logger = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    private User getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> {
                    logger.warn("유저 없음 : {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "유저 정보를 찾을 수 없습니다. 로그인 정보를 확인하세요");
                });
        return user;
    }

    private Posts getPost(Long postId) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(()->{
                    logger.warn("게시글 없음 : {}", postId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
                });
        return post;
    }

    @Transactional
    public CommentDto createComment(Long userId, Long postId, String content) {
        User user = getUser(userId);
        Posts post = getPost(postId);
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPosts(post);
        comment.setComment(content);
        commentRepository.save(comment);
        return new CommentDto(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getAllComments(Long postId) {
        Posts post = getPost(postId);
        List<Comment> comments = commentRepository.findAllByPostsFetch(post);
        List<CommentDto> commentDtos = comments.stream()
                .map(CommentDto::new)
                .toList();
        return commentDtos;
    }

    private Comment getCommentbyId(Long commentId){
        Comment comment = commentRepository.findById(commentId).orElseThrow(()->{
            logger.warn("comment id 조회 안됨 : {}", commentId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다");
        });
        return comment;
    }

    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, String content) {
        Comment comment = getCommentbyId(commentId);
        if (!comment.getUser().getId().equals(userId)) {
            logger.warn("작성자 아님");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "작성자만 게시글을 수정할 수 있습니다");
        }
        comment.setComment(content);
        return new CommentDto(comment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = getCommentbyId(commentId);
        if (!comment.getUser().getId().equals(userId)) {
            logger.warn("작성자 아님. 작성자 : {}, 요청자 : {}", comment.getUser().getId(), userId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "작성자만 댓글을 삭제할 수 있습니다.");
        }else {
            commentRepository.delete(comment);
        }
    }
}
