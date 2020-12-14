package com.xiliulou.electricity.web.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author: eclair
 * @Date: 2020/12/11 09:59
 * @Description:
 */
@Data
public class RoleQuery {
	@NotNull(message = "id不能为空", groups = UpdateGroup.class)
	private Long id;

	@NotEmpty(message = "角色名称不能为空", groups = CreateGroup.class)
	private String name;

	@NotEmpty(message = "角色code不能为空", groups = CreateGroup.class)
	private String code;

	private String desc;
}
