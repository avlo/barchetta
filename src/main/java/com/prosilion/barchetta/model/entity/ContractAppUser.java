package com.prosilion.barchetta.model.entity;

import com.prosilion.barchetta.model.dto.ContractAppUserDto;
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
public class ContractAppUser extends AppUser {
  private String unqiueContractAppUserField;
  @Override
  public ContractAppUser getInstantiatedCustomAppUserType() {
    return this;
  }

  @Override
  public ContractAppUser createNewCustomAppUserInstance() {
    return new ContractAppUser();
  }

  public ContractAppUserDto convertToDto() throws InvocationTargetException, IllegalAccessException {
    ContractAppUserDto contractAppUserDto = new ContractAppUserDto();
    BeanUtils.copyProperties(contractAppUserDto, this);
    return contractAppUserDto;
  }
}
