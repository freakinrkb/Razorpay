package com.example.razorpay.payment.mapper;

import com.example.razorpay.payment.dto.response.PaymentResponse;
import com.example.razorpay.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    @Mapping(target = "orderId", source = "order.id")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> paymentList);
}
