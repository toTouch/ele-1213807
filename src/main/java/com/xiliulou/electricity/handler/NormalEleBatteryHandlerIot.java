package com.xiliulou.electricity.handler;

import com.alibaba.fastjson.JSON;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.BatteryOtherProperties;
import com.xiliulou.electricity.entity.BatteryOtherPropertiesQuery;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.FranchiseeBindElectricityBattery;
import com.xiliulou.electricity.entity.NotExistSn;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.service.BatteryOtherPropertiesService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FranchiseeBindElectricityBatteryService;
import com.xiliulou.electricity.service.NotExistSnService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.vo.BigEleBatteryVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.AbstractIotMessageHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service
@Slf4j
public class NormalEleBatteryHandlerIot extends AbstractIotMessageHandler {
	@Autowired
	ElectricityCabinetService electricityCabinetService;
	@Autowired
	ElectricityBatteryService electricityBatteryService;
	@Autowired
	ElectricityCabinetBoxService electricityCabinetBoxService;
	@Autowired
	RedisService redisService;

	@Autowired
	FranchiseeBindElectricityBatteryService franchiseeBindElectricityBatteryService;

	@Autowired
	StoreService storeService;

	@Autowired
	BatteryOtherPropertiesService batteryOtherPropertiesService;

	@Autowired
	NotExistSnService notExistSnService;

	public static final String TERNARY_LITHIUM = "TERNARY_LITHIUM";
	public static final String IRON_LITHIUM = "IRON_LITHIUM";

	@Override
	protected Pair<SendHardwareMessage, String> generateMsg(HardwareCommandQuery hardwareCommandQuery) {
		String sessionId = generateSessionId(hardwareCommandQuery);
		SendHardwareMessage message = SendHardwareMessage.builder()
				.sessionId(sessionId)
				.type(hardwareCommandQuery.getCommand())
				.data(hardwareCommandQuery.getData()).build();
		return Pair.of(message, sessionId);
	}

