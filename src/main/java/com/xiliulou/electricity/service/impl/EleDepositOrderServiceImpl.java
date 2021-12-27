package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.mapper.EleDepositOrderMapper;
import com.xiliulou.electricity.query.EleDepositOrderQuery;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.EleDepositOrderExcelVO;
import com.xiliulou.electricity.vo.EleDepositOrderVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 缴纳押金订单表(TEleDepositOrder)表服务实现类
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
@Service("eleDepositOrderService")
@Slf4j
public class EleDepositOrderServiceImpl implements EleDepositOrderService {
	@Resource
	EleDepositOrderMapper eleDepositOrderMapper;
	@Autowired
	RedisService redisService;
	@Autowired
	FranchiseeService franchiseeService;
	@Autowired
	UserService userService;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	ElectricityTradeOrderService electricityTradeOrderService;
	@Autowired
	ElectricityPayParamsService electricityPayParamsService;
	@Autowired
	UserOauthBindService userOauthBindService;
	@Autowired
	EleRefundOrderService eleRefundOrderService;
	@Autowired
	RentBatteryOrderService rentBatteryOrderService;
	@Autowired
	FranchiseeUserInfoService franchiseeUserInfoService;
	@Autowired
	ElectricityCabinetOrderService electricityCabinetOrderService;
	@Autowired
	ElectricityCabinetService electricityCabinetService;
	@Autowired
	StoreService storeService;

	@Override
	public EleDepositOrder queryByOrderId(String orderNo) {
		return eleDepositOrderMapper.selectOne(new LambdaQueryWrapper<EleDepositOrder>().eq(EleDepositOrder::getOrderId, orderNo));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R payDeposit(String productKey, String deviceName,Long franchiseeId,  Integer model, HttpServletRequest request) {
		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("payDeposit  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//限频
		Boolean getLockSuccess = redisService.setNx(ElectricityCabinetConstant.ELE_CACHE_USER_DEPOSIT_LOCK_KEY + user.getUid(), IdUtil.fastSimpleUUID(), 3 * 1000L, false);
		if (!getLockSuccess) {
			return R.fail("ELECTRICITY.0034", "操作频繁");
		}

		//支付相关
		ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
		if (Objects.isNull(electricityPayParams)) {
			log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND PAY_PARAMS");
			return R.failMsg("未配置支付参数!");
		}

		UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);

		if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
			log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND USEROAUTHBIND OR THIRDID IS NULL  UID:{}", user.getUid());
			return R.failMsg("未找到用户的第三方授权信息!");
		}

		//判断是否实名认证
		UserInfo userInfo = userInfoService.queryByUid(user.getUid());

		//用户是否可用
		if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("payDeposit  ERROR! not found userInfo,uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}
		//未实名认证
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			log.error("payDeposit  ERROR! user not auth! ,uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}

		//是否缴纳押金，是否绑定电池
		FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

		//未找到用户
		if (Objects.isNull(franchiseeUserInfo)) {
			log.error("payDeposit  ERROR! not found user! userId:{}", user.getUid());
			return R.fail("ELECTRICITY.0001", "未找到用户");

		}

		if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_DEPOSIT)) {
			log.error("payDeposit  ERROR! user is rent deposit! ,uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0049", "已缴纳押金");
		}

		if(Objects.isNull(franchiseeId)) {
			//换电柜
			ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(productKey, deviceName);
			if (Objects.isNull(electricityCabinet)) {
				log.error("queryDeposit  ERROR! not found electricityCabinet ！productKey{},deviceName{}", productKey, deviceName);
				return R.fail("ELECTRICITY.0005", "未找到换电柜");
			}

			//计算押金
			//查找换电柜门店
			if (Objects.isNull(electricityCabinet.getStoreId())) {
				log.error("payDeposit  ERROR! not found store ！electricityCabinetId{}", electricityCabinet.getId());
				return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
			}
			Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
			if (Objects.isNull(store)) {
				log.error("payDeposit  ERROR! not found store ！storeId{}", electricityCabinet.getStoreId());
				return R.fail("ELECTRICITY.0018", "未找到门店");
			}

			//查找门店加盟商
			if (Objects.isNull(store.getFranchiseeId())) {
				log.error("payDeposit  ERROR! not found Franchisee ！storeId{}", store.getId());
				return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
			}

			franchiseeId=store.getFranchiseeId();

		}

		Franchisee franchisee = franchiseeService.queryByIdFromDB(franchiseeId);
		if (Objects.isNull(franchisee)) {
			log.error("payDeposit  ERROR! not found Franchisee ！franchiseeId{}", franchiseeId);
			return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
		}

		BigDecimal payAmount = null;

		if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
			payAmount = franchisee.getBatteryDeposit();
		}

