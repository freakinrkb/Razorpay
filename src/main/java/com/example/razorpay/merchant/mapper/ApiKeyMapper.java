package com.example.razorpay.merchant.mapper;

import com.example.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.example.razorpay.merchant.dto.response.ApiKeyResponse;
import com.example.razorpay.merchant.entity.ApiKey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApiKeyMapper {

    @Mapping(target = "keySecret", expression = "java(rawSecret)")
    ApiKeyCreateResponse toCreateResponse(ApiKey apiKey, String rawSecret);

    List<ApiKeyResponse> toResponseList(List<ApiKey> apiKeyList);
}