	@Override
	protected boolean receiveMessageProcess(ReceiverMessage receiverMessage) {

		ElectricityCabinet electricityCabinet = electricityCabinetService.queryByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
		if (Objects.isNull(electricityCabinet)) {
			log.error("ELE ERROR! no product and device ,p={},d={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
			return false;
		}

		EleBatteryVo eleBatteryVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleBatteryVo.class);
		if (Objects.isNull(eleBatteryVo)) {
			log.error("ele battery error! no eleCellVo,{}", receiverMessage.getOriginContent());
			return false;
		}

		String cellNo = eleBatteryVo.getCellNo();
		if (StringUtils.isEmpty(cellNo)) {
			log.error("ele cell error! no eleCellVo,{}", receiverMessage.getOriginContent());
			return false;
		}

		ElectricityCabinetBox oldElectricityCabinetBox = electricityCabinetBoxService.queryByCellNo(electricityCabinet.getId(), cellNo);
		if (Objects.isNull(oldElectricityCabinetBox)) {
			log.error("ELE ERROR! no cellNo! p={},d={},cell={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName(), cellNo);
			return false;
		}

		ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
		ElectricityBattery newElectricityBattery = new ElectricityBattery();

		electricityCabinetBox.setBatteryType(null);

		//若上报时间小于上次上报时间则忽略此条上报
		Long reportTime = eleBatteryVo.getReportTime();
		if (Objects.nonNull(reportTime) && Objects.nonNull(oldElectricityCabinetBox.getReportTime())
				&& oldElectricityCabinetBox.getReportTime() >= reportTime) {
			log.error("ele battery error! reportTime is less ,reportTime:{}", reportTime);
			return false;
		}

		if (Objects.nonNull(reportTime)) {
			electricityCabinetBox.setReportTime(reportTime);
		}

		String batteryName = eleBatteryVo.getBatteryName();
		Boolean existsBattery = eleBatteryVo.getExistsBattery();

		//存在电池但是电池名字没有上报
		if (Objects.nonNull(existsBattery) && StringUtils.isEmpty(batteryName) && existsBattery) {
			log.error("ELE ERROR! battery report illegal! existsBattery={},batteryName={}", existsBattery, batteryName);
			return false;
		}

		//缓存存换电柜中电量最多的电池
		BigEleBatteryVo bigEleBatteryVo = redisService.getWithHash(ElectricityCabinetConstant.ELE_BIG_POWER_CELL_NO_CACHE_KEY + electricityCabinet.getId().toString(), BigEleBatteryVo.class);

		//不存在电池
		if (Objects.nonNull(existsBattery) && !existsBattery && StrUtil.isNotEmpty(batteryName)) {
			log.warn("ELE WARN! battery report illegal! battery name is exists! but existsBattery is false ! batteryName ={}", batteryName);
			batteryName = null;
		}

		if (StringUtils.isEmpty(batteryName)) {
			electricityCabinetBox.setSn(null);
			electricityCabinetBox.setPower(null);
			electricityCabinetBox.setElectricityCabinetId(electricityCabinet.getId());
			electricityCabinetBox.setCellNo(cellNo);
			electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
			electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
			electricityCabinetBoxService.modifyByCellNo(electricityCabinetBox);

			//原来仓门是否有电池
			if (StringUtils.isNotEmpty(oldElectricityCabinetBox.getSn())) {

				if (oldElectricityCabinetBox.getSn().contains("UNKNOW")) {
					oldElectricityCabinetBox.setSn(oldElectricityCabinetBox.getSn().substring(6));
				}


				//修改电池
				ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySn(oldElectricityCabinetBox.getSn());
				if (Objects.nonNull(oldElectricityBattery) && !Objects.equals(oldElectricityBattery.getStatus(), ElectricityBattery.LEASE_STATUS)) {
					newElectricityBattery.setId(oldElectricityBattery.getId());
					newElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_STATUS);
					newElectricityBattery.setElectricityCabinetId(null);
					newElectricityBattery.setElectricityCabinetName(null);
					newElectricityBattery.setUid(null);
					newElectricityBattery.setUpdateTime(System.currentTimeMillis());
					electricityBatteryService.updateByOrder(newElectricityBattery);
				}
			}

			//若最大电池的仓门现在为空，则删除最大电量的缓存
			if (Objects.nonNull(bigEleBatteryVo) && Objects.equals(bigEleBatteryVo.getCellNo(), cellNo)) {
				redisService.delete(electricityCabinet.getId().toString());
			}
			return true;
		}

		NotExistSn oldNotExistSn = notExistSnService.queryByBatteryName(batteryName,electricityCabinet.getId(),Integer.valueOf(cellNo));

		ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(batteryName);
		if (Objects.isNull(electricityBattery)) {
			log.error("ele battery error! no electricityBattery,sn,{}", batteryName);

			//插入表
			if (Objects.isNull(oldNotExistSn)) {
				NotExistSn notExistSn = new NotExistSn();
				notExistSn.setEId(electricityCabinet.getId());
				notExistSn.setBatteryName(batteryName);
				notExistSn.setCellNo(Integer.valueOf(cellNo));
				notExistSn.setCreateTime(System.currentTimeMillis());
				notExistSn.setUpdateTime(System.currentTimeMillis());
				notExistSn.setTenantId(electricityCabinet.getTenantId());
				notExistSnService.insert(notExistSn);
			}
			return false;
		}

		//查询表中是否有电池
		if (Objects.nonNull(oldNotExistSn)) {
			oldNotExistSn.setDelFlag(NotExistSn.DEL_DEL);
			oldNotExistSn.setUpdateTime(System.currentTimeMillis());
			notExistSnService.update(oldNotExistSn);
		}

		if (!Objects.equals(electricityCabinet.getTenantId(), electricityBattery.getTenantId())) {
			log.error("ele battery error! tenantId is not equal,tenantId1:{},tenantId2:{}", electricityCabinet.getTenantId(), electricityBattery.getTenantId());
			return false;
		}

		//根据电池查询仓门类型
		if (Objects.nonNull(eleBatteryVo.getIsMultiBatteryModel()) && eleBatteryVo.getIsMultiBatteryModel()) {
			String batteryModel = parseBatteryNameAcquireBatteryModel(batteryName);
			electricityCabinetBox.setBatteryType(batteryModel);
			newElectricityBattery.setModel(batteryModel);
		}

		//修改电池
		newElectricityBattery.setId(electricityBattery.getId());
		newElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
		newElectricityBattery.setElectricityCabinetId(electricityCabinet.getId());
		newElectricityBattery.setElectricityCabinetName(electricityCabinet.getName());
		newElectricityBattery.setUid(null);
		newElectricityBattery.setBorrowExpireTime(null);
		newElectricityBattery.setUpdateTime(System.currentTimeMillis());
		//newElectricityBattery.setReportType(ElectricityBattery.REPORT_TYPE_ELECTRICITY_CABINET);
		Double power = eleBatteryVo.getPower();
		if (Objects.nonNull(power)) {
			newElectricityBattery.setPower(power * 100);
		}
		String health = eleBatteryVo.getHealth();
		if (StringUtils.isNotEmpty(health)) {
			newElectricityBattery.setHealthStatus(Integer.valueOf(health));
		}
		String chargeStatus = eleBatteryVo.getChargeStatus();
		if (StringUtils.isNotEmpty(chargeStatus)) {
			newElectricityBattery.setChargeStatus(Integer.valueOf(chargeStatus));
		}
		electricityBatteryService.updateByOrder(newElectricityBattery);

		//电池上报是否有其他信息
		if (Objects.nonNull(eleBatteryVo.getHasOtherAttr()) && eleBatteryVo.getHasOtherAttr()) {
			BatteryOtherPropertiesQuery batteryOtherPropertiesQuery = eleBatteryVo.getBatteryOtherProperties();
			BatteryOtherProperties batteryOtherProperties = new BatteryOtherProperties();
			BeanUtils.copyProperties(batteryOtherPropertiesQuery, batteryOtherProperties);
			batteryOtherProperties.setBatteryName(batteryName);
			batteryOtherProperties.setBatteryCoreVList(JsonUtil.toJson(batteryOtherPropertiesQuery.getBatteryCoreVList()));
			batteryOtherPropertiesService.insertOrUpdate(batteryOtherProperties);
		}

		//比较最大电量，保证仓门电池是最大电量的电池
		if (Objects.isNull(eleBatteryVo.getIsMultiBatteryModel()) || !eleBatteryVo.getIsMultiBatteryModel()) {
			Double nowPower = eleBatteryVo.getPower();
			BigEleBatteryVo newBigEleBatteryVo = new BigEleBatteryVo();
			newBigEleBatteryVo.setCellNo(cellNo);
			if (Objects.isNull(bigEleBatteryVo)) {
				newBigEleBatteryVo.setPower(nowPower);
				redisService.saveWithHash(ElectricityCabinetConstant.ELE_BIG_POWER_CELL_NO_CACHE_KEY + electricityCabinet.getId().toString(), newBigEleBatteryVo);
			} else {
				Double oldPower = bigEleBatteryVo.getPower();
				if (Objects.nonNull(oldPower) && Objects.nonNull(nowPower) && nowPower > oldPower) {
					newBigEleBatteryVo.setPower(nowPower);
					redisService.saveWithHash(ElectricityCabinetConstant.ELE_BIG_POWER_CELL_NO_CACHE_KEY + electricityCabinet.getId().toString(), newBigEleBatteryVo);
				}
			}
		}

		//修改仓门
		electricityCabinetBox.setSn(electricityBattery.getSn());
		electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);

