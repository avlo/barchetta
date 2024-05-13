package com.prosilion.scdecisionmatrix.repository;

import com.prosilion.scdecisionmatrix.model.entity.ContractAppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractUserRepository extends JpaRepository<ContractAppUser, Long> {
}
