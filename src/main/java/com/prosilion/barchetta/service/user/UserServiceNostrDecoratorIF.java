package com.prosilion.barchetta.service.user;

import com.prosilion.barchetta.model.entity.User;
import lombok.NonNull;

public interface UserServiceNostrDecoratorIF extends UserServiceIF {
  User findByPubKey(@NonNull String pubKey);
}