		//型号押金计算
		if (Objects.equals(franchisee.getModelType(), Franchisee.MEW_MODEL_TYPE)) {
			if (Objects.isNull(model)) {
				return R.fail("ELECTRICITY.0007", "不合法的参数");
			}

			//型号押金
			List<Map> modelBatteryDepositList = JsonUtil.fromJson(franchisee.getModelBatteryDeposit(), List.class);
			if (ObjectUtil.isEmpty(modelBatteryDepositList)) {
				log.error("payDeposit  ERROR! not found modelBatteryDepositList ！franchiseeId{}", franchiseeId);
				return R.fail("ELECTRICITY.00110", "未找到押金");
			}

			log.info("modelBatteryDepositList is -->{}", modelBatteryDepositList);
			for (Map map : modelBatteryDepositList) {
				if ((double) (map.get("model")) - model < 1 && (double) (map.get("model")) - model >= 0) {
					payAmount = BigDecimal.valueOf((double) map.get("batteryDeposit"));
					break;
				}
			}

		}

		if (Objects.isNull(payAmount)) {
			log.error("payDeposit  ERROR! payAmount is null ！franchiseeId{}", franchiseeId);
			return R.fail("ELECTRICITY.00110", "未找到押金");
		}

		String orderId = generateOrderId(user.getUid());

		//生成订单
		EleDepositOrder eleDepositOrder = EleDepositOrder.builder()
				.orderId(orderId)
				.uid(user.getUid())
				.phone(userInfo.getPhone())
				.name(userInfo.getName())
				.payAmount(payAmount)
				.status(EleDepositOrder.STATUS_INIT)
				.createTime(System.currentTimeMillis())
				.updateTime(System.currentTimeMillis())
				.tenantId(tenantId)
				.franchiseeId(franchisee.getId())
				.modelType(franchisee.getModelType()).build();

		if (Objects.equals(franchisee.getModelType(), Franchisee.MEW_MODEL_TYPE)) {
			eleDepositOrder.setBatteryType(BatteryConstant.acquireBatteryShort(model));
		}

		//支付零元
		if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
			eleDepositOrder.setStatus(EleDepositOrder.STATUS_SUCCESS);
			eleDepositOrderMapper.insert(eleDepositOrder);

			//用户缴纳押金
			FranchiseeUserInfo franchiseeUserInfoUpdate = new FranchiseeUserInfo();
			franchiseeUserInfoUpdate.setId(userInfo.getId());
			franchiseeUserInfoUpdate.setFranchiseeId(eleDepositOrder.getFranchiseeId());
			franchiseeUserInfoUpdate.setServiceStatus(FranchiseeUserInfo.STATUS_IS_DEPOSIT);
			franchiseeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
			franchiseeUserInfoUpdate.setBatteryDeposit(BigDecimal.valueOf(0));
			franchiseeUserInfoUpdate.setOrderId(orderId);
			franchiseeUserInfoUpdate.setModelType(eleDepositOrder.getModelType());

			if (Objects.equals(eleDepositOrder.getModelType(), Franchisee.MEW_MODEL_TYPE)) {
				franchiseeUserInfoUpdate.setBatteryType(eleDepositOrder.getBatteryType());
			}

