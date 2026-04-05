package com.congsang.financetracker.service;

import com.congsang.financetracker.common.enums.AuthProvider;
import com.congsang.financetracker.common.enums.Role;
import com.congsang.financetracker.common.enums.Status;
import com.congsang.financetracker.dto.request.AuthRequestDTO;
import com.congsang.financetracker.dto.request.UserRequestDTO;
import com.congsang.financetracker.dto.response.AuthResponseDTO;
import com.congsang.financetracker.dto.response.UserResponseDTO;
import com.congsang.financetracker.entity.RefreshTokenEntity;
import com.congsang.financetracker.entity.UserEntity;
import com.congsang.financetracker.exception.BadRequestException;
import com.congsang.financetracker.exception.UnauthorizedException;
import com.congsang.financetracker.mapper.UserMapper;
import com.congsang.financetracker.repository.UserRepository;
import com.congsang.financetracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final MailService mailService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.activation.url}")
    private String backendURL;

    public UserResponseDTO registerUser(UserRequestDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BadRequestException("Email này đã được sử dụng. Vui lòng chọn email khác!");
        }

        UserEntity newUser = userMapper.toEntity(userDTO);
        newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        newUser.setActivationToken(UUID.randomUUID().toString());
        newUser.setProvider(AuthProvider.LOCAL);
        newUser.setStatus(Status.INACTIVE);
        newUser.setRole(Role.CUSTOMER);

        //send activation email
        String activationLink = backendURL + "/api/auth/activate?token=" + newUser.getActivationToken();
        String subject = "Xác thực tài khoản Finance Tracker";
        String body = "Chào " + newUser.getFullName() + ",\n\n" +
                "Vui lòng click vào link sau để kích hoạt tài khoản của bạn:\n" + activationLink;
        mailService.sendEmail(newUser.getEmail(), subject, body);
        return userMapper.toDTO(userRepository.save(newUser));
    }

    public UserEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("Vui lòng đăng nhập để thực hiện thao tác này!");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        assert principal != null;
        return principal.getUser();
    }

    public UserResponseDTO getPublicProfile(String email) {
        UserEntity currentUser = null;
        if(email == null) {
            currentUser = getCurrentProfile();
        } else {
            currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
        }

        return userMapper.toDTO(currentUser);
    }

    public boolean activateUser(String activationToken) {
        return userRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setStatus(Status.ACTIVE);
                    userRepository.save(profile);
                    return  true;
                })
                .orElse(false);
    }

    public AuthResponseDTO authenticate(AuthRequestDTO authRequestDTO) throws BadCredentialsException {
        Optional<UserEntity> userOptional = userRepository.findByEmail(authRequestDTO.getEmail());
        UserEntity user;

        if(userOptional.isPresent()) {
            user = userOptional.get();
            if(user.getPassword().isEmpty() && user.getProvider() == AuthProvider.GOOGLE) {
                throw new BadRequestException(
                        "Tài khoản này được tạo qua Google. Vui lòng đăng nhập bằng Google và vào cài đặt để thiết lập mật khẩu");
            }
        } else {
            throw  new UsernameNotFoundException("Tài khoản chưa được đăng ký!");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequestDTO.getEmail(),
                        authRequestDTO.getPassword()
                )
        );

        UserPrincipal principal = UserPrincipal.create(user);
        String jwtToken = jwtService.generateToken(principal);
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return AuthResponseDTO.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .user(userMapper.toDTO(principal.getUser()))
                .build();
    }
}
