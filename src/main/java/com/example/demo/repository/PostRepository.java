package com.example.demo.repository;

import com.example.demo.model.Post;
import com.example.demo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);
    
    Page<Post> findAllByOrderByPinnedDescCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.author IN :friends ORDER BY p.pinned DESC, p.createdAt DESC")
    Page<Post> findPostsByFriends(@Param("friends") List<User> friends, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.author = :user OR p.author IN " +
           "(SELECT f FROM User u JOIN u.friends f WHERE u = :user) " +
           "ORDER BY p.pinned DESC, p.createdAt DESC")
    Page<Post> findFeedForUser(@Param("user") User user, Pageable pageable);
    
    List<Post> findByPinnedTrueOrderByCreatedAtDesc();
    
    List<Post> findByHighlightedTrueOrderByCreatedAtDesc();
    
    @Query("SELECT COUNT(p) FROM Post p WHERE p.author = :author")
    long countByAuthor(@Param("author") User author);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    long countCommentsByPostId(@Param("postId") Long postId);
} 