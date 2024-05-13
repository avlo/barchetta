package com.prosilion.barchetta.model.dto;

import com.prosilion.barchetta.model.entity.ContractAppUser;
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
public class ContractAppUserDto extends AppUserDto {
	private String unqiueContractAppUserField;
	public ContractAppUser convertToContractAppUser() throws InvocationTargetException, IllegalAccessException {
		ContractAppUser contractAppUser = new ContractAppUser();
		BeanUtils.copyProperties(contractAppUser, this);
		return contractAppUser;
	}
}
