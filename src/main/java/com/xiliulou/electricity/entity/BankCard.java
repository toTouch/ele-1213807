package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 *
 *
 * @author Hardy
 * @email ${email}
 * @date 2021-05-24 13:58:23
 */
@Data
@TableName("t_bank_card")
public class BankCard {
	@TableId(value = "id",type = IdType.AUTO)
	private Integer id;

	//用户id
	private Long uid;

	//银行全称
	@NotEmpty(message = "银行全称不能为空!")
	private String fullName;

	//银行卡号
	@NotEmpty(message = "银行卡号不能为空!")
	private String encBankNo;

	//银行卡号绑定人
	@NotEmpty(message = "开卡人姓名不能为空!")
	private String encBindUserName;


	//银行卡号身份证
	@NotEmpty(message = "开卡人身份证不能为空!")
	private String encBindIdNumber;

	//银行编号
	@NotEmpty(message = "银行编号不能为空!")
	private String encBankCode;

	//删除标志
	private Integer delFlag;

	//创建时间
	private Long createTime;

	//修改时间
	private Long updateTime;

	//租户id
	private Integer tenantId;

	public static final Integer DEL_NORMAL = 0;
	public static final Integer DEL_DEL = 1;

}


