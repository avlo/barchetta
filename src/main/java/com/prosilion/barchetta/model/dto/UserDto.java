package com.prosilion.barchetta.model.dto;

import com.prosilion.barchetta.model.entity.User;
import com.prosilion.presto.web.model.AppUserDto;
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
public class UserDto extends AppUserDto {
  private String nostrPubKey;
  public User convertToContractAppUser() throws InvocationTargetException, IllegalAccessException {
    User user = new User();
    BeanUtils.copyProperties(user, this);
    return user;
  }
}
