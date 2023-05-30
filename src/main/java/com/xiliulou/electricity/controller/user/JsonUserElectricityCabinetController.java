package com.xiliulou.electricity.controller.user;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Objects;

/**
 * 换电柜表(TElectricityCabinet)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@RestController
@Slf4j
public class JsonUserElectricityCabinetController extends BaseController {
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
			@RequestParam(value = "franchiseeId" , required = false) Long franchiseeId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam("lon") Double lon,
			@RequestParam("lat") Double lat) {

		if (Objects.isNull(lon) || lon <= 0.0 || Objects.isNull(lat) || lat <= 0.0) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}

		Integer tenantId = TenantContextHolder.getTenantId();

		ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
				.name(name)
				.distance(distance)
				.lon(lon)
				.lat(lat)
				.franchiseeId(franchiseeId)
				.tenantId(tenantId).build();

		return electricityCabinetService.showInfoByDistance(electricityCabinetQuery);
	}

	/**
	 * TODO 优化
	 * @return
	 */
	@GetMapping(value = "/outer/electricityCabinet/showInfoByDistanceV2")
	public R showInfoByDistanceV2(@RequestParam(value = "distance", required = false) Double distance,
								@RequestParam(value = "franchiseeId" , required = false) Long franchiseeId,
								@RequestParam(value = "name", required = false) String name,
								@RequestParam("lon") Double lon,
								@RequestParam("lat") Double lat) {

		if (Objects.isNull(lon) || lon <= 0.0 || Objects.isNull(lat) || lat <= 0.0) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}

		Integer tenantId = TenantContextHolder.getTenantId();

		ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
				.name(name)
				.distance(distance)
				.lon(lon)
				.lat(lat)
				.franchiseeId(franchiseeId)
				.tenantId(tenantId).build();

		return electricityCabinetService.showInfoByDistanceV2(electricityCabinetQuery);
	}

	/**
	 * 根据柜机位置搜索 TODO 优化
	 *
	 * @param address
	 * @return
	 */
	@GetMapping(value = "/user/electricityCabinet/selectByAddress")
	public R selectElectricityCabinetByAddress(@RequestParam("size") long size,
											   @RequestParam("offset") long offset,
											   @RequestParam("lon") Double lon,
											   @RequestParam("lat") Double lat,
											   @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
											   @RequestParam(value = "address", required = false) String address) {
		if (Objects.isNull(lon) || lon <= 0.0 || Objects.isNull(lat) || lat <= 0.0) {
			return R.ok(Collections.EMPTY_LIST);
		}

		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
				.offset(offset)
				.size(size)
				.lon(lon)
				.lat(lat)
				.franchiseeId(franchiseeId)
				.address(address)
				.tenantId(TenantContextHolder.getTenantId()).build();

		return R.ok(electricityCabinetService.selectElectricityCabinetByAddress(electricityCabinetQuery));
	}

	//列表查询
	@GetMapping(value = "/user/electricityCabinet/showInfoByStoreId/{storeId}")
	public R showInfoByStoreId(@PathVariable("storeId") Long storeId) {

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
	 * 查询租车套餐信息
	 * @return
	 */
	@GetMapping(value = "/user/rentCarMemberCard/info")
	@Deprecated
	public R getRentCarMemberCardInfo(){
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户!");
		}
		return userInfoService.getRentCarMemberCardInfo(uid);
	}

	/**
	 * 获取 小程序服务信息
	 *
	 * @return
	 */
	@GetMapping("user/servicePhone")
	public R getServicePhone() {
		//租户
		Integer tenantId = TenantContextHolder.getTenantId();
		String phone = redisService.get(CacheConstant.CACHE_SERVICE_PHONE+tenantId);
//		if(StrUtil.isBlank(phone)){
//			List<User> userList = userService.queryByTenantIdAndType(tenantId, User.TYPE_USER_OPERATE);
//			if(CollectionUtils.isNotEmpty(userList)){
//				phone = userList.get(0).getPhone();
//			}
//
//		}
		return R.ok(phone);
	}

	@PostMapping("/user/address")
	@Deprecated
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
	@GetMapping(value = "/user/electricityCabinet/queryByRentBattery")
	public R queryByRentBattery(@RequestParam("productKey") String productKey, @RequestParam("deviceName") String deviceName) {
		return electricityCabinetService.queryByRentBattery(productKey, deviceName);
	}

	/**
	 * 查询换电柜 按三元组
	 *
	 * @return
	 */
	@GetMapping(value = "/user/electricityCabinet/queryByOrder")
	@Deprecated
	public R queryByOrder(@RequestParam("productKey") String productKey, @RequestParam("deviceName") String deviceName) {
		return electricityCabinetService.queryByOrder(productKey, deviceName);
	}

	/**
	 * 查询换电柜 电池详情
	 */
	@GetMapping(value="/user/electricityCabinetBoxInfo/{electricityCabinetId}")
	public R queryElectricityCabinetBoxInfoById(@PathVariable("electricityCabinetId") Integer electricityCabinetId){

		return electricityCabinetService.queryElectricityCabinetBoxInfoById(electricityCabinetId);
	}

	/**
	 * 查询换电柜图片
	 */
	@GetMapping(value="/user/electricityCabinetFile/{electricityCabinetId}")
	public R queryElectricityCabinetFileById(@PathVariable("electricityCabinetId") Integer electricityCabinetId){

		return electricityCabinetService.queryElectricityCabinetFileById(electricityCabinetId);
	}


	/**
	 * 用户租车和换电套餐详情
	 * @return
	 */
	@GetMapping("/user/memberCard/detail")
	public R memberCardDetail() {
		return userService.memberCardDetail();
	}


	/**
	 * 查询租户下是否有该换电柜 按三元组
	 *
	 * @return
	 */
	@GetMapping(value = "/user/electricityCabinet/existsElectricityCabinet")
	public R existsElectricityCabinet(@RequestParam("productKey") String productKey, @RequestParam("deviceName") String deviceName) {
		return returnTripleResult(electricityCabinetService.existsElectricityCabinet(productKey, deviceName));
	}

}
