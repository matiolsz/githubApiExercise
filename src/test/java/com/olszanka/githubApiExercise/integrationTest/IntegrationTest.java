package com.olszanka.githubApiExercise.integrationTest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class IntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

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
    void testGetUserRepositories_ShouldReturnValidRepositories_WhenUserExists() {
        // Given
        var username = "testuser";

        // Mock GitHub API responses
        stubFor(get(urlEqualTo("/users/" + username))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"login\":\"testuser\"}")));

        stubFor(get(urlEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[" +
                                "{\"name\":\"test-repo-1\",\"fork\":false,\"owner\":{\"login\":\"testuser\"}}," +
                                "{\"name\":\"test-repo-2\",\"fork\":false,\"owner\":{\"login\":\"testuser\"}}" +
                                "]")));

        stubFor(get(urlEqualTo("/repos/" + username + "/test-repo-1/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[" +
                                "{\"name\":\"main\",\"commit\":{\"sha\":\"abc123def456789\"}}," +
                                "{\"name\":\"develop\",\"commit\":{\"sha\":\"def456ghi789012\"}}" +
                                "]")));

        stubFor(get(urlEqualTo("/repos/" + username + "/test-repo-2/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[" +
                                "{\"name\":\"master\",\"commit\":{\"sha\":\"xyz789abc123456\"}}" +
                                "]")));

        // When & Then
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
    void testGetUserRepositories_ShouldReturn404_WhenUserDoesNotExist() {
        // Given
        var username = "nonexistentuser";

        // Mock GitHub API response for non-existent user
        stubFor(get(urlEqualTo("/users/" + username))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Not Found\"}")));

        // When & Then
        webTestClient.get()
                .uri("/api/github/repositories/" + username)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("User not found");
    }

    @Test
    void testGetUserRepositories_ShouldFilterOutForks() {
        // Given
        var username = "testuser";

        // Mock GitHub API responses
        stubFor(get(urlEqualTo("/users/" + username))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"login\":\"testuser\"}")));

        stubFor(get(urlEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[" +
                                "{\"name\":\"test-repo-1\",\"fork\":false,\"owner\":{\"login\":\"testuser\"}}," +
                                "{\"name\":\"test-repo-2\",\"fork\":false,\"owner\":{\"login\":\"testuser\"}}," +
                                "{\"name\":\"forked-repo\",\"fork\":true,\"owner\":{\"login\":\"testuser\"}}" +
                                "]")));

        stubFor(get(urlEqualTo("/repos/" + username + "/test-repo-1/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[" +
                                "{\"name\":\"main\",\"commit\":{\"sha\":\"abc123def456789\"}}" +
                                "]")));

        stubFor(get(urlEqualTo("/repos/" + username + "/test-repo-2/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[" +
                                "{\"name\":\"master\",\"commit\":{\"sha\":\"xyz789abc123456\"}}" +
                                "]")));

        // When & Then
        webTestClient.get()
                .uri("/api/github/repositories/" + username)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[0].name").isEqualTo("test-repo-1")
                .jsonPath("$[1].name").isEqualTo("test-repo-2")
                .jsonPath("$[*].name").value(not(hasItem("forked-repo")));
    }
} 