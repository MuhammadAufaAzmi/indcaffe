package com.indcaffe.repository;

import com.indcaffe.entity.SurplusPost;
import com.indcaffe.entity.SurplusStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SurplusPostRepository extends JpaRepository<SurplusPost, Long> {
    List<SurplusPost> findByCafeId(Long cafeId);
    List<SurplusPost> findByStatus(SurplusStatus status);
    List<SurplusPost> findByClaimedById(Long mitraId);
}
