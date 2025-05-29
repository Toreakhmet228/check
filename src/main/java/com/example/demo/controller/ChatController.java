package com.example.demo.controller;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.service.MessageService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final UserService userService;
    
    @GetMapping
    public String getChatPage(Model model) {
        List<Message> messages = messageService.getAllMessages();
        model.addAttribute("messages", messages);
        return "chat/chat";
    }

    @PostMapping
    public String sendMessage(@RequestParam String content,
                              @AuthenticationPrincipal User sender) {
        messageService.sendMessage(sender, content);
        return "redirect:/chat";
    }
    
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Получаем аутентифицированного пользователя
        Authentication auth = (Authentication) headerAccessor.getUser();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User sender = (User) auth.getPrincipal();
            
            // Проверяем тип сообщения
            if (chatMessage.getType() == ChatMessage.MessageType.CHAT) {
                // Если указан получатель, это личное сообщение
                if (chatMessage.getReceiver() != null && !chatMessage.getReceiver().isEmpty()) {
            // Находим получателя
            User receiver = userService.findByUsername(chatMessage.getReceiver())
                    .orElse(null);
            
                    if (receiver != null) {
                Message message = new Message();
                message.setContent(chatMessage.getContent());
                message.setRead(false);
                messageService.sendMessage(message, sender, receiver);
                
                chatMessage.setSender(sender.getUsername());
                chatMessage.setSenderName(sender.getFullName());
                chatMessage.setTimestamp(LocalDateTime.now());
                
                // Отправляем сообщение получателю
                messagingTemplate.convertAndSendToUser(
                    receiver.getUsername(),
                    "/queue/messages",
                    chatMessage
                );
                
                // Отправляем подтверждение отправителю
                messagingTemplate.convertAndSendToUser(
                    sender.getUsername(),
                    "/queue/messages",
                    chatMessage
                );
                    }
                } else {
                    // Если получатель не указан, это сообщение в общий чат
                    Message message = new Message();
                    message.setContent(chatMessage.getContent());
                    message.setSender(sender);
                    message.setGlobal(true);
                    messageService.sendMessage(sender, chatMessage.getContent());
                    
                    chatMessage.setSender(sender.getUsername());
                    chatMessage.setSenderName(sender.getFullName());
                    chatMessage.setTimestamp(LocalDateTime.now());
                    chatMessage.setAdmin(sender.isAdmin());
                    
                    // Отправляем сообщение всем
                    messagingTemplate.convertAndSend("/topic/public", chatMessage);
                }
            }
        }
    }
    
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Добавляем пользователя в WebSocket сессию
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        
        // Уведомляем о подключении
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setTimestamp(LocalDateTime.now());
        
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }
    
    @MessageMapping("/chat.typing")
    public void userTyping(@Payload ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.TYPING);
        chatMessage.setTimestamp(LocalDateTime.now());
        
        // Отправляем уведомление о наборе текста получателю
        messagingTemplate.convertAndSendToUser(
            chatMessage.getReceiver(),
            "/queue/typing",
            chatMessage
        );
    }
    
    @MessageMapping("/chat.stopTyping")
    public void userStopTyping(@Payload ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.STOP_TYPING);
        chatMessage.setTimestamp(LocalDateTime.now());
        
        // Отправляем уведомление о прекращении набора текста
        messagingTemplate.convertAndSendToUser(
            chatMessage.getReceiver(),
            "/queue/typing",
            chatMessage
        );
    }
    
    @MessageMapping("/chat.signal")
    public void handleSignal(@Payload ChatMessage chatMessage) {
        // chatMessage.receiver - username получателя
        // chatMessage.content - SDP или ICE
        // chatMessage.type - SIGNAL
        messagingTemplate.convertAndSendToUser(
            chatMessage.getReceiver(),
            "/queue/signals",
            chatMessage
        );
    }

    @MessageMapping("/private-chat")
    public void handlePrivateChat(@Payload ChatMessage chatMessage) {
        // Найти отправителя и получателя по chatMessage.sender и chatMessage.receiver
        User sender = userService.findById(Long.valueOf(chatMessage.getSender())).orElse(null);
        User receiver = userService.findById(Long.valueOf(chatMessage.getReceiver())).orElse(null);
        if (sender == null || receiver == null) return;

        // Сохраняем сообщение в базу (если нужно)
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(chatMessage.getContent());
        message.setGlobal(false);
        message.setCreatedAt(java.time.LocalDateTime.now());
        messageService.sendMessage(message, sender, receiver);

        // Отправляем сообщение получателю
        messagingTemplate.convertAndSendToUser(
            receiver.getUsername(),
            "/queue/private-messages",
            chatMessage
        );
        // Отправляем подтверждение отправителю
        messagingTemplate.convertAndSendToUser(
            sender.getUsername(),
            "/queue/private-messages",
            chatMessage
        );
    }
} 