		//查电池所属加盟商
		FranchiseeBindElectricityBattery franchiseeBindElectricityBattery = franchiseeBindElectricityBatteryService.queryByBatteryId(electricityBattery.getId());
		if (Objects.isNull(franchiseeBindElectricityBattery)) {
			log.error("ele battery error! battery not bind franchisee,electricityBatteryId:{}", electricityBattery.getId());
			electricityCabinetBox.setSn("UNKNOW" + electricityBattery.getSn());
			electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
		} else {

			// 查换电柜所属加盟商
			Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
			if (Objects.isNull(store)) {
				log.error("ele battery error! not find store,storeId:{}", electricityCabinet.getStoreId());
				return false;
			}

			if (!Objects.equals(store.getFranchiseeId(), franchiseeBindElectricityBattery.getFranchiseeId().longValue())) {
				log.error("ele battery error! franchisee is not equal,franchiseeId1:{},franchiseeId2:{}", store.getFranchiseeId(), franchiseeBindElectricityBattery.getFranchiseeId());
				electricityCabinetBox.setSn("UNKNOW" + electricityBattery.getSn());
				electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
			}
		}

		electricityCabinetBox.setElectricityCabinetId(electricityCabinet.getId());
		electricityCabinetBox.setCellNo(cellNo);
		electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
		electricityCabinetBoxService.modifyByCellNo(electricityCabinetBox);
		return true;
	}

	public static String parseBatteryNameAcquireBatteryModel(String batteryName) {
		if (StringUtils.isEmpty(batteryName) || batteryName.length() < 11) {
			return "";
		}

		StringBuilder modelName = new StringBuilder("B_");
		char[] batteryChars = batteryName.toCharArray();

		//获取电压
		String chargeV = split(batteryChars, 4, 6);
		modelName.append(chargeV).append("V").append("_");

		//获取材料体系
		char material = batteryChars[2];
		if (material == '1') {
			modelName.append(IRON_LITHIUM).append("_");
		} else {
			modelName.append(TERNARY_LITHIUM).append("_");
		}

		modelName.append(split(batteryChars, 9, 11));
		return modelName.toString();
	}

	private static String split(char[] strArray, int beginIndex, int endIndex) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = beginIndex; i < endIndex; i++) {
			stringBuilder.append(strArray[i]);
		}
		return stringBuilder.toString();
	}
}

@Data
class EleBatteryVo {
	private String batteryName;
	//电量
	private Double power;
	//健康状态
	private String health;
	//充电状态
	private String chargeStatus;
	//cellNo
	private String cellNo;
	//reportTime
	private Long reportTime;

	private Boolean existsBattery;

	private Boolean isMultiBatteryModel;

	private Boolean hasOtherAttr;

	private BatteryOtherPropertiesQuery batteryOtherProperties;

}


