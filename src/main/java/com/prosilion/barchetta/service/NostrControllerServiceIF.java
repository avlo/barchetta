package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.User;
import lombok.NonNull;

public interface NostrControllerServiceIF extends ControllerServiceIF {
  ContractDto getContractDtoByContractId(@NonNull Long id, @NonNull User user);
}
