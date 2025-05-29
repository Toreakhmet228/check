package com.example.demo.service;

import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final UserService userService;
    
    public Message sendMessage(Message message, User sender, User receiver) {
        // Удаляем проверку на друзей, чтобы позволить отправлять сообщения любому пользователю
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setGlobal(false);
        return messageRepository.save(message);
    }
    
    public List<Message> getConversation(User user1, User user2) {
        // Удаляем проверку на друзей, чтобы позволить просматривать переписку с любым пользователем
        return messageRepository.findConversation(user1, user2);
    }
    
    public List<User> getConversationPartners(User user) {
        List<User> sent = messageRepository.findReceiversBySender(user);
        List<User> received = messageRepository.findSendersByReceiver(user);
        Set<User> all = new HashSet<>(sent);
        all.addAll(received);
        return new ArrayList<>(all);
    }
    
    public void markMessagesAsRead(User sender, User receiver) {
        List<Message> unreadMessages = messageRepository.findConversation(sender, receiver);
        for (Message message : unreadMessages) {
            if (message.isReceivedBy(receiver) && !message.isRead()) {
                message.setRead(true);
                messageRepository.save(message);
            }
        }
    }
    
    public long getUnreadMessageCount(User user) {
        return messageRepository.countUnreadMessages(user);
    }
    
    public long getUnreadMessageCountFromSender(User user, User sender) {
        return messageRepository.countUnreadMessagesFromSender(user, sender);
    }
    
    public List<Message> getUnreadMessages(User user) {
        return messageRepository.findByReceiverAndIsReadFalse(user);
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public void sendMessage(User sender, String content) {
        Message message = new Message();
        message.setSender(sender);
        message.setContent(content);
        message.setGlobal(true);
        message.setRead(true); // Глобальные сообщения не имеют статуса "прочитано"
        messageRepository.save(message);
    }
    
    public List<Message> getGlobalMessages() {
        return messageRepository.findAllGlobalMessages();
    }

    public List<Message> getLatestGlobalMessages(int limit) {
        return messageRepository.findTopGlobalMessagesByOrderByCreatedAtDesc(limit);
    }
} 