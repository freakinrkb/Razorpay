package com.example.razorpay.payment.service;

import com.example.razorpay.payment.dto.request.CreateOrderRequest;
import com.example.razorpay.payment.dto.response.OrderResponse;
import com.example.razorpay.payment.dto.response.PaymentResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse create(UUID merchantId, @Valid CreateOrderRequest request);

    OrderResponse getById(UUID merchantId, UUID orderId);

    OrderResponse cancel(UUID merchantId, UUID orderId);

    List<PaymentResponse> listPayments(UUID merchantId, UUID orderId);
}
