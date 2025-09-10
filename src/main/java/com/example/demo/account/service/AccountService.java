package com.example.demo.account.service;

import com.example.demo.account.entity.Account;
import com.example.demo.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {
    
    private final AccountRepository accountRepository;
    
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
    
    public Optional<Account> getAccountById(String id) {
        return accountRepository.findById(id);
    }
    
    public List<Account> getAccountsByStatus(Account.AccountStatus status) {
        return accountRepository.findByStatus(status);
    }
    
    public List<Account> getAccountsByType(Account.AccountType accountType) {
        return accountRepository.findByAccountType(accountType);
    }
    
    @Transactional
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }
    
    @Transactional
    public Account updateAccount(Account account) {
        return accountRepository.save(account);
    }
    
    @Transactional
    public void deleteAccount(String id) {
        accountRepository.deleteById(id);
    }
}
