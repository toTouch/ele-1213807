package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.query.MemberCardOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityMemberCardOrderExcelVO;
import com.xiliulou.electricity.vo.ElectricityMemberCardOrderVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 10:54
 **/
@Service
@Slf4j
public class ElectricityMemberCardOrderServiceImpl extends ServiceImpl<ElectricityMemberCardOrderMapper, ElectricityMemberCardOrder> implements ElectricityMemberCardOrderService {

	@Autowired
	ElectricityMemberCardService electricityMemberCardService;
	@Autowired
	ElectricityTradeOrderService electricityTradeOrderService;
	@Autowired
	ElectricityPayParamsService electricityPayParamsService;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	UserService userService;
	@Autowired
	UserOauthBindService userOauthBindService;
	@Autowired
	StoreService storeService;
	@Autowired
	ElectricityCabinetService electricityCabinetService;
	@Autowired
	FranchiseeUserInfoService franchiseeUserInfoService;

	/**
	 * 创建月卡订单
	 *
	 * @param
	 * @param memberId
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R createOrder(Integer memberId,Integer electricityCabinetId, HttpServletRequest request) {
		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("rentBattery  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//支付相关
		ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
		if (Objects.isNull(electricityPayParams)) {
			log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND PAY_PARAMS");
			return R.failMsg("未配置支付参数!");
		}

		UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid());

		if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
			log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND USEROAUTHBIND OR THIRDID IS NULL  UID:{}", user.getUid());
			return R.failMsg("未找到用户的第三方授权信息!");
		}

		//换电柜
		ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
		if (Objects.isNull(electricityCabinet)) {
			log.error("rentBattery  ERROR! not found electricityCabinet ！electricityCabinetId{}", electricityCabinetId);
			return R.fail("ELECTRICITY.0005", "未找到换电柜");
		}



		//3、查出套餐
		//查找换电柜门店
		if(Objects.isNull(electricityCabinet.getStoreId())){
			log.error("queryByDevice  ERROR! not found store ！electricityCabinetId{}", electricityCabinet.getId());
			return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
		}
		Store store=storeService.queryByIdFromCache(electricityCabinet.getStoreId());
		if(Objects.isNull(store)){
			log.error("queryByDevice  ERROR! not found store ！storeId{}", electricityCabinet.getStoreId());
			return R.fail("ELECTRICITY.0018", "未找到门店");
		}


		//查找门店加盟商
		if(Objects.isNull(store.getFranchiseeId())){
			log.error("queryByDevice  ERROR! not found Franchisee ！storeId{}", store.getId());
			return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
		}


		//用户
		UserInfo userInfo = userInfoService.selectUserByUid(user.getUid());

		if (Objects.isNull(userInfo)) {
			log.error("ELECTRICITY  ERROR! not found user,uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0019", "未找到用户");
		}

		//用户是否可用
		if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("ELECTRICITY  ERROR! user is unUsable! uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}

		//未实名认证
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			log.error("ELECTRICITY  ERROR! user not auth! uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}

		//是否缴纳押金，是否绑定电池
		List<FranchiseeUserInfo> franchiseeUserInfoList = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
		//未找到用户
		if (franchiseeUserInfoList.size() < 1) {
			log.error("rentBattery  ERROR! not found user! uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0001", "未找到用户");

		}

		//出现多个用户绑定或没有用户绑定
		if (franchiseeUserInfoList.size() > 1) {
			log.error("rentBattery  ERROR! user status is error! uid:{} ",user.getUid());
			return R.fail("ELECTRICITY.0052", "用户状态异常，请联系管理员");
		}


		//用户
		FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoList.get(0);

		//判断该换电柜加盟商和用户加盟商是否一致
		if(!Objects.equals(store.getFranchiseeId(),franchiseeUserInfo.getFranchiseeId())){
			log.error("queryByDevice  ERROR!FranchiseeId is not equal!uid:{} , FranchiseeId1:{} ,FranchiseeId2:{}", user.getUid(),store.getFranchiseeId(),franchiseeUserInfo.getFranchiseeId());
			return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
		}

		//判断是否缴纳押金
		if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
				|| Objects.isNull(franchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(franchiseeUserInfo.getOrderId())) {
			log.error("rentBattery  ERROR! not pay deposit! uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0042", "未缴纳押金");
		}


		ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(memberId);
		if (Objects.isNull(electricityMemberCard)) {
			log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND MEMBER_CARD BY ID:{}", memberId);
			return R.fail("ELECTRICITY.0087", "未找到月卡套餐!");
		}
		if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
			log.error("CREATE MEMBER_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID:{}", memberId);
			return R.fail("ELECTRICITY.0088", "月卡已禁用!");
		}


		Long now = System.currentTimeMillis();
		Long remainingNumber = electricityMemberCard.getMaxUseCount();

		//同一个套餐可以续费
		if (Objects.equals(franchiseeUserInfo.getCardId(), memberId)) {
			now = franchiseeUserInfo.getMemberCardExpireTime();
			//TODO 使用次数暂时叠加
			if (franchiseeUserInfo.getRemainingNumber() > 0) {
				remainingNumber = remainingNumber + franchiseeUserInfo.getRemainingNumber();
			}

		} else {
			if (Objects.nonNull(franchiseeUserInfo.getMemberCardExpireTime())
					&& Objects.nonNull(franchiseeUserInfo.getRemainingNumber()) &&
					franchiseeUserInfo.getMemberCardExpireTime() > now &&
					(ObjectUtil.equal(ElectricityMemberCard.UN_LIMITED_COUNT, franchiseeUserInfo.getRemainingNumber()) || franchiseeUserInfo.getRemainingNumber() > 0)) {
				log.error("CREATE MEMBER_ORDER ERROR ,MEMBER_CARD IS NOT EXPIRED USERINFO:{}", userInfo);
				return R.fail("ELECTRICITY.0089", "您的月卡还未过期,无需再次购买!");
			}
		}


		ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
		electricityMemberCardOrder.setOrderId(String.valueOf(System.currentTimeMillis()));
		electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
		electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
		electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
		electricityMemberCardOrder.setMemberCardId(memberId);
		electricityMemberCardOrder.setUid(user.getUid());
		electricityMemberCardOrder.setMaxUseCount(electricityMemberCard.getMaxUseCount());
		electricityMemberCardOrder.setMemberCardType(electricityMemberCard.getType());
		electricityMemberCardOrder.setCardName(electricityMemberCard.getName());
		electricityMemberCardOrder.setPayAmount(electricityMemberCard.getHolidayPrice());
		electricityMemberCardOrder.setUserName(userInfo.getUserName());
		electricityMemberCardOrder.setValidDays(electricityMemberCard.getValidDays());
		baseMapper.insert(electricityMemberCardOrder);
		//支付零元
		if (electricityMemberCardOrder.getPayAmount().compareTo(BigDecimal.valueOf(0.01)) < 0) {

			//用户
			FranchiseeUserInfo franchiseeUserInfoUpdate = new FranchiseeUserInfo();
			franchiseeUserInfoUpdate.setId(franchiseeUserInfo.getId());
			Long memberCardExpireTime = now +
					electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
			franchiseeUserInfoUpdate.setMemberCardExpireTime(memberCardExpireTime);
			franchiseeUserInfoUpdate.setRemainingNumber(remainingNumber);
			franchiseeUserInfoUpdate.setCardName(electricityMemberCardOrder.getCardName());
			franchiseeUserInfoUpdate.setCardId(electricityMemberCardOrder.getMemberCardId());
			franchiseeUserInfoUpdate.setCardType(electricityMemberCardOrder.getMemberCardType());
			franchiseeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
			franchiseeUserInfoService.update(franchiseeUserInfoUpdate);


			//月卡订单
			ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
			electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
			electricityMemberCardOrderUpdate.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
			electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
			baseMapper.updateById(electricityMemberCardOrderUpdate);
			return R.ok();
		}

		//调起支付
		try {
			CommonPayOrder commonPayOrder = CommonPayOrder.builder()
					.orderId(electricityMemberCardOrder.getOrderId())
					.uid(user.getUid())
					.payAmount(electricityMemberCardOrder.getPayAmount())
					.orderType(ElectricityTradeOrder.ORDER_TYPE_DEPOSIT)
					.attach(ElectricityTradeOrder.ATTACH_DEPOSIT).build();

			WechatJsapiOrderResultDTO resultDTO =
					electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
			return R.ok(resultDTO);
		} catch (WechatPayException e) {
			log.error("CREATE MEMBER_ORDER ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
		}

		return R.fail("ELECTRICITY.0099", "下单失败");
	}

	@Override
	public BigDecimal homeOne(Long first, Long now, List<Integer> cardIdList) {
		return baseMapper.homeOne(first, now, cardIdList);
	}

	@Override
	public List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay, List<Integer> cardIdList) {
		return baseMapper.homeThree(startTimeMilliDay, endTimeMilliDay, cardIdList);
	}

	@Override
	public R queryUserList(Long offset, Long size, Long startTime, Long endTime) {
		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("rentBattery  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}
		return R.ok(baseMapper.queryUserList(user.getUid(), offset, size, startTime, endTime));
	}

	/**
	 * 获取交易次数
	 *
	 * @param uid
	 * @return
	 */
	@Override
	public R getMemberCardOrderCount(Long uid, Long startTime, Long endTime) {
		return R.ok(baseMapper.getMemberCardOrderCount(uid, startTime, endTime));
	}


