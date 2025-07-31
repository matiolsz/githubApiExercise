package com.olszanka.githubApiExercise.integrationTest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class MockDataTest {

    @Autowired
    private WebTestClient webTestClient;

    private WireMockServer wireMockServer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("github.api.base-url", () -> "http://localhost:8089");
    }

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testMockDataLoader_ShouldLoadMockDataCorrectly() {
        // Test that mock data loader works correctly
        var userResponse = MockDataLoader.getUserResponse();
        var reposResponse = MockDataLoader.getRepositoriesResponse();
        var branchesResponse = MockDataLoader.getBranchesResponse("test-repo-1");
        var errorResponse = MockDataLoader.getErrorResponse("user_not_found");
        var nonForkReposResponse = MockDataLoader.getNonForkRepositoriesResponse();

        // Validate user response
        assertTrue(userResponse.contains("testuser"));
        assertTrue(userResponse.contains("12345"));

        // Validate repositories response
        assertTrue(reposResponse.contains("test-repo-1"));
        assertTrue(reposResponse.contains("test-repo-2"));
        assertTrue(reposResponse.contains("forked-repo"));

        // Validate branches response
        assertTrue(branchesResponse.contains("main"));
        assertTrue(branchesResponse.contains("develop"));
        assertTrue(branchesResponse.contains("abc123def456789"));

        // Validate error response
        assertTrue(errorResponse.contains("Not Found"));

        // Validate non-fork repositories response
        assertTrue(nonForkReposResponse.contains("test-repo-1"));
        assertTrue(nonForkReposResponse.contains("test-repo-2"));
        assertFalse(nonForkReposResponse.contains("forked-repo"));
    }

    @Test
    void testGetUserRepositories_WithMockData() {
        // Given
        var username = "testuser";

        // Mock GitHub API responses using MockDataLoader
        stubFor(get(urlEqualTo("/users/" + username))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(MockDataLoader.getUserResponse())));

        stubFor(get(urlEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(MockDataLoader.getRepositoriesResponse())));

        stubFor(get(urlEqualTo("/repos/" + username + "/test-repo-1/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(MockDataLoader.getBranchesResponse("test-repo-1"))));

        stubFor(get(urlEqualTo("/repos/" + username + "/test-repo-2/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(MockDataLoader.getBranchesResponse("test-repo-2"))));

        // When & Then - Validate against mock data expectations
        webTestClient.get()
                .uri("/api/github/repositories/" + username)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[0].name").isEqualTo("test-repo-1")
                .jsonPath("$[0].owner_login").isEqualTo("testuser")
                .jsonPath("$[0].branches[0].name").isEqualTo("main")
                .jsonPath("$[0].branches[0].last_commit_sha").isEqualTo("abc123def456789")
                .jsonPath("$[0].branches[1].name").isEqualTo("develop")
                .jsonPath("$[0].branches[1].last_commit_sha").isEqualTo("def456ghi789012")
                .jsonPath("$[1].name").isEqualTo("test-repo-2")
                .jsonPath("$[1].owner_login").isEqualTo("testuser")
                .jsonPath("$[1].branches[0].name").isEqualTo("master")
                .jsonPath("$[1].branches[0].last_commit_sha").isEqualTo("xyz789abc123456");
    }

    @Test
    void testGetUserRepositories_ShouldNotContainForkedRepos() {
        // Given
        var username = "testuser";

        // Mock GitHub API responses
        stubFor(get(urlEqualTo("/users/" + username))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(MockDataLoader.getUserResponse())));

        stubFor(get(urlEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(MockDataLoader.getRepositoriesResponse())));

        stubFor(get(urlEqualTo("/repos/" + username + "/test-repo-1/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(MockDataLoader.getBranchesResponse("test-repo-1"))));

        stubFor(get(urlEqualTo("/repos/" + username + "/test-repo-2/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(MockDataLoader.getBranchesResponse("test-repo-2"))));

        // When & Then - Validate that forked repositories are filtered out
        webTestClient.get()
                .uri("/api/github/repositories/" + username)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[0].name").isEqualTo("test-repo-1")
                .jsonPath("$[1].name").isEqualTo("test-repo-2");
    }
} 