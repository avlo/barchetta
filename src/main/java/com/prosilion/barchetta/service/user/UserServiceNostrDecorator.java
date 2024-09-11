package com.prosilion.barchetta.service.user;

import com.prosilion.barchetta.model.dto.UserDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.presto.security.entity.AuthUserDetails;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

@Slf4j
public class UserServiceNostrDecorator implements UserServiceNostrDecoratorIF {
  private final UserServiceIF userService;

  public UserServiceNostrDecorator(UserServiceIF userService) {
    this.userService = userService;
  }

  @Override
  public User findByPubKey(@NonNull String pubKey) {
    return userService.findByPubKey(pubKey);
  }

  @Override
  public User findByUserId(Long id) {
    return userService.findByUserId(id);
  }

  @Override
  public User findByUsername(@NonNull String username) {
    return userService.findByUsername(username);
  }

  @Override
  public UserDto update(@NonNull UserDto userDto) throws InvocationTargetException, IllegalAccessException {
    log.info("CONTRACT NOSTR USER - updating");
    return userService.update(userDto);
  }

  @Override
  public CreatorRoleEnum getRole(Contract contract, AuthUserDetails user) {
    return userService.getRole(contract, user);
  }
}
