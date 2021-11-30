package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.core.wp.entity.AppTemplateQuery;
import com.xiliulou.core.wp.service.WeChatAppTemplateService;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.config.WechatTemplateAdminNotificationConfig;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.StoreElectricityCabinetQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * 换电柜电池表(ElectricityBattery)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
@Service
@Slf4j
public class ElectricityBatteryServiceImpl extends ServiceImpl<ElectricityBatteryMapper, ElectricityBattery> implements ElectricityBatteryService {
	@Resource
	private ElectricityBatteryMapper electricitybatterymapper;
	@Autowired
	StoreService storeService;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	ElectricityCabinetService electricityCabinetService;
	@Autowired
	FranchiseeBindElectricityBatteryService franchiseeBindElectricityBatteryService;
	@Autowired
	WechatTemplateAdminNotificationConfig wechatTemplateAdminNotificationConfig;
	@Autowired
	RedisService redisService;
	@Autowired
	WechatTemplateAdminNotificationConfig wechatTemplateAdminNotificationConfig;
	@Autowired
	UserService userService;
	@Autowired
	WechatTemplateAdminNotificationService wechatTemplateAdminNotificationService;
	@Autowired
	WeChatAppTemplateService weChatAppTemplateService;
	@Autowired
	FranchiseeService franchiseeService;
	@Autowired
	ElectricityPayParamsService electricityPayParamsService;
	@Autowired
	TemplateConfigService templateConfigService;


