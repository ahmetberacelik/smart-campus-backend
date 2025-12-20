package com.smartcampus.meal.dto.response;

import com.smartcampus.meal.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private Transaction.TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private Transaction.ReferenceType referenceType;
    private Long referenceId;
    private String description;
    private String paymentMethod;
    private Transaction.TransactionStatus status;
    private LocalDateTime createdAt;
}