	@Override
	@DS("slave_1")
	public R queryList(Long offset, Long size, MemberCardOrderQuery memberCardOrderQuery) {
		return R.ok(baseMapper.queryList(memberCardOrderQuery));
	}

	@Override
	public void exportExcel(MemberCardOrderQuery memberCardOrderQuery, HttpServletResponse response) {
		List<ElectricityMemberCardOrderVO> electricityMemberCardOrderVOList =baseMapper.queryList(memberCardOrderQuery);
		if (ObjectUtil.isEmpty(electricityMemberCardOrderVOList)) {
			throw new CustomBusinessException("查不到订单");
		}

		List<ElectricityMemberCardOrderExcelVO> electricityMemberCardOrderExcelVOS = new ArrayList();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int index = 0;
		for (ElectricityMemberCardOrderVO electricityMemberCardOrderVO : electricityMemberCardOrderVOList) {
			index++;
			ElectricityMemberCardOrderExcelVO excelVo = new ElectricityMemberCardOrderExcelVO();
			excelVo.setId(index);
			excelVo.setOrderId(electricityMemberCardOrderVO.getOrderId());
			excelVo.setPhone(electricityMemberCardOrderVO.getPhone());

			if (Objects.nonNull(electricityMemberCardOrderVO.getUpdateTime())) {
				excelVo.setBeginningTime(simpleDateFormat.format(new Date(electricityMemberCardOrderVO.getUpdateTime())));
				if (Objects.nonNull(electricityMemberCardOrderVO.getValidDays())) {
					excelVo.setEndTime(simpleDateFormat.format(new Date(electricityMemberCardOrderVO.getUpdateTime() + electricityMemberCardOrderVO.getValidDays() * 24 * 60 * 60 * 1000)));
				}
			}

			if (Objects.isNull(electricityMemberCardOrderVO.getMemberCardType())) {
				excelVo.setMemberCardType("");
			}
			if (Objects.equals(electricityMemberCardOrderVO.getMemberCardType(), ElectricityCabinetOrder.PAYMENT_METHOD_MONTH_CARD)) {
				excelVo.setMemberCardType("月卡");
			}
			if (Objects.equals(electricityMemberCardOrderVO.getMemberCardType(), ElectricityCabinetOrder.PAYMENT_METHOD_SEASON_CARD)) {
				excelVo.setMemberCardType("季卡");
			}
			if (Objects.equals(electricityMemberCardOrderVO.getMemberCardType(), ElectricityCabinetOrder.PAYMENT_METHOD_YEAR_CARD)) {
				excelVo.setMemberCardType("年卡");
			}

			if (Objects.isNull(electricityMemberCardOrderVO.getStatus())) {
				excelVo.setStatus("");
			}
			if (Objects.equals(electricityMemberCardOrderVO.getStatus(), ElectricityMemberCardOrder.STATUS_INIT)) {
				excelVo.setStatus("未支付");
			}
			if (Objects.equals(electricityMemberCardOrderVO.getStatus(), ElectricityMemberCardOrder.STATUS_SUCCESS)) {
				excelVo.setStatus("支付成功");
			}
			if (Objects.equals(electricityMemberCardOrderVO.getStatus(), ElectricityMemberCardOrder.STATUS_FAIL)) {
				excelVo.setStatus("支付失败");
			}

			electricityMemberCardOrderExcelVOS.add(excelVo);
		}

		String fileName = "购卡订单报表.xlsx";
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			// 告诉浏览器用什么软件可以打开此文件
			response.setHeader("content-Type", "application/vnd.ms-excel");
			// 下载文件的默认名称
			response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
			EasyExcel.write(outputStream, ElectricityMemberCardOrderExcelVO.class).sheet("sheet").doWrite(electricityMemberCardOrderExcelVOS);
			return;
		} catch (IOException e) {
			log.error("导出报表失败！", e);
		}
	}
}
