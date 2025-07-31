package com.olszanka.githubApiExercise.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BranchDto(
    String name,
    @JsonProperty("last_commit_sha") String lastCommitSha
) {} 