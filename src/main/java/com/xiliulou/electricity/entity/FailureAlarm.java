package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/**
 *
 * @author maxiaodong
 * @date 2023-12-15 13:58:23
 */
@Data
@TableName("t_failure_alarm")
public class FailureAlarm {
	@TableId(value = "id",type = IdType.AUTO)
	private Long id;
	
	/**
	 * 分类(1-故障， 2-告警)
	 */
	private Integer type;
	
	/**
	 *等级(1- 一级， 2-二级，3- 三级，4 -四级)
	 */
	private Integer grade;
	
	/**
	 *模块(1- 主板， 2- 子板，3- 电池，4 -电池异常消失，5 -车辆，6-充电器，7-BMS)
	 */
	private Integer model;
	
	/**
	 * 名称
	 */
	private String name;
	
	
	/**
	 * 错误码
	 */
	private Integer errorCode;
	
	/**
	 * 触发规则
	 */
	private String triggerRules;
	
	/**
	 * 运营商可见(0-不可见， 1-可见)
	 */
	private Integer tenantVisible;
	
	/**
	 * 运作状态(0-启用， 1-禁用)
	 */
	private Integer status;
	
	/**
	 * 删除标记(0-未删除，1-已删除)
	 */
	private Integer delFlag;
	
	/**
	 * 创建时间
	 */
	private Long createTime;
	
	/**
	 * 修改时间
	 */
	private Long updateTime;

	public static final Integer DEL_NORMAL = 0;
	public static final Integer DEL_DEL = 1;

}


