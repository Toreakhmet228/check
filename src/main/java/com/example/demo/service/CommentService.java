package com.example.demo.service;

import com.example.demo.model.Comment;
import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    
    private final CommentRepository commentRepository;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public Comment createComment(Comment comment, User author, Post post) {
        comment.setAuthor(author);
        comment.setPost(post);
        return commentRepository.save(comment);
    }
    
    /**
     * Принудительное добавление комментария, которое игнорирует ошибки конкурентного доступа
     * и повторяет попытки сохранения до успешного завершения
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Comment forceCreateComment(Comment comment, User author, Post post) {
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());
        
        Comment savedComment = null;
        int maxAttempts = 5;
        int attempt = 0;
        
        while (savedComment == null && attempt < maxAttempts) {
            attempt++;
            try {
                // Создаем новый экземпляр комментария при каждой попытке
                Comment newComment = new Comment();
                newComment.setContent(comment.getContent());
                newComment.setAuthor(author);
                newComment.setPost(post);
                newComment.setCreatedAt(LocalDateTime.now());
                
                savedComment = commentRepository.save(newComment);
            } catch (Exception e) {
                // Пауза перед следующей попыткой
                try {
                    Thread.sleep(100 * attempt); // Увеличиваем время ожидания с каждой попыткой
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Прерывание во время ожидания повторной попытки сохранения", ie);
                }
            }
        }
        
        if (savedComment == null) {
            // Создаем совершенно новый комментарий с уникальным временем, если все попытки не удались
            Comment finalComment = new Comment();
            finalComment.setContent(comment.getContent() + " (Force added)");
            finalComment.setAuthor(author);
            finalComment.setPost(post);
            finalComment.setCreatedAt(LocalDateTime.now().plusSeconds(1)); // Увеличиваем время на секунду
            
            return commentRepository.save(finalComment);
        }
        
        return savedComment;
    }
    
    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }
    
    public List<Comment> getCommentsByPost(Post post) {
        return commentRepository.findByPostOrderByCreatedAtAsc(post);
    }
    
    public List<Comment> getCommentsByAuthor(User author) {
        return commentRepository.findByAuthorOrderByCreatedAtDesc(author);
    }
    
    public Comment updateComment(Comment comment, User currentUser) {
        if (!comment.isAuthoredBy(currentUser) && !currentUser.isAdmin()) {
            throw new RuntimeException("Нет прав на редактирование этого комментария");
        }
        return commentRepository.save(comment);
    }
    
    public void deleteComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Комментарий не найден"));
        
        if (!comment.isAuthoredBy(currentUser) && !currentUser.isAdmin()) {
            throw new RuntimeException("Нет прав на удаление этого комментария");
        }
        
        commentRepository.delete(comment);
    }
    
    public long getCommentCountByPost(Post post) {
        return commentRepository.countByPost(post);
    }
    
    public long getCommentCountByAuthor(User author) {
        return commentRepository.countByAuthor(author);
    }
} 