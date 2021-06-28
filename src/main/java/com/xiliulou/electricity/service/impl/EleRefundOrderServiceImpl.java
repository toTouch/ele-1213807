package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.RefundOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.EleRefundOrderMapper;
import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.pay.weixinv3.query.WechatV3RefundQuery;
import com.xiliulou.pay.weixinv3.service.WechatV3JsapiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 退款订单表(TEleRefundOrder)表服务实现类
 *
 * @author makejava
 * @since 2021-02-22 10:21:24
 */
@Service("eleRefundOrderService")
@Slf4j
public class EleRefundOrderServiceImpl implements EleRefundOrderService {
	@Resource
	EleRefundOrderMapper eleRefundOrderMapper;
	@Autowired
	RedisService redisService;
	@Autowired
	UserService userService;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	ElectricityTradeOrderService electricityTradeOrderService;
	@Autowired
	EleDepositOrderService eleDepositOrderService;
	@Autowired
	ElectricityPayParamsService electricityPayParamsService;
	@Autowired
	EleRefundOrderService eleRefundOrderService;
	@Autowired
	FranchiseeUserInfoService franchiseeUserInfoService;
	@Autowired
	WechatV3JsapiService wechatV3JsapiService;


	/**
	 * 新增数据
	 *
	 * @param eleRefundOrder 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public EleRefundOrder insert(EleRefundOrder eleRefundOrder) {
		this.eleRefundOrderMapper.insert(eleRefundOrder);
		return eleRefundOrder;
	}

	/**
	 * 修改数据
	 *
	 * @param eleRefundOrder 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(EleRefundOrder eleRefundOrder) {
		return this.eleRefundOrderMapper.updateById(eleRefundOrder);

	}

	@Override
	public WechatJsapiRefundResultDTO commonCreateRefundOrder(RefundOrder refundOrder, HttpServletRequest request) throws WechatPayException {

		//第三方订单号
		ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByOrderId(refundOrder.getOrderId());
		if (Objects.isNull(electricityTradeOrder)) {
			log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER ORDER_NO:{}", refundOrder.getOrderId());
			throw new CustomBusinessException("未找到交易订单!");
		}

		//退款
		WechatV3RefundQuery wechatV3RefundQuery = new WechatV3RefundQuery();
		wechatV3RefundQuery.setTenantId(electricityTradeOrder.getTenantId());
		wechatV3RefundQuery.setTotal(refundOrder.getPayAmount().multiply(new BigDecimal(100)).intValue());
		wechatV3RefundQuery.setRefund(refundOrder.getRefundAmount().multiply(new BigDecimal(100)).intValue());
		wechatV3RefundQuery.setReason("老子要退");
		wechatV3RefundQuery.setOrderId(electricityTradeOrder.getTradeOrderNo());
		wechatV3RefundQuery.setNotifyUrl("https://eclair.xiliulou.com/frp/qifaner/outer/wechat/refund/notified/" + electricityTradeOrder.getTenantId());
		wechatV3RefundQuery.setCurrency("CNY");
		wechatV3RefundQuery.setRefundId(electricityTradeOrder.getTradeOrderNo() + "_re");

		return wechatV3JsapiService.refund(wechatV3RefundQuery);
	}

	@Override
	public Pair<Boolean, Object> notifyDepositRefundOrder(WechatJsapiRefundOrderCallBackResource callBackResource) {
		//回调参数
		String tradeRefundNo = callBackResource.getOutRefundNo();
		String outTradeNo = callBackResource.getOutTradeNo();
		String refundStatus = callBackResource.getRefundStatus();

		//退款订单
		EleRefundOrder eleRefundOrder = eleRefundOrderMapper.selectOne(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getRefundOrderNo, tradeRefundNo));
		if (Objects.isNull(eleRefundOrder)) {
			log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER ORDER_NO:{}", tradeRefundNo);
			return Pair.of(false, "未找到退款订单!");
		}
		if (ObjectUtil.notEqual(EleRefundOrder.STATUS_REFUND, eleRefundOrder.getStatus())) {
			log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", tradeRefundNo);
			return Pair.of(false, "退款订单已处理");
		}

		//交易订单
		ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByTradeOrderNo(outTradeNo);
		if (Objects.isNull(electricityTradeOrder)) {
			log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER ORDER_NO:{}", outTradeNo);
			return Pair.of(false, "未找到交易订单!");
		}

		EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(electricityTradeOrder.getOrderNo());
		if (ObjectUtil.isEmpty(eleDepositOrder)) {
			log.error("NOTIFY_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO:{}", electricityTradeOrder.getOrderNo());
			return Pair.of(false, "未找到订单!");
		}

		Integer refundOrderStatus = EleRefundOrder.STATUS_FAIL;
		boolean result = false;
		if (StringUtils.isNotEmpty(refundStatus) && ObjectUtil.equal(refundStatus, "SUCCESS")) {
			refundOrderStatus = EleRefundOrder.STATUS_SUCCESS;
			result = true;
		} else {
			log.error("NOTIFY REDULT PAY FAIL,ORDER_NO:{}" + tradeRefundNo);
		}

		UserInfo userInfo = userInfoService.selectUserByUid(eleDepositOrder.getUid());
		if (Objects.isNull(userInfo)) {
			log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID:{},ORDER_NO:{}", eleDepositOrder.getUid(), tradeRefundNo);
			return Pair.of(false, "未找到用户信息!");
		}

		//是否缴纳押金，是否绑定电池
		FranchiseeUserInfo oldFranchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

		//未找到用户
		if (Objects.isNull(oldFranchiseeUserInfo)) {
			log.error("NOTIFY  ERROR! not found user! uid:{} ", userInfo.getUid());
			return Pair.of(false, "未找到用户信息!");

		}


		if (Objects.equals(refundOrderStatus, EleRefundOrder.STATUS_SUCCESS)) {
			FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
			franchiseeUserInfo.setId(oldFranchiseeUserInfo.getId());
			franchiseeUserInfo.setServiceStatus(UserInfo.STATUS_IS_AUTH);
			franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
			franchiseeUserInfo.setBatteryDeposit(null);
			franchiseeUserInfo.setOrderId(null);
			franchiseeUserInfoService.update(franchiseeUserInfo);
		}

		EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
		eleRefundOrderUpdate.setId(eleRefundOrder.getId());
		eleRefundOrderUpdate.setStatus(refundOrderStatus);
		eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
		eleRefundOrderMapper.updateById(eleRefundOrderUpdate);
		return Pair.of(result, null);
	}

	@Override
	public R handleRefund(String refundOrderNo, Integer status, HttpServletRequest request) {
		EleRefundOrder eleRefundOrder = eleRefundOrderMapper.selectOne(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getRefundOrderNo, refundOrderNo).in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_REFUSE_REFUND));
		if (Objects.isNull(eleRefundOrder)) {
			log.error("REFUND_ORDER ERROR ,NOT FOUND ELECTRICITY_REFUND_ORDER ORDER_NO:{}", refundOrderNo);
			return R.fail("未找到退款订单!");
		}

		//同意退款
		if (Objects.equals(status, EleRefundOrder.STATUS_AGREE_REFUND)) {
			//修改订单状态
			EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
			eleRefundOrderUpdate.setId(eleRefundOrder.getId());
			eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_AGREE_REFUND);
			eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
			eleRefundOrderService.update(eleRefundOrderUpdate);

			//调起退款
			try {
				RefundOrder refundOrder = RefundOrder.builder()
						.orderId(eleRefundOrder.getOrderId())
						.refundOrderNo(eleRefundOrder.getRefundOrderNo())
						.payAmount(eleRefundOrder.getPayAmount())
						.refundAmount(eleRefundOrder.getRefundAmount()).build();
				WechatJsapiRefundResultDTO getPayParamsPair = eleRefundOrderService.commonCreateRefundOrder(refundOrder, request);
				//提交成功
				eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUND);
				eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
				eleRefundOrderService.update(eleRefundOrderUpdate);
				return R.ok();
			} catch (WechatPayException e) {
				log.error("handleRefund ERROR! wechat v3 refund  error! ", e);
			}
			//提交失败
			eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_FAIL);
			eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
			eleRefundOrderService.update(eleRefundOrderUpdate);
			return R.fail("ELECTRICITY.00100", "退款失败");

		}

		//拒绝退款
		if (Objects.equals(status, EleRefundOrder.STATUS_REFUSE_REFUND)) {
			//修改订单状态
			EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
			eleRefundOrderUpdate.setId(eleRefundOrder.getId());
			eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUSE_REFUND);
			eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
			eleRefundOrderService.update(eleRefundOrderUpdate);
		}
		return R.ok();
	}

	@Override
	public R queryList(EleRefundQuery eleRefundQuery) {
		return R.ok(eleRefundOrderMapper.queryList(eleRefundQuery));
	}

	@Override
	public Integer queryCountByOrderId(String orderId) {
		return eleRefundOrderMapper.selectCount(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, orderId).in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_AGREE_REFUND, EleRefundOrder.STATUS_REFUND, EleRefundOrder.STATUS_SUCCESS));
	}

	@Override
	public Integer queryStatusByOrderId(String orderId) {
		List<EleRefundOrder> eleRefundOrderList = eleRefundOrderMapper.selectList(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, orderId).orderByDesc(EleRefundOrder::getUpdateTime));
		if (ObjectUtil.isEmpty(eleRefundOrderList)) {
			return null;
		}
		return eleRefundOrderList.get(0).getStatus();
	}

}
