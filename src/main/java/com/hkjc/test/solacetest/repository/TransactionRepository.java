package com.hkjc.test.solacetest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hkjc.test.solacetest.domain.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

}
