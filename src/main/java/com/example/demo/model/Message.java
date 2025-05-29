package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Сообщение не может быть пустым")
    @Size(max = 1000, message = "Сообщение не может быть длиннее 1000 символов")
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonBackReference(value = "sentMessages")
    private User sender;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = true)  // Делаем receiver опциональным для общего чата
    @JsonBackReference(value = "receivedMessages")
    private User receiver;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private boolean isRead = false;
    
    // Является ли сообщение для общего чата
    @Column(name = "is_global")
    private boolean isGlobal = false;
    
    public boolean isSentBy(User user) {
        return sender != null && sender.getId().equals(user.getId());
    }
    
    public boolean isReceivedBy(User user) {
        return receiver != null && receiver.getId().equals(user.getId());
    }

    public boolean sentBy(User user) {
        return sender != null && sender.getId().equals(user.getId());
    }

    public Message() {
        this.createdAt = LocalDateTime.now();
    }
} 