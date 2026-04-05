package com.congsang.financetracker.service;

import com.congsang.financetracker.common.enums.AuthProvider;
import com.congsang.financetracker.common.enums.Status;
import com.congsang.financetracker.entity.UserEntity;
import com.congsang.financetracker.repository.UserRepository;
import com.congsang.financetracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println(oAuth2User);
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        UserEntity user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getProvider() == AuthProvider.LOCAL) {
                user = userRepository.save(user);
            }
        } else {
            user = new UserEntity();
            user.setEmail(email);
            user.setFullName(name);
            user.setStatus(Status.ACTIVE);
            user.setProvider(AuthProvider.GOOGLE);

            user = userRepository.save(user);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }
}