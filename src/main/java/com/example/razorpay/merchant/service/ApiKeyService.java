package com.example.razorpay.merchant.service;

import com.example.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.example.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import jakarta.validation.Valid;

import java.util.UUID;

public interface ApiKeyService {
    ApiKeyCreateResponse create(UUID merchantId, @Valid CreateApiKeyRequest request);

    ApiKeyService listByMerchant(UUID merchantId);

    void revoke(UUID merchantId, UUID keyId);

    ApiKeyCreateResponse rotate(UUID merchantId, UUID keyId);
}
