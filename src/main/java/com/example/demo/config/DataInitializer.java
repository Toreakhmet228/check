package com.example.demo.config;

import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.service.PostService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Lazy
public class DataInitializer implements CommandLineRunner {
    
    private final UserService userService;
    private final PostService postService;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Инициализация тестовых данных...");
        
        // Создаем администратора
        if (userService.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword("admin123");
            admin.setFirstName("Администратор");
            admin.setLastName("Системы");
            admin.setRole(User.Role.ADMIN);
            admin.setBio("Главный администратор социальной сети");
            
            admin = userService.registerUser(admin);
            log.info("Создан администратор: {}", admin.getUsername());
            
            // Создаем пост от администратора
            Post adminPost = new Post();
            adminPost.setContent("Добро пожаловать в нашу социальную сеть! Здесь вы можете общаться с друзьями, делиться постами и находить новых знакомых.");
            postService.createPost(adminPost, admin);
            
            // Закрепляем пост
            postService.pinPost(adminPost.getId(), admin);
            log.info("Создан приветственный пост от администратора");
        }
        
        // Создаем тестового ученика
        if (userService.findByUsername("student1").isEmpty()) {
            User student = new User();
            student.setUsername("student1");
            student.setEmail("student1@example.com");
            student.setPassword("student123");
            student.setFirstName("Иван");
            student.setLastName("Петров");
            student.setRole(User.Role.STUDENT);
            student.setBio("Студент, изучающий программирование");
            
            student = userService.registerUser(student);
            log.info("Создан тестовый ученик: {}", student.getUsername());
            
            // Создаем пост от ученика
            Post studentPost = new Post();
            studentPost.setContent("Привет всем! Это мой первый пост в социальной сети. Рад быть здесь!");
            postService.createPost(studentPost, student);
            log.info("Создан пост от тестового ученика");
        }
        
        // Создаем еще одного ученика
        if (userService.findByUsername("student2").isEmpty()) {
            User student2 = new User();
            student2.setUsername("student2");
            student2.setEmail("student2@example.com");
            student2.setPassword("student123");
            student2.setFirstName("Мария");
            student2.setLastName("Сидорова");
            student2.setRole(User.Role.STUDENT);
            student2.setBio("Люблю читать и путешествовать");
            
            student2 = userService.registerUser(student2);
            log.info("Создан второй тестовый ученик: {}", student2.getUsername());
            
            // Создаем пост от второго ученика
            Post student2Post = new Post();
            student2Post.setContent("Сегодня прекрасный день для изучения новых технологий! Кто-нибудь изучает Spring Boot?");
            postService.createPost(student2Post, student2);
            log.info("Создан пост от второго тестового ученика");
        }
        
        log.info("Инициализация тестовых данных завершена");
        log.info("Для входа используйте:");
        log.info("Администратор - логин: admin, пароль: admin123");
        log.info("Ученик 1 - логин: student1, пароль: student123");
        log.info("Ученик 2 - логин: student2, пароль: student123");
    }
} 