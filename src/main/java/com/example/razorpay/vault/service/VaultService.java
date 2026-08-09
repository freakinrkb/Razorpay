package com.example.razorpay.vault.service;

import com.example.razorpay.common.entity.Money;
import com.example.razorpay.payment.processor.dto.PaymentProcessorResponse;
import com.example.razorpay.vault.dto.request.TokenizeRequest;
import com.example.razorpay.vault.dto.response.TokenizeResponse;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
    PaymentProcessorResponse charge(UUID uuid, String token, Money amount, Map<String, Object> stringObjectMap);

    TokenizeResponse tokenize(@Valid TokenizeRequest request, java.util.UUID merchantId);
}
