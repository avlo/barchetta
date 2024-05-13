//package com.prosilion.barchetta.model.entity;
//
//import com.prosilion.barchetta.model.dto.PayeeDto;
//import com.prosilion.presto.security.entity.AppUser;
//import jakarta.persistence.Embeddable;
//import jakarta.persistence.Entity;
//import lombok.EqualsAndHashCode;
//import lombok.Getter;
//import lombok.Setter;
//import org.springframework.beans.BeanUtils;
//
//@Getter
//@Setter
//@EqualsAndHashCode
//@Embeddable
//@Entity
//public class Payee extends AppUser implements Creator {
//
//  private final ContractAppUser contractAppUser;
//
//  public Payee(ContractAppUser contractAppUser) {
//    this.contractAppUser = contractAppUser;
//  }
//
//  @Override
//  public Payee getInstantiatedCustomAppUserType() {
//    return this;
//  }
//
//  @Override
//  public Payee createNewCustomAppUserInstance() {
//    return new Payee();
//  }
//
//  public PayeeDto convertToDto() {
//    PayeeDto payeeDto = new PayeeDto();
//    BeanUtils.copyProperties(payeeDto, this);
//    return payeeDto;
//  }
//}
