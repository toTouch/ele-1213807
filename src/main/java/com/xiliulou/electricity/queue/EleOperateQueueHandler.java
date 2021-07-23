package com.xiliulou.electricity.queue;

import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.dto.EleOpenDTO;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.HardwareCommand;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.handler.EleHardwareHandlerManager;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.vo.WarnMsgVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.service.PubHardwareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author: lxc
 * @Date: 2020/12/3 08:31
 * @Description:
 */

@Service
@Slf4j
public class EleOperateQueueHandler {

	ExecutorService executorService = XllExecutors.newFixedThreadPool(20);
	ExecutorService startService = XllExecutors.newFixedThreadPool(1);
	private volatile boolean shutdown = false;
	private final LinkedBlockingQueue<EleOpenDTO> queue = new LinkedBlockingQueue<>();

	@Autowired
	ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;
	@Autowired
	ElectricityCabinetOrderService electricityCabinetOrderService;
	@Autowired
	ElectricityCabinetBoxService electricityCabinetBoxService;
	@Autowired
	RedisService redisService;
	@Autowired
	ElectricityBatteryService electricityBatteryService;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	ElectricityCabinetService electricityCabinetService;
	@Autowired
	EleHardwareHandlerManager eleHardwareHandlerManager;
	@Autowired
	RentBatteryOrderService rentBatteryOrderService;
	@Autowired
	ElectricityConfigService electricityConfigService;
	@Autowired
	FranchiseeUserInfoService franchiseeUserInfoService;

	@EventListener({WebServerInitializedEvent.class})
	public void startHandleElectricityCabinetOperate() {
		initElectricityCabinetOperate();
	}

	private void initElectricityCabinetOperate() {
		log.info("初始化换电柜操作响应处理器");
		startService.execute(() -> {
			while (!shutdown) {
				EleOpenDTO eleOpenDTO = null;
				try {
					eleOpenDTO = queue.take();
					log.info(" QUEUE get a message ={}", eleOpenDTO);

					EleOpenDTO finalOpenDTO = eleOpenDTO;
					executorService.execute(() -> {
						handleOrderAfterOperated(finalOpenDTO);
					});

				} catch (Exception e) {
					log.error("ELECTRICITY CABINET OPERATE QUEUE ERROR! ", e);
				}

			}
		});
	}

	/**
	 * 接收到响应的操作信息
	 *
	 * @param finalOpenDTO
	 */
	private void handleOrderAfterOperated(EleOpenDTO finalOpenDTO) {
		//参数
		String type = finalOpenDTO.getType();
		String orderId = finalOpenDTO.getOrderId();
		Double orderSeq = finalOpenDTO.getOrderSeq();

		//查找订单
		if (Objects.equals(type, HardwareCommand.ELE_COMMAND_INIT_EXCHANGE_ORDER_RSP)
				|| Objects.equals(type, HardwareCommand.ELE_COMMAND_COMPLETE_EXCHANGE_ORDER_RSP)) {

			ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderService.queryByOrderId(orderId);
			if (Objects.isNull(electricityCabinetOrder)) {
				return;
			}

			//若app订单状态大于云端订单状态则处理
			if (Objects.isNull(orderSeq) || orderSeq - electricityCabinetOrder.getOrderSeq() >= 1 || Math.abs(orderSeq - electricityCabinetOrder.getOrderSeq()) < 1) {
				if (Objects.equals(type, HardwareCommand.ELE_COMMAND_INIT_EXCHANGE_ORDER_RSP)) {
					handelInitExchangeOrder(electricityCabinetOrder, finalOpenDTO);
				}

				if (Objects.equals(type, HardwareCommand.ELE_COMMAND_COMPLETE_EXCHANGE_ORDER_RSP)) {
					handelCompleteExchangeOrder(electricityCabinetOrder, finalOpenDTO);
				}
			}
		} else {

			//租还电池订单
			RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByOrderId(orderId);
			if (Objects.isNull(rentBatteryOrder)) {
				return;
			}

			handleRentOrder(rentBatteryOrder, finalOpenDTO);

		}
	}

	public void shutdown() {
		shutdown = true;
		executorService.shutdown();
	}

	public void putQueue(EleOpenDTO eleOpenDTO) {
		try {
			queue.put(eleOpenDTO);
		} catch (InterruptedException e) {
			log.error("ELECTRICITY CABINET OPERATE QUEUE ERROR!", e);
		}
	}

