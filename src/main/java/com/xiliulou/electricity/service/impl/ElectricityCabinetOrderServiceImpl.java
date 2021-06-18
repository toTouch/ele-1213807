package com.xiliulou.electricity.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.handler.EleHardwareHandlerManager;
import com.xiliulou.electricity.mapper.ElectricityCabinetOrderMapper;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.query.OpenDoorQuery;
import com.xiliulou.electricity.query.OrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.utils.PageUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderExcelVO;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderVO;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.WarnMsgVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单表(TElectricityCabinetOrder)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
@Service("electricityCabinetOrderService")
@Slf4j
public class ElectricityCabinetOrderServiceImpl implements ElectricityCabinetOrderService {
	@Resource
	private ElectricityCabinetOrderMapper electricityCabinetOrderMapper;
	@Autowired
	ElectricityCabinetService electricityCabinetService;
	@Autowired
	ElectricityCabinetBoxService electricityCabinetBoxService;
	@Autowired
	RedisService redisService;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	EleHardwareHandlerManager eleHardwareHandlerManager;
	@Autowired
	ElectricityConfigService electricityConfigService;
	@Autowired
	RentBatteryOrderService rentBatteryOrderService;

	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public ElectricityCabinetOrder queryByIdFromDB(Long id) {
		return this.electricityCabinetOrderMapper.queryById(id);
	}

