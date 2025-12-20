package com.smartcampus.meal.controller;

import com.smartcampus.meal.dto.request.TopUpRequest;
import com.smartcampus.meal.dto.response.ApiResponse;
import com.smartcampus.meal.dto.response.TransactionResponse;
import com.smartcampus.meal.dto.response.WalletResponse;
import com.smartcampus.meal.security.CustomUserDetails;
import com.smartcampus.meal.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * Cüzdan bilgilerini getir (yoksa oluştur)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<WalletResponse>> getMyWallet(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WalletResponse wallet = walletService.getOrCreateWallet(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    /**
     * Bakiye sorgula
     */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<WalletResponse>> getBalance(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WalletResponse wallet = walletService.getWalletByUserId(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    /**
     * Para yükle
     */
    @PostMapping("/topup")
    public ResponseEntity<ApiResponse<WalletResponse>> topUp(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TopUpRequest request) {
        
        WalletResponse wallet = walletService.topUp(
                userDetails.getId(),
                request.getAmount(),
                request.getPaymentMethod(),
                "TOPUP-" + System.currentTimeMillis()
        );
        return ResponseEntity.ok(ApiResponse.success("Para yüklendi", wallet));
    }

    /**
     * İşlem geçmişi
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        Page<TransactionResponse> transactions = walletService.getTransactionHistory(userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    /**
     * Burs durumunu kontrol et
     */
    @GetMapping("/scholarship")
    public ResponseEntity<ApiResponse<Boolean>> checkScholarship(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean canUse = walletService.canUseScholarship(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(canUse));
    }

    /**
     * Stripe webhook (public endpoint)
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        walletService.handlePaymentWebhook(payload, signature);
        return ResponseEntity.ok("OK");
    }
}
