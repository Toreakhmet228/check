package com.example.demo.controller;

import com.example.demo.dto.UserSearchResultDTO;
import com.example.demo.model.FriendRequest;
import com.example.demo.model.User;
import com.example.demo.repository.FriendRequestRepository;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {
    
    private final UserService userService;
    private final FriendRequestRepository friendRequestRepository;
    
    @GetMapping
    @Transactional(readOnly = true)
    public String friends(@AuthenticationPrincipal User currentUser, Model model) {
        List<User> friends = userService.getFriends(currentUser);
        List<FriendRequest> pendingRequests = userService.getPendingFriendRequests(currentUser);
        long friendsCount = userService.getFriendsCount(currentUser.getId());
        
        model.addAttribute("friends", friends);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("friendsCount", friendsCount);
        model.addAttribute("currentUser", currentUser);
        
        return "friend/list";
    }
    
    @GetMapping("/search")
    public String searchFriends(@RequestParam(required = false) String query,
                               @AuthenticationPrincipal User currentUser,
                               Model model,
                               @ModelAttribute("_csrf") CsrfToken csrfToken) {
        List<User> foundUsers;
        if (query != null && !query.trim().isEmpty()) {
            foundUsers = userService.searchUsers(query, currentUser.getId());
        } else {
            foundUsers = userService.findPotentialFriends(currentUser.getId());
        }
        
        List<UserSearchResultDTO> userResults = foundUsers.stream()
            .map(user -> new UserSearchResultDTO(user, userService.getFriendshipStatus(currentUser, user)))
            .collect(Collectors.toList());
        
        model.addAttribute("userResults", userResults);
        model.addAttribute("query", query);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("_csrf", csrfToken);
        
        return "friend/search";
    }
    
    @PostMapping("/request/{userId}")
    public String sendFriendRequest(@PathVariable Long userId,
                                   @AuthenticationPrincipal User currentUser,
                                   RedirectAttributes redirectAttributes) {
        try {
            User receiver = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            userService.sendFriendRequest(currentUser, receiver);
            redirectAttributes.addFlashAttribute("success", 
                "Запрос в друзья отправлен пользователю " + receiver.getFullName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/friends/search";
    }
    
    @PostMapping("/accept/{requestId}")
    public String acceptFriendRequest(@PathVariable Long requestId,
                                     @AuthenticationPrincipal User currentUser,
                                   RedirectAttributes redirectAttributes,
                                   HttpServletRequest servletRequest) {
        try {
            // Ищем запрос напрямую по ID
            FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Запрос в друзья не найден"));
            
            // Проверяем, что запрос адресован текущему пользователю
            if (!friendRequest.getReceiver().getId().equals(currentUser.getId())) {
                throw new RuntimeException("У вас нет прав для выполнения этого действия");
            }
            
            userService.acceptFriendRequest(requestId, currentUser);
            redirectAttributes.addFlashAttribute("success", "Запрос в друзья принят!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        // Определяем, откуда был вызван метод и перенаправляем соответственно
        String referer = servletRequest.getHeader("Referer");
        if (referer != null && referer.contains("/friends/search")) {
            return "redirect:/friends/search";
        }
        return "redirect:/friends";
    }
    
    @PostMapping("/reject/{requestId}")
    public String rejectFriendRequest(@PathVariable Long requestId,
                                     @AuthenticationPrincipal User currentUser,
                                   RedirectAttributes redirectAttributes,
                                   HttpServletRequest servletRequest) {
        try {
            // Ищем запрос напрямую по ID
            FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Запрос в друзья не найден"));
            
            // Проверяем, что запрос адресован текущему пользователю
            if (!friendRequest.getReceiver().getId().equals(currentUser.getId())) {
                throw new RuntimeException("У вас нет прав для выполнения этого действия");
            }
            
            userService.rejectFriendRequest(requestId, currentUser);
            redirectAttributes.addFlashAttribute("success", "Запрос в друзья отклонен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        // Определяем, откуда был вызван метод и перенаправляем соответственно
        String referer = servletRequest.getHeader("Referer");
        if (referer != null && referer.contains("/friends/search")) {
            return "redirect:/friends/search";
        }
        return "redirect:/friends";
    }
    
    @PostMapping("/remove/{friendId}")
    public String removeFriend(@PathVariable Long friendId,
                              @AuthenticationPrincipal User currentUser,
                              RedirectAttributes redirectAttributes) {
        try {
            User friend = userService.findById(friendId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            userService.removeFriend(currentUser, friend);
            redirectAttributes.addFlashAttribute("success", 
                friend.getFullName() + " удален из друзей");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/friends";
    }
} 