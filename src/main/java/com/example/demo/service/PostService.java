package com.example.demo.service;

import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    
    private final PostRepository postRepository;
    
    public Post createPost(Post post, User author) {
        post.setAuthor(author);
        
        // Автоматически выделяем посты администраторов
        if (author.isAdmin()) {
            post.setHighlighted(true);
        }
        
        return postRepository.save(post);
    }
    
    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }
    
    public Page<Post> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findAllByOrderByPinnedDescCreatedAtDesc(pageable);
    }
    
    public Page<Post> getFeedForUser(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findFeedForUser(user, pageable);
    }
    
    public List<Post> getPostsByAuthor(User author) {
        return postRepository.findByAuthorOrderByCreatedAtDesc(author);
    }
    
    public List<Post> getPinnedPosts() {
        return postRepository.findByPinnedTrueOrderByCreatedAtDesc();
    }
    
    public List<Post> getHighlightedPosts() {
        return postRepository.findByHighlightedTrueOrderByCreatedAtDesc();
    }
    
    public Post updatePost(Post post, User currentUser) {
        if (!post.isAuthoredBy(currentUser) && !currentUser.isAdmin()) {
            throw new RuntimeException("Нет прав на редактирование этого поста");
        }
        return postRepository.save(post);
    }
    
    public void deletePost(Long postId, User currentUser) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            if (post.getAuthor().equals(currentUser) || currentUser.isAdmin()) {
                postRepository.delete(post);
            } else {
                throw new RuntimeException("Нет прав для удаления поста");
            }
        }
    }
    
    // Методы для администраторов
    public Post pinPost(Long postId, User admin) {
        if (!admin.isAdmin()) {
            throw new RuntimeException("Только администраторы могут закреплять посты");
        }
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Пост не найден"));
        
        post.setPinned(true);
        return postRepository.save(post);
    }
    
    public Post unpinPost(Long postId, User admin) {
        if (!admin.isAdmin()) {
            throw new RuntimeException("Только администраторы могут открепить посты");
        }
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Пост не найден"));
        
        post.setPinned(false);
        return postRepository.save(post);
    }
    
    public Post highlightPost(Long postId, User admin) {
        if (!admin.isAdmin()) {
            throw new RuntimeException("Только администраторы могут выделять посты");
        }
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Пост не найден"));
        
        post.setHighlighted(true);
        return postRepository.save(post);
    }
    
    public Post removeHighlight(Long postId, User admin) {
        if (!admin.isAdmin()) {
            throw new RuntimeException("Только администраторы могут убирать выделение постов");
        }
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Пост не найден"));
        
        post.setHighlighted(false);
        return postRepository.save(post);
    }
    
    public long getPostCountByAuthor(User author) {
        return postRepository.countByAuthor(author);
    }
    
    public long getCommentsCount(Long postId) {
        return postRepository.countCommentsByPostId(postId);
    }
    
    // Методы для лайков
    @Transactional
    public boolean toggleLike(Post post, User user) {
        if (post.isLikedBy(user)) {
            post.removeLike(user);
            postRepository.save(post);
            return false; // убрали лайк
        } else {
            post.addLike(user);
            postRepository.save(post);
            return true; // поставили лайк
        }
    }
    
    public boolean isLikedByUser(Post post, User user) {
        return post.isLikedBy(user);
    }
} 