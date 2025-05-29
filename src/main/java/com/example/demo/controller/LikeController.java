package com.example.demo.controller;

import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {
    
    private final PostService postService;
    
    @PostMapping("/toggle/{postId}")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            Optional<Post> postOpt = postService.findById(postId);
            if (postOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Post post = postOpt.get();
            boolean isLiked = postService.toggleLike(post, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("liked", isLiked);
            response.put("likesCount", post.getLikesCount());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Ошибка при обработке лайка");
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/status/{postId}")
    public ResponseEntity<Map<String, Object>> getLikeStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            Optional<Post> postOpt = postService.findById(postId);
            if (postOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Post post = postOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("liked", post.isLikedBy(currentUser));
            response.put("likesCount", post.getLikesCount());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 