package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(exclude = {"friends", "friendRequests", "posts", "comments"})
@ToString(exclude = {"friends", "friendRequests", "posts", "comments"})
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    @Column(unique = true)
    private String username;
    
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email")
    @Column(unique = true)
    private String email;
    
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен быть минимум 6 символов")
    private String password;
    
    @NotBlank(message = "Имя обязательно")
    private String firstName;
    
    @NotBlank(message = "Фамилия обязательна")
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    private Role role = Role.STUDENT;
    
    private String bio;
    private String avatarUrl;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private boolean enabled = true;
    
    // Не используйте напрямую вне сессии Hibernate! Используйте userRepository.findFriendsByUserId(user.getId())
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_friends",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();
    
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "sentMessages")
    private Set<FriendRequest> friendRequests = new HashSet<>();
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();
    
    @OneToMany(mappedBy = "sender")
    @JsonManagedReference(value = "sentMessages")
    private java.util.List<Message> sentMessages;

    @OneToMany(mappedBy = "receiver")
    @JsonManagedReference(value = "receivedMessages")
    private java.util.List<Message> receivedMessages;
    
    public enum Role {
        ADMIN("Администратор"),
        STUDENT("Ученик");
        
        private final String displayName;
        
        Role(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    public boolean isStudent() {
        return role == Role.STUDENT;
    }
} 