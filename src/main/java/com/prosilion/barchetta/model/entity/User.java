package com.prosilion.barchetta.model.entity;

import com.prosilion.barchetta.model.dto.UserDto;
import com.prosilion.presto.security.entity.AppUser;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Embeddable
@Entity
public class User extends AppUser {
  private String nostrPubKey;

  @Override
  public User getInstantiatedCustomAppUserType() {
    return this;
  }

  @Override
  public User createNewCustomAppUserInstance() {
    return new User();
  }

  public UserDto convertToDto() throws InvocationTargetException, IllegalAccessException {
    UserDto userDto = new UserDto();
    BeanUtils.copyProperties(userDto, this);
    return userDto;
  }
}