			franchiseeUserInfoService.update(franchiseeUserInfoUpdate);
			return R.ok();
		}
		eleDepositOrderMapper.insert(eleDepositOrder);

		//调起支付
		try {
			CommonPayOrder commonPayOrder = CommonPayOrder.builder()
					.orderId(orderId)
					.uid(user.getUid())
					.payAmount(payAmount)
					.orderType(ElectricityTradeOrder.ORDER_TYPE_DEPOSIT)
					.attach(ElectricityTradeOrder.ATTACH_DEPOSIT)
					.description("押金收费")
					.tenantId(tenantId).build();

			WechatJsapiOrderResultDTO resultDTO =
					electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
			return R.ok(resultDTO);
		} catch (WechatPayException e) {
			log.error("payDeposit ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
		}

		return R.fail("ELECTRICITY.0099", "下单失败");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R returnDeposit(HttpServletRequest request) {
		//登录用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("returnDeposit  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//限频
		Boolean getLockSuccess = redisService.setNx(ElectricityCabinetConstant.ELE_CACHE_USER_DEPOSIT_LOCK_KEY + user.getUid(), IdUtil.fastSimpleUUID(), 3 * 1000L, false);
		if (!getLockSuccess) {
			return R.fail("操作频繁,请稍后再试!");
		}

		//用户
		UserInfo userInfo = userInfoService.queryByUid(user.getUid());
		if (Objects.isNull(userInfo)) {
			log.error("returnDeposit  ERROR! not found user,uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0019", "未找到用户");
		}

		//用户是否可用
		if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("returnDeposit  ERROR! user is unUsable! uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}

		//是否缴纳押金，是否绑定电池
		FranchiseeUserInfo oldFranchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

		//未找到用户
		if (Objects.isNull(oldFranchiseeUserInfo)) {
			log.error("payDeposit  ERROR! not found user! userId:{}", user.getUid());
			return R.fail("ELECTRICITY.0001", "未找到用户");

		}

		//判断是否退电池
		if (Objects.equals(oldFranchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_BATTERY)) {
			log.error("returnDeposit  ERROR! not return battery! uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0046", "未退还电池");
		}

		//判断是否缴纳押金
		if (Objects.equals(oldFranchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
				|| Objects.isNull(oldFranchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(oldFranchiseeUserInfo.getOrderId())) {
			log.error("returnDeposit  ERROR! not pay deposit! uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0042", "未缴纳押金");
		}

		if(Objects.equals(oldFranchiseeUserInfo.getOrderId(),"-1")){
			return R.fail("ELECTRICITY.00115", "请线下退押");
		}

		//是否存在未完成的租电池订单
		RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByUidAndType(user.getUid());
		if (Objects.nonNull(rentBatteryOrder)) {
			if(Objects.equals(rentBatteryOrder.getType(),RentBatteryOrder.TYPE_USER_RENT)) {
				return R.fail((Object) rentBatteryOrder.getOrderId(), "ELECTRICITY.0013", "存在未完成租电订单，不能下单");
			}else if(Objects.equals(rentBatteryOrder.getType(),RentBatteryOrder.TYPE_USER_RETURN)){
				return R.fail((Object) rentBatteryOrder.getOrderId(), "ELECTRICITY.0095", "存在未完成还电订单，不能下单");
			}
		}



		//是否存在未完成的换电订单
		ElectricityCabinetOrder oldElectricityCabinetOrder = electricityCabinetOrderService.queryByUid(user.getUid());
		if (Objects.nonNull(oldElectricityCabinetOrder)) {
			return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "ELECTRICITY.0094", "存在未完成换电订单，不能下单");
		}

		//查找缴纳押金订单
		EleDepositOrder eleDepositOrder = eleDepositOrderMapper.selectOne(new LambdaQueryWrapper<EleDepositOrder>().eq(EleDepositOrder::getOrderId, oldFranchiseeUserInfo.getOrderId()));
		if (Objects.isNull(eleDepositOrder)) {
			return R.fail("ELECTRICITY.0015", "未找到订单");
		}

		BigDecimal deposit = oldFranchiseeUserInfo.getBatteryDeposit();
		if (!Objects.equals(eleDepositOrder.getPayAmount(), deposit)) {
			return R.fail("ELECTRICITY.0044", "退款金额不符");
		}

		BigDecimal payAmount = eleDepositOrder.getPayAmount();

		//退款零元
		if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
			eleDepositOrder.setStatus(EleDepositOrder.STATUS_SUCCESS);
			eleDepositOrderMapper.insert(eleDepositOrder);

			//用户
			FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
			franchiseeUserInfo.setId(userInfo.getId());
			franchiseeUserInfo.setServiceStatus(FranchiseeUserInfo.STATUS_IS_INIT);
			franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
			franchiseeUserInfo.setBatteryDeposit(null);
			franchiseeUserInfo.setOrderId(null);
			franchiseeUserInfoService.updateRefund(franchiseeUserInfo);
			return R.ok();
		}

		//是否有正在进行中的退款
		Integer refundCount = eleRefundOrderService.queryCountByOrderId(eleDepositOrder.getOrderId());
		if (refundCount > 0) {
			return R.fail("ELECTRICITY.0047", "请勿重复退款");
		}

		String orderId = generateOrderId(user.getUid());

		//生成退款订单
		EleRefundOrder eleRefundOrder = EleRefundOrder.builder()
				.orderId(eleDepositOrder.getOrderId())
				.refundOrderNo(orderId)
				.payAmount(payAmount)
				.refundAmount(payAmount)
				.status(EleRefundOrder.STATUS_INIT)
				.createTime(System.currentTimeMillis())
				.updateTime(System.currentTimeMillis())
				.tenantId(eleDepositOrder.getTenantId()).build();
		eleRefundOrderService.insert(eleRefundOrder);

		//等到后台同意退款
		return R.ok();
	}

	@Override
	public R queryList(EleDepositOrderQuery eleDepositOrderQuery) {
		return R.ok(eleDepositOrderMapper.queryList(eleDepositOrderQuery));
	}

	@Override
	public void update(EleDepositOrder eleDepositOrderUpdate) {
		eleDepositOrderMapper.updateById(eleDepositOrderUpdate);
	}

	@Override
	public R queryUserDeposit() {
		Map<String, String> map = new HashMap<>();
		//用户信息
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}
		User user = userService.queryByUidFromCache(uid);
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user! userId:{}", uid);
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//用户是否缴纳押金
		UserInfo userInfo = userInfoService.queryByUid(uid);
		if (Objects.isNull(userInfo)) {
			log.error("ELECTRICITY  ERROR! not found userInfo! userId:{}", uid);
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//是否缴纳押金，是否绑定电池
		FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

		//未找到用户
		if (Objects.isNull(franchiseeUserInfo)) {
			log.error("payDeposit  ERROR! not found user! userId:{}", user.getUid());
			return R.fail("ELECTRICITY.0001", "未找到用户");

		}

		String batteryType = franchiseeUserInfo.getBatteryType();
		if (Objects.nonNull(batteryType)) {
			map.put("batteryType", BatteryConstant.acquireBattery(batteryType).toString());
		}else {
			map.put("batteryType", null);
		}

		if ((Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_DEPOSIT)
				|| Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_BATTERY))
				&& Objects.nonNull(franchiseeUserInfo.getBatteryDeposit()) && Objects.nonNull(franchiseeUserInfo.getOrderId())) {

			if(Objects.equals(franchiseeUserInfo.getOrderId(),"-1")){
				map.put("refundStatus", null);
				map.put("deposit", franchiseeUserInfo.getBatteryDeposit().toString());
				map.put("time", String.valueOf(System.currentTimeMillis()));
			}else {
				//是否退款
				Integer refundStatus = eleRefundOrderService.queryStatusByOrderId(franchiseeUserInfo.getOrderId());
				if (Objects.nonNull(refundStatus)) {
					map.put("refundStatus", refundStatus.toString());
				} else {
					map.put("refundStatus", null);
				}

				map.put("deposit", franchiseeUserInfo.getBatteryDeposit().toString());
				//最后一次缴纳押金时间
				map.put("time", this.queryByOrderId(franchiseeUserInfo.getOrderId()).getUpdateTime().toString());
			}
			return R.ok(map);

		}
		return R.ok(null);
	}

	@Override
	public void exportExcel(EleDepositOrderQuery eleDepositOrderQuery, HttpServletResponse response) {
		eleDepositOrderQuery.setOffset(0L);
		eleDepositOrderQuery.setSize(2000L);
		List<EleDepositOrderVO> eleDepositOrderList = eleDepositOrderMapper.queryList(eleDepositOrderQuery);
		if (ObjectUtil.isEmpty(eleDepositOrderList)) {
			throw new CustomBusinessException("查不到订单");
		}

		List<EleDepositOrderExcelVO> eleDepositOrderExcelVOS = new ArrayList();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int index = 0;
		for (EleDepositOrderVO eleDepositOrder : eleDepositOrderList) {
			index++;
			EleDepositOrderExcelVO excelVo = new EleDepositOrderExcelVO();
			excelVo.setId(index);
			excelVo.setOrderId(eleDepositOrder.getOrderId());
			excelVo.setPhone(eleDepositOrder.getPhone());
			excelVo.setName(eleDepositOrder.getName());
			excelVo.setPayAmount(eleDepositOrder.getPayAmount());

			if (Objects.nonNull(eleDepositOrder.getCreateTime())) {
				excelVo.setCreatTime(simpleDateFormat.format(new Date(eleDepositOrder.getCreateTime())));
			}

			if (Objects.isNull(eleDepositOrder.getStatus())) {
				excelVo.setStatus("");
			}
			if (Objects.equals(eleDepositOrder.getStatus(), EleDepositOrder.STATUS_INIT)) {
				excelVo.setStatus("未支付");
			}
			if (Objects.equals(eleDepositOrder.getStatus(), EleDepositOrder.STATUS_SUCCESS)) {
				excelVo.setStatus("支付成功");
			}
			if (Objects.equals(eleDepositOrder.getStatus(), EleDepositOrder.STATUS_FAIL)) {
				excelVo.setStatus("支付失败");
			}

			eleDepositOrderExcelVOS.add(excelVo);
		}

		String fileName = "换电订单报表.xlsx";
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			// 告诉浏览器用什么软件可以打开此文件
			response.setHeader("content-Type", "application/vnd.ms-excel");
			// 下载文件的默认名称
			response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
			EasyExcel.write(outputStream, EleDepositOrderExcelVO.class).sheet("sheet").doWrite(eleDepositOrderExcelVOS);
			return;
		} catch (IOException e) {
			log.error("导出报表失败！", e);
		}
	}

	@Override
	public R queryDeposit(String productKey, String deviceName) {
		//换电柜
		ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(productKey, deviceName);
		if (Objects.isNull(electricityCabinet)) {
			log.error("queryDeposit  ERROR! not found electricityCabinet ！productKey{},deviceName{}", productKey, deviceName);
			return R.fail("ELECTRICITY.0005", "未找到换电柜");
		}

		//查询押金
		//查找换电柜门店
		if (Objects.isNull(electricityCabinet.getStoreId())) {
			log.error("queryDeposit  ERROR! not found store ！electricityCabinetId{}", electricityCabinet.getId());
			return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
		}
		Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
		if (Objects.isNull(store)) {
			log.error("queryDeposit  ERROR! not found store ！storeId{}", electricityCabinet.getStoreId());
			return R.fail("ELECTRICITY.0018", "未找到门店");
		}

		//查找门店加盟商
		if (Objects.isNull(store.getFranchiseeId())) {
			log.error("queryDeposit  ERROR! not found Franchisee ！storeId{}", store.getId());
			return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
		}

		Franchisee franchisee = franchiseeService.queryByIdFromDB(store.getFranchiseeId());
		if (Objects.isNull(franchisee)) {
			log.error("queryDeposit  ERROR! not found Franchisee ！franchiseeId{}", store.getFranchiseeId());
			return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
		}

		//根据类型分押金
		if (Objects.equals(franchisee.getModelType(), Franchisee.MEW_MODEL_TYPE)) {
			//型号押金
			List modelBatteryDepositList = JsonUtil.fromJson(franchisee.getModelBatteryDeposit(), List.class);
			return R.ok(modelBatteryDepositList);
		}

		return R.ok(franchisee.getBatteryDeposit());
	}

	@Override
	public R queryCount(EleDepositOrderQuery eleDepositOrderQuery) {
		return R.ok(eleDepositOrderMapper.queryCount(eleDepositOrderQuery));
	}

	@Override
	public void insert(EleDepositOrder eleDepositOrder) {
		eleDepositOrderMapper.insert(eleDepositOrder);
	}

	@Override
	public R queryModelType(String productKey, String deviceName) {
		//换电柜
		ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(productKey, deviceName);
		if (Objects.isNull(electricityCabinet)) {
			log.error("queryDeposit  ERROR! not found electricityCabinet ！productKey{},deviceName{}", productKey, deviceName);
			return R.fail("ELECTRICITY.0005", "未找到换电柜");
		}

		//查询押金
		//查找换电柜门店
		if (Objects.isNull(electricityCabinet.getStoreId())) {
			log.error("queryDeposit  ERROR! not found store ！electricityCabinetId{}", electricityCabinet.getId());
			return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
		}
		Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
		if (Objects.isNull(store)) {
			log.error("queryDeposit  ERROR! not found store ！storeId{}", electricityCabinet.getStoreId());
			return R.fail("ELECTRICITY.0018", "未找到门店");
		}

		//查找门店加盟商
		if (Objects.isNull(store.getFranchiseeId())) {
			log.error("queryDeposit  ERROR! not found Franchisee ！storeId{}", store.getId());
			return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
		}

		Franchisee franchisee = franchiseeService.queryByIdFromDB(store.getFranchiseeId());
		if (Objects.isNull(franchisee)) {
			log.error("queryDeposit  ERROR! not found Franchisee ！franchiseeId{}", store.getFranchiseeId());
			return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
		}

		return R.ok(franchisee.getModelType());
	}

	public String generateOrderId(Long uid) {
		return String.valueOf(System.currentTimeMillis()).substring(2) + uid +
				RandomUtil.randomNumbers(6);
	}
}
