package com.xiliulou.electricity.web.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author: eclair
 * @Date: 2020/12/10 18:05
 * @Description:
 */
@Data
public class PermissionResourceQuery {
	@NotNull(message = "id不能为空", groups = UpdateGroup.class)
	private long id;

	@NotEmpty(message = "名称不能为空", groups = CreateGroup.class)
	private String name;

	@NotNull(message = "类型不能为空", groups = CreateGroup.class)
	@Range(min = 1, max = 2, message = "type类型不合法")
	private Integer type;

	@NotEmpty(message = "uri不能为空", groups = CreateGroup.class)
	private String uri;

	@NotEmpty(message = "方法不能为空", groups = CreateGroup.class)
	private String method;

	@NotNull(message = "排序不能为空", groups = CreateGroup.class)
	private Double sort;

	private String desc;

	@NotNull(message = "父id不能为空",groups = CreateGroup.class)
	private Long parent;

}
