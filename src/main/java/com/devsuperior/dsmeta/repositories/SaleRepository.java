package com.devsuperior.dsmeta.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.devsuperior.dsmeta.entities.Sale;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("SELECT s FROM Sale s " +
            "WHERE s.date BETWEEN :minDate AND :maxDate AND UPPER(s.seller.name) LIKE UPPER(CONCAT('%', :name, '%'))")
    Page<Sale> searchReport(LocalDate minDate, LocalDate maxDate, String name, Pageable pageable);

    @Query("SELECT s.seller.name, SUM(s.amount) " +
            "FROM Sale s " +
            "WHERE s.date BETWEEN :minDate AND :maxDate " +
            "GROUP BY s.seller.name")
    List<Object[]> searchSummary(LocalDate minDate, LocalDate maxDate);

}

