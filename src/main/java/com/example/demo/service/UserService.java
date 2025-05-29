package com.example.demo.service;

import com.example.demo.model.FriendRequest;
import com.example.demo.model.User;
import com.example.demo.repository.FriendRequestRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final String UPLOAD_DIR = "src/main/resources/static/images/avatars/";

    // Create the upload directory on service initialization
    @PostConstruct
    public void init() {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                System.out.println("Created directory for avatar uploads: " + UPLOAD_DIR);
            } else {
                System.out.println("Failed to create directory for avatar uploads: " + UPLOAD_DIR);
            }
        }
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
    }
    
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> searchUsers(String query, Long currentUserId) {
        return userRepository.searchUsers(query, currentUserId);
    }
    
    public List<User> findPotentialFriends(Long userId) {
        return userRepository.findPotentialFriends(userId);
    }
    
    public void sendFriendRequest(User sender, User receiver) {
        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("Нельзя отправить запрос самому себе");
        }
        
        if (areFriends(sender, receiver)) {
            throw new RuntimeException("Вы уже друзья");
        }
        
        if (friendRequestRepository.existsBySenderAndReceiver(sender, receiver) ||
            friendRequestRepository.existsBySenderAndReceiver(receiver, sender)) {
            throw new RuntimeException("Запрос уже отправлен");
        }
        
        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        friendRequestRepository.save(request);
    }
    
    public void acceptFriendRequest(Long requestId, User currentUser) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Запрос не найден"));
        
        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Нет прав на выполнение этого действия");
        }
        
        if (!request.isPending()) {
            throw new RuntimeException("Запрос уже обработан");
        }
        
        request.setStatus(FriendRequest.Status.ACCEPTED);
        friendRequestRepository.save(request);
        
        // Добавляем в друзья
        User sender = request.getSender();
        User receiver = request.getReceiver();
        
        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);
        
        userRepository.save(sender);
        userRepository.save(receiver);
    }
    
    public void rejectFriendRequest(Long requestId, User currentUser) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Запрос не найден"));
        
        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Нет прав на выполнение этого действия");
        }
        
        request.setStatus(FriendRequest.Status.REJECTED);
        friendRequestRepository.save(request);
    }
    
    public void removeFriend(User user, User friend) {
        List<User> friends1 = userRepository.findFriendsByUserId(user.getId());
        List<User> friends2 = userRepository.findFriendsByUserId(friend.getId());
        friends1.remove(friend);
        friends2.remove(user);
        userRepository.save(user);
        userRepository.save(friend);
    }
    
    public boolean areFriends(User user1, User user2) {
        List<User> friends = userRepository.findFriendsByUserId(user1.getId());
        return friends.contains(user2);
    }
    
    public List<FriendRequest> getPendingFriendRequests(User user) {
        return friendRequestRepository.findByReceiverAndStatus(user, FriendRequest.Status.PENDING);
    }
    
    public List<User> getFriends(User user) {
        return userRepository.findFriendsByUserId(user.getId());
    }
    
    public User updateProfile(User user, MultipartFile avatarFile) throws IOException {
        // Save the user's bio, name, etc.
        User existingUser = userRepository.findById(user.getId())
                                     .orElseThrow(() -> new RuntimeException("User not found"));

        // Update basic fields
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setBio(user.getBio());

        // Handle avatar upload
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String fileName = saveAvatar(avatarFile);
            existingUser.setAvatarUrl("/images/avatars/" + fileName);
        } else if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
             // If no new file is uploaded and avatarUrl is explicitly set to null or empty,
             // it means the user wants to remove the existing avatar.
             existingUser.setAvatarUrl(null);
        }

        return userRepository.save(existingUser);
    }

    private String saveAvatar(MultipartFile avatarFile) throws IOException {
        // Create the directory if it doesn't exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            if (!uploadDir.mkdirs()) {
                throw new IOException("Failed to create directory for avatar upload: " + UPLOAD_DIR);
            }
        }

        // Generate a unique file name
        String originalFilename = avatarFile.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        // Save the file
        Files.write(filePath, avatarFile.getBytes());

        return fileName;
    }
    
    public long getFriendRequestCount(User user) {
        return friendRequestRepository.countByReceiverAndStatus(user, FriendRequest.Status.PENDING);
    }
    
    public long getFriendsCount(Long userId) {
        return userRepository.countFriendsByUserId(userId);
    }
    
    public long getPostsCount(Long userId) {
        return userRepository.countPostsByUserId(userId);
    }
    
    public String getFriendshipStatus(User currentUser, User otherUser) {
        if (currentUser.getId().equals(otherUser.getId())) {
            return "SELF"; // Пользователь смотрит свой профиль
        }

        if (areFriends(currentUser, otherUser)) {
            return "FRIENDS"; // Пользователи уже друзья
        }

        // Проверяем, отправлял ли текущий пользователь запрос другому пользователю
        Optional<FriendRequest> sentRequest = friendRequestRepository.findBySenderAndReceiverAndStatus(currentUser, otherUser, FriendRequest.Status.PENDING);
        if (sentRequest.isPresent()) {
            return "REQUEST_SENT"; // Запрос отправлен текущим пользователем
        }

        // Проверяем, получал ли текущий пользователь запрос от другого пользователя
        Optional<FriendRequest> receivedRequest = friendRequestRepository.findBySenderAndReceiverAndStatus(otherUser, currentUser, FriendRequest.Status.PENDING);
        if (receivedRequest.isPresent()) {
            return "REQUEST_RECEIVED"; // Запрос получен текущим пользователем
        }

        return "NOT_FRIENDS"; // Нет активных запросов и не друзья
    }
} 