package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 *
 *
 * @author Hardy
 * @email ${email}
 * @date 2021-05-24 13:58:23
 */
@Data
@Builder
public class BankCardQuery {
	private Long size;
	private Long offset;
	/**
	 * uid
	 */
	private Long uid;
	//银行卡号绑定人
	private String encBindUserName;
}
