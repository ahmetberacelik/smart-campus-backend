package com.smartcampus.meal.service.impl;

import com.smartcampus.meal.dto.response.TransactionResponse;
import com.smartcampus.meal.dto.response.WalletResponse;
import com.smartcampus.meal.entity.Transaction;
import com.smartcampus.meal.entity.Wallet;
import com.smartcampus.meal.repository.TransactionRepository;
import com.smartcampus.meal.repository.WalletRepository;
import com.smartcampus.meal.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cüzdan bulunamadı"));
        return mapToWalletResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse getOrCreateWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .map(this::mapToWalletResponse)
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder()
                            .userId(userId)
                            .balance(BigDecimal.ZERO)
                            .currency("TRY")
                            .isScholarship(false)
                            .dailyScholarshipLimit(2)
                            .scholarshipUsedToday(0)
                            .isActive(true)
                            .build();
                    Wallet saved = walletRepository.save(newWallet);
                    log.info("Yeni cüzdan oluşturuldu: userId={}", userId);
                    return mapToWalletResponse(saved);
                });
    }

    @Override
    @Transactional
    public WalletResponse topUp(Long userId, BigDecimal amount, String paymentMethod, String paymentReference) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new RuntimeException("Cüzdan bulunamadı"));

        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        // Transaction kaydı
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .type(Transaction.TransactionType.CREDIT)
                .amount(amount)
                .balanceAfter(newBalance)
                .referenceType(Transaction.ReferenceType.TOPUP)
                .description("Para yükleme")
                .paymentMethod(paymentMethod)
                .paymentReference(paymentReference)
                .status(Transaction.TransactionStatus.COMPLETED)
                .build();
        transactionRepository.save(transaction);

        log.info("Para yüklendi: userId={}, amount={}, newBalance={}", userId, amount, newBalance);
        return mapToWalletResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse debit(Long userId, BigDecimal amount, String referenceType, Long referenceId, String description) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new RuntimeException("Cüzdan bulunamadı"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Yetersiz bakiye");
        }

        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        // Transaction kaydı
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .type(Transaction.TransactionType.DEBIT)
                .amount(amount)
                .balanceAfter(newBalance)
                .referenceType(Transaction.ReferenceType.valueOf(referenceType))
                .referenceId(referenceId)
                .description(description)
                .status(Transaction.TransactionStatus.COMPLETED)
                .build();
        transactionRepository.save(transaction);

        log.info("Harcama yapıldı: userId={}, amount={}, newBalance={}", userId, amount, newBalance);
        return mapToWalletResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse refund(Long userId, BigDecimal amount, Long originalTransactionId, String description) {
        return topUp(userId, amount, "REFUND", "REF-" + originalTransactionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(Long userId, Pageable pageable) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cüzdan bulunamadı"));

        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable)
                .map(this::mapToTransactionResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEnoughBalance(Long userId, BigDecimal amount) {
        return walletRepository.findByUserId(userId)
                .map(wallet -> wallet.getBalance().compareTo(amount) >= 0)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUseScholarship(Long userId) {
        return walletRepository.findByUserId(userId)
                .map(Wallet::canUseScholarship)
                .orElse(false);
    }

    @Override
    @Transactional
    public void useScholarship(Long userId) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new RuntimeException("Cüzdan bulunamadı"));
        wallet.useScholarship();
        walletRepository.save(wallet);
    }

    @Override
    public void handlePaymentWebhook(String payload, String signature) {
        // Stripe webhook handler - basit implementasyon
        log.info("Stripe webhook alındı: {}", payload.substring(0, Math.min(100, payload.length())));
        // TODO: Gerçek Stripe webhook doğrulama ve işleme
    }

    private WalletResponse mapToWalletResponse(Wallet wallet) {
        wallet.resetDailyScholarship(); // Günlük burs limitini kontrol et
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .isScholarship(wallet.getIsScholarship())
                .dailyScholarshipLimit(wallet.getDailyScholarshipLimit())
                .scholarshipUsedToday(wallet.getScholarshipUsedToday())
                .isActive(wallet.getIsActive())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .referenceType(transaction.getReferenceType())
                .referenceId(transaction.getReferenceId())
                .description(transaction.getDescription())
                .paymentMethod(transaction.getPaymentMethod())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
