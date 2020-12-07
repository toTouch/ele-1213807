package com.xiliulou.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WXMinProPhoneResultDTO {
	private String phoneNumber;
	private String purePhoneNumber;
	private String countryCode;
}
