package com.example.razorpay.payment.config;

import com.example.razorpay.common.enums.PaymentMethod;
import com.example.razorpay.payment.processor.PaymentProcessor;
import com.example.razorpay.payment.processor.strategy.CardPaymentProcessor;
import com.example.razorpay.payment.processor.strategy.NetBankingPaymentProcessor;
import com.example.razorpay.payment.processor.strategy.UpiPaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
@RequiredArgsConstructor
@Configuration
public class PaymentProcessorConfig {
    private final CardPaymentProcessor cardPaymentProcessor;
    private final NetBankingPaymentProcessor netBankingPaymentProcessor;
    private final UpiPaymentProcessor upiPaymentProcessor;

    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap() {
        return Map.of(
                PaymentMethod.CARD, cardPaymentProcessor,
                PaymentMethod.NETBANKING, netBankingPaymentProcessor,
                PaymentMethod.UPI, upiPaymentProcessor
        );
    }
}
