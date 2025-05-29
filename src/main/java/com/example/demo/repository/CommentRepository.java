package com.example.demo.repository;

import com.example.demo.model.Comment;
import com.example.demo.model.Post;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);
    
    List<Comment> findByAuthorOrderByCreatedAtDesc(User author);
    
    long countByPost(Post post);
    
    long countByAuthor(User author);
} 