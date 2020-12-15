package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.web.query.OauthBindQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author: eclair
 * @Date: 2020/12/4 08:32
 * @Description:
 */
@RestController
@RequestMapping("/admin")
public class JsonAdminUserOauthBindController extends BaseController {
	@Autowired
	UserOauthBindService userOauthBindService;

	@GetMapping("/oauth/list")
	public R getList(@RequestParam(value = "offset") Integer offset, @RequestParam("size") Integer size,
			@RequestParam(value = "uid", required = false) Long uid,
			@RequestParam(value = "thirdId", required = false) String thirdId,
			@RequestParam(value = "phone", required = false) String phone) {
		if (offset < 0) {
			offset = 0;
		}
		if (size < 0 || size > 50) {
			size = 10;
		}

		return returnPairResult(userOauthBindService.queryListByCondition(size, offset, uid, thirdId, phone));
	}

	@PreAuthorize(value = "hasAuthority('oauth_bind_modify')")
	public R modifyOauthBind(@Validated @RequestBody OauthBindQuery oauthBindQuery, BindingResult bindingResult) {
		if (bindingResult.hasFieldErrors()) {
			return R.fail("SYSTEM.0002", bindingResult.getFieldError().getDefaultMessage());
		}

		if (Objects.isNull(oauthBindQuery.getStatus()) && Objects.isNull(oauthBindQuery.getPhone()) && Objects.isNull(oauthBindQuery.getThirdId())) {
			return R.fail("SYSTEM.0002", "参数错误");
		}
		return returnPairResult(userOauthBindService.updateOauthBind(oauthBindQuery));
	}
}
