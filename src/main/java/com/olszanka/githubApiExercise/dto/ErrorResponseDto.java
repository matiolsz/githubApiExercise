package com.olszanka.githubApiExercise.dto;

public record ErrorResponseDto(
    int status,
    String message
) {} 