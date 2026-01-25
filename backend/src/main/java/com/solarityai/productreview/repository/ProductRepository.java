package com.solarityai.productreview.repository;

import com.solarityai.backendfw.foundation.repository.BaseRepository;
import com.solarityai.productreview.dto.ProductStatsDto;
import com.solarityai.productreview.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends BaseRepository<ProductEntity, UUID>,
        JpaSpecificationExecutor<ProductEntity> {

    @Query("SELECT p FROM ProductEntity p JOIN p.categories c WHERE c = :category")
    Page<ProductEntity> findByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<ProductEntity> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p JOIN p.categories c WHERE c = :category AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<ProductEntity> findByCategoryAndNameContainingIgnoreCase(
            @Param("category") String category,
            @Param("name") String name,
            Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE p.id IN :ids")
    Page<ProductEntity> findByIdIn(@Param("ids") Collection<UUID> ids, Pageable pageable);

    @Query("SELECT new com.solarityai.productreview.dto.ProductStatsDto(" +
           "COUNT(p), COALESCE(SUM(p.reviewCount), 0L), COALESCE(AVG(p.averageRating), 0.0)) " +
           "FROM ProductEntity p")
    ProductStatsDto getGlobalStats();

    @Query("SELECT new com.solarityai.productreview.dto.ProductStatsDto(" +
           "COUNT(p), COALESCE(SUM(p.reviewCount), 0L), COALESCE(AVG(p.averageRating), 0.0)) " +
           "FROM ProductEntity p JOIN p.categories c WHERE c = :category")
    ProductStatsDto getCategoryStats(@Param("category") String category);

    @Query("SELECT new com.solarityai.productreview.dto.ProductStatsDto(" +
           "COUNT(p), COALESCE(SUM(p.reviewCount), 0L), COALESCE(AVG(p.averageRating), 0.0)) " +
           "FROM ProductEntity p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    ProductStatsDto getSearchStats(@Param("name") String name);

    @Query("SELECT new com.solarityai.productreview.dto.ProductStatsDto(" +
           "COUNT(p), COALESCE(SUM(p.reviewCount), 0L), COALESCE(AVG(p.averageRating), 0.0)) " +
           "FROM ProductEntity p JOIN p.categories c " +
           "WHERE c = :category AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    ProductStatsDto getCategoryAndSearchStats(@Param("category") String category, @Param("name") String name);
}
