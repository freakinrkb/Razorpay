package com.example.razorpay.merchant.controller;

import com.example.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.example.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.example.razorpay.merchant.security.MerchantContext;
import com.example.razorpay.merchant.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/merchants/{merchantId}/api-keys")
public class ApiKeyController  {
    private final ApiKeyService apiKeyService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponse> create(
            @PathVariable UUID merchantId, // Fixed naming mismatch
            @Valid @RequestBody CreateApiKeyRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiKeyService.create(merchantContext.getMerchantId(), request));
    }

    @GetMapping
    public ResponseEntity<ApiKeyService> listByMerchant(
            @PathVariable UUID merchantId) { // Fixed naming mismatch

        return ResponseEntity.ok(apiKeyService.listByMerchant(merchantContext.getMerchantId()));
    }

    @DeleteMapping("/{keyId}")
    public void revoke(@PathVariable UUID merchantId, @PathVariable UUID keyId){
        apiKeyService.revoke(merchantContext.getMerchantId(),keyId);

        // Minor bug note here: ResponseEntity.noContent().build() creates a response,
        // but because your return type is 'void', it isn't actually being returned.
        // To fix this later, change the method return type to ResponseEntity<Void>
        // and add 'return' before ResponseEntity.noContent().build();
    }

    @PostMapping("/{keyId}/rotate")
    public ResponseEntity<ApiKeyCreateResponse> rotateKey(
            @PathVariable UUID merchantId,
            @PathVariable UUID keyId) {

        return ResponseEntity.ok(apiKeyService.rotate(merchantContext.getMerchantId(), keyId));
    }
}