package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author: Miss.Li
 * @Date: 2021/8/30 09:33
 * @Description:
 */
@Data
@TableName("t_withdraw_password")
public class WithdrawPassword {


	@TableId(value = "id",type = IdType.AUTO)
	private Integer id;


	//密码
	@NotEmpty(message = "密码不能为空!")
	private String password;



	//创建时间
	private Long createTime;

	//修改时间
	private Long updateTime;

	private Integer tenantId;


}


