package com.smartcampus.meal.service;

import com.smartcampus.meal.dto.response.WalletResponse;
import com.smartcampus.meal.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface WalletService {
    
    WalletResponse getWalletByUserId(Long userId);
    
    WalletResponse getOrCreateWallet(Long userId);
    
    WalletResponse topUp(Long userId, BigDecimal amount, String paymentMethod, String paymentReference);
    
    WalletResponse debit(Long userId, BigDecimal amount, String referenceType, Long referenceId, String description);
    
    WalletResponse refund(Long userId, BigDecimal amount, Long originalTransactionId, String description);
    
    Page<TransactionResponse> getTransactionHistory(Long userId, Pageable pageable);
    
    boolean hasEnoughBalance(Long userId, BigDecimal amount);
    
    boolean canUseScholarship(Long userId);
    
    void useScholarship(Long userId);
    
    // Stripe webhook handler
    void handlePaymentWebhook(String payload, String signature);
}
