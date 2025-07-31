package com.olszanka.githubApiExercise.service;

import com.olszanka.githubApiExercise.dto.BranchDto;
import com.olszanka.githubApiExercise.dto.ErrorResponseDto;
import com.olszanka.githubApiExercise.dto.RepositoryDto;
import com.olszanka.githubApiExercise.model.GitHubBranch;
import com.olszanka.githubApiExercise.model.GitHubRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitHubApiService {
    
    private final RestClient restClient;
    private final String baseUrl;
    
    public GitHubApiService(RestClient restClient, @Value("${github.api.base-url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }
    
    public ResponseEntity<?> getUserRepositoriesWithValidation(String username) {
        try {
            if (!userExists(username)) {
                var errorResponse = new ErrorResponseDto(
                        404,
                        "User not found"
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            var repositories = getUserRepositories(username);
            return ResponseEntity.ok(repositories);
        } catch (GitHubApiException e) {
            var errorResponse = new ErrorResponseDto(
                    e.getStatusCode(),
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.valueOf(e.getStatusCode())).body(errorResponse);
        }
    }
    
    public List<RepositoryDto> getUserRepositories(String username) {
        var url = baseUrl + "/users/" + username + "/repos";
        
        try {
            var repositories = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(GitHubRepository[].class);
            
            if (repositories == null) {
                throw new GitHubApiException("Failed to retrieve repositories for user: " + username, 500);
            }
            
            return Arrays.stream(repositories)
                    .filter(repo -> !repo.fork())
                    .map(repo -> {
                        var branches = getRepositoryBranches(username, repo.name());
                        return new RepositoryDto(
                                repo.name(),
                                repo.owner().login(),
                                branches
                        );
                    })
                    .collect(Collectors.toList());
        } catch (HttpClientErrorException e) {
            throw new GitHubApiException("Failed to retrieve repositories: " + e.getMessage(), e.getStatusCode().value());
        } catch (Exception e) {
            throw new GitHubApiException("Unexpected error while retrieving repositories: " + e.getMessage(), 500);
        }
    }
    
    private List<BranchDto> getRepositoryBranches(String username, String repoName) {
        var url = baseUrl + "/repos/" + username + "/" + repoName + "/branches";
        
        try {
            var branches = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(GitHubBranch[].class);
            
            if (branches == null) {
                throw new GitHubApiException("Failed to retrieve branches for repository: " + repoName, 500);
            }
            
            return Arrays.stream(branches)
                    .map(branch -> new BranchDto(branch.name(), branch.commit().sha()))
                    .collect(Collectors.toList());
        } catch (HttpClientErrorException e) {
            throw new GitHubApiException("Failed to retrieve branches: " + e.getMessage(), e.getStatusCode().value());
        } catch (Exception e) {
            throw new GitHubApiException("Unexpected error while retrieving branches: " + e.getMessage(), 500);
        }
    }
    
    private boolean userExists(String username) {
        var url = baseUrl + "/users/" + username;
        
        try {
            var response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .toEntity(String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            throw new GitHubApiException("Error checking if user exists: " + e.getMessage(), 500);
        }
    }
    
    public static class GitHubApiException extends RuntimeException {
        private final int statusCode;
        
        public GitHubApiException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
    }
} 