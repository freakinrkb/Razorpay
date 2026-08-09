package com.example.razorpay.vault.dto.response;

import com.example.razorpay.common.enums.CardBrand;

public record TokenizeResponse(
        String token,
        String lastFour,
        CardBrand brand,
        Integer expiryMonth,
        Integer expiryYear
) {
}
