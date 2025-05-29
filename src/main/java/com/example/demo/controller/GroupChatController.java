package com.example.demo.controller;

import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.service.MessageService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/groupchat")
@RequiredArgsConstructor
public class GroupChatController {

    private final MessageService messageService;
    private final UserService userService;
    
    @GetMapping
    public String showGroupChat(@AuthenticationPrincipal User currentUser, Model model) {
        // Получаем последние 50 сообщений для общего чата
        List<Message> messages = messageService.getLatestGlobalMessages(50);
        
        model.addAttribute("messages", messages);
        model.addAttribute("currentUser", currentUser);
        
        return "chat/groupchat";
    }
    
    @PostMapping
    public String sendMessage(@RequestParam("content") String content,
                             @AuthenticationPrincipal User currentUser,
                             RedirectAttributes redirectAttributes) {
        try {
            // Создаем и сохраняем сообщение в базе данных
            messageService.sendMessage(currentUser, content);
            return "redirect:/groupchat";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при отправке сообщения: " + e.getMessage());
            return "redirect:/groupchat";
        }
    }
}