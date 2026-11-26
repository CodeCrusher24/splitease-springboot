package com.splitease.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
            SELECT e FROM Expense e
            WHERE e.group.id = :groupId AND e.deletedAt IS NULL
            ORDER BY e.expenseDate DESC, e.createdAt DESC
            """)
    List<Expense> findActiveByGroupId(@Param("groupId") Long groupId);
}