package com.vaibhav.aiinterviewcoach.service;

import com.vaibhav.aiinterviewcoach.dto.*;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.exception.GlobalExceptionHandler;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import com.vaibhav.aiinterviewcoach.util.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

import com.vaibhav.aiinterviewcoach.exception.UserNotFoundException;
import com.vaibhav.aiinterviewcoach.exception.InvalidPasswordException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User saveUser(UserRequest userRequest) {

        User user = new User();

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole(userRequest.getRole());

        return userRepository.save(user);
    }
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream().map(user -> {
            UserResponse response = new UserResponse();

            response.setId(user.getId());
            response.setName(user.getName());
            response.setEmail(user.getEmail());
            response.setRole(user.getRole());

            return response;
        }).toList();
    }    public User getUserById(long id){
        return userRepository.findById(id).orElse(null);
    }
    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }
    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id).orElse(null);

        if (existingUser != null) {
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPassword(updatedUser.getPassword());
            existingUser.setRole(updatedUser.getRole());

            return userRepository.save(existingUser);
        }


        return null;
    }
    public LoginResponse loginUser(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Invalid password");
        }
        String token = jwtService.generateToken(user.getEmail());


        LoginResponse response = new LoginResponse();

        response.setMessage("Login Successful");
        response.setName(user.getName());
        response.setRole(user.getRole());

        response.setToken(token);

        return response;


    }
    @Autowired
    private JwtService jwtService;
}