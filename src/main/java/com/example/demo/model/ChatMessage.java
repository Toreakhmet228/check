package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    public enum MessageType {
        CHAT, JOIN, LEAVE, TYPING, STOP_TYPING, SIGNAL
    }
    
    private MessageType type;
    private String content;
    private String sender;
    private String senderName;
    private String receiver;
    private LocalDateTime timestamp;
    private boolean isRead;
    private boolean isAdmin;
    
    public ChatMessage(MessageType type, String sender, String senderName) {
        this.type = type;
        this.sender = sender;
        this.senderName = senderName;
        this.timestamp = LocalDateTime.now();
    }
} 