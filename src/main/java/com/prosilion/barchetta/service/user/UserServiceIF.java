package com.prosilion.barchetta.service.user;

import com.prosilion.barchetta.model.dto.UserDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.presto.security.entity.AuthUserDetails;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;

public interface UserServiceIF {
  User findByUserId(Long id);
  User findByUsername(@NonNull String username);
  User findByPubKey(@NonNull String pubKey);
  UserDto update(@NonNull UserDto userDto) throws InvocationTargetException, IllegalAccessException;
  CreatorRoleEnum getRole(Contract contract, AuthUserDetails user);
}
