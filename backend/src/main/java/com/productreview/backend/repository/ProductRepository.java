package com.productreview.backend.repository;

import com.productreview.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE :category MEMBER OF p.categories")
    Page<Product> findByCategory(@Param("category") String category, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE :category MEMBER OF p.categories AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> findByCategoryAndNameContainingIgnoreCase(
            @Param("category") String category,
            @Param("name") String name,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    Page<Product> findByIdIn(@Param("ids") List<Long> ids, Pageable pageable);
}