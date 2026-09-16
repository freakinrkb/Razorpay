package com.example.razorpay.merchant.repository;

import com.example.razorpay.common.enums.MerchantStatus;
import com.example.razorpay.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    boolean existsByEmail(String email);
    List<Merchant> findByStatus(MerchantStatus merchantStatus);

}
