package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequiredArgsConstructor
public class MainController {
    
    private final UserService userService;
    
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/feed")
    public String feed() {
        return "redirect:/posts/feed";
    }
    
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute User user, 
                              BindingResult result, 
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        
        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", 
                "Регистрация прошла успешно! Теперь вы можете войти в систему.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            result.rejectValue("username", "error.user", e.getMessage());
            return "auth/register";
        }
    }
}