package com.example.productreview.repository;

import com.example.productreview.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    /**
     * Atomically recomputes review_count and average_rating from the reviews table in a
     * single UPDATE statement, eliminating the read-modify-write race that the separate
     * getReviewStats → setReviewCount → save pattern was susceptible to.
     *
     * ROUND(COALESCE(AVG(rating) * 10, 0)) / 10.0 produces a value rounded to 1 decimal
     * place without relying on the two-argument ROUND overload, keeping the query
     * compatible with both H2 (dev) and PostgreSQL (prod).
     */
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE products SET " +
            "review_count = (SELECT COUNT(*) FROM reviews WHERE product_id = :productId), " +
            "average_rating = (SELECT ROUND(COALESCE(AVG(rating) * 10, 0)) / 10.0 FROM reviews WHERE product_id = :productId) " +
            "WHERE id = :productId",
            nativeQuery = true)
    void updateProductStatsAtomic(@Param("productId") Long productId);

    Page<Product> findByIdIn(List<Long> ids, Pageable pageable);
}
