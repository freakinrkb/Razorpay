package com.example.razorpay.payment.controller;

import com.example.razorpay.payment.dto.request.PaymentInitRequest;
import com.example.razorpay.payment.dto.response.PaymentResponse;
import com.example.razorpay.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/v1/payments")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    UUID merchantId = UUID.fromString("1f6caca9-cab9-40cd-a491-a714146e5a55"); //TODO: replace it with MerchantContext

    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@Valid @RequestBody PaymentInitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiate(merchantId, request));
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponse> capture(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.capture(merchantId, paymentId));
    }
}
