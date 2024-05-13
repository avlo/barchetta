//package com.prosilion.scdecisionmatrix.model.dto;
//
//import com.prosilion.scdecisionmatrix.model.entity.Payee;
//import edu.mayo.lpea.cad.cadence3.web.model.AppUserDto;
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
