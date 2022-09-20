package com.xiliulou.electricity.web.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: eclair
 * @Date: 2020/11/30 17:06
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminUserQuery {
	@NotNull(message = "uid不能为空", groups = {UpdateGroup.class})
	private Long uid;
	@NotBlank(message = "用户名不能为空", groups = {CreateGroup.class})
	private String name;
	@NotBlank(message = "密码不能为空", groups = {CreateGroup.class})
	private String password;
	@NotEmpty(message = "手机号的不能为空", groups = {CreateGroup.class})
	private String phone;
	@Range(min = 1, max = 10, message = "用户类型不合法", groups = {CreateGroup.class, UpdateGroup.class})
//	@NotNull(message = "用户类型不为空", groups = {CreateGroup.class})
	private Integer userType;

	@Range(min = 1, max = 10, message = "数据可见类型不合法", groups = {CreateGroup.class, UpdateGroup.class})
	@NotNull(message = "数据可见类型不为空", groups = {CreateGroup.class})
	private Integer dataType;

	@NotEmpty(message = "语言不能为空", groups = {CreateGroup.class})
	private String lang;
	@Range(min = 0, max = 1, message = "性别类型不合法", groups = {CreateGroup.class, UpdateGroup.class})
	@NotNull(message = "性别不能为空", groups = {CreateGroup.class})
	private Integer gender;
	@Range(min = 0, max = 1, message = "锁定类型不合法", groups = {UpdateGroup.class})
	private Integer lock;

	private Integer cityId;

	private Integer provinceId;

	private Long roleId;
	/**
	 * 数据可见范围
	 */
	private List<Long> dataIdList;
}
