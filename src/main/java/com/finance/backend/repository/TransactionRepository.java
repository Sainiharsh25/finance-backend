package com.finance.backend.repository;

import com.finance.backend.entity.Transaction;
import com.finance.backend.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find active (non-deleted) by id
    Optional<Transaction> findByIdAndDeletedFalse(Long id);

    // Filtered listing with pagination (soft delete aware)
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.deleted = false
          AND (:type IS NULL OR t.type = :type)
          AND (:category IS NULL OR LOWER(t.category) = LOWER(:category))
          AND (:startDate IS NULL OR t.date >= :startDate)
          AND (:endDate IS NULL OR t.date <= :endDate)
        ORDER BY t.date DESC
    """)
    Page<Transaction> findAllWithFilters(
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    // Dashboard: sum by type
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.deleted = false AND t.type = :type")
    BigDecimal sumByType(@Param("type") TransactionType type);

    // Dashboard: category-wise totals
    @Query("""
        SELECT t.category, t.type, SUM(t.amount)
        FROM Transaction t
        WHERE t.deleted = false
        GROUP BY t.category, t.type
        ORDER BY SUM(t.amount) DESC
    """)
    List<Object[]> getCategoryWiseTotals();

    // Dashboard: monthly trends for a given year
    @Query("""
        SELECT MONTH(t.date), t.type, SUM(t.amount)
        FROM Transaction t
        WHERE t.deleted = false AND YEAR(t.date) = :year
        GROUP BY MONTH(t.date), t.type
        ORDER BY MONTH(t.date)
    """)
    List<Object[]> getMonthlyTrends(@Param("year") int year);

    // Dashboard: recent N transactions
    @Query("SELECT t FROM Transaction t WHERE t.deleted = false ORDER BY t.createdAt DESC")
    Page<Transaction> findRecentTransactions(Pageable pageable);
}
