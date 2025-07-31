package com.olszanka.githubApiExercise.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RepositoryDto(
    String name,
    @JsonProperty("owner_login") String ownerLogin,
    List<BranchDto> branches
) {} 