	/**
	 * 修改数据
	 *
	 * @param electricityCabinetOrder 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(ElectricityCabinetOrder electricityCabinetOrder) {
		return this.electricityCabinetOrderMapper.update(electricityCabinetOrder);

	}

	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param orderId 主键
	 * @return 实例对象
	 */
	@Override
	public ElectricityCabinetOrder queryByOrderId(String orderId) {
		return this.electricityCabinetOrderMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinetOrder>().eq(ElectricityCabinetOrder::getOrderId, orderId));
	}

	/*
	  1.判断参数
	  2.判断用户是否有电池是否有月卡
	  3.生成订单
	  4.开旧电池门
	  5.旧电池门开回调
	  6.旧电池门关回调
	  7.旧电池检测回调
	  8.检测失败重复开门
	  9.检测成功开新电池门
	  10.新电池开门回调
	  11.新电池关门回调
	  */
	@Override
	@Transactional
	public R order(OrderQuery orderQuery) {
		if (Objects.isNull(orderQuery.getElectricityCabinetId())) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}
		//操作频繁
		boolean result = redisService.setNx(ElectricityCabinetConstant.ORDER_UID + user.getUid(), "1", 15 * 1000L, false);
		if (!result) {
			return R.fail("ELECTRICITY.0034", "操作频繁");
		}

		//用户成功换电后才会添加缓存，用户换电周期限制
		ElectricityConfig electricityConfig = electricityConfigService.queryOne();
		String orderLimit = redisService.get(ElectricityCabinetConstant.ORDER_TIME_UID + user.getUid());
		if (StringUtils.isNotEmpty(orderLimit)) {
			return R.fail("ELECTRICITY.0061", "下单过于频繁 请" + electricityConfig.getOrderTime() + "分钟后重试");
		}

		//判断用户是否有未完成订单
		ElectricityCabinetOrder oldElectricityCabinetOrder = queryByUid(user.getUid());
		if (Objects.nonNull(oldElectricityCabinetOrder)) {
			log.error("ELECTRICITY  ERROR! find ele order! uid:{} ", user.getUid());
			return R.fail(oldElectricityCabinetOrder.getOrderId(),"ELECTRICITY.0094", "存在未完成换电订单，不能下单");
		}


		//是否存在未完成的还电池订单
		RentBatteryOrder oldRentBatteryOrder1 = rentBatteryOrderService.queryByUidAndType(user.getUid(),  RentBatteryOrder.TYPE_USER_RETURN);
		if (Objects.nonNull(oldRentBatteryOrder1)) {
			log.error("ELECTRICITY  ERROR! find return order! uid:{} ", user.getUid());
			return R.fail(oldRentBatteryOrder1.getOrderId(),"ELECTRICITY.0013", "存在未完成订单，不能下单");
		}


		//是否存在未完成的租电池订单
		RentBatteryOrder oldRentBatteryOrder2 = rentBatteryOrderService.queryByUidAndType(user.getUid(),  RentBatteryOrder.TYPE_USER_RENT);
		if (Objects.nonNull(oldRentBatteryOrder2)) {
			log.error("ELECTRICITY  ERROR! find rent order! uid:{} ", user.getUid());
			return R.fail(oldRentBatteryOrder2.getOrderId(),"ELECTRICITY.0013", "存在未完成订单，不能下单");
		}

		ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(orderQuery.getElectricityCabinetId());
		if (Objects.isNull(electricityCabinet)) {
			log.error("ELECTRICITY  ERROR! not found electricityCabinet ！electricityCabinetId{}", orderQuery.getElectricityCabinetId());
			return R.fail("ELECTRICITY.0005", "未找到换电柜");
		}

		//换电柜是否在线
		boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
		if (!eleResult) {
			log.error("ELECTRICITY  ERROR!  electricityCabinet is offline ！electricityCabinet{}", electricityCabinet);
			return R.fail("ELECTRICITY.0035", "换电柜不在线");
		}

		//换电柜是否出现异常被锁住
		String isLock = redisService.get(ElectricityCabinetConstant.UNLOCK_CABINET_CACHE + electricityCabinet.getId());
		if (StringUtils.isNotEmpty(isLock)) {
			log.error("ELECTRICITY  ERROR!  electricityCabinet is lock ！electricityCabinet{}", electricityCabinet);
			return R.fail("ELECTRICITY.0063", "换电柜出现异常，暂时不能下单");
		}

		Boolean isBusiness = this.isBusiness(electricityCabinet);
		if (isBusiness) {
			return R.fail("ELECTRICITY.0017", "换电柜已打烊");
		}

		if (Objects.isNull(orderQuery.getSource())) {
			orderQuery.setSource(OrderQuery.SOURCE_WX_MP);
		}

		//2.判断用户是否有电池是否有月卡
		UserInfo userInfo = userInfoService.queryByUid(user.getUid());
		if (Objects.isNull(userInfo)) {
			log.error("ELECTRICITY  ERROR! not found user,uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0019", "未找到用户");
		}
		//用户是否可用
		if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("ELECTRICITY  ERROR! user is unusable! uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}

		//未实名认证
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			log.error("ELECTRICITY  ERROR! not auth! uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}
		//未缴纳押金
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_AUTH)) {
			log.error("ELECTRICITY  ERROR! not pay deposit! uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0042", "未缴纳押金");
		}

		//判断用户是否开通月卡
		if (Objects.isNull(userInfo.getMemberCardExpireTime()) || Objects.isNull(userInfo.getRemainingNumber())) {
			log.error("ELECTRICITY  ERROR! not found memberCard uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0022", "未开通月卡");
		}
		Long now = System.currentTimeMillis();
		if (userInfo.getMemberCardExpireTime() < now || userInfo.getRemainingNumber() == 0) {
			log.error("ELECTRICITY  ERROR! memberCard is  Expire uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0023", "月卡已过期");
		}

		//未租电池
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_DEPOSIT)) {
			log.error("ELECTRICITY  ERROR! not rent battery! uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0033", "用户未绑定电池");
		}

		//用户状态异常
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_BATTERY) && Objects.isNull(userInfo.getNowElectricityBatterySn())) {
			log.error("ELECTRICITY  ERROR! userInfo is error!uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0052", "用户状态异常，请联系管理员");
		}

		//判断是否电池
		if (Objects.isNull(userInfo.getNowElectricityBatterySn())) {
			log.error("ELECTRICITY  ERROR! not found userInfo ");
			return R.fail("ELECTRICITY.0033", "用户未绑定电池");
		}

		//分配开门格挡
		String cellNo = findUsableCellNo(electricityCabinet.getId());
		try {
			if (Objects.isNull(cellNo)) {
				return R.fail("ELECTRICITY.0008", "换电柜暂无空仓");
			}
			if (userInfo.getRemainingNumber()!=-1) {
				//扣除月卡
				int row = userInfoService.minCount(userInfo.getId());
				if (row < 1) {
					log.error("ELECTRICITY  ERROR! memberCard is  Expire uid:{} ", user.getUid());
					return R.fail("ELECTRICITY.0023", "月卡已过期");
				}
			}

			//3.根据用户查询旧电池
			String oldElectricityBatterySn = userInfo.getNowElectricityBatterySn();
			ElectricityCabinetOrder electricityCabinetOrder = ElectricityCabinetOrder.builder()
					.orderId(generateOrderId(orderQuery.getElectricityCabinetId(), cellNo))
					.uid(user.getUid())
					.phone(userInfo.getPhone())
					.electricityCabinetId(orderQuery.getElectricityCabinetId())
					.oldElectricityBatterySn(oldElectricityBatterySn)
					.oldCellNo(Integer.valueOf(cellNo))
					.status(ElectricityCabinetOrder.STATUS_ORDER_PAY)
					.source(orderQuery.getSource())
					.paymentMethod(userInfo.getCardType())
					.createTime(System.currentTimeMillis())
					.updateTime(System.currentTimeMillis()).build();
			electricityCabinetOrderMapper.insert(electricityCabinetOrder);

			//4.开旧电池门
			//发送命令
			HashMap<String, Object> dataMap = Maps.newHashMap();
			dataMap.put("cell_no", cellNo);
			dataMap.put("order_id", electricityCabinetOrder.getOrderId());
			dataMap.put("serial_number", electricityCabinetOrder.getOldElectricityBatterySn());
			dataMap.put("status", electricityCabinetOrder.getStatus().toString());

			HardwareCommandQuery comm = HardwareCommandQuery.builder()
					.sessionId(ElectricityCabinetConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getId())
					.data(dataMap)
					.productKey(electricityCabinet.getProductKey())
					.deviceName(electricityCabinet.getDeviceName())
					.command(HardwareCommand.ELE_COMMAND_ORDER_OPEN_OLD_DOOR).build();
			eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
			return R.ok(electricityCabinetOrder.getOrderId());
		} catch (Exception e) {
			log.error("order is error" + e);
			return R.fail("ELECTRICITY.0025", "下单失败");
		} finally {
			redisService.delete(ElectricityCabinetConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + orderQuery.getElectricityCabinetId() + "_" + cellNo);
		}
	}

	@Override
	public R queryList(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
		Page page = PageUtil.getPage(electricityCabinetOrderQuery.getOffset(), electricityCabinetOrderQuery.getSize());

		electricityCabinetOrderMapper.queryList(page, electricityCabinetOrderQuery);
		if (ObjectUtil.isEmpty(page.getRecords())) {
			return R.ok(new ArrayList<>());
		}
		List<ElectricityCabinetOrderVO> electricityCabinetOrderVOList = page.getRecords();
		if (ObjectUtil.isNotEmpty(electricityCabinetOrderVOList)) {
			electricityCabinetOrderVOList.parallelStream().forEach(e -> {
				ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(e.getElectricityCabinetId());
				if (Objects.nonNull(electricityCabinet)) {
					e.setElectricityCabinetName(electricityCabinet.getName());
					e.setElectricityCabinetSn(electricityCabinet.getSn());
				}
			});
		}

		page.setRecords(electricityCabinetOrderVOList);
		return R.ok(page);
	}

	@Override
	@Transactional
	public R openDoor(OpenDoorQuery openDoorQuery) {
		if (Objects.isNull(openDoorQuery.getOrderId()) || Objects.isNull(openDoorQuery.getOpenType())) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}
		ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderMapper.selectOne(Wrappers.<ElectricityCabinetOrder>lambdaQuery().eq(ElectricityCabinetOrder::getOrderId, openDoorQuery.getOrderId()));
		if (Objects.isNull(electricityCabinetOrder)) {
			log.error("ELECTRICITY  ERROR! not found order,orderId{} ", openDoorQuery.getOrderId());
			return R.fail("ELECTRICITY.0015", "未找到订单");
		}

		//旧电池开门
		if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.OLD_OPEN_TYPE)) {
			if (!Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.STATUS_ORDER_PAY)) {
				return R.fail("ELECTRICITY.0015", "未找到订单");
			}
		}

		//新电池开门
		if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.NEW_OPEN_TYPE)) {
			if (!Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.STATUS_ORDER_WAIT_NEW_BATTERY)) {
				return R.fail("ELECTRICITY.0015", "未找到订单");
			}
		}

		//判断开门用户是否匹配
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}
		if (!Objects.equals(electricityCabinetOrder.getUid(), user.getUid())) {
			return R.fail("ELECTRICITY.0016", "订单用户不匹配，非法开门");
		}

		//查找换电柜
		ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetOrder.getElectricityCabinetId());
		if (Objects.isNull(electricityCabinet)) {
			log.error("ELECTRICITY  ERROR! not found electricityCabinet ！electricityCabinetId{}", electricityCabinetOrder.getElectricityCabinetId());
			return R.fail("ELECTRICITY.0005", "未找到换电柜");
		}

		//换电柜是否在线
		boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
		if (!eleResult) {
			log.error("ELECTRICITY  ERROR!  electricityCabinet is offline ！electricityCabinet{}", electricityCabinet);
			return R.fail("ELECTRICITY.0035", "换电柜不在线");
		}

		//旧电池开门
		if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.OLD_OPEN_TYPE)) {
			//发送命令
			HashMap<String, Object> dataMap = Maps.newHashMap();
			dataMap.put("cell_no", electricityCabinetOrder.getOldCellNo());
			dataMap.put("order_id", electricityCabinetOrder.getOrderId());
			dataMap.put("serial_number", electricityCabinetOrder.getOldElectricityBatterySn());
			dataMap.put("status", electricityCabinetOrder.getStatus().toString());

			HardwareCommandQuery comm = HardwareCommandQuery.builder()
					.sessionId(ElectricityCabinetConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getId())
					.data(dataMap)
					.productKey(electricityCabinet.getProductKey())
					.deviceName(electricityCabinet.getDeviceName())
					.command(HardwareCommand.ELE_COMMAND_ORDER_OPEN_OLD_DOOR).build();
			eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
		}

		//新电池开门
		if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.NEW_OPEN_TYPE)) {
			//发送命令
			HashMap<String, Object> dataMap = Maps.newHashMap();
			dataMap.put("cell_no", electricityCabinetOrder.getNewCellNo());
			dataMap.put("order_id", electricityCabinetOrder.getOrderId());
			dataMap.put("serial_number", electricityCabinetOrder.getNewElectricityBatterySn());
			dataMap.put("status", electricityCabinetOrder.getStatus().toString());
			dataMap.put("old_cell_no", electricityCabinetOrder.getOldCellNo());

			HardwareCommandQuery comm = HardwareCommandQuery.builder()
					.sessionId(ElectricityCabinetConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getId())
					.data(dataMap)
					.productKey(electricityCabinet.getProductKey())
					.deviceName(electricityCabinet.getDeviceName())
					.command(HardwareCommand.ELE_COMMAND_ORDER_OPEN_NEW_DOOR).build();
			eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
		}
		redisService.delete(ElectricityCabinetConstant.ELE_ORDER_OPERATOR_CACHE_KEY + electricityCabinetOrder.getOrderId());
		redisService.delete(ElectricityCabinetConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + electricityCabinetOrder.getOrderId());
		return R.ok();
	}

	@Override
	public Integer homeOneCount(Long first, Long now, List<Integer> eleIdList) {
		return electricityCabinetOrderMapper.homeOneCount(first, now, eleIdList);
	}

	@Override
	public BigDecimal homeOneSuccess(Long first, Long now, List<Integer> eleIdList) {
		Integer countTotal = homeOneCount(first, now, eleIdList);
		Integer successTotal = electricityCabinetOrderMapper.homeOneSuccess(first, now, eleIdList);
		if (successTotal == 0 || countTotal == 0) {
			return BigDecimal.valueOf(0);
		}
		return BigDecimal.valueOf(successTotal).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(countTotal), BigDecimal.ROUND_HALF_EVEN);
	}

	@Override
	public List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay, List<Integer> eleIdList) {
		return electricityCabinetOrderMapper.homeThree(startTimeMilliDay, endTimeMilliDay, eleIdList);
	}

	@Override
	public Integer homeMonth(Long uid, Long first, Long now) {
		return electricityCabinetOrderMapper.selectCount(new LambdaQueryWrapper<ElectricityCabinetOrder>().between(ElectricityCabinetOrder::getCreateTime, first, now).eq(ElectricityCabinetOrder::getUid, uid));
	}

	@Override
	public Integer homeTotal(Long uid) {
		return electricityCabinetOrderMapper.selectCount(new LambdaQueryWrapper<ElectricityCabinetOrder>().eq(ElectricityCabinetOrder::getUid, uid));
	}

	@Override
	public R queryCount(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
		Integer count = electricityCabinetOrderMapper.selectCount(new LambdaQueryWrapper<ElectricityCabinetOrder>().between(ElectricityCabinetOrder::getCreateTime, electricityCabinetOrderQuery.getBeginTime(), electricityCabinetOrderQuery.getEndTime())
				.eq(ElectricityCabinetOrder::getUid, electricityCabinetOrderQuery.getUid()));
		return R.ok(count);
	}

	@Override
	public void handlerExpiredCancelOrder(String orderId) {
		log.info("handel  cancel order start ------->");
		ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderMapper.selectOne(Wrappers.<ElectricityCabinetOrder>lambdaQuery().eq(ElectricityCabinetOrder::getOrderId, orderId)
				.in(ElectricityCabinetOrder::getStatus, ElectricityCabinetOrder.STATUS_ORDER_PAY, ElectricityCabinetOrder.STATUS_ORDER_OLD_BATTERY_OPEN_DOOR));
		Integer row = electricityCabinetOrderMapper.updateExpiredCancelOrder(orderId, System.currentTimeMillis());

		if (row > 0) {
			//修改仓门为无电池
			ElectricityCabinetBox electricityCabinetNewBox = new ElectricityCabinetBox();
			electricityCabinetNewBox.setCellNo(String.valueOf(electricityCabinetOrder.getOldCellNo()));
			electricityCabinetNewBox.setElectricityCabinetId(electricityCabinetOrder.getElectricityCabinetId());
			electricityCabinetNewBox.setStatus(ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);
			electricityCabinetNewBox.setElectricityBatteryId(-1L);
			electricityCabinetBoxService.modifyByCellNo(electricityCabinetNewBox);
			//回退月卡
			UserInfo userInfo = userInfoService.queryByUid(electricityCabinetOrder.getUid());
			Long now = System.currentTimeMillis();
			if (Objects.nonNull(userInfo) && Objects.nonNull(userInfo.getMemberCardExpireTime()) && Objects.nonNull(userInfo.getRemainingNumber())
					&& userInfo.getMemberCardExpireTime() > now && userInfo.getRemainingNumber() != -1) {
				//回退月卡次数
				userInfoService.plusCount(userInfo.getId());
			}
		}
		log.info("handel  cancel order end ,orderId:{}  <-------", orderId);
	}

	@Override
	public R queryStatus(String orderId) {
		Map<String, String> map = new HashMap<>();
		ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderMapper.selectOne(Wrappers.<ElectricityCabinetOrder>lambdaQuery().eq(ElectricityCabinetOrder::getOrderId, orderId));
		if (Objects.isNull(electricityCabinetOrder)) {
			log.error("ELECTRICITY  ERROR! not found order,orderId{} ", orderId);
			return R.fail("ELECTRICITY.0015", "未找到订单");
		}
		Integer queryStatus = 0;
		String s = redisService.get(ElectricityCabinetConstant.ELE_ORDER_OPERATOR_CACHE_KEY + orderId);
		if (StrUtil.isNotEmpty(s)) {
			queryStatus = 1;
		}
		Long now = (System.currentTimeMillis() - electricityCabinetOrder.getCreateTime()) / 1000;
		Long time = 300 - now;
		map.put("time", time.toString());
		map.put("status", electricityCabinetOrder.getStatus().toString());
		map.put("queryStatus", queryStatus.toString());
		return R.ok(map);
	}

	@Override
	@Transactional
	public R endOrder(String orderId) {
		//结束异常订单只改订单状态，不用考虑其他
		ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderMapper.selectOne(Wrappers.<ElectricityCabinetOrder>lambdaQuery().eq(ElectricityCabinetOrder::getOrderId, orderId)
				.notIn(ElectricityCabinetOrder::getStatus, ElectricityCabinetOrder.STATUS_ORDER_COMPLETE, ElectricityCabinetOrder.STATUS_ORDER_EXCEPTION_CANCEL, ElectricityCabinetOrder.STATUS_ORDER_CANCEL));
		if (Objects.isNull(electricityCabinetOrder)) {
			log.error("ELECTRICITY  ERROR! not found order,orderId{} ", orderId);
			return R.fail("ELECTRICITY.0015", "未找到订单");
		}
		ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
		newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
		newElectricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_EXCEPTION_CANCEL);
		newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
		electricityCabinetOrderMapper.update(newElectricityCabinetOrder);
		//回退月卡
		UserInfo userInfo = userInfoService.queryByUid(electricityCabinetOrder.getUid());
		Long now = System.currentTimeMillis();
		if (Objects.nonNull(userInfo) && Objects.nonNull(userInfo.getMemberCardExpireTime()) && Objects.nonNull(userInfo.getRemainingNumber())
				&& userInfo.getMemberCardExpireTime() > now && userInfo.getRemainingNumber() != -1) {
			//回退月卡次数
			userInfoService.plusCount(userInfo.getId());
		}

		//删除开门失败缓存
		redisService.delete(ElectricityCabinetConstant.ELE_ORDER_OPERATOR_CACHE_KEY + orderId);
		redisService.delete(ElectricityCabinetConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + orderId);
		return R.ok();
	}

	@Override
	public void exportExcel(ElectricityCabinetOrderQuery electricityCabinetOrderQuery, HttpServletResponse response) {
		Page page = PageUtil.getPage(0L, 2000L);
		electricityCabinetOrderMapper.queryList(page, electricityCabinetOrderQuery);
		if (ObjectUtil.isEmpty(page.getRecords())) {
			throw new CustomBusinessException("查不到订单");
		}

		List<ElectricityCabinetOrderVO> electricityCabinetOrderVOList = page.getRecords();
		if (!DataUtil.collectionIsUsable(electricityCabinetOrderVOList)) {
			throw new CustomBusinessException("查不到订单");
		}

		List<ElectricityCabinetOrderExcelVO> electricityCabinetOrderExcelVOS = new ArrayList();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int index = 0;
		for (ElectricityCabinetOrderVO electricityCabinetOrderVO : electricityCabinetOrderVOList) {
			index++;
			ElectricityCabinetOrderExcelVO excelVo = new ElectricityCabinetOrderExcelVO();
			excelVo.setId(index);
			excelVo.setOrderId(electricityCabinetOrderVO.getOrderId());
			excelVo.setPhone(electricityCabinetOrderVO.getPhone());
			excelVo.setOldElectricityBatterySn(electricityCabinetOrderVO.getOldElectricityBatterySn());
			excelVo.setNewElectricityBatterySn(electricityCabinetOrderVO.getNewElectricityBatterySn());

			if (Objects.nonNull(electricityCabinetOrderVO.getCreateTime())) {
				excelVo.setCreateTime(simpleDateFormat.format(new Date(electricityCabinetOrderVO.getCreateTime())));
			}
			if (Objects.nonNull(electricityCabinetOrderVO.getUpdateTime())) {
				excelVo.setUpdateTime(simpleDateFormat.format(new Date(electricityCabinetOrderVO.getUpdateTime())));
			}

			if (Objects.isNull(electricityCabinetOrderVO.getPaymentMethod())) {
				excelVo.setPaymentMethod("");
			}
			if (Objects.equals(electricityCabinetOrderVO.getPaymentMethod(), ElectricityCabinetOrder.PAYMENT_METHOD_MONTH_CARD)) {
				excelVo.setPaymentMethod("月卡");
			}
			if (Objects.equals(electricityCabinetOrderVO.getPaymentMethod(), ElectricityCabinetOrder.PAYMENT_METHOD_SEASON_CARD)) {
				excelVo.setPaymentMethod("季卡");
			}
			if (Objects.equals(electricityCabinetOrderVO.getPaymentMethod(), ElectricityCabinetOrder.PAYMENT_METHOD_YEAR_CARD)) {
				excelVo.setPaymentMethod("年卡");
			}

			if (Objects.isNull(electricityCabinetOrderVO.getStatus())) {
				excelVo.setStatus("");
			}
			if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.STATUS_ORDER_PAY)) {
				excelVo.setStatus("已支付未开门");
			}
			if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.STATUS_ORDER_OLD_BATTERY_OPEN_DOOR)) {
				excelVo.setStatus("旧电池开门");
			}
			if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.STATUS_ORDER_OLD_BATTERY_DEPOSITED)) {
				excelVo.setStatus("旧电池已存入");
			}
			if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.STATUS_ORDER_WAIT_NEW_BATTERY)) {
				excelVo.setStatus("等待新电池");
			}
			if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.STATUS_ORDER_NEW_BATTERY_OPEN_DOOR)) {
				excelVo.setStatus("新电池开门");
			}
			if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.STATUS_ORDER_COMPLETE)) {
				excelVo.setStatus("订单完成");
			}
			if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.STATUS_ORDER_EXCEPTION_CANCEL)) {
				excelVo.setStatus("订单异常结束");
			}
			if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.STATUS_ORDER_CANCEL)) {
				excelVo.setStatus("订单取消");
			}
			electricityCabinetOrderExcelVOS.add(excelVo);
		}

		String fileName = "换电订单报表.xlsx";
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			// 告诉浏览器用什么软件可以打开此文件
			response.setHeader("content-Type", "application/vnd.ms-excel");
			// 下载文件的默认名称
			response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
			EasyExcel.write(outputStream, ElectricityCabinetOrderExcelVO.class).sheet("sheet").doWrite(electricityCabinetOrderExcelVOS);
			return;
		} catch (IOException e) {
			log.error("导出报表失败！", e);
		}
	}

	@Override
	public void insert(ElectricityCabinetOrder electricityCabinetOrder) {
		electricityCabinetOrderMapper.insert(electricityCabinetOrder);
	}

	@Override
	public ElectricityCabinetOrder queryByUid(Long uid) {
		return electricityCabinetOrderMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinetOrder>().eq(ElectricityCabinetOrder::getUid, uid)
				.notIn(ElectricityCabinetOrder::getStatus, ElectricityCabinetOrder.STATUS_ORDER_COMPLETE, ElectricityCabinetOrder.STATUS_ORDER_EXCEPTION_CANCEL, ElectricityCabinetOrder.STATUS_ORDER_CANCEL)
				.orderByDesc(ElectricityCabinetOrder::getCreateTime).last("limit 0,1"));
	}

	@Override
	public ElectricityCabinetOrder queryByCellNoAndEleId(Integer eleId, Integer cellNo) {
		return electricityCabinetOrderMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinetOrder>()
				.eq(ElectricityCabinetOrder::getElectricityCabinetId, eleId)
				.eq(ElectricityCabinetOrder::getOldCellNo, cellNo).or().eq(ElectricityCabinetOrder::getNewCellNo, cellNo)
				.orderByDesc(ElectricityCabinetOrder::getCreateTime).last("limit 0,1"));
	}

	@Override
	public String findUsableCellNo(Integer id) {
		List<ElectricityCabinetBox> usableBoxes = electricityCabinetBoxService.queryNoElectricityBatteryBox(id);
		if (!DataUtil.collectionIsUsable(usableBoxes)) {
			return null;
		}

		List<Integer> boxes = usableBoxes.stream().map(ElectricityCabinetBox::getCellNo).map(Integer::parseInt).sorted(Integer::compareTo).collect(Collectors.toList());

		//查看有没有初始化过设备的上次操作过的格挡,这里不必关心线程安全，不需要保证原子性
		if (!redisService.hasKey(ElectricityCabinetConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id)) {
			redisService.setNx(ElectricityCabinetConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id, boxes.get(0).toString());
		}

		String lastCellNo = redisService.get(ElectricityCabinetConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id);

		boxes = rebuildByCellCircleForDevice(boxes, Integer.parseInt(lastCellNo));

		for (Integer box : boxes) {
			if (redisService.setNx(ElectricityCabinetConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + id + "_" + box.toString(), "1", 300 * 1000L, false)) {
				redisService.set(ElectricityCabinetConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id, box.toString());
				return box.toString();
			}
		}

		return null;
	}

	@Override
	public R queryNewStatus(String orderId) {

		Map<String, String> map = new HashMap<>();
		ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderMapper.selectOne(Wrappers.<ElectricityCabinetOrder>lambdaQuery().eq(ElectricityCabinetOrder::getOrderId, orderId));
		if (Objects.isNull(electricityCabinetOrder)) {
			log.error("ELECTRICITY  ERROR! not found order,orderId{} ", orderId);
			return R.fail("ELECTRICITY.0015", "未找到订单");
		}

		Integer type = 0;
		Integer isTry = 1;
		map.put("status", electricityCabinetOrder.getStatus().toString());

		String result = redisService.get(ElectricityCabinetConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + orderId);
		if (StringUtils.isNotEmpty(result)) {
			WarnMsgVo warnMsgVo = JsonUtil.fromJson(result, WarnMsgVo.class);
			String queryStatus = warnMsgVo.getCode().toString();

			//是否重试
			if (Objects.equals(queryStatus, ElectricityCabinetOrderOperHistory.STATUS_DOOR_IS_OPEN_EXCEPTION.toString())
					|| Objects.equals(queryStatus, ElectricityCabinetOrderOperHistory.STATUS_LOCKER_LOCK.toString())
					|| Objects.equals(queryStatus, ElectricityCabinetOrderOperHistory.STATUS_BUSINESS_PROCESS.toString())
					|| Objects.equals(queryStatus, ElectricityCabinetOrderOperHistory.STATUS_OPEN_DOOR_FAIL.toString())) {
				isTry = 0;
			}

			//提示放入电池不对，应该放入什么电池
			if (Objects.equals(queryStatus, ElectricityCabinetOrderOperHistory.BATTERY_NOT_MATCH_CLOUD.toString())) {
				queryStatus = "放入电池不对，应该放入编号为" + electricityCabinetOrder.getOldElectricityBatterySn() + "的电池";
			}else {
				queryStatus = warnMsgVo.getMsg();
			}

			map.put("queryStatus", queryStatus);
			type = 1;

		}

		map.put("type", type.toString());
		map.put("isTry", isTry.toString());
		return R.ok(map);
	}

	public static List<Integer> rebuildByCellCircleForDevice(List<Integer> cellNos, Integer lastCellNo) {

		if (cellNos.get(0) > lastCellNo) {
			return cellNos;
		}

		int index = 0;

		for (int i = 0; i < cellNos.size(); i++) {
			if (cellNos.get(i) > lastCellNo) {
				index = i;
				break;
			}

			if (cellNos.get(i).equals(lastCellNo)) {
				index = i + 1;
				break;
			}
		}

		List<Integer> firstSegmentList = cellNos.subList(0, index);
		List<Integer> twoSegmentList = cellNos.subList(index, cellNos.size());

		ArrayList<Integer> resultList = com.google.common.collect.Lists.newArrayList();
		resultList.addAll(twoSegmentList);
		resultList.addAll(firstSegmentList);

		return resultList;
	}

	public String generateOrderId(Integer id, String cellNo) {
		return String.valueOf(System.currentTimeMillis()).substring(2) + id +
				cellNo +
				RandomUtil.randomNumbers(6);
	}

	public Long getTime(Long time) {
		Date date1 = new Date(time);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String format = dateFormat.format(date1);
		Date date2 = null;
		try {
			date2 = dateFormat.parse(format);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Long ts = date2.getTime();
		return time - ts;
	}

	public boolean isBusiness(ElectricityCabinet electricityCabinet) {
		//营业时间
		if (Objects.nonNull(electricityCabinet.getBusinessTime())) {
			String businessTime = electricityCabinet.getBusinessTime();
			if (!Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
				int index = businessTime.indexOf("-");
				if (!Objects.equals(index, -1) && index > 0) {
					Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
					long now = System.currentTimeMillis();
					Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
					Long beginTime = getTime(totalBeginTime);
					Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
					Long endTime = getTime(totalEndTime);
					if (firstToday + beginTime > now || firstToday + endTime < now) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
