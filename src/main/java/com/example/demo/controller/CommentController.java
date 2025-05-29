package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    @PostMapping("/delete/{id}")
    public String deleteComment(@PathVariable Long id,
                               @AuthenticationPrincipal User currentUser,
                               RedirectAttributes redirectAttributes) {
        try {
            // Получаем пост ID перед удалением для редиректа
            Long postId = commentService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Комментарий не найден"))
                    .getPost().getId();
            
            commentService.deleteComment(id, currentUser);
            redirectAttributes.addFlashAttribute("success", "Комментарий успешно удален");
            return "redirect:/posts/" + postId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении комментария: " + e.getMessage());
            return "redirect:/posts/feed";
        }
    }
} 