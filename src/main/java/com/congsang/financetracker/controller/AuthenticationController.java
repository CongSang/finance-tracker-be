package com.congsang.financetracker.controller;

import com.congsang.financetracker.dto.request.AuthRequestDTO;
import com.congsang.financetracker.dto.request.UserRequestDTO;
import com.congsang.financetracker.dto.response.AuthResponseDTO;
import com.congsang.financetracker.dto.response.UserResponseDTO;
import com.congsang.financetracker.security.UserPrincipal;
import com.congsang.financetracker.service.AuthenticationService;
import com.congsang.financetracker.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody UserRequestDTO userDTO) {
        UserResponseDTO registeredUser = authService.registerUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activate(@RequestParam String token) {
        boolean isActivated = authService.activateUser(token);

        if(isActivated) {
            return ResponseEntity.ok("Profile activated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activation token not found or already used");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO authRequest) {
        return ResponseEntity.ok(authService.authenticate(authRequest));
    }

    @GetMapping("/account-info")
    public ResponseEntity<UserResponseDTO> getPublicProfile() {
        UserResponseDTO user = authService.getPublicProfile(null);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(@RequestParam String refreshToken) {
        return ResponseEntity.ok(refreshTokenService.refreshNewToken(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        refreshTokenService.deleteByUserId(currentUser.getUser());

        return ResponseEntity.noContent().build();
    }

}
