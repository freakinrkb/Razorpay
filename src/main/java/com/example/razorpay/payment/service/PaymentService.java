package com.example.razorpay.payment.service;

import com.example.razorpay.payment.dto.request.PaymentInitRequest;
import com.example.razorpay.payment.dto.response.PaymentResponse;
import jakarta.validation.Valid;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse initiate(UUID merchantId, @Valid PaymentInitRequest request);

    PaymentResponse capture(UUID merchantId, UUID paymentId);

    void resolveAuthorization(UUID paymentId, boolean approve, String bankRef, String errorCode, String errorDescription);

}
