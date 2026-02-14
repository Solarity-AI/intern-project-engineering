package com.example.productreview.repository;

import com.example.productreview.model.Product;
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
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE :category MEMBER OF p.categories AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> findByCategoryAndNameContainingIgnoreCase(@Param("category") String category, @Param("name") String name, Pageable pageable);

    @Query("SELECT SUM(p.reviewCount), AVG(p.averageRating), COUNT(p) FROM Product p")
    List<Object[]> getGlobalStats();

    @Query("SELECT SUM(p.reviewCount), AVG(p.averageRating), COUNT(p) FROM Product p WHERE :category MEMBER OF p.categories")
    List<Object[]> getCategoryStats(@Param("category") String category);

    @Query("SELECT SUM(p.reviewCount), AVG(p.averageRating), COUNT(p) FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Object[]> getSearchStats(@Param("name") String name);

    @Query("SELECT SUM(p.reviewCount), AVG(p.averageRating), COUNT(p) FROM Product p WHERE :category MEMBER OF p.categories AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Object[]> getCategoryAndSearchStats(@Param("category") String category, @Param("name") String name);

    // ✨ New method for paged find by IDs
    Page<Product> findByIdIn(List<Long> ids, Pageable pageable);
}
