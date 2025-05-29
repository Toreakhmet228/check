package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(User.Role role);
    
    @Query("SELECT u FROM User u WHERE u.id != :userId AND u NOT IN " +
           "(SELECT f FROM User user JOIN user.friends f WHERE user.id = :userId)")
    List<User> findPotentialFriends(@Param("userId") Long userId);
    
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "u.id != :currentUserId")
    List<User> searchUsers(@Param("query") String query, @Param("currentUserId") Long currentUserId);
    
    @Query("SELECT f FROM User u JOIN u.friends f WHERE u.id = :userId")
    List<User> findFriendsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(f) FROM User u JOIN u.friends f WHERE u.id = :userId")
    long countFriendsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(p) FROM Post p WHERE p.author.id = :userId")
    long countPostsByUserId(@Param("userId") Long userId);
} 