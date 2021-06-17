package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.mapper.EleDepositOrderMapper;
import com.xiliulou.electricity.query.EleDepositOrderQuery;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.utils.PageUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.EleDepositOrderExcelVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
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

	@Override
	public EleDepositOrder queryByOrderId(String orderNo) {
		return eleDepositOrderMapper.selectOne(new LambdaQueryWrapper<EleDepositOrder>().eq(EleDepositOrder::getOrderId, orderNo));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R payDeposit(HttpServletRequest request) {
		//用户信息
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}
		//限频
		Boolean getLockSuccess = redisService.setNx(ElectricityCabinetConstant.ELE_CACHE_USER_DEPOSIT_LOCK_KEY + uid, IdUtil.fastSimpleUUID(), 3 * 1000L, false);
		if (!getLockSuccess) {
			return R.fail("ELECTRICITY.0034", "操作频繁");
		}
		User user = userService.queryByUidFromCache(uid);
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user! userId:{}", uid);
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//判断是否实名认证
		UserInfo userInfo = userInfoService.queryByUid(uid);
		//用户是否可用
		if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("ELECTRICITY  ERROR! not found userInfo,uid:{} ", uid);
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}

		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_DEPOSIT)) {
			return R.fail("ELECTRICITY.0049", "已缴纳押金");
		}

		//计算押金
		//根据用户cid找到对应的加盟商
		Franchisee franchisee = franchiseeService.queryByCid(user.getCid());
		if (Objects.isNull(franchisee)) {
			log.error("ELECTRICITY  ERROR! not found franchisee ! cid:{} ", user.getCid());
			//麒迹 未找到加盟商默认郑州，郑州也找不到再提示找不到 其余客服需要换  TODO
			franchisee = franchiseeService.queryByCid(147);
			if (Objects.isNull(franchisee)) {
				return R.fail("ELECTRICITY.0038", "未找到加盟商");
			}
		}
		BigDecimal payAmount = franchisee.getBatteryDeposit();
		String orderId = generateOrderId(uid);

		//生成订单
		EleDepositOrder eleDepositOrder = EleDepositOrder.builder()
				.orderId(orderId)
				.uid(user.getUid())
				.phone(userInfo.getPhone())
				.name(userInfo.getName())
				.payAmount(payAmount)
				.status(EleDepositOrder.STATUS_INIT)
				.createTime(System.currentTimeMillis())
				.updateTime(System.currentTimeMillis()).build();

		//支付零元
		if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
			eleDepositOrder.setStatus(EleDepositOrder.STATUS_SUCCESS);
			eleDepositOrderMapper.insert(eleDepositOrder);
			UserInfo userInfoUpdate = new UserInfo();
			userInfoUpdate.setId(userInfo.getId());
			userInfoUpdate.setServiceStatus(UserInfo.STATUS_IS_DEPOSIT);
			userInfoUpdate.setUpdateTime(System.currentTimeMillis());
			userInfoUpdate.setBatteryDeposit(BigDecimal.valueOf(0));
			userInfoUpdate.setOrderId(orderId);
			userInfoService.updateById(userInfoUpdate);
			return R.ok();
		}
		eleDepositOrderMapper.insert(eleDepositOrder);

		//调起支付
		CommonPayOrder commonPayOrder = CommonPayOrder.builder()
				.orderId(orderId)
				.uid(user.getUid())
				.payAmount(payAmount)
				.orderType(ElectricityTradeOrder.ORDER_TYPE_DEPOSIT)
				.attach(ElectricityTradeOrder.ATTACH_DEPOSIT).build();
		ElectricityPayParams electricityPayParams = electricityPayParamsService.getElectricityPayParams();
		UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(uid);
		Pair<Boolean, Object> getPayParamsPair =
				electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
		if (!getPayParamsPair.getLeft()) {
			return R.failMsg(getPayParamsPair.getRight().toString());
		}
		return R.ok(getPayParamsPair.getRight());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R returnDeposit(HttpServletRequest request) {
		//用户信息
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}
		//限频
		Boolean getLockSuccess = redisService.setNx(ElectricityCabinetConstant.ELE_CACHE_USER_DEPOSIT_LOCK_KEY + uid, IdUtil.fastSimpleUUID(), 3 * 1000L, false);
		if (!getLockSuccess) {
			return R.fail("操作频繁,请稍后再试!");
		}
		User user = userService.queryByUidFromCache(uid);
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user! userId:{}", uid);
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		UserInfo userInfo = userInfoService.queryByUid(uid);
		if (Objects.isNull(userInfo)) {
			log.error("ELECTRICITY  ERROR! not found user,uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0019", "未找到用户");
		}
		//用户是否可用
		if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("ELECTRICITY  ERROR! user is unusable! uid:{} ", uid);
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}

		//是否存在未完成的还电池订单
		Integer count1 = rentBatteryOrderService.queryByUidAndType(uid,  RentBatteryOrder.TYPE_USER_RETURN);
		if (count1 > 0) {
			log.error("ELECTRICITY  ERROR! find return order! uid:{} ", uid);
			return R.fail("ELECTRICITY.0013", "存在未完成订单，不能下单");
		}

		//是否存在未完成的租电池订单
		Integer count2 = rentBatteryOrderService.queryByUidAndType(uid,  RentBatteryOrder.TYPE_USER_RENT);
		if (count2 > 0) {
			log.error("ELECTRICITY  ERROR! find rent order! uid:{} ", uid);
			return R.fail("ELECTRICITY.0013", "存在未完成订单，不能下单");
		}

		//判断是否退电池
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_BATTERY)) {
			log.error("ELECTRICITY  ERROR! not return battery! userInfo:{} ", userInfo);
			return R.fail("ELECTRICITY.0046", "未退还电池");
		}

		//用户状态异常
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_BATTERY) && Objects.isNull(userInfo.getNowElectricityBatterySn())) {
			log.error("ELECTRICITY  ERROR! userInfo is error!userInfo:{} ", userInfo);
			return R.fail("ELECTRICITY.0052", "用户状态异常，请联系管理员");
		}

		//判断是否缴纳押金
		if (!Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_DEPOSIT) || Objects.isNull(userInfo.getBatteryDeposit()) || Objects.isNull(userInfo.getOrderId())) {
			log.error("ELECTRICITY  ERROR! not pay deposit! userInfo:{} ", userInfo);
			return R.fail("ELECTRICITY.0042", "未缴纳押金");
		}


		EleDepositOrder eleDepositOrder = eleDepositOrderMapper.selectOne(new LambdaQueryWrapper<EleDepositOrder>().eq(EleDepositOrder::getOrderId, userInfo.getOrderId()));
		if (Objects.isNull(eleDepositOrder)) {
			return R.fail("ELECTRICITY.0015", "未找到订单");
		}

		BigDecimal deposit = userInfo.getBatteryDeposit();
		if (!Objects.equals(eleDepositOrder.getPayAmount(), deposit)) {
			return R.fail("ELECTRICITY.0044", "退款金额不符");
		}

		BigDecimal payAmount = eleDepositOrder.getPayAmount();
		//退款零元
		if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
			eleDepositOrder.setStatus(EleDepositOrder.STATUS_SUCCESS);
			eleDepositOrderMapper.insert(eleDepositOrder);
			UserInfo userInfoUpdate = new UserInfo();
			userInfoUpdate.setId(userInfo.getId());
			userInfoUpdate.setServiceStatus(UserInfo.STATUS_IS_AUTH);
			userInfoUpdate.setUpdateTime(System.currentTimeMillis());
			userInfoUpdate.setBatteryDeposit(null);
			userInfoUpdate.setOrderId(null);
			userInfoService.updateRefund(userInfoUpdate);
			return R.ok();
		}

		//是否有正在进行中的退款
		Integer refundCount = eleRefundOrderService.queryCountByOrderId(eleDepositOrder.getOrderId());
		if (refundCount > 0) {
			return R.fail("ELECTRICITY.0047", "请勿重复退款");
		}

		String orderId = generateOrderId(uid);

		//生成退款订单
		EleRefundOrder eleRefundOrder = EleRefundOrder.builder()
				.orderId(eleDepositOrder.getOrderId())
				.refundOrderNo(orderId)
				.payAmount(payAmount)
				.refundAmount(payAmount)
				.status(EleRefundOrder.STATUS_INIT)
				.createTime(System.currentTimeMillis())
				.updateTime(System.currentTimeMillis()).build();
		eleRefundOrderService.insert(eleRefundOrder);

		//等到后台同意退款
		return R.ok();
	}

	@Override
	public R queryList(EleDepositOrderQuery eleDepositOrderQuery) {
		Page page = PageUtil.getPage(eleDepositOrderQuery.getOffset(), eleDepositOrderQuery.getSize());
		return R.ok(eleDepositOrderMapper.queryList(page, eleDepositOrderQuery));
	}

	@Override
	public void update(EleDepositOrder eleDepositOrderUpdate) {
		eleDepositOrderMapper.updateById(eleDepositOrderUpdate);
	}

	@Override
	public R queryDeposit() {
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

		if ((Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_DEPOSIT) || Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_BATTERY))
				&& Objects.nonNull(userInfo.getBatteryDeposit()) && Objects.nonNull(userInfo.getOrderId())) {
			//是否退款
			Integer refundStatus = eleRefundOrderService.queryStatusByOrderId(userInfo.getOrderId());
			if (Objects.nonNull(refundStatus)) {
				map.put("refundStatus", refundStatus.toString());
			} else {
				map.put("refundStatus", null);
			}
			map.put("deposit", userInfo.getBatteryDeposit().toString());
			//最后一次缴纳押金时间
			map.put("time", this.queryByOrderId(userInfo.getOrderId()).getUpdateTime().toString());
			return R.ok(map);
		}

		Franchisee franchisee = franchiseeService.queryByCid(user.getCid());
		if (Objects.isNull(franchisee)) {
			log.error("ELECTRICITY  ERROR! not found franchisee ! cid:{} ", user.getCid());
			//麒迹 未找到加盟商默认郑州，郑州也找不到再提示找不到 其余客服需要换  TODO
			franchisee = franchiseeService.queryByCid(147);
			if (Objects.isNull(franchisee)) {
				return R.fail("ELECTRICITY.0038", "未找到加盟商");
			}
		}
		map.put("deposit", franchisee.getBatteryDeposit().toString());
		map.put("time", null);
		map.put("refundStatus", null);
		return R.ok(map);
	}

	@Override
	public void exportExcel(EleDepositOrderQuery eleDepositOrderQuery, HttpServletResponse response) {
		Page page = PageUtil.getPage(0L, 2000L);
		eleDepositOrderMapper.queryList(page, eleDepositOrderQuery);
		if (ObjectUtil.isEmpty(page.getRecords())) {
			throw new CustomBusinessException("查不到订单");
		}

		List<EleDepositOrder> eleDepositOrderList = page.getRecords();
		if (!DataUtil.collectionIsUsable(eleDepositOrderList)) {
			throw new CustomBusinessException("查不到订单");
		}

		List<EleDepositOrderExcelVO> EleDepositOrderExcelVOS = new ArrayList();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int index = 0;
		for (EleDepositOrder eleDepositOrder : eleDepositOrderList) {
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

			EleDepositOrderExcelVOS.add(excelVo);
		}

		String fileName = "换电订单报表.xlsx";
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			// 告诉浏览器用什么软件可以打开此文件
			response.setHeader("content-Type", "application/vnd.ms-excel");
			// 下载文件的默认名称
			response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
			EasyExcel.write(outputStream, EleDepositOrderExcelVO.class).sheet("sheet").doWrite(EleDepositOrderExcelVOS);
			return;
		} catch (IOException e) {
			log.error("导出报表失败！", e);
		}
	}

	public String generateOrderId(Long uid) {
		return String.valueOf(System.currentTimeMillis()).substring(2) + uid +
				RandomUtil.randomNumbers(6);
	}
}
