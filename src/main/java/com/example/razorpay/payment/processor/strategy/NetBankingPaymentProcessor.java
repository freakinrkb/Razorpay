package com.example.razorpay.payment.processor.strategy;

import com.example.razorpay.common.util.RandomizerUtil;
import com.example.razorpay.payment.processor.PaymentProcessor;
import com.example.razorpay.payment.processor.dto.PaymentProcessorRequest;
import com.example.razorpay.payment.processor.dto.PaymentProcessorResponse;
import org.springframework.stereotype.Component;

@Component
public class NetBankingPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String BANK_CODE_FAIL = "BANK_CODE_FAIL";

        String bankCode = (request.methodDetails() != null) ?
                request.methodDetails().get("bank").toString() : null;

        // simulation
        if (BANK_CODE_FAIL.equals(bankCode)) {
            return new PaymentProcessorResponse.Failure("BANK_REJECTED",
                    "Banked rejected the transaction registration"
            );
        }

        String processorRef = "NBK_PROCESSOR_"+ RandomizerUtil.randomBase64(16);

//        String redirectRef = "https://REDIRECT_BANK.com/" +processorRef;

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
