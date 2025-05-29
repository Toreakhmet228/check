package com.example.demo.controller;

import com.example.demo.model.Comment;
import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.service.CommentService;
import com.example.demo.service.PostService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    
    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;
    
    private static final String UPLOAD_DIR = "src/main/resources/static/images/uploads/";
    
    @PostMapping("/add")
public String addPost(@Valid @ModelAttribute("post") Post post,
                      BindingResult result,
                      @RequestParam(value = "media", required = false) MultipartFile media,
                      @RequestParam(value = "title", required = false) String title,
                      @AuthenticationPrincipal User user,
                      RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
        redirectAttributes.addFlashAttribute("error", result.getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("; ")));
        return "redirect:/profile";
    }
    try {
        if (title != null && !title.trim().isEmpty()) {
            post.setContent("<h3>" + title + "</h3>" + post.getContent());
        }
        if (media != null && !media.isEmpty()) {
            String fileName = saveImage(media); // Переиспользуем метод saveImage
            post.setImageUrl("/images/uploads/" + fileName);
        }
        postService.createPost(post, user);
        redirectAttributes.addFlashAttribute("success", "Пост создан!");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
    }
    return "redirect:/profile";
}
    // Создаем директорию для загрузки изображений при инициализации контроллера
    @PostConstruct
    public void init() {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                System.out.println("Создана директория для загрузки изображений: " + UPLOAD_DIR);
            } else {
                System.out.println("Не удалось создать директорию для загрузки изображений: " + UPLOAD_DIR);
            }
        }
    }
    
    @GetMapping("/feed")
    @Transactional(readOnly = true)
    public String feed(@RequestParam(defaultValue = "0") int page,
                      @AuthenticationPrincipal User currentUser,
                      Model model) {
        Page<Post> posts = postService.getFeedForUser(currentUser, page, 10);
        long friendsCount = userService.getFriendsCount(currentUser.getId());
        long postsCount = userService.getPostsCount(currentUser.getId());
        
        // Создаем Map с количеством комментариев для каждого поста
        Map<Long, Long> commentsCount = new HashMap<>();
        for (Post post : posts.getContent()) {
            commentsCount.put(post.getId(), (long) post.getComments().size());
        }
        
        model.addAttribute("posts", posts);
        model.addAttribute("newPost", new Post());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("friendsCount", friendsCount);
        model.addAttribute("postsCount", postsCount);
        model.addAttribute("commentsCount", commentsCount);
        
        return "post/feed";
    }
    
    @PostMapping("/create")
    public String createPost(@Valid @ModelAttribute("newPost") Post post,
                             BindingResult result,
                             @RequestParam(value = "image", required = false) MultipartFile image,
                             @RequestParam(value = "title", required = false) String title,
                             @AuthenticationPrincipal User user,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", result.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining("; ")));
            return "redirect:/posts/feed";
        }
        try {
            if (title != null && !title.trim().isEmpty()) {
                post.setContent("<h3>" + title + "</h3>" + post.getContent());
            }
            if (image != null && !image.isEmpty()) {
                String fileName = saveImage(image);
                post.setImageUrl("/images/uploads/" + fileName);
            }
            postService.createPost(post, user);
            redirectAttributes.addFlashAttribute("success", "Пост создан!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/posts/feed";
    }
    
    private String saveImage(MultipartFile image) throws IOException {
        // Создаем директорию, если она не существует
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            if (!uploadDir.mkdirs()) {
                throw new IOException("Не удалось создать директорию для загрузки: " + UPLOAD_DIR);
            }
        }
        
        // Генерируем уникальное имя файла
        String originalFilename = image.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        
        // Сохраняем файл
        Files.write(filePath, image.getBytes());
        
        return fileName;
    }
    
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String viewPost(@PathVariable Long id,
                          @AuthenticationPrincipal User currentUser,
                          Model model) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new RuntimeException("Пост не найден"));
        
        model.addAttribute("post", post);
        model.addAttribute("comments", commentService.getCommentsByPost(post));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("newComment", new Comment());
        
        return "post/view";
    }
    
    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id,
                           @Valid @ModelAttribute("newComment") Comment comment,
                           BindingResult result,
                           @RequestParam(value = "force", defaultValue = "false") boolean force,
                           @AuthenticationPrincipal User currentUser,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении комментария");
            return "redirect:/posts/" + id;
        }
        
        try {
            Post post = postService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Пост не найден"));
            
            commentService.forceCreateComment(comment, currentUser, post);
            redirectAttributes.addFlashAttribute("success", "Комментарий добавлен!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении комментария: " + e.getMessage());
        }
        
        return "redirect:/posts/" + id;
    }
    
    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id,
                           @AuthenticationPrincipal User currentUser,
                           RedirectAttributes redirectAttributes) {
        try {
            postService.deletePost(id, currentUser);
            redirectAttributes.addFlashAttribute("success", "Пост удален!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении поста: " + e.getMessage());
        }
        
        return "redirect:/posts/feed";
    }
    
    // Методы для администраторов
    @PostMapping("/{id}/pin")
    public String pinPost(@PathVariable Long id,
                         @AuthenticationPrincipal User currentUser,
                         RedirectAttributes redirectAttributes) {
        try {
            postService.pinPost(id, currentUser);
            redirectAttributes.addFlashAttribute("success", "Пост закреплен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/posts/" + id;
    }
    
    @PostMapping("/{id}/unpin")
    public String unpinPost(@PathVariable Long id,
                           @AuthenticationPrincipal User currentUser,
                           RedirectAttributes redirectAttributes) {
        try {
            postService.unpinPost(id, currentUser);
            redirectAttributes.addFlashAttribute("success", "Пост откреплен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/posts/" + id;
    }
} 