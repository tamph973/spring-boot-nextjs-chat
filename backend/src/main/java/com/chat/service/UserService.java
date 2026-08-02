package com.chat.service;

import com.chat.model.User;
import com.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String USER_STATUS_KEY = "user:status:";
    
    public void setUserOnline(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setStatus(User.UserStatus.ONLINE);
            userRepository.save(user);
            redisTemplate.opsForValue().set(USER_STATUS_KEY + userId, "ONLINE", Duration.ofMinutes(30));
        }
    }
    
    public void setUserOffline(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setStatus(User.UserStatus.OFFLINE);
            userRepository.save(user);
            redisTemplate.delete(USER_STATUS_KEY + userId);
        }
    }
    
    public boolean isUserOnline(String userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(USER_STATUS_KEY + userId));
    }
    
    public User getUser(String id) {
        return userRepository.findById(id).orElse(null);
    }
}
