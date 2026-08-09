package com.example.razorpay.merchant.dto.request;

import com.example.razorpay.common.enums.Environment;

public record CreateApiKeyRequest(
        Environment environment
) {
}
