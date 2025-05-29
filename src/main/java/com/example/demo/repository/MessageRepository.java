package com.example.demo.repository;

import com.example.demo.model.Message;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender = :user1 AND m.receiver = :user2) OR " +
           "(m.sender = :user2 AND m.receiver = :user1) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("user1") User user1, @Param("user2") User user2);
    
    @Query("SELECT DISTINCT m.receiver FROM Message m WHERE m.sender = :user")
    List<User> findReceiversBySender(@Param("user") User user);

    @Query("SELECT DISTINCT m.sender FROM Message m WHERE m.receiver = :user")
    List<User> findSendersByReceiver(@Param("user") User user);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = :user AND m.isRead = false")
    long countUnreadMessages(@Param("user") User user);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE " +
           "m.receiver = :user AND m.sender = :sender AND m.isRead = false")
    long countUnreadMessagesFromSender(@Param("user") User user, @Param("sender") User sender);
    
    List<Message> findByReceiverAndIsReadFalse(User receiver);
    
    @Query("SELECT m FROM Message m WHERE m.isGlobal = true ORDER BY m.createdAt ASC")
    List<Message> findGlobalMessages();
    
    @Query("SELECT m FROM Message m WHERE m.isGlobal = true ORDER BY m.createdAt ASC")
    List<Message> findAllGlobalMessages();
    
    @Query(value = "SELECT * FROM messages m WHERE m.is_global = true ORDER BY m.created_at DESC LIMIT :limit", nativeQuery = true)
    List<Message> findTopGlobalMessagesByOrderByCreatedAtDesc(@Param("limit") int limit);
} 