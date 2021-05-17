package com.xiliulou.electricity.controller.user;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 换电柜表(TElectricityCabinet)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@RestController
public class ElectricityCabinetUserController extends BaseController {
	/**
	 * 服务对象
	 */
	@Autowired
	ElectricityCabinetService electricityCabinetService;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	RedisService redisService;
	@Autowired
	UserService userService;

	//列表查询
	@GetMapping(value = "/outer/electricityCabinet/showInfoByDistance")
	public R showInfoByDistance(@RequestParam(value = "distance", required = false) Double distance,
			@RequestParam("lon") Double lon,
			@RequestParam("lat") Double lat) {

		if (Objects.isNull(lon) || lon <= 0.0 || Objects.isNull(lat) || lat <= 0.0) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}

		ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
				.distance(distance)
				.lon(lon)
				.lat(lat).build();

		return electricityCabinetService.showInfoByDistance(electricityCabinetQuery);
	}

	//列表查询
	@GetMapping(value = "/user/electricityCabinet/showInfoByStoreId/{storeId}")
	public R showInfoByStoreId(@PathVariable("storeId") Integer storeId) {

		if (Objects.isNull(storeId)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}

		return electricityCabinetService.showInfoByStoreId(storeId);
	}

	/**
	 * 查询换电柜 按三元组
	 *
	 * @return
	 */
	@GetMapping(value = "/user/electricityCabinet")
	public R queryByDevice(@RequestParam("productKey") String productKey, @RequestParam("deviceName") String deviceName) {
		return electricityCabinetService.queryByDevice(productKey, deviceName);
	}

	/**
	 * 用户端首页
	 *
	 * @return
	 */
	@GetMapping(value = "/user/electricityCabinet/home")
	public R homeOne() {
		return electricityCabinetService.home();
	}

	/**
	 * 查询套餐信息
	 *
	 * @return
	 */
	@GetMapping(value = "/user/memberCard/info")
	public R getMemberCardInfo() {
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户!");
		}
		return userInfoService.getMemberCardInfo(uid);
	}

	/**
	 * 获取 小程序服务信息
	 *
	 * @return
	 */
	@GetMapping("user/servicePhone")
	public R getServicePhone() {
		return R.ok(redisService.get(ElectricityCabinetConstant.CACHE_SERVICE_PHONE));
	}

	@PostMapping("/user/address")
	public R addUserAddress(@RequestParam("cityCode") String cityCode) {
		return returnPairResult(userService.addUserAddress(cityCode));
	}

	@GetMapping("/user/detail")
	public R userDetail() {
		return returnPairResult(userService.getUserDetail());
	}

	/**
	 * 查询换电柜 按三元组
	 *
	 * @return
	 */
	@GetMapping(value = "/user/electricityCabinet/rentBattery")
	public R rentBattery(@RequestParam("productKey") String productKey, @RequestParam("deviceName") String deviceName) {
		return electricityCabinetService.rentBattery(productKey, deviceName);
	}

	/*public static void main(String[] args) {
		String s = "1";
		String t = "99";
		int i = s.length() - 1;
		System.out.println("i="+i);
		int j = t.length() - 1;
		System.out.println("j="+j);

		int m1 = 0, m2 = 0, carray = 0;//进位
		StringBuilder sb = new StringBuilder();

		while (i >= 0 || j >= 0) {
			if (i >= 0) {
				m1 = s.charAt(i) - '0';
			} else {
				m1 = 0;
			}

			System.out.println("m1="+m1);

			if (j >= 0) {
				m2 = t.charAt(j) - '0';
			} else {
				m2 = 0;
			}

			System.out.println("m2="+m2);

			int sum = m1 + m2 + carray;
			System.out.println("sum="+sum);


			carray = sum / 10;
			System.out.println("carray="+carray);


			System.out.println("sum="+(char)(sum % 10) + '0');
			sb.append((char) ((sum % 10) + '0'));
			System.out.println("sb="+sb);
			i--;
			j--;
		}

		System.out.println("carray="+carray);
		if (carray != 0) {
			sb.append('1');
		}
		System.out.println("sb="+sb);
		sb.reverse();
		System.out.println(sb.toString());
	}*/

}
