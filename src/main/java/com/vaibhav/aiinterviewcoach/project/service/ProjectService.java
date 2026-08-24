package com.vaibhav.aiinterviewcoach.project.service;

import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.project.dto.ProjectRequest;
import com.vaibhav.aiinterviewcoach.project.dto.ProjectResponse;
import com.vaibhav.aiinterviewcoach.project.entity.Project;
import com.vaibhav.aiinterviewcoach.project.repository.ProjectRepository;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public ProjectResponse createProject(ProjectRequest request) {
        User user = getCurrentUser();
        Project project = Project.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .techStack(request.getTechStack())
                .projectUrl(request.getProjectUrl())
                .build();
        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    public List<ProjectResponse> getUserProjects() {
        User user = getCurrentUser();
        List<Project> projects = projectRepository.findByUserId(user.getId());
        return projects.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ProjectResponse getProject(Long projectId) {
        User user = getCurrentUser();
        Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found or you don't have access"));
        return mapToResponse(project);
    }

    public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
        User user = getCurrentUser();
        Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found or you don't have access"));
        
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setTechStack(request.getTechStack());
        project.setProjectUrl(request.getProjectUrl());
        
        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    public void deleteProject(Long projectId) {
        User user = getCurrentUser();
        Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found or you don't have access"));
        projectRepository.delete(project);
    }

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .techStack(project.getTechStack())
                .projectUrl(project.getProjectUrl())
                .build();
    }
}
