package com.prosilion.scdecisionmatrix.repository;

import com.prosilion.scdecisionmatrix.model.entity.Contract;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
  @Query("SELECT c FROM Contract c WHERE c.appUserId = :appUserId")
  List<Contract> getContractsByAppUserId(@NonNull Long appUserId);

  @Query("SELECT c FROM Contract c WHERE c.counterPartyId = :coPartyId")
  List<Contract> getContractsByCoPartyId(@NonNull Long coPartyId);

  @Query("SELECT c FROM Contract c WHERE c.appUserId <> :appUserId and c.counterPartyId is null")
  List<Contract> getOpenContractsFor(@NonNull Long appUserId);

  @Query("SELECT c FROM Contract c WHERE c.appUserId <> :appUserId")
  List<Contract> getAvailableOppositeRoleContracts(@NonNull Long appUserId);

  @Query("SELECT c FROM Contract c WHERE c.id = :id")
  Optional<Contract> getContractById(@NonNull Long id);
}
