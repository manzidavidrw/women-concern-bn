package com.womenconcern.api.auth.service.impl;


import com.womenconcern.api.auth.dto.*;
import com.womenconcern.api.auth.service.AuthService;
import com.womenconcern.api.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        String url = String.format("%s/realms/%s/protocol/openid-connect/token", authServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.isEmpty() && !clientSecret.equals("your-client-secret")) {
            map.add("client_secret", clientSecret);
        }
        map.add("username", loginRequest.getEmail());
        map.add("password", loginRequest.getPassword());
        map.add("scope", "openid profile email");


        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, request, AuthResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.out.println("Error here" + e.getResponseBodyAsString());
            e.printStackTrace();
            throw new UnauthorizedException("Invalid credentials or authentication failure");
        }
    }



    @Override
    public AuthResponse refresh(RefreshRequest refreshRequest) {
        String url = String.format("%s/realms/%s/protocol/openid-connect/token", authServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.isEmpty() && !clientSecret.equals("your-client-secret")) {
            map.add("client_secret", clientSecret);
        }
        map.add("refresh_token", refreshRequest.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        System.out.println("CLIENT ID = " + clientId);
        System.out.println("CLIENT SECRET = " + clientSecret);
        System.out.println("AUTH URL = " + authServerUrl);
        System.out.println("AUTH refresh = " + map.toSingleValueMap().toString());


        try {
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, request, AuthResponse.class);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.out.println("Error here 2" + e.getResponseBodyAsString());
            throw new UnauthorizedException("Invalid or expired refresh token");
        } catch (Exception e) {
            System.out.println("Error here 3" + e.getMessage());

            throw new RuntimeException("Failed to refresh token", e);
        }
    }

    @Override
    public MessageResponse logout(LogoutRequest logoutRequest) {
        String url = String.format("%s/realms/%s/protocol/openid-connect/logout", authServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.isEmpty() && !clientSecret.equals("your-client-secret")) {
            map.add("client_secret", clientSecret);
        }
        map.add("refresh_token", logoutRequest.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
            return new MessageResponse( "Logged out successfully");
        } catch (HttpClientErrorException e) {
            throw new UnauthorizedException("Invalid refresh token");
        } catch (Exception e) {
            throw new RuntimeException("Failed to logout from Keycloak", e);
        }
    }
}
