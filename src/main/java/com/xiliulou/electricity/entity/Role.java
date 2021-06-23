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

	private Integer tenantId;

	public static final Integer DEL_NORMAL = 0;
	public static final Integer DEL_DEL = 1;

	public static final Integer ROLE_ADMIN = 1;
	public static final Integer ROLE_OPERATE_USER = 2;
	public static final Integer ROLE_FRANCHISEE_USER = 3;
	public static final Integer ROLE_STORE_USER = 4;
	public static final Integer ROLE_CABINET_USER = 5;

}
