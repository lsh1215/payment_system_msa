package com.example.demo.batch.step;

import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TransactionItemReader implements ItemReader<Transaction> {
    
    private final TransactionRepository transactionRepository;
    private Iterator<Transaction> transactionIterator;
    
    @Override
    public Transaction read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (transactionIterator == null) {
            List<Transaction> unprocessedTransactions = transactionRepository.findUnprocessedTransactionsOrderByCreatedAt(false);
            transactionIterator = unprocessedTransactions.iterator();
        }
        
        return transactionIterator.hasNext() ? transactionIterator.next() : null;
    }
}
