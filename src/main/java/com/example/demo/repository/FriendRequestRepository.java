package com.example.demo.repository;

import com.example.demo.model.FriendRequest;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    
    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequest.Status status);
    
    List<FriendRequest> findBySenderAndStatus(User sender, FriendRequest.Status status);
    
    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);
    
    Optional<FriendRequest> findBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequest.Status status);
    
    @Query("SELECT fr FROM FriendRequest fr WHERE " +
           "(fr.sender = :user1 AND fr.receiver = :user2) OR " +
           "(fr.sender = :user2 AND fr.receiver = :user1)")
    Optional<FriendRequest> findBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
    
    boolean existsBySenderAndReceiver(User sender, User receiver);
    
    long countByReceiverAndStatus(User receiver, FriendRequest.Status status);
    
    @Query("SELECT CASE " +
           "WHEN EXISTS(SELECT 1 FROM FriendRequest fr WHERE fr.sender = :sender AND fr.receiver = :receiver AND fr.status = 'PENDING') THEN 'SENT' " +
           "WHEN EXISTS(SELECT 1 FROM FriendRequest fr WHERE fr.sender = :receiver AND fr.receiver = :sender AND fr.status = 'PENDING') THEN 'RECEIVED' " +
           "WHEN EXISTS(SELECT 1 FROM FriendRequest fr WHERE ((fr.sender = :sender AND fr.receiver = :receiver) OR (fr.sender = :receiver AND fr.receiver = :sender)) AND fr.status = 'ACCEPTED') THEN 'FRIENDS' " +
           "ELSE 'NONE' END")
    String getFriendRequestStatus(@Param("sender") User sender, @Param("receiver") User receiver);
} 