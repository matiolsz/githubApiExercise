package com.olszanka.githubApiExercise.controller;

import com.olszanka.githubApiExercise.service.GitHubApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github")
public class GitHubController {
    
    private final GitHubApiService gitHubApiService;
    
    public GitHubController(GitHubApiService gitHubApiService) {
        this.gitHubApiService = gitHubApiService;
    }
    
    @GetMapping("/repositories/{username}")
    public ResponseEntity<?> getUserRepositories(@PathVariable String username) {
        return gitHubApiService.getUserRepositoriesWithValidation(username);
    }
} 