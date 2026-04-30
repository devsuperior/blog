package com.devsuperior.billing.repository;

import com.devsuperior.billing.entity.ProcessedPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedPaymentRepository extends JpaRepository<ProcessedPayment, Long> {

    boolean existsByPaymentId(String paymentId);
}
