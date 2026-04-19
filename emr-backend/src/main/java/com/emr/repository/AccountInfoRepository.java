package com.emr.repository;

import com.emr.model.AccountInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountInfoRepository extends JpaRepository<AccountInfo, Long> {
  Optional<AccountInfo> findByUserId(Long userId);
}

