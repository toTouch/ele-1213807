package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (Role)实体类
 *
 * @author Eclair
 * @since 2020-12-09 14:34:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_role")
public class Role {

	private Long id;
	/**
	 * 角色名称
	 */
	private String name;
	/**
	 * 角色标识
	 */
	private String code;
	/**
	 * 角色描述
	 */
	private String desc;

	private Long createTime;

	private Long updateTime;

	public static final Integer DEL_NORMAL = 0;
	public static final Integer DEL_DEL = 1;

}