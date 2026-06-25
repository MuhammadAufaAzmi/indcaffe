package com.indcaffe.repository;

import com.indcaffe.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCafeId(Long cafeId);
    List<Product> findByCategoryId(Long categoryId);
}
