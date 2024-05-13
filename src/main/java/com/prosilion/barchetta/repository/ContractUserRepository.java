package com.prosilion.barchetta.repository;

import com.prosilion.barchetta.model.entity.ContractAppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractUserRepository extends JpaRepository<ContractAppUser, Long> {
}
