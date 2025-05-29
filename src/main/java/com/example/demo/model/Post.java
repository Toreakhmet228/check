package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
@Data
@EqualsAndHashCode(exclude = {"comments", "likedBy"})
@ToString(exclude = {"comments", "likedBy"})
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Содержание поста не может быть пустым")
    @Size(max = 2000, message = "Пост не может быть длиннее 2000 символов")
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private String imageUrl;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();
    
    private int likesCount = 0;
    
    // Особое выделение для постов администраторов
    private boolean pinned = false;
    private boolean highlighted = false;
    
    // Связь many-to-many для лайков
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "post_likes",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedBy = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean isAuthoredBy(User user) {
        return author != null && author.getId().equals(user.getId());
    }
    
    public boolean isAdminPost() {
        return author != null && author.isAdmin();
    }
    
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }
    
    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }
    
    // Вспомогательные методы для лайков
    public void addLike(User user) {
        if (likedBy.add(user)) {
            likesCount++;
        }
    }
    
    public void removeLike(User user) {
        if (likedBy.remove(user)) {
            likesCount--;
        }
    }
    
    public boolean isLikedBy(User user) {
        return likedBy.contains(user);
    }
} 