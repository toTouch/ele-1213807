package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.pay.weixin.entity.AccessTokenResult;
import com.xiliulou.pay.weixin.entity.SubscriptionMessageResult;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 优惠券规则表(TCoupon)表控制层
 *
 * @author makejava
 * @since 2021-04-14 09:28:22
 */
@RestController
@Slf4j
public class JsonAdminCouponController {
	/**
	 * 服务对象
	 */
	@Autowired
	private CouponService couponService;

	@Autowired
	FranchiseeService franchiseeService;

	//新增
	@PostMapping(value = "/admin/coupon")
	public R save(@RequestBody @Validated(value = CreateGroup.class) Coupon coupon) {
		return couponService.insert(coupon);
	}

	//修改--暂时无此功能
	@PutMapping(value = "/admin/coupon")
	public R update(@RequestBody @Validated(value = UpdateGroup.class) Coupon coupon) {
		return couponService.update(coupon);
	}

	//删除--暂时无此功能
	@DeleteMapping(value = "/admin/coupon/{id}")
	public R delete(@PathVariable("id") Integer id) {
		if (Objects.isNull(id)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}
		return couponService.delete(id);
	}

	//列表查询
	@GetMapping(value = "/admin/coupon/list")
	public R queryList(@RequestParam(value = "size", required = false) Long size,
			@RequestParam(value = "offset", required = false) Long offset,
			@RequestParam(value = "discountType", required = false) Integer discountType,
			@RequestParam(value = "franchiseeId", required = false) Integer franchiseeId,
			@RequestParam(value = "name", required = false) String name) {
		if (Objects.isNull(size)) {
			size = 10L;
		}

		if (Objects.isNull(offset) || offset < 0) {
			offset = 0L;
		}

		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
			Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
			if (Objects.isNull(franchisee)) {
				return R.ok();
			}
			franchiseeId = franchisee.getId();
		}

		CouponQuery couponQuery = CouponQuery.builder()
				.offset(offset)
				.size(size)
				.name(name)
				.discountType(discountType)
				.franchiseeId(franchiseeId).build();
		return couponService.queryList(couponQuery);
	}

	//测试
	@GetMapping(value = "/outer/test")
	public R test() {
		//获取缓存accesstoken
		String accessToken =
				getAccessToken("wx76159ea6aa7a64bc", "b44586ca1b4ff8def2b4c869cdd8ea6a");
		if (ObjectUtil.isEmpty(accessToken)) {
			return R.fail("失败");
		}

		HashMap<String, String> map = new HashMap<>();
		map.put("path", "pages/start/index");
		map.put("scene", "1");

		//发送
		String url = " https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken;
		log.info("SEND_SUBSCRIPTION_MESSAGE ,JSON_PARAMS:{}", JsonUtil.toJson(map));
		String mapResult = HttpUtil.post(url, JsonUtil.toJson(map));
		return R.ok(mapResult);

	}

	private String getAccessToken(String appId, String appSecret) {

		String getAccessToken = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;
		String result = HttpUtil.get(getAccessToken);
		String accessToken = JSONUtil.toBean(result, AccessTokenResult.class).getAccess_token();
		log.info("GET ACCESS_TOKEN  RESULT：" + result);
		return accessToken;
	}

}
