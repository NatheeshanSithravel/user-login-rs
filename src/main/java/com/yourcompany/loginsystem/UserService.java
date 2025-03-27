package com.yourcompany.loginsystem;

import com.yourcompany.loginsystem.User;
import com.yourcompany.loginsystem.UserRegistrationDto;
import com.yourcompany.loginsystem.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public User registerNewUser(UserRegistrationDto registrationDto) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setEmail(registrationDto.getEmail());
        // Encode password before storing
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        return userRepository.save(newUser);
    }

    public String authenticateUser(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Update last login time
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Check password
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Generate JWT token
                return jwtService.generateToken(user);
            }
        }
        throw new RuntimeException("Invalid credentials");
    }
}