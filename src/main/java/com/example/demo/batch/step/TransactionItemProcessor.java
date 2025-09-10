package com.example.demo.batch.step;

import com.example.demo.transaction.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionItemProcessor implements ItemProcessor<Transaction, Transaction> {
    
    @Override
    public Transaction process(Transaction transaction) throws Exception {
        log.debug("Processing transaction: ID={}, Account={}, Amount={}, Type={}", 
                 transaction.getId(), transaction.getAccountId(), transaction.getAmount(), transaction.getType());
        
        // 여기서는 단순히 로깅만 수행하고, 실제 정산 로직은 Stored Procedure에서 처리
        // 필요시 추가적인 검증 로직을 여기에 추가할 수 있음
        
        return transaction;
    }
}
