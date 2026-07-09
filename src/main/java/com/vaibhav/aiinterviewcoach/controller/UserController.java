package com.vaibhav.aiinterviewcoach.controller;

import com.vaibhav.aiinterviewcoach.dto.UserRequest;
import com.vaibhav.aiinterviewcoach.dto.UserResponse;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.vaibhav.aiinterviewcoach.dto.LoginRequest;
import com.vaibhav.aiinterviewcoach.dto.LoginResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User registerUser(@Valid @RequestBody UserRequest userRequest) {
        return userService.saveUser(userRequest);
    }

    @GetMapping
    public List<UserResponse> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
    return"user deleted successfully!";
}
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id,
                           @RequestBody User user) {
        return userService.updateUser(id, user);
    }
    @PostMapping("/login")
    public LoginResponse loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.loginUser(loginRequest);
    }
@GetMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public String admin() {
    return "Welcome Admin";
}

@GetMapping("/profile")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public String profile() {
    return "Welcome User";
}

}