	//开旧门通知
	public void handelInitExchangeOrder(ElectricityCabinetOrder electricityCabinetOrder, EleOpenDTO finalOpenDTO) {

		//开门失败
		if (finalOpenDTO.getIsProcessFail()) {
			//取消订单
			if (finalOpenDTO.getIsNeedEndOrder()) {
				ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
				newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
				newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
				newElectricityCabinetOrder.setStatus(ElectricityCabinetOrder.ORDER_CANCEL);
				newElectricityCabinetOrder.setOrderSeq(ElectricityCabinetOrder.STATUS_ORDER_CANCEL);
				electricityCabinetOrderService.update(newElectricityCabinetOrder);

				//清除柜机锁定缓存
				redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + electricityCabinetOrder.getElectricityCabinetId());
				return;
			}

			//修改订单
			ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
			newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
			newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
			newElectricityCabinetOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
			newElectricityCabinetOrder.setStatus(finalOpenDTO.getOrderStatus());
			electricityCabinetOrderService.update(newElectricityCabinetOrder);
			return;
		}

		//修改订单状态
		ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
		newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
		newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
		newElectricityCabinetOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
		newElectricityCabinetOrder.setStatus(finalOpenDTO.getOrderStatus());
		electricityCabinetOrderService.update(newElectricityCabinetOrder);

