//package com.prosilion.barchetta.model.dto;
//
//import com.prosilion.barchetta.model.entity.Payer;
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
//public class PayerDto extends AppUserDto {
//  private String customPayerField;
//
//  public Payer convertToPayer() {
//    Payer payer = new Payer();
//    BeanUtils.copyProperties(payer, this);
//    return payer;
//  }
//}
