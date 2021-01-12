package com.xiliulou.electricity.web.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author: lxc
 * @Date: 2021/01/12 17:06
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordQuery {
	@NotBlank(message = "用户名不能为空", groups = {CreateGroup.class})
	private String name;
	@NotBlank(message = "密码不能为空", groups = {CreateGroup.class})
	private String password;
}
