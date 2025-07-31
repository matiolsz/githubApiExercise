package com.olszanka.githubApiExercise.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubBranch(
    String name,
    @JsonProperty("commit") GitHubCommit commit
) {} 