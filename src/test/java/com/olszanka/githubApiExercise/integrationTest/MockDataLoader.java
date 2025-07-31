package com.olszanka.githubApiExercise.integrationTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MockDataLoader {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static JsonNode mockData;
    
    static {
        try {
            ClassPathResource resource = new ClassPathResource("mock-github-responses.json");
            String jsonContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            mockData = objectMapper.readTree(jsonContent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load mock data", e);
        }
    }
    
    public static String getUserResponse() {
        return mockData.get("user").toString();
    }
    
    public static String getRepositoriesResponse() {
        return mockData.get("repositories").toString();
    }
    
    public static String getBranchesResponse(String repoName) {
        return mockData.get("branches").get(repoName).toString();
    }
    
    public static String getErrorResponse(String errorType) {
        return mockData.get("error_responses").get(errorType).toString();
    }
    
    public static String getNonForkRepositoriesResponse() {
        StringBuilder response = new StringBuilder("[");
        boolean first = true;
        
        for (JsonNode repo : mockData.get("repositories")) {
            if (!repo.get("fork").asBoolean()) {
                if (!first) {
                    response.append(",");
                }
                response.append(repo.toString());
                first = false;
            }
        }
        response.append("]");
        
        return response.toString();
    }
} 