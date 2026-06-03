package fr.utc.sr03.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.utc.sr03.model.Users.UserDTO;

// Réponse d'authentification renvoyée par /api/auth/login et /api/auth/refresh.
public record AuthResponse(
        @JsonProperty("access_token")  String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        UserDTO user
) {}
