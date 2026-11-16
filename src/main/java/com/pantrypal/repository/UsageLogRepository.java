package com.pantrypal.repository;

import com.pantrypal.model.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {

    @Query("select coalesce(sum(u.estimatedValue), 0) from UsageLog u where u.user.id = :userId and u.action = :action")
    double sumValue(@Param("userId") Long userId, @Param("action") UsageLog.Action action);

    @Query("select coalesce(sum(u.estimatedValue), 0) from UsageLog u where u.user.id = :userId and u.action = :action and u.rescued = true")
    double sumRescuedValue(@Param("userId") Long userId, @Param("action") UsageLog.Action action);

    long countByUserIdAndAction(Long userId, UsageLog.Action action);

    long countByUserIdAndActionAndRescuedTrue(Long userId, UsageLog.Action action);

    List<UsageLog> findTop10ByUserIdOrderByLoggedAtDesc(Long userId);
}
