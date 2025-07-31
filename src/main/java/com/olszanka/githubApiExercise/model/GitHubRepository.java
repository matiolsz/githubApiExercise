package com.olszanka.githubApiExercise.model;

public record GitHubRepository(
    String name,
    GitHubOwner owner,
    boolean fork
) {} 