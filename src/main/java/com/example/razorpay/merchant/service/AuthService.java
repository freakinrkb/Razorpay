package com.example.razorpay.merchant.service;

import com.example.razorpay.merchant.dto.request.LoginRequest;
import com.example.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.example.razorpay.merchant.dto.response.LoginResponse;
import com.example.razorpay.merchant.dto.response.MerchantResponse;
import jakarta.validation.Valid;

public interface AuthService {
    MerchantResponse signup(@Valid MerchantSignupRequest request);

    LoginResponse login(@Valid LoginRequest request);
}
