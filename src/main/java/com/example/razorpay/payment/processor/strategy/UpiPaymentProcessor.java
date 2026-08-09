package com.example.razorpay.payment.processor.strategy;

import com.example.razorpay.common.util.RandomizerUtil;
import com.example.razorpay.payment.processor.PaymentProcessor;
import com.example.razorpay.payment.processor.dto.PaymentProcessorRequest;
import com.example.razorpay.payment.processor.dto.PaymentProcessorResponse;
import org.springframework.stereotype.Component;

@Component
public class UpiPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String VPA_CODE_FAIL = "fail@okaxis";
        String bankCode = request.methodDetails() != null ?
                request.methodDetails().get("vpa").toString() : null;

        if (VPA_CODE_FAIL.equals(bankCode)) {
            return new PaymentProcessorResponse.Failure("UPI_REJECTED",
                    "Banked rejected the transaction registration"
            );
        }
        String processorRef = "UPI_PROCESSOR_"+ RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
