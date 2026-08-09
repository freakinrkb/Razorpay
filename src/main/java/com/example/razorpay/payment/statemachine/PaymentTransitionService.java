package com.example.razorpay.payment.statemachine;

import com.example.razorpay.common.enums.PaymentActor;
import com.example.razorpay.common.enums.PaymentEvent;
import com.example.razorpay.common.enums.PaymentStatus;
import com.example.razorpay.payment.entity.Payment;
import com.example.razorpay.payment.entity.PaymentTransitionLog;
import com.example.razorpay.payment.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentTransitionService {
    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;

    public PaymentStatus apply(Payment payment, PaymentEvent event) {
        PaymentStatus next = paymentStateMachine.transition(payment.getStatus(), event);
        PaymentTransitionLog log = PaymentTransitionLog.builder()
                .payment(payment)
                .fromStatus(payment.getStatus())
                .event(event)
                .toStatus(next)
                .actor(PaymentActor.SYSTEM) //TODO: fetch merchant context to identify actor
                .occurredAt(LocalDateTime.now())
                .build();

        payment.setStatus(next);
        paymentTransitionLogRepository.save(log);
        return next;
    }
}
