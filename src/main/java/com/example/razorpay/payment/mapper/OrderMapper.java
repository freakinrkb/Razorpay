package com.example.razorpay.payment.mapper;

import com.example.razorpay.payment.dto.response.OrderResponse;
import com.example.razorpay.payment.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    @Mapping(source = "orderStatus", target = "status")
    OrderResponse toResponse(OrderRecord orderRecord);
}
