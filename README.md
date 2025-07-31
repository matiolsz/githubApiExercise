# GitHub API Exercise

A Spring Boot application that consumes the GitHub API to provide information about user repositories. This application filters out forked repositories and provides detailed information about each repository including branch names and their last commit SHAs.

## Features

- **Repository Listing**: Lists all non-fork repositories for a given GitHub user
- **Branch Information**: For each repository, provides branch names and their last commit SHAs
- **Error Handling**: Proper 404 responses for non-existent users
- **Spring MVC**: Built with traditional Spring Web for synchronous operations
- **RESTful API**: Clean REST endpoints following industry standards

## API Endpoints

### Get User Repositories

**Endpoint**: `GET /api/github/repositories/{username}`

**Description**: Retrieves all non-fork repositories for a GitHub user along with branch information.

**Path Parameters**:
- `username` (string): GitHub username

**Response Format**:
```json
[
  {
    "name": "repository-name",
    "owner_login": "username",
    "branches": [
      {
        "name": "main",
        "last_commit_sha": "abc123def456..."
      },
      {
        "name": "develop",
        "last_commit_sha": "def456ghi789..."
      }
    ]
  }
]
```

**Error Response (404 - User Not Found)**:
```json
{
  "status": 404,
  "message": "User not found"
}
```

## Technology Stack

- **Java 21**
- **Spring Boot 3.5.4**
- **Spring Web (MVC)** - For traditional web development
- **RestClient** - For HTTP client operations
- **JUnit 5** - For testing
- **WireMock** - For mocking external HTTP APIs in integration tests
- **Gradle** - Build tool

## Prerequisites

- Java 21 or higher
- Gradle (included in the project)

## Running the Application

### Using Gradle Wrapper

```bash
# Run the application
./gradlew bootRun

# Or on Windows
gradlew.bat bootRun
```

### Using JAR file

```bash
# Build the application
./gradlew build

# Run the JAR file
java -jar build/libs/githubApiExercise-0.0.1-SNAPSHOT.jar
```

The application will start on port 8080 by default.

## Testing

### Running Tests

```bash
./gradlew test
```

### Manual Testing

You can test the API using curl or any HTTP client:

```bash
# Test with existing user
curl http://localhost:8080/api/github/repositories/octocat

# Test with non-existent user
curl http://localhost:8080/api/github/repositories/nonexistentuser12345
```

### Integration Testing with WireMock

Integration tests use [WireMock](http://wiremock.org/) to mock the GitHub API.

**Dependency (see `build.gradle`):**
```gradle
testImplementation 'org.wiremock:wiremock-standalone:3.4.2'
```

**Usage:**
- The tests start a `WireMockServer` on a local port (e.g., 8089).
- All GitHub API calls are stubbed/mocked in the test code.
- No real GitHub API calls are made during tests.

## Project Structure

```
src/
├── main/
│   ├── java/com/olszanka/githubApiExercise/
│   │   ├── controller/
│   │   │   └── GitHubController.java          # REST endpoints
│   │   ├── dto/
│   │   │   ├── BranchDto.java                # Branch data transfer object
│   │   │   ├── RepositoryDto.java            # Repository data transfer object
│   │   │   └── ErrorResponseDto.java         # Error response DTO
│   │   ├── model/
│   │   │   ├── GitHubRepository.java         # GitHub API repository model
│   │   │   ├── GitHubOwner.java              # GitHub API owner model
│   │   │   ├── GitHubBranch.java             # GitHub API branch model
│   │   │   └── GitHubCommit.java             # GitHub API commit model
│   │   ├── service/
│   │   │   └── GitHubApiService.java         # GitHub API integration
│   │   └── GitHubApiExerciseApplication.java # Main application class
│   └── resources/
│       └── application.properties            # Application configuration
└── test/
    ├── java/com/olszanka/githubApiExercise/
    │   └── integrationTest/
    │       ├── IntegrationTest.java          # Main integration test
    │       └── MockDataLoader.java           # Utility for loading mock JSON data
    └── resources/
        └── mock-github-responses.json        # Mock data for integration tests
```

## Configuration

The application can be configured through `application.properties`:

```properties
server.port=8080
github.api.base-url=https://api.github.com
```

## GitHub API Integration

This application uses the GitHub REST API v3:
- **Base URL**: https://api.github.com
- **Endpoints Used**:
  - `GET /users/{username}/repos` - Get user repositories
  - `GET /repos/{owner}/{repo}/branches` - Get repository branches
  - `GET /users/{username}` - Check if user exists

## Error Handling

1. **404 - User Not Found**: Returns structured error response
