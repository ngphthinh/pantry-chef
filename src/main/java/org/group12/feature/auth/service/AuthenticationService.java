package org.group12.feature.auth.service;

import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.exception.UnauthenticatedException;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.dto.request.*;
import org.group12.feature.auth.dto.response.AuthenticateResponse;
import org.group12.feature.auth.dto.response.IntrospectResponse;
import org.group12.feature.auth.dto.response.UserResponse;
import org.group12.feature.auth.entity.User;
import org.group12.feature.auth.enums.MeasurementSystem;
import org.group12.feature.auth.enums.RoleName;
import org.group12.feature.auth.enums.ThemeMode;
import org.group12.feature.auth.exception.*;
import org.group12.feature.auth.mapper.UserMapper;
import org.group12.feature.auth.repository.RoleRepository;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.common.exception.AppException;
import org.group12.common.exception.ErrorCode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlackListService tokenBlackListService;
    private final AppUtil appUtil;
    private final UserMapper userMapper;
    private final ChangePasswordService changePasswordService;
    private final EmailService emailService;
    private final PasswordGenerator passwordGenerator;


    @Transactional
    public UserResponse register(UserCreateRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailDuplicateException();
        }

        User user = userMapper.toUser(request);
        user.setId(appUtil.generateUUID());
        user.setMeasurementSystem(MeasurementSystem.METRIC);
        user.setThemeMode(ThemeMode.SYSTEM);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(roleRepository.findByName(RoleName.ROLE_USER).orElseThrow(RoleNotFoundException::new)));
        user = userRepository.save(user);
        return userMapper.toUserResponse(user);
    }


    @Transactional(readOnly = true)
    public AuthenticateResponse authenticate(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // HIỆN KHÔNG CÓ CHỨC NĂNG BAN
//        if (user.getUserStatus() == UserStatus.BANNED) {
//            throw new AppException(ErrorCode.ACCOUNT_BANNED);
//        }
        String refreshToken = jwtService.generateRefreshToken(user);
        log.info("User {} logged in", user.getEmail());

        return buildAuthResponse(user, refreshToken);
    }

    private AuthenticateResponse buildAuthResponse(User user, String refreshToken) {
        return AuthenticateResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .user(userMapper.toUserResponse(user))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthenticateResponse refreshToken(RefreshTokenRequest refreshToken) {

        String refreshTokenStr = refreshToken.getRefreshToken();

        if (!refreshTokenService.isRefreshTokenExists(refreshTokenStr)) {
            throw new TokenExpiredException();
        }

        String username = refreshTokenService.getUser(refreshTokenStr);

        User user = userRepository.findById(UUID.fromString(username))
                .orElseThrow(UserNotFoundException::new);

        String newRefreshToken = jwtService.generateRefreshToken(user);

        refreshTokenService.deleteRefreshToken(refreshTokenStr);

        return buildAuthResponse(user, newRefreshToken);

    }


    public void logout(LogoutRequest request) {
        String token = request.getAccessToken();
        String refreshToken = request.getRefreshToken();
        SignedJWT signedJWT = jwtService.verifyToken(token);

        try {
            String jitToken = signedJWT.getJWTClaimsSet().getJWTID();
            long expirationTime = getSecondsUntilExpiration(signedJWT.getJWTClaimsSet().getExpirationTime());

            // store blacklist with time to live equals to the remaining time of the token
            tokenBlackListService.blacklistToken(jitToken, expirationTime);

            refreshTokenService.deleteRefreshToken(refreshToken);
        } catch (ParseException e) {
            throw new UnauthenticatedException();
        }

    }

    private long getSecondsUntilExpiration(Date expirationDate) {
        long expirationEpoch = expirationDate.toInstant().getEpochSecond();
        long nowEpoch = Instant.now().getEpochSecond();
        return expirationEpoch - nowEpoch;
    }


    /**
     * TODO: KHI NÀO LÀM BAN THÌ MỞ LÀM Ở CHỖ COMMENT
     */
    public IntrospectResponse introspect(IntrospectRequest request) {
        IntrospectResponse introspectResponse = jwtService.introspect(request.getAccessToken());
//        if (introspectResponse.isActive() && introspectResponse.getUserId() != null) {
//            banService.findActiveBanForUser(UUID.fromString(introspectResponse.getUserId()))
//                    .ifPresent(ban -> {
//                        throw new AccountBanException(ban.getEndDate());
//                    });
//        }
        return introspectResponse;
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = userRepository.findById(appUtil.userIdFromAuthentication())
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new PasswordInCorrectException();
        }
        // Invalidate all tokens by saving the current timestamp in Redis
        String changedPasswordDate = String.valueOf(Instant.now().toEpochMilli());
        changePasswordService.saveTokenInvalidationTimestamp(user.getId().toString(), changedPasswordDate);

        refreshTokenService.revokeAllRefreshTokens(user.getId().toString());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        IntrospectResponse introspectResponse = introspect(new IntrospectRequest(request.getToken()));
        User user = userRepository.findById(UUID.fromString(introspectResponse.getUserId()))
                .orElseThrow(UserNotFoundException::new);
        // Invalidate all tokens by saving the current timestamp in Redis
        String changedPasswordDate = String.valueOf(Instant.now().toEpochMilli());
        changePasswordService.saveTokenInvalidationTimestamp(user.getId().toString(), changedPasswordDate);
        refreshTokenService.revokeAllRefreshTokens(user.getId().toString());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresentOrElse(user -> {
            String newPassword = passwordGenerator.generatePassword();

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Invalidate all refresh tokens for the user
            refreshTokenService.revokeAllRefreshTokens(user.getId().toString());

            emailService.sendResetPasswordEmail(
                    user.getEmail(),
                    newPassword
            );
        }, () -> {
            throw new UserNotFoundException();
        });
    }


}