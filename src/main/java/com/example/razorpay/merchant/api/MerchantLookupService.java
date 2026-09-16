package com.example.razorpay.merchant.api;


import com.example.razorpay.common.dto.SettlementBankDetails;
import com.example.razorpay.common.dto.WebhookTarget;

import java.util.List;
import java.util.UUID;

public interface MerchantLookupService {

    List<WebhookTarget> getActiveConfigsForEvent(UUID merchantId, String eventType);

    List<UUID> listActiveMerchantIds();

    SettlementBankDetails getSettlementBankDetails(UUID merchantId);
}
