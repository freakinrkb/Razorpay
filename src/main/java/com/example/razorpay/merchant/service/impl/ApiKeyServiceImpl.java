package com.example.razorpay.merchant.service.impl;

import com.example.razorpay.common.config.AppConfig;
import com.example.razorpay.common.exception.ResourceNotFoundException;
import com.example.razorpay.common.util.RandomizerUtil;
import com.example.razorpay.merchant.cache.ApiKeyCache;
import com.example.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.example.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.example.razorpay.merchant.entity.ApiKey;
import com.example.razorpay.merchant.entity.Merchant;
import com.example.razorpay.merchant.mapper.ApiKeyMapper;
import com.example.razorpay.merchant.repository.ApiKeyRepository;
import com.example.razorpay.merchant.repository.MerchantRepository;
import com.example.razorpay.merchant.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ApiKeyServiceImpl implements ApiKeyService {

    private final MerchantRepository merchantRepository;
    private final ApiKeyMapper apiKeyMapper;
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApiKeyCache apiKeyCache;


    @Transactional
    @Override
    public ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow
                (() -> new ResourceNotFoundException("merchant", merchantId));
        String keyId = "rzp_"+request.environment().name().toLowerCase()+"_"+ RandomizerUtil.randomBase64(24);
        String rawSecret = RandomizerUtil.randomBase64(40);

        ApiKey apiKey = ApiKey.builder()
                .merchant(merchant)
                .keyId(keyId)
                .keySecretHash(passwordEncoder.encode(rawSecret))
                .environment(request.environment())
                .build();
        apiKey = apiKeyRepository.save(apiKey);
        return new ApiKeyCreateResponse(apiKey.getId(), keyId,
                rawSecret, request.environment());
    }

    @Override
    public ApiKeyService listByMerchant(UUID merchantId) {
        return (ApiKeyService) apiKeyMapper.toResponseList(apiKeyRepository.findByMerchant_Id(merchantId));
    }

    @Transactional
    @Override
    public void revoke(UUID merchantId, UUID keyId) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .filter(k -> k.getMerchant().getId().equals(merchantId))
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey", keyId));
        key.setEnabled(false);
        apiKeyCache.evict(key.getKeyId());
    }
    @Transactional
    @Override
    public ApiKeyCreateResponse rotate(UUID merchantId, UUID keyId) {
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .filter(k -> k.getMerchant().getId().equals(merchantId))
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey", keyId));

        if(!apiKey.isEnabled()) throw new RuntimeException("Cannot do this");

        String newRawSecret = RandomizerUtil.randomBase64(40);
        apiKey.setPreviousKeySecretHash(apiKey.getKeySecretHash());
        apiKey.setKeySecretHash(passwordEncoder.encode(newRawSecret));
        apiKey.setRotatedAt(LocalDateTime.now());
        apiKey.setGracePeriodExpiresAt(LocalDateTime.now().plusHours(24));

        apiKey = apiKeyRepository.save(apiKey);
        apiKeyCache.evict(apiKey.getKeyId());

        return apiKeyMapper.toCreateResponse(apiKey, newRawSecret);
    }
}
