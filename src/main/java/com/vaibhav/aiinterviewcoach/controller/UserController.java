package com.vaibhav.aiinterviewcoach.controller;

import com.vaibhav.aiinterviewcoach.dto.*;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public UserResponse registerUser(@Valid @RequestBody UserRequest userRequest) {
        User user = userService.saveUser(userRequest);
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
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
    public UserResponse updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request) {
        System.out.println("===== UPDATE CONTROLLER HIT =====");


        return userService.updateUser(id, request);
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
public UserResponse profile() {
    org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return userService.getAllUsers().stream()
            .filter(u -> email.equals(u.getEmail()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("User not found"));
}


}