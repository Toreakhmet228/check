package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.IOException;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("user", currentUser);
        model.addAttribute("currentUser", currentUser);
        return "user/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("user") User user,
                                BindingResult result,
                                @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
                                @AuthenticationPrincipal User currentUser,
                                RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении профиля: " + result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/profile";
        }

        try {
            user.setId(currentUser.getId()); // Ensure updating the current user
            userService.updateProfile(user, avatarFile);
            redirectAttributes.addFlashAttribute("success", "Профиль успешно обновлен!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке аватара: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении профиля: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    // Add other user related methods here if needed (e.g., viewing other users' profiles)
    @GetMapping("/{username}")
    public String viewOtherUserProfile(@PathVariable String username, @AuthenticationPrincipal User currentUser, Model model) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Prevent viewing other users' profiles if not friends or not admin (optional logic)
        // if (!currentUser.isAdmin() && !userService.areFriends(currentUser, user) && !currentUser.getUsername().equals(username)) {
        //     throw new RuntimeException("You   do not have permission to view this profile.");
        // }

        model.addAttribute("user", user);
        model.addAttribute("currentUser", currentUser);
        // You might want to add friendship status, posts, etc. here
        return "user/profile"; // Reuse the same profile template
    }

} 