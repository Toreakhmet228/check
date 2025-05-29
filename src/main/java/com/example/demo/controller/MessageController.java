package com.example.demo.controller;

import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.service.MessageService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    private final UserService userService;
    
    @GetMapping
    public String messages(@AuthenticationPrincipal User currentUser, Model model) {
        List<User> conversationPartners = messageService.getConversationPartners(currentUser);
        long unreadCount = messageService.getUnreadMessageCount(currentUser);
        
        model.addAttribute("conversationPartners", conversationPartners);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("currentUser", currentUser);
        
        return "message/list";
    }
    
    @GetMapping("/conversation/{userId}")
    public String conversation(@PathVariable Long userId,
                              @AuthenticationPrincipal User currentUser,
                              Model model) {
        User friend = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        List<Message> conversation = messageService.getConversation(currentUser, friend);
        
        // Отмечаем сообщения как прочитанные
        messageService.markMessagesAsRead(friend, currentUser);
        
        model.addAttribute("friend", friend);
        model.addAttribute("conversation", conversation);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("newMessage", new Message());
        
        return "message/conversation";
    }
    
    @PostMapping("/send/{userId}")
    public String sendMessage(@PathVariable Long userId,
                             @Valid @ModelAttribute("newMessage") Message message,
                             BindingResult result,
                             @AuthenticationPrincipal User currentUser,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при отправке сообщения");
            return "redirect:/messages/conversation/" + userId;
        }
        
        try {
            User receiver = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            messageService.sendMessage(message, currentUser, receiver);
            redirectAttributes.addFlashAttribute("success", "Сообщение отправлено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/messages/conversation/" + userId;
    }
} 