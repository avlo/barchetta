package com.prosilion.barchetta.service.user;

import com.prosilion.barchetta.model.dto.UserDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.repository.UserRepository;
import com.prosilion.presto.nostr.service.NostrUserService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class UserService implements UserServiceIF {
  private final UserRepository userRepository;
  private final NostrUserService nostrUserService;

  @Autowired
  public UserService(UserRepository userRepository, NostrUserService nostrUserService) {
    this.userRepository = userRepository;
    this.nostrUserService = nostrUserService;
  }

  @Override
  public User findByUserId(Long id) {
    return userRepository.findById(id).get();
  }

  @Override
  public User findByUsername(@NonNull String username) {
    return findByUserId(nostrUserService.getAppuserAuthuser(username).getId());
  }

  @Override
  public User findByPubKey(@NonNull String pubKey) {
    return findByUserId(nostrUserService.findUserByPubkey(pubKey).getId());
  }

  @Override
  public UserDto update(@NonNull UserDto userDto) {
    log.info("CONTRACT USER - updating");
    User user = userDto.convertToUser();
    User retrievedUser = find(user);
    log.info("Confirm retrieved existing contractAppUser [{}]", retrievedUser);
    User returnUser = userRepository.save(user);
    log.info("Updating contractAppUser [{}]", returnUser);
    return userRepository.findById(user.getId()).get().convertToDto();
  }

  @Override
  public CreatorRoleEnum getRole(Contract contract, User user) {
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
