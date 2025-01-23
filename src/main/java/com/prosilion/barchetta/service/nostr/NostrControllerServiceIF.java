package com.prosilion.barchetta.service.nostr;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.service.controller.ControllerServiceIF;
import com.prosilion.presto.nostr.entity.NostrUser;
import lombok.NonNull;

import java.util.List;

public interface NostrControllerServiceIF extends ControllerServiceIF {
  List<Contract> getAllNostrUserContracts(@NonNull NostrUser user);
  List<Contract> getOpenContracts(@NonNull NostrUser nostrUser);
}
