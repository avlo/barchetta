//package com.prosilion.scdecisionmatrix.model.entity;
//
//import com.prosilion.scdecisionmatrix.model.dto.PayerDto;
//import jakarta.persistence.Embeddable;
//import jakarta.persistence.Entity;
//import lombok.EqualsAndHashCode;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import org.springframework.beans.BeanUtils;
//
//@Getter
//@Setter
//@EqualsAndHashCode
//@NoArgsConstructor
//@Embeddable
//@Entity
//public class Payer extends ContractAppUser implements Creator {
//
//  @Override
//  public Payer getInstantiatedCustomAppUserType() {
//    return this;
//  }
//
//  @Override
//  public Payer createNewCustomAppUserInstance() {
//    return new Payer();
//  }
//
//  public PayerDto convertToDto() {
//    PayerDto payerDto = new PayerDto();
//    BeanUtils.copyProperties(payerDto, this);
//    return payerDto;
//  }
//}
