package com.indcaffe.service;

import com.indcaffe.entity.Product;
import com.indcaffe.entity.StockTransaction;
import com.indcaffe.entity.SurplusPost;
import com.indcaffe.entity.Cafe;
import com.indcaffe.repository.ProductRepository;
import com.indcaffe.repository.StockTransactionRepository;
import com.indcaffe.repository.SurplusPostRepository;
import com.indcaffe.repository.CafeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDateTime;

@Service
public class TransactionService {
    @Autowired StockTransactionRepository stockTxRepo;
    @Autowired ProductRepository productRepo;
    @Autowired SurplusPostRepository surplusRepo;
    @Autowired CafeRepository cafeRepository;

    public List<StockTransaction> getTransactionsByCafe(Long cafeId) {
        return stockTxRepo.findByProductCafeId(cafeId);
    }

    @Transactional
    public StockTransaction addStockTransaction(StockTransaction tx) {
        Product product = productRepo.findById(tx.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        tx.setProduct(product); // Fix transient product

        if ("INCOMING".equals(tx.getType().name())) {
            product.setCurrentStock(product.getCurrentStock() + tx.getQuantity());
        } else if ("OUTGOING".equals(tx.getType().name())) {
            if (product.getCurrentStock() < tx.getQuantity()) {
                throw new RuntimeException("Insufficient stock");
            }
            product.setCurrentStock(product.getCurrentStock() - tx.getQuantity());
        }
        productRepo.save(product);
        return stockTxRepo.save(tx);
    }

    public List<SurplusPost> getAllSurplus() {
        return surplusRepo.findAll();
    }
    
    public List<SurplusPost> getSurplusByCafe(Long cafeId) {
        return surplusRepo.findByCafeId(cafeId);
    }

    public SurplusPost createSurplus(SurplusPost surplus) {
        Cafe cafe = cafeRepository.findById(surplus.getCafe().getId()).orElseThrow();
        Product product = productRepo.findById(surplus.getProduct().getId()).orElseThrow();
        
        surplus.setCafe(cafe);
        surplus.setProduct(product);
        surplus.setCreatedAt(LocalDateTime.now());
        surplus.setStatus(com.indcaffe.entity.SurplusStatus.AVAILABLE);
        
        // Reduce stock when donating surplus
        if (product.getCurrentStock() < surplus.getQuantity()) {
            throw new RuntimeException("Insufficient stock to donate");
        }
        product.setCurrentStock(product.getCurrentStock() - surplus.getQuantity());
        productRepo.save(product);
        
        return surplusRepo.save(surplus);
    }
}
