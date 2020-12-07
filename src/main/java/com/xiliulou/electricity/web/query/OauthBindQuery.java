package com.xiliulou.electricity.web.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * @author: eclair
 * @Date: 2020/12/4 08:41
 * @Description:
 */
@Data
public class OauthBindQuery {
	@NotNull(message = "id不能为空")
	private Long id;
	@Range(min = 1, max = 2, message = "状态不合法")
	private Integer status;
	private String phone;
	private String thirdId;
}
