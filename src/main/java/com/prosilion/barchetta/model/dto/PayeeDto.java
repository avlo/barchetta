//package com.prosilion.barchetta.model.dto;
//
//import com.prosilion.barchetta.model.entity.Payee;
//import com.prosilion.presto.web.model.AppUserDto;
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
//public class PayeeDto extends AppUserDto {
//  private String customPayeeField;
//
//  public Payee convertToPayee() {
//    Payee payee = new Payee();
//    BeanUtils.copyProperties(payee, this);
//    return payee;
//  }
//}
