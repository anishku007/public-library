package com.org.booklibrary.service;

import com.org.booklibrary.entity.User;
import com.org.booklibrary.exception.ResourceNotFoundException;
import com.org.booklibrary.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User addUser(User user) {
        return userRepository.save(user);
    }
}
