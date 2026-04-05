package com.congsang.financetracker.config;

import com.congsang.financetracker.entity.RefreshTokenEntity;
import com.congsang.financetracker.security.UserPrincipal;
import com.congsang.financetracker.service.JwtService;
import com.congsang.financetracker.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.activation.frontend.url}")
    private String frontendURL;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized User Type");
            return;
        }
        Long userId = principal.getUser().getId();
        String token = jwtService.generateToken(principal);
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(userId);

        String targetUrl = UriComponentsBuilder.fromUriString(frontendURL + "/auth/redirect")
                .queryParam("token", token)
                .queryParam("refreshToken", refreshToken.getToken())
                .build().encode().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}