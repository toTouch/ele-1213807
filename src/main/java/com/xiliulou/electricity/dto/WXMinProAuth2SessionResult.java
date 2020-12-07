package com.xiliulou.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WXMinProAuth2SessionResult {
	private String openid;
	private String session_key;
	private String unionid;
	private String errcode;
	private String errmsg;
}
