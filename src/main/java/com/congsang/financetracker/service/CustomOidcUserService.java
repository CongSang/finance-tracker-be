package com.congsang.financetracker.service;

import com.congsang.financetracker.common.enums.AuthProvider;
import com.congsang.financetracker.common.enums.Status;
import com.congsang.financetracker.entity.UserEntity;
import com.congsang.financetracker.repository.UserRepository;
import com.congsang.financetracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Lấy thông tin User mặc định từ Google
        OidcUser oidcUser = super.loadUser(userRequest);
    System.out.println(oidcUser);
        try {
            return processOAuth2User(oidcUser);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OidcUser processOAuth2User(OidcUser oidcUser) {
        String email = oidcUser.getEmail();

        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> registerNewUser(oidcUser));

        return (OidcUser) UserPrincipal.create(user, oidcUser.getAttributes());
    }

    private UserEntity registerNewUser(OidcUser oidcUser) {
        UserEntity user = new UserEntity();
        user.setEmail(oidcUser.getEmail());
        user.setFullName(oidcUser.getFullName());
        user.setAvatarUrl(oidcUser.getPicture());
        user.setStatus(Status.ACTIVE);
        user.setProvider(AuthProvider.GOOGLE);
        return userRepository.save(user);
    }
}
