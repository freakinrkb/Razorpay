package com.example.razorpay.merchant.service.impl;

import com.example.razorpay.common.enums.MerchantStatus;
import com.example.razorpay.common.enums.UserRole;
import com.example.razorpay.common.exception.DuplicateResourceException;
import com.example.razorpay.common.exception.ResourceNotFoundException;
import com.example.razorpay.merchant.dto.request.LoginRequest;
import com.example.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.example.razorpay.merchant.dto.response.LoginResponse;
import com.example.razorpay.merchant.dto.response.MerchantResponse;
import com.example.razorpay.merchant.entity.AppUser;
import com.example.razorpay.merchant.entity.Merchant;
import com.example.razorpay.merchant.mapper.MerchantMapper;
import com.example.razorpay.merchant.repository.AppUserRepository;
import com.example.razorpay.merchant.repository.MerchantRepository;
import com.example.razorpay.merchant.security.JwtUtil;
import com.example.razorpay.merchant.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public MerchantResponse signup(MerchantSignupRequest request) {
        if(merchantRepository.existsByEmail(request.email())){
            throw new DuplicateResourceException("DUPLICATE_MERCHANT_EMAIL",
                    "Merchant with email already exists: " + request.email());
        }
        Merchant merchant = merchantMapper.toEntityFromSignUpRequest(request);
        merchant.setStatus(MerchantStatus.PENDING_KYC);

        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .merchant(merchant)
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.OWNER)
                .build();
        appUserRepository.save(appUser);

        return merchantMapper.toResponse(merchant);
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AppUser appUser = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.email()));

        String token = jwtUtil.generateAccessToken(request.email(), appUser.getMerchant().getId(), appUser.getRole().toString());

        return new LoginResponse(token);
    }
}
