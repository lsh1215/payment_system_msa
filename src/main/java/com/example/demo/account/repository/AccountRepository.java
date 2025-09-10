package com.example.demo.account.repository;

import com.example.demo.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    
    List<Account> findByStatus(Account.AccountStatus status);
    
    List<Account> findByAccountType(Account.AccountType accountType);
    
    List<Account> findByStatusAndAccountType(Account.AccountStatus status, Account.AccountType accountType);
}
