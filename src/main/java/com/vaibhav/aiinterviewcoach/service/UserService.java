package com.vaibhav.aiinterviewcoach.service;

import com.vaibhav.aiinterviewcoach.dto.UserRequest;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.vaibhav.aiinterviewcoach.dto.UserRequest;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    public User saveUser(UserRequest userRequest) {

        User user = new User();

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(userRequest.getPassword());
        user.setRole(userRequest.getRole());

        return userRepository.save(user);
    }
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }
    public User getUserById(long id){
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
}