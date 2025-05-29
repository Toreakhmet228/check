package com.example.demo.dto;

import com.example.demo.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResultDTO {
    private User user;
    private String friendshipStatus; // e.g., "FRIENDS", "REQUEST_SENT", "REQUEST_RECEIVED", "NOT_FRIENDS", "SELF"
} 