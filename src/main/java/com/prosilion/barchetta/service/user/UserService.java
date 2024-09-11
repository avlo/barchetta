package com.prosilion.barchetta.service.user;

import com.prosilion.barchetta.model.dto.UserDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.repository.UserRepository;
import com.prosilion.presto.security.entity.AuthUserDetails;
import com.prosilion.presto.security.service.AuthUserService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

@Slf4j
@Service
public class UserService implements UserServiceIF {
  private final UserRepository userRepository;
  private final AuthUserService authUserService;

  @Autowired
  public UserService(UserRepository userRepository, AuthUserService authUserService) {
    this.userRepository = userRepository;
    this.authUserService = authUserService;
  }

  public User findByPubKey(@NonNull String pubKey) {
    return userRepository.findByNostrPubKey(pubKey).get();
  }

  @Override
  public User findByUserId(Long id) {
    return userRepository.findById(id).get();
  }

  @Override
  public User findByUsername(@NonNull String username) {
    return findByUserId(authUserService.getAppuserAuthuser(username).getId());
  }

  @Override
  public UserDto update(@NonNull UserDto userDto) throws InvocationTargetException, IllegalAccessException {
    log.info("CONTRACT USER - updating");
    User user = userDto.convertToContractAppUser();
    User retrievedUser = find(user);
    log.info("Confirm retrieved existing contractAppUser [{}]", retrievedUser);
    User returnUser = userRepository.save(user);
    log.info("Updating contractAppUser [{}]", returnUser);
    return userRepository.findById(user.getId()).get().convertToDto();
  }

  @Override
  public CreatorRoleEnum getRole(Contract contract, AuthUserDetails user) {
    return getRoleEnum(
        contract.getCreatorRole(),
        contract.getAppUserId(),
        findByUsername(user.getUsername()).getId());
  }

  private User find(@NonNull User user) {
    return Objects.isNull(user.getId()) ? user : findByUserId(user.getId());
  }

  private CreatorRoleEnum getRoleEnum(@NonNull CreatorRoleEnum role, @NonNull Long contractAppUserId, @NonNull Long appUserId) {
    if (contractAppUserId.equals(appUserId) && role.equals(CreatorRoleEnum.PAYER)) {
      return CreatorRoleEnum.PAYER;
    }
    if (contractAppUserId.equals(appUserId) && role.equals(CreatorRoleEnum.PAYEE)) {
      return CreatorRoleEnum.PAYEE;
    }
    if (!contractAppUserId.equals(appUserId) && role.equals(CreatorRoleEnum.PAYER)) {
      return CreatorRoleEnum.PAYEE;
    }
    return CreatorRoleEnum.PAYER;
  }
}