	/**
	 * 保存电池
	 *
	 * @param
	 * @return
	 */
	@Override
	public R saveElectricityBattery(ElectricityBattery electricityBattery) {
		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		Integer count = electricitybatterymapper.selectCount(new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, electricityBattery.getSn())
				.eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL));
		if (count > 0) {
			return R.fail("该电池已被其他租户使用!");
		}
		electricityBattery.setStatus(ElectricityBattery.STOCK_STATUS);
		electricityBattery.setCreateTime(System.currentTimeMillis());
		electricityBattery.setUpdateTime(System.currentTimeMillis());
		electricityBattery.setTenantId(tenantId);
		return R.ok(electricitybatterymapper.insert(electricityBattery));
	}

	/**
	 * 修改电池
	 *
	 * @param electricityBattery
	 * @return
	 */
	@Override
	public R update(ElectricityBattery electricityBattery) {
		ElectricityBattery electricityBatteryDb = electricitybatterymapper.selectById(electricityBattery.getId());
		if (Objects.isNull(electricityBatteryDb)) {
			log.error("UPDATE ELECTRICITY_BATTERY  ERROR, NOT FOUND ELECTRICITY_BATTERY BY ID:{}", electricityBattery.getId());
			return R.fail("电池不存在!");
		}
		Integer count = electricitybatterymapper.selectCount(new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, electricityBattery.getSn())
				.eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL).ne(ElectricityBattery::getId, electricityBattery.getId()));
		if (count > 0) {
			return R.fail("电池编号已绑定其他电池!");
		}
		electricityBattery.setUpdateTime(System.currentTimeMillis());
		Integer rows = electricitybatterymapper.updateById(electricityBattery);
		if (rows > 0) {
			return R.ok();
		} else {
			return R.fail("修改失败!");

		}
	}

	/**
	 * 电池分页
	 *
	 * @param electricityBatteryQuery
	 * @param
	 * @return
	 */
	@Override
	@DS("slave_1")
	public R queryList(ElectricityBatteryQuery electricityBatteryQuery, Long offset, Long size) {

		List<ElectricityBattery> electricityBatteryList = electricitybatterymapper.queryList(electricityBatteryQuery, offset, size);

		if (ObjectUtil.isEmpty(electricityBatteryList)) {
			return R.ok(electricityBatteryList);
		}

		List<ElectricityBatteryVO> electricityBatteryVOList = new ArrayList<>();

		List<FranchiseeBindElectricityBattery> franchiseeBindElectricityBatteryList = new ArrayList<>();
		if (Objects.nonNull(electricityBatteryQuery.getFranchiseeId())) {
			franchiseeBindElectricityBatteryList = franchiseeBindElectricityBatteryService.queryByFranchiseeId(electricityBatteryQuery.getFranchiseeId());
		}

		for (ElectricityBattery electricityBattery : electricityBatteryList) {

			ElectricityBatteryVO electricityBatteryVO = new ElectricityBatteryVO();
			BeanUtil.copyProperties(electricityBattery, electricityBatteryVO);

			if (Objects.equals(electricityBattery.getStatus(), ElectricityBattery.LEASE_STATUS) && Objects.nonNull(electricityBattery.getUid())) {
				UserInfo userInfo = userInfoService.queryByUid(electricityBattery.getUid());
				if (Objects.nonNull(userInfo)) {
					electricityBatteryVO.setUserName(userInfo.getName());
				}
			}

			if (Objects.equals(electricityBattery.getStatus(), ElectricityBattery.WARE_HOUSE_STATUS) && Objects.nonNull(electricityBattery.getElectricityCabinetId())) {
				ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityBattery.getElectricityCabinetId());
				if (Objects.nonNull(electricityCabinet)) {
					electricityBatteryVO.setElectricityCabinetName(electricityCabinet.getName());
				}
			}

			//用于电池绑定问题
			electricityBatteryVO.setIsBind(false);

			if (ObjectUtil.isNotEmpty(franchiseeBindElectricityBatteryList)) {
				for (FranchiseeBindElectricityBattery franchiseeBindElectricityBattery : franchiseeBindElectricityBatteryList) {
					if (Objects.equals(franchiseeBindElectricityBattery.getElectricityBatteryId(), electricityBattery.getId())) {
						electricityBatteryVO.setIsBind(true);
					}
				}
			}

			electricityBatteryVOList.add(electricityBatteryVO);
		}
		return R.ok(electricityBatteryVOList);
	}

	@Override
	public R queryById(Long electricityBatteryId) {
		ElectricityBattery electricityBattery = electricitybatterymapper.selectById(electricityBatteryId);

		ElectricityBatteryVO electricityBatteryVO = new ElectricityBatteryVO();
		BeanUtil.copyProperties(electricityBattery, electricityBatteryVO);

		if (Objects.equals(electricityBattery.getStatus(), ElectricityBattery.LEASE_STATUS) && Objects.nonNull(electricityBattery.getUid())) {
			UserInfo userInfo = userInfoService.queryByUid(electricityBattery.getUid());
			if (Objects.nonNull(userInfo)) {
				electricityBatteryVO.setUserName(userInfo.getName());
			}
		}
		return R.ok(electricityBatteryVO);
	}

	/**
	 * 删除电池
	 *
	 * @param id
	 * @return
	 */
	@Override
	public R deleteElectricityBattery(Long id) {
		ElectricityBattery electricityBattery = electricitybatterymapper.selectById(id);
		if (Objects.isNull(electricityBattery)) {
			log.error("DELETE_ELECTRICITY_BATTERY  ERROR ,NOT FOUND ELECTRICITYBATTERY ID:{}", id);
			return R.failMsg("未找到电池!");
		}

		if (ObjectUtil.equal(ElectricityBattery.LEASE_STATUS, electricityBattery.getStatus())) {
			log.error("DELETE_ELECTRICITY_BATTERY  ERROR ,THIS ELECTRICITY_BATTERY IS USING:{}", id);
			return R.failMsg("电池正在租用中,无法删除!");
		}

		int raws = electricitybatterymapper.deleteById(id);
		if (raws > 0) {
			return R.ok();
		} else {
			return R.failMsg("删除失败!");
		}
	}

	@Override
	public ElectricityBattery queryByBindSn(String initElectricityBatterySn) {
		return electricitybatterymapper.selectOne(new LambdaQueryWrapper<ElectricityBattery>()
				.eq(ElectricityBattery::getSn, initElectricityBatterySn));
	}

	/**
	 * 获取个人电池
	 *
	 * @param uid
	 * @return
	 */
	@Override
	public ElectricityBattery queryByUid(Long uid) {
		return baseMapper.selectBatteryInfo(uid);
	}

	@Override
	public ElectricityBattery queryBySn(String oldElectricityBatterySn) {
		return electricitybatterymapper.selectOne(new LambdaQueryWrapper<ElectricityBattery>().
				eq(ElectricityBattery::getSn, oldElectricityBatterySn));
	}

	@Override
	public Integer updateByOrder(ElectricityBattery electricityBattery) {
		return electricitybatterymapper.updateByOrder(electricityBattery);
	}

	@Override
	public R queryCount(ElectricityBatteryQuery electricityBatteryQuery) {
		return R.ok(electricitybatterymapper.queryCount(electricityBatteryQuery));
	}

	@Override
	public R batteryOutTimeInfo(Long tenantId){
		String json = redisService.get(ElectricityCabinetConstant.CACHE_ADMIN_ALREADY_NOTIFICATION + tenantId);
		List<ElectricityBattery> list = null;
		if(StrUtil.isNotBlank(json)){
			list = JSON.parseArray(json, ElectricityBattery.class);
		}
		return R.ok(list);
	}

	@Override
	public void handlerBatteryNotInCabinetWarning() {

		Integer offset = 0;
		Integer size = 300;
		while (true) {
			List<ElectricityBattery> borrowExpireBatteryList = electricitybatterymapper.queryBorrowExpireBattery(System.currentTimeMillis(), offset, size);
			if (CollectionUtils.isEmpty(borrowExpireBatteryList)) {
				return;
			}
			//将电池按租户id分组
			Map<Integer, List<ElectricityBattery>> batteryMaps = borrowExpireBatteryList.stream().collect(Collectors.groupingBy(ElectricityBattery::getTenantId));
			//频率
			Long frequency = Long.parseLong(wechatTemplateAdminNotificationConfig.getFrequency()) * 60000;

			batteryMaps.entrySet().parallelStream().forEach(entry -> {
				Integer tenantId = entry.getKey();
				List<ElectricityBattery> batteryList = entry.getValue();

				boolean isOutTime = redisService.setNx(ElectricityCabinetConstant.CACHE_ADMIN_ALREADY_NOTIFICATION + tenantId, JSON.toJSONString(batteryList), frequency, false);
				if (!isOutTime) {
					return;
				}

				WechatTemplateAdminNotification wechatTemplateAdminNotification = wechatTemplateAdminNotificationService.queryByTenant(tenantId);
				if (Objects.isNull(wechatTemplateAdminNotification)) {
					log.error("WECHAT_TEMPLATE_ADMIN_NOTIFICATION IS NULL ERROR! tenantId={}", tenantId);
					return;
				}

				BaseMapper<ElectricityPayParams> mapper = electricityPayParamsService.getBaseMapper();
				QueryWrapper<ElectricityPayParams> wrapper = new QueryWrapper<>();
				wrapper.eq("tenant_id", tenantId);
				ElectricityPayParams ele = mapper.selectOne(wrapper);

				if (Objects.isNull(ele)) {
					log.error("ELECTRICITY_PAY_PARAMS IS NULL ERROR! tenantId={}", tenantId);
					return;
				}

				TemplateConfigEntity templateConfigEntity = templateConfigService.queryByTenantIdFromCache(tenantId);

				if(Objects.isNull(templateConfigEntity) || Objects.isNull(templateConfigEntity.getBatteryOuttimeTemplate())){
					log.error("TEMPLATE_CONFIG IS NULL ERROR! tenantId={}", tenantId);
					return;
				}

				String openStr = wechatTemplateAdminNotification.getOpenIds();
				List<String> openIds = JSON.parseArray(openStr, String.class);
				AppTemplateQuery appTemplateQuery = createAppTemplateQuery(batteryList, tenantId, ele.getMerchantMinProAppId(), ele.getMerchantMinProAppSecert(), templateConfigEntity.getBatteryOuttimeTemplate());

				if (CollectionUtils.isNotEmpty(openIds)) {
					for (String openId : openIds) {
						appTemplateQuery.setTouser(openId);
						weChatAppTemplateService.sendWeChatAppTemplate(appTemplateQuery);
					}
				}
			});
			offset += size;
		}
	}



	private AppTemplateQuery createAppTemplateQuery(List<ElectricityBattery> batteryList, Integer tenantId, String appId, String appSecret, String batteryOuttimeTemplate){
		AppTemplateQuery appTemplateQuery = new AppTemplateQuery();
		appTemplateQuery.setAppId(appId);
		appTemplateQuery.setSecret(appSecret);
		//appTemplateQuery.setTouser(openId);
		appTemplateQuery.setTemplateId(batteryOuttimeTemplate);
		appTemplateQuery.setFormId(RandomUtil.randomString(20));
		//TODO 这块写个页面 调用 user/battery/outTime/Info
		appTemplateQuery.setPage("xxxxx?tenantId"+tenantId);
		//发送内容
		appTemplateQuery.setData(createData(batteryList));
		return appTemplateQuery;
	}

	private Map<String, Object> createData(List<ElectricityBattery> batteryList){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 hh时mm分ss秒");

		Map<String, Object> data = new HashMap<>();
		Map<String, String> keyword1 = new HashMap<>();
		keyword1.put("value", sdf.format(new Date(System.currentTimeMillis())));
		data.put("keyword1", keyword1);
		Map<String, String> keyword2 = new HashMap<>();
		keyword2.put("value", String.valueOf(batteryList.size()));
		data.put("keyword2", keyword2);

		return data;
	}

}
