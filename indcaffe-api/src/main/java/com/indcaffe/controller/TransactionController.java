package com.indcaffe.controller;

import com.indcaffe.entity.StockTransaction;
import com.indcaffe.entity.SurplusPost;
import com.indcaffe.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @GetMapping("/cafe/{cafeId}")
    public ResponseEntity<?> getTransactions(@PathVariable Long cafeId) {
        return ResponseEntity.ok(transactionService.getTransactionsByCafe(cafeId));
    }

    @PostMapping("/")
    @PreAuthorize("hasRole('CAFE')")
    public ResponseEntity<?> addTransaction(@RequestBody StockTransaction tx) {
        try {
            return ResponseEntity.ok(transactionService.addStockTransaction(tx));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/surplus")
    public ResponseEntity<?> getAllSurplus() {
        return ResponseEntity.ok(transactionService.getAllSurplus());
    }

    @GetMapping("/surplus/cafe/{cafeId}")
    public ResponseEntity<?> getSurplusByCafe(@PathVariable Long cafeId) {
        return ResponseEntity.ok(transactionService.getSurplusByCafe(cafeId));
    }

    @PostMapping("/surplus")
    @PreAuthorize("hasRole('CAFE')")
    public ResponseEntity<?> createSurplus(@RequestBody SurplusPost surplus) {
        return ResponseEntity.ok(transactionService.createSurplus(surplus));
    }

    @GetMapping("/surplus/claims/cafe/{cafeId}")
    public ResponseEntity<?> getSurplusClaimsByCafe(@PathVariable Long cafeId) {
        java.util.List<SurplusPost> claims = transactionService.getSurplusByCafe(cafeId).stream()
            .filter(p -> "CLAIMED".equals(p.getStatus().name()) || "PICKED_UP".equals(p.getStatus().name()))
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(claims);
    }

    @PostMapping("/surplus/{id}/pickup")
    @PreAuthorize("hasRole('CAFE')")
    public ResponseEntity<?> markAsPickedUp(@PathVariable Long id, @Autowired com.indcaffe.repository.SurplusPostRepository repo) {
        return repo.findById(id).map(post -> {
            post.setStatus(com.indcaffe.entity.SurplusStatus.PICKED_UP);
            return ResponseEntity.ok(repo.save(post));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/surplus/{id}/claim-dummy")
    public ResponseEntity<?> dummyClaimSurplus(@PathVariable Long id, @Autowired com.indcaffe.repository.SurplusPostRepository repo) {
        // DUMMY endpoint to simulate a Mitra claiming it so we can test the UI
        return repo.findById(id).map(post -> {
            post.setStatus(com.indcaffe.entity.SurplusStatus.CLAIMED);
            post.setClaimDate(java.time.LocalDateTime.now());
            return ResponseEntity.ok(repo.save(post));
        }).orElse(ResponseEntity.notFound().build());
    }
}
