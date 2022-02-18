package com.hkjc.test.solacetest.noncloud.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hkjc.test.solacetest.noncloud.domain.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

}
