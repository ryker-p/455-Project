package com.emr.repository;

import com.emr.model.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
  @Query("select a.action, count(a) from AccessLog a group by a.action order by count(a) desc")
  List<Object[]> countByAction();
}