		//订单状态为旧电池检测成功则分配新仓门
		if (Objects.equals(newElectricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)) {
			String cellNo = null;
			try {//查找用户
				UserInfo userInfo = userInfoService.queryByUid(electricityCabinetOrder.getUid());
				if (Objects.isNull(userInfo)) {
					return;
				}

				//用户解绑旧电池 旧电池到底是哪块，不确定
				FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
				franchiseeUserInfo.setUserInfoId(userInfo.getId());
				franchiseeUserInfo.setNowElectricityBatterySn(null);
				franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
				franchiseeUserInfoService.updateByUserInfoId(franchiseeUserInfo);

				//分配新仓门
				cellNo = rentBatteryOrderService.findUsableBatteryCellNo(electricityCabinetOrder.getElectricityCabinetId(), electricityCabinetOrder.getOldCellNo().toString());
				if (Objects.isNull(cellNo)) {
					log.error("check Old Battery not find fully battery!orderId:{}", electricityCabinetOrder.getOrderId());
					return;
				}

				//根据换电柜id和仓门查出电池
				ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(electricityCabinetOrder.getElectricityCabinetId(), cellNo);
				if (Objects.isNull(electricityCabinetBox)) {
					log.error("check Old Battery not find electricityCabinetBox! electricityCabinetId:{},cellNo:{}", electricityCabinetOrder.getElectricityCabinetId(), cellNo);
					return;
				}
				ElectricityBattery newElectricityBattery = electricityBatteryService.queryBySn(electricityCabinetBox.getSn());
				if (Objects.isNull(newElectricityBattery)) {
					log.error("check Old Battery not find electricityBattery! sn:{}", electricityCabinetBox.getSn());
					return;
				}

				//修改订单状态
				ElectricityCabinetOrder innerElectricityCabinetOrder = new ElectricityCabinetOrder();
				innerElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
				innerElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
				innerElectricityCabinetOrder.setNewElectricityBatterySn(newElectricityBattery.getSn());
				innerElectricityCabinetOrder.setNewCellNo(Integer.valueOf(cellNo));
				electricityCabinetOrderService.update(innerElectricityCabinetOrder);

				//新电池开门
				ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetOrder.getElectricityCabinetId());
				//发送命令
				HashMap<String, Object> dataMap = Maps.newHashMap();
				dataMap.put("cell_no", cellNo);
				dataMap.put("order_id", electricityCabinetOrder.getOrderId());
				dataMap.put("serial_number", newElectricityCabinetOrder.getNewElectricityBatterySn());
				dataMap.put("status", electricityCabinetOrder.getStatus().toString());
				dataMap.put("old_cell_no", electricityCabinetOrder.getOldCellNo());

				HardwareCommandQuery comm = HardwareCommandQuery.builder()
						.sessionId(ElectricityCabinetConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getUid() + "_" + electricityCabinetOrder.getOrderId())
						.data(dataMap)
						.productKey(electricityCabinet.getProductKey())
						.deviceName(electricityCabinet.getDeviceName())
						.command(HardwareCommand.ELE_COMMAND_ORDER_OPEN_NEW_DOOR).build();
				eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
			} catch (Exception e) {
				log.error("e", e);
			} finally {
				redisService.delete(ElectricityCabinetConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + electricityCabinetOrder.getElectricityCabinetId() + "_" + cellNo);
			}

		}
	}

	//开新门通知
	public void handelCompleteExchangeOrder(ElectricityCabinetOrder electricityCabinetOrder, EleOpenDTO finalOpenDTO) {
		//开门失败
		if (finalOpenDTO.getIsProcessFail()) {
			//取消订单
			if (finalOpenDTO.getIsNeedEndOrder()) {
				ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
				newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
				newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
				newElectricityCabinetOrder.setStatus(ElectricityCabinetOrder.ORDER_CANCEL);
				newElectricityCabinetOrder.setOrderSeq(ElectricityCabinetOrder.STATUS_ORDER_CANCEL);
				electricityCabinetOrderService.update(newElectricityCabinetOrder);

				//清除柜机锁定缓存
				redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + electricityCabinetOrder.getElectricityCabinetId());
				return;
			}

			//修改订单
			ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
			newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
			newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
			newElectricityCabinetOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
			newElectricityCabinetOrder.setStatus(finalOpenDTO.getOrderStatus());
			electricityCabinetOrderService.update(newElectricityCabinetOrder);
			return;
		}

		//修改订单
		ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
		newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
		newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
		newElectricityCabinetOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
		newElectricityCabinetOrder.setStatus(finalOpenDTO.getOrderStatus());
		electricityCabinetOrderService.update(newElectricityCabinetOrder);

		if (Objects.equals(newElectricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {

			UserInfo userInfo = userInfoService.queryByUid(electricityCabinetOrder.getUid());
			if (Objects.isNull(userInfo)) {
				return;
			}

			//修改仓门为无电池
			ElectricityCabinetBox newElectricityCabinetBox = new ElectricityCabinetBox();
			newElectricityCabinetBox.setCellNo(String.valueOf(electricityCabinetOrder.getNewCellNo()));
			newElectricityCabinetBox.setElectricityCabinetId(electricityCabinetOrder.getElectricityCabinetId());
			newElectricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
			newElectricityCabinetBox.setSn(null);
			newElectricityCabinetBox.setUpdateTime(System.currentTimeMillis());
			electricityCabinetBoxService.modifyByCellNo(newElectricityCabinetBox);

			//用户绑新电池
			FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
			franchiseeUserInfo.setUserInfoId(userInfo.getId());
			franchiseeUserInfo.setNowElectricityBatterySn(electricityCabinetOrder.getNewElectricityBatterySn());
			franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
			franchiseeUserInfoService.updateByUserInfoId(franchiseeUserInfo);

			//电池改为在用
			ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(electricityCabinetOrder.getNewElectricityBatterySn());
			ElectricityBattery newElectricityBattery = new ElectricityBattery();
			newElectricityBattery.setId(electricityBattery.getId());
			newElectricityBattery.setStatus(ElectricityBattery.LEASE_STATUS);
			newElectricityBattery.setElectricityCabinetId(null);
			newElectricityBattery.setUid(electricityCabinetOrder.getUid());
			newElectricityBattery.setUpdateTime(System.currentTimeMillis());
			electricityBatteryService.updateByOrder(newElectricityBattery);

			//删除柜机被锁缓存
			redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + electricityCabinetOrder.getElectricityCabinetId());
		}
	}

	//开租/还 电池门
	public void handleRentOrder(RentBatteryOrder rentBatteryOrder, EleOpenDTO finalOpenDTO) {

		//开门失败
		if (finalOpenDTO.getIsProcessFail()) {
			//取消订单
			if (finalOpenDTO.getIsNeedEndOrder()) {
				RentBatteryOrder newRentBatteryOrder = new RentBatteryOrder();
				newRentBatteryOrder.setId(rentBatteryOrder.getId());
				newRentBatteryOrder.setUpdateTime(System.currentTimeMillis());
				newRentBatteryOrder.setOrderSeq(RentBatteryOrder.STATUS_ORDER_CANCEL);
				newRentBatteryOrder.setStatus(RentBatteryOrder.ORDER_CANCEL);
				rentBatteryOrderService.update(newRentBatteryOrder);

				//清除柜机锁定缓存
				redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + rentBatteryOrder.getElectricityCabinetId());
				return;
			}

			//订单状态
			RentBatteryOrder newRentBatteryOrder = new RentBatteryOrder();
			newRentBatteryOrder.setId(rentBatteryOrder.getId());
			newRentBatteryOrder.setUpdateTime(System.currentTimeMillis());
			newRentBatteryOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
			newRentBatteryOrder.setStatus(finalOpenDTO.getOrderStatus());
			rentBatteryOrderService.update(newRentBatteryOrder);
			return;
		}

		//订单状态
		RentBatteryOrder newRentBatteryOrder = new RentBatteryOrder();
		newRentBatteryOrder.setId(rentBatteryOrder.getId());
		newRentBatteryOrder.setUpdateTime(System.currentTimeMillis());
		newRentBatteryOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
		newRentBatteryOrder.setStatus(finalOpenDTO.getOrderStatus());
		rentBatteryOrderService.update(newRentBatteryOrder);

		if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)
				&& Objects.equals(newRentBatteryOrder.getStatus(), RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS)) {
			checkRentBatteryDoor(rentBatteryOrder);
		}

		if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)
				&& Objects.equals(newRentBatteryOrder.getStatus(), RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS)) {
			checkReturnBatteryDoor(rentBatteryOrder);
		}

	}

	//检测租电池
	public void checkRentBatteryDoor(RentBatteryOrder rentBatteryOrder) {

		//查找用户
		UserInfo userInfo = userInfoService.queryByUid(rentBatteryOrder.getUid());
		if (Objects.isNull(userInfo)) {
			return;
		}

		//修改仓门为无电池
		ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
		electricityCabinetBox.setCellNo(String.valueOf(rentBatteryOrder.getCellNo()));
		electricityCabinetBox.setElectricityCabinetId(rentBatteryOrder.getElectricityCabinetId());
		electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
		electricityCabinetBox.setSn(null);
		electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
		electricityCabinetBoxService.modifyByCellNo(electricityCabinetBox);

		//用户绑新电池
		FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
		franchiseeUserInfo.setUserInfoId(userInfo.getId());
		franchiseeUserInfo.setInitElectricityBatterySn(rentBatteryOrder.getElectricityBatterySn());
		franchiseeUserInfo.setNowElectricityBatterySn(rentBatteryOrder.getElectricityBatterySn());
		franchiseeUserInfo.setServiceStatus(FranchiseeUserInfo.STATUS_IS_BATTERY);
		franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
		franchiseeUserInfoService.updateByUserInfoId(franchiseeUserInfo);

		//电池改为在用
		ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(rentBatteryOrder.getElectricityBatterySn());
		ElectricityBattery newElectricityBattery = new ElectricityBattery();
		newElectricityBattery.setId(electricityBattery.getId());
		newElectricityBattery.setStatus(ElectricityBattery.LEASE_STATUS);
		newElectricityBattery.setElectricityCabinetId(null);
		newElectricityBattery.setUid(rentBatteryOrder.getUid());
		newElectricityBattery.setUpdateTime(System.currentTimeMillis());
		electricityBatteryService.updateByOrder(newElectricityBattery);

		//删除柜机被锁缓存
		redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + rentBatteryOrder.getElectricityCabinetId());
	}

	//检测还电池
	public void checkReturnBatteryDoor(RentBatteryOrder rentBatteryOrder) {

		//查找用户
		UserInfo userInfo = userInfoService.queryByUid(rentBatteryOrder.getUid());
		if (Objects.isNull(userInfo)) {
			return;
		}

		//用户解绑电池
		FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
		franchiseeUserInfo.setUserInfoId(userInfo.getId());
		franchiseeUserInfo.setNowElectricityBatterySn(null);
		franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
		franchiseeUserInfo.setServiceStatus(FranchiseeUserInfo.STATUS_IS_DEPOSIT);
		franchiseeUserInfoService.updateByUserInfoId(franchiseeUserInfo);

		//删除柜机被锁缓存
		redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + rentBatteryOrder.getElectricityCabinetId());
	}

}
