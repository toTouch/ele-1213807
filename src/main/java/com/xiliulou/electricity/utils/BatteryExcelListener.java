package com.xiliulou.electricity.utils;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.BatteryExcelQuery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.impl.ElectricityBatteryServiceImpl;
import com.xiliulou.electricity.tenant.TenantContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 模板的读取类
 *
 * @author Jiaju Zhuang
 */
// 有个很重要的点 DemoDataListener 不能被spring管理，要每次读取excel都要new,然后里面用到spring可以构造方法传进去
public class BatteryExcelListener extends AnalysisEventListener<BatteryExcelQuery> {
	/**
	 * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
	 */
	private static final int BATCH_COUNT = 5;
	List<BatteryExcelQuery> list = new ArrayList<>();
	/**
	 * 假设这个是一个DAO，当然有业务逻辑这个也可以是一个service。当然如果不用存储这个对象没用。
	 */
	private ElectricityBatteryService electricityBatteryService;

	private Long franchiseeId;


	public BatteryExcelListener() {
		// 这里是demo，所以随便new一个。实际使用如果到了spring,请使用下面的有参构造函数
		electricityBatteryService = new ElectricityBatteryServiceImpl();
	}

	/**
	 * 如果使用了spring,请使用这个构造方法。每次创建Listener的时候需要把spring管理的类传进来
	 *
	 */
	public BatteryExcelListener(ElectricityBatteryService electricityBatteryService) {
		this.electricityBatteryService = electricityBatteryService;
	}

	/**
	 * 如果使用了spring,请使用这个构造方法。每次创建Listener的时候需要把spring管理的类传进来, 扩展新的参数, 运营商ID
	 *
	 */
	public BatteryExcelListener(ElectricityBatteryService electricityBatteryService, Long franchiseeId) {
		this.electricityBatteryService = electricityBatteryService;
		this.franchiseeId = franchiseeId;
	}


	/**
	 * 这个每一条数据解析都会来调用
	 *
	 * @param data
	 *            one row value. Is is same as {@link AnalysisContext#readRowHolder()}
	 * @param context
	 */
	@Override
	public void invoke(BatteryExcelQuery data, AnalysisContext context) {
		list.add(data);
		// 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
		if (list.size() >= BATCH_COUNT) {
			saveData();
			// 存储完成清理 list
			list.clear();
		}
	}

	/**
	 * 所有数据解析完成了 都会来调用
	 *
	 * @param context
	 */
	@Override
	public void doAfterAllAnalysed(AnalysisContext context) {
		// 这里也要保存数据，确保最后遗留的数据也存储到数据库
		saveData();
	}

	/**
	 * 加上存储数据库
	 */
	private void saveData() {
		if(ObjectUtil.isNotEmpty(list)) {
			for (BatteryExcelQuery batteryExcelQuery : list) {
				//租户
				Integer tenantId = TenantContextHolder.getTenantId();

				ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(batteryExcelQuery.getSn());
				if (Objects.nonNull(electricityBattery)) {
					continue;
				}
				electricityBattery=new ElectricityBattery();
				if(Objects.isNull(batteryExcelQuery.getSn())){
					continue;
				}
				electricityBattery.setSn(batteryExcelQuery.getSn());


				if(Objects.isNull(batteryExcelQuery.getModel())){
					batteryExcelQuery.setModel("0");
				}
				electricityBattery.setModel(batteryExcelQuery.getModel());

				if(Objects.isNull(batteryExcelQuery.getCapacity())){
					batteryExcelQuery.setCapacity(0);
				}
				electricityBattery.setCapacity(batteryExcelQuery.getCapacity());

				if(Objects.isNull(batteryExcelQuery.getVoltage())){
					batteryExcelQuery.setVoltage(0);
				}
				electricityBattery.setVoltage(batteryExcelQuery.getVoltage());
				electricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_INPUT);
				electricityBattery.setPhysicsStatus(ElectricityBattery.PHYSICS_STATUS_NOT_WARE_HOUSE);
				electricityBattery.setCreateTime(System.currentTimeMillis());
				electricityBattery.setUpdateTime(System.currentTimeMillis());
				electricityBattery.setTenantId(tenantId);
				electricityBattery.setFranchiseeId(franchiseeId);
				electricityBatteryService.insert(electricityBattery);
			}
		}

	}
}
