package dev.gustavo.authservice.dto;

public record TokenResponse(String accessToken, String refreshToken) {
}
