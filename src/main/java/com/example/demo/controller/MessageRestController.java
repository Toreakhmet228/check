package com.example.demo.controller;

import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.service.MessageService;
import com.example.demo.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageRestController {
    private final MessageService messageService;
    private final UserService userService;

    @GetMapping("/conversation/{userId}")
    public List<Message> getConversation(@PathVariable Long userId, @AuthenticationPrincipal User currentUser) {
        User friend = userService.findById(userId).orElseThrow();
        return messageService.getConversation(currentUser, friend);
    }

    @PostMapping("/send/{userId}")
    public Message sendMessage(@PathVariable Long userId, @RequestBody MessageDTO messageDto, @AuthenticationPrincipal User currentUser) {
        User receiver = userService.findById(userId).orElseThrow();
        Message message = new Message();
        message.setContent(messageDto.getContent());
        return messageService.sendMessage(message, currentUser, receiver);
    }

    @Data
    public static class MessageDTO {
        private String content;
    }
} 