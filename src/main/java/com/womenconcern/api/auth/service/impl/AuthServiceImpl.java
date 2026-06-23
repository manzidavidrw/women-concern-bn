package com.womenconcern.api.auth.service.impl;


import com.womenconcern.api.auth.dto.*;
import com.womenconcern.api.auth.service.AuthService;
import com.womenconcern.api.exception.UnauthorizedException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
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
import java.time.LocalDate;


import java.util.*;


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
    private final Keycloak keycloak;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        String url = String.format("%s/realms/%s/protocol/openid-connect/token", authServerUrl, realm);
        System.out.println(url);

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
        System.out.println(map.toString());


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
            return new MessageResponse("Logged out successfully");
        } catch (HttpClientErrorException e) {
            throw new UnauthorizedException("Invalid refresh token");
        } catch (Exception e) {
            throw new RuntimeException("Failed to logout from Keycloak", e);
        }
    }


    public MessageResponse createUser(CreateUserRequest request) {

        // Step 1 — Build user
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(false);

        // Required actions — forces password setup on first login
        user.setRequiredActions(Collections.emptyList());

//        user.setRequiredActions(Arrays.asList("UPDATE_PASSWORD", "VERIFY_EMAIL"));

        user.singleAttribute("phoneNumber", request.getPhoneNumber());
        user.singleAttribute("employeeId", UUID.randomUUID().toString());

        // Step 2 — Set a random temporary password (needed to create account)
        // but user never sees or uses this — they'll set their own via email
        String tempPassword = UUID.randomUUID().toString();
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(true);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(tempPassword);
        user.setCredentials(Collections.singletonList(credential));

        // Step 3 — Create user in Keycloak
        Response response = keycloak.realm(realm).users().create(user);

        if (response.getStatus() != 201) {
            String errorBody = response.readEntity(String.class);
            if (response.getStatus() == 409) {
                throw new RuntimeException("A user with this email already exists.");
            }
            throw new RuntimeException("Failed to create user: "
                    + response.getStatus() + " - " + errorBody);
        }

        String userId = response.getLocation().getPath()
                .replaceAll(".*/([^/]+)$", "$1");

        // Step 4 — Assign role
        try {
            var roleRepresentation = keycloak.realm(realm)
                    .roles()
                    .list()
                    .stream()
                    .filter(r -> r.getName().equals(request.getRole()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "Role not found in Keycloak: " + request.getRole()
                    ));

            keycloak.realm(realm)
                    .users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .add(Collections.singletonList(roleRepresentation));

        } catch (RuntimeException e) {
            // Role assignment failed — delete the user to avoid orphaned accounts
            keycloak.realm(realm).users().get(userId).remove();
            throw e;
        }

        // Step 5 — Send welcome email with set-password link
        try {
            keycloak.realm(realm)
                    .users()
                    .get(userId)
                    .executeActionsEmail(Arrays.asList("UPDATE_PASSWORD"));

            log.info("Welcome email sent to {}", request.getEmail());
        } catch (Exception e) {
            log.warn("User created but welcome email failed for {}: {}",
                    request.getEmail(), e.getMessage());
            // Don't fail — user exists, admin can manually reset password
        }

        return new MessageResponse(
                "User account created for " + request.getEmail() +
                        ". A welcome email has been sent with instructions to set their password."
        );
    }

    public String resetPassword(String userId) {
        String newPassword = UUID.randomUUID().toString().substring(0, 8);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(true);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);

        keycloak.realm(realm).users().get(userId).resetPassword(credential);

        return newPassword;
    }

    public void forgotPassword(String userId) {
        keycloak.realm(realm)
                .users()
                .get(userId)
                .executeActionsEmail(Arrays.asList("UPDATE_PASSWORD"));
    }

    @Override
    public EmployeeProfileResponse updateMyProfile(String userId, UpdateProfileRequest request) {
        UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();

        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        log.info("BEFORE update - existing attributes: {}", user.getAttributes());
        log.info("Incoming request - phone: {}, address: {}",
                request.getPhoneNumber(), request.getAddress());

        // Build a fresh, guaranteed-mutable copy of the attributes map
        Map<String, List<String>> attributes = new HashMap<>();
        if (user.getAttributes() != null) {
            attributes.putAll(user.getAttributes());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            attributes.put("phoneNumber", List.of(request.getPhoneNumber()));
        }
        if (request.getAddress() != null) {
            attributes.put("address", List.of(request.getAddress()));
        }
        if (request.getEmergencyContact() != null) {
            attributes.put("emergencyContact", List.of(request.getEmergencyContact()));
        }
        if (request.getCertificates() != null) {
            attributes.put("certificates", List.of(String.join(",", request.getCertificates())));
        }
        if (request.getDateOfBirth() != null) {
            attributes.put("dateOfBirth", List.of(request.getDateOfBirth().toString()));
        }

        // Explicitly assign the whole map back onto the user object
        user.setAttributes(attributes);

        log.info("SENDING to Keycloak - attributes: {}", user.getAttributes());

        try {
            keycloak.realm(realm).users().get(userId).update(user);
        } catch (Exception e) {
            log.error("Failed to update user {} in Keycloak: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update profile: " + e.getMessage());
        }

        // Re-fetch fresh from Keycloak to confirm what was actually persisted
        UserRepresentation updated = keycloak.realm(realm).users().get(userId).toRepresentation();
        Map<String, List<String>> updatedAttrs = updated.getAttributes() != null
                ? updated.getAttributes()
                : Collections.emptyMap();

        log.info("AFTER update - re-fetched attributes: {}", updatedAttrs);

        EmployeeProfileResponse response = new EmployeeProfileResponse();
        response.setEmail(updated.getEmail());
        response.setFirstName(updated.getFirstName());
        response.setLastName(updated.getLastName());
        response.setPhoneNumber(getAttr(updatedAttrs, "phoneNumber"));
        response.setJobTitle(getAttr(updatedAttrs, "jobTitle"));
        response.setAddress(getAttr(updatedAttrs, "address"));
        response.setEmergencyContact(getAttr(updatedAttrs, "emergencyContact"));

        String certs = getAttr(updatedAttrs, "certificates");
        response.setCertificates(certs.isBlank() ? List.of() : List.of(certs.split(",")));

        String dob = getAttr(updatedAttrs, "dateOfBirth");
        if (!dob.isBlank()) {
            try {
                response.setDateOfBirth(LocalDate.parse(dob));
            } catch (Exception e) {
                log.warn("Invalid dateOfBirth for user {}: {}", userId, dob);
            }
        }

        return response;
    }
    public EmployeeProfileResponse getMyProfile(String userId) {
        UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();

        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        // Attributes can be null if the user has no custom attributes set
        Map<String, List<String>> attributes = user.getAttributes() != null
                ? user.getAttributes()
                : Collections.emptyMap();

        EmployeeProfileResponse response = new EmployeeProfileResponse();
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhoneNumber(getAttr(attributes, "phoneNumber"));
        response.setJobTitle(getAttr(attributes, "jobTitle"));
        response.setAddress(getAttr(attributes, "address"));
        response.setEmergencyContact(getAttr(attributes, "emergencyContact"));

        String certs = getAttr(attributes, "certificates");
        response.setCertificates(certs.isBlank() ? List.of() : List.of(certs.split(",")));

        String dob = getAttr(attributes, "dateOfBirth");
        if (!dob.isBlank()) {
            try {
                response.setDateOfBirth(LocalDate.parse(dob));
            } catch (Exception e) {
                log.warn("Invalid dateOfBirth format for user {}: {}", userId, dob);
            }
        }

        return response;
    }

    // Helper - safely get a single-value attribute, default to empty string
    private String getAttr(Map<String, List<String>> attributes, String key) {
        List<String> values = attributes.get(key);
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.get(0);
    }

    @Override
    public List<EmployeeProfileResponse> getAllProfiles() {
        List<UserRepresentation> users = keycloak.realm(realm).users().list();

        return users.stream().map(user -> {
            Map<String, List<String>> attributes = user.getAttributes() != null
                    ? user.getAttributes()
                    : Collections.emptyMap();

            EmployeeProfileResponse response = new EmployeeProfileResponse();
            response.setEmail(user.getEmail());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setPhoneNumber(getAttr(attributes, "phoneNumber"));
            response.setJobTitle(getAttr(attributes, "jobTitle"));
            response.setAddress(getAttr(attributes, "address"));
            response.setEmergencyContact(getAttr(attributes, "emergencyContact"));

            String certs = getAttr(attributes, "certificates");
            response.setCertificates(certs.isBlank() ? List.of() : List.of(certs.split(",")));

            String dob = getAttr(attributes, "dateOfBirth");
            if (!dob.isBlank()) {
                try {
                    response.setDateOfBirth(LocalDate.parse(dob));
                } catch (Exception e) {
                    log.warn("Invalid dateOfBirth for user {}: {}", user.getId(), dob);
                }
            }

            return response;
        }).toList();
    }
}

