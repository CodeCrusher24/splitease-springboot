package com.splitease.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {

    @Query("SELECT s FROM ExpenseSplit s WHERE s.expense.id = :expenseId")
    List<ExpenseSplit> findByExpenseId(@Param("expenseId") Long expenseId);
}