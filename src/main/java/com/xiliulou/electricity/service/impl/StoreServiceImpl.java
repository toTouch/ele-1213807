package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.StoreMapper;
import com.xiliulou.electricity.query.ElectricityCabinetAddAndUpdate;
import com.xiliulou.electricity.query.StoreAddAndUpdate;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.StoreVO;
import com.xiliulou.electricity.web.query.AdminUserQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 门店表(TStore)表服务实现类
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@Service("storeService")
@Slf4j
public class StoreServiceImpl implements StoreService {
	@Resource
	private StoreMapper storeMapper;
	@Autowired
	RedisService redisService;
	@Autowired
	ElectricityBatteryService electricityBatteryService;
	@Autowired
	ElectricityCabinetService electricityCabinetService;
	@Autowired
	UserService userService;

	/**
	 * 通过ID查询单条数据从缓存
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public Store queryByIdFromCache(Integer id) {
		Store cacheStore = redisService.getWithHash(ElectricityCabinetConstant.CACHE_STORE + id, Store.class);
		if (Objects.nonNull(cacheStore)) {
			return cacheStore;
		}
		Store store = storeMapper.selectById(id);
		if (Objects.isNull(store)) {
			return null;
		}
		redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE + id, store);
		return store;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R save(StoreAddAndUpdate storeAddAndUpdate) {
		//新增加盟商新增用户
		AdminUserQuery adminUserQuery = new AdminUserQuery();
		BeanUtil.copyProperties(storeAddAndUpdate, adminUserQuery);
		adminUserQuery.setUserType(User.TYPE_USER_STORE);
		adminUserQuery.setLang(User.DEFAULT_LANG);
		adminUserQuery.setGender(User.GENDER_FEMALE);
		adminUserQuery.setPhone(storeAddAndUpdate.getServicePhone());

		R result = userService.addInnerUser(adminUserQuery);
		if (result.getCode() == 1) {
			return result;
		}

		Long uid = (Long) result.getData();
		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		Store store = new Store();
		BeanUtil.copyProperties(storeAddAndUpdate, store);

		//校验参数
		if (checkParam(storeAddAndUpdate, store)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}

		//填充参数
		if (Objects.isNull(store.getUsableStatus())) {
			store.setUsableStatus(Store.STORE_UN_USABLE_STATUS);
		}
		store.setCreateTime(System.currentTimeMillis());
		store.setUpdateTime(System.currentTimeMillis());
		store.setDelFlag(ElectricityCabinet.DEL_NORMAL);
		store.setTenantId(tenantId);
		store.setUid(uid);

		int insert = storeMapper.insert(store);
		DbUtils.dbOperateSuccessThen(insert, () -> {
			//新增缓存
			redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE + store.getId(), store);
			return null;
		});

		if (insert > 0) {
			return R.ok();
		}
		return R.fail("ELECTRICITY.0086", "操作失败");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R edit(StoreAddAndUpdate storeAddAndUpdate) {

		Store store = new Store();
		BeanUtil.copyProperties(storeAddAndUpdate, store);
		Store oldStore = queryByIdFromCache(store.getId());
		if (Objects.isNull(oldStore)) {
			return R.fail("ELECTRICITY.0018", "未找到门店");
		}
		if (Objects.nonNull(storeAddAndUpdate.getBusinessTimeType())) {
			if (checkParam(storeAddAndUpdate, store)) {
				return R.fail("ELECTRICITY.0007", "不合法的参数");
			}
		}

		store.setUpdateTime(System.currentTimeMillis());
		int update = storeMapper.updateById(store);
		DbUtils.dbOperateSuccessThen(update, () -> {
			//更新缓存
			redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE + store.getId(), store);
			return null;
		});

		if (update > 0) {
			return R.ok();
		}
		return R.fail("ELECTRICITY.0086", "操作失败");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R delete(Integer id) {

		Store store = queryByIdFromCache(id);
		if (Objects.isNull(store)) {
			return R.fail("ELECTRICITY.0018", "未找到门店");
		}

		//查询门店是否绑定换电柜
		Integer count=electricityCabinetService.queryCountByStoreId(store.getId());

		if(count>0){
			return R.fail("门店已绑定换电柜");
		}

		store.setUpdateTime(System.currentTimeMillis());
		store.setDelFlag(ElectricityCabinet.DEL_DEL);

		int update = storeMapper.updateById(store);
		DbUtils.dbOperateSuccessThen(update, () -> {
			//删除缓存
			redisService.delete(ElectricityCabinetConstant.CACHE_STORE + id);
			//删除用户
			 userService.deleteInnerUser(store.getUid());
			return null;
		});


		if (update > 0) {
			return R.ok();
		}
		return R.fail("ELECTRICITY.0086", "操作失败");
	}

	@Override
	@DS("slave_1")
	public R queryList(StoreQuery storeQuery) {
		List<StoreVO> storeVOList = storeMapper.queryList(storeQuery);
		if (ObjectUtil.isEmpty(storeVOList)) {
			return R.ok(new ArrayList<>());
		}
		if (ObjectUtil.isNotEmpty(storeVOList)) {
			storeVOList.parallelStream().forEach(e -> {
				//营业时间
				if (Objects.nonNull(e.getBusinessTime())) {
					String businessTime = e.getBusinessTime();
					if (Objects.equals(businessTime, StoreVO.ALL_DAY)) {
						e.setBusinessTimeType(StoreVO.ALL_DAY);
					} else {
						e.setBusinessTimeType(StoreVO.ILLEGAL_DATA);
						Integer index = businessTime.indexOf("-");
						if (!Objects.equals(index, -1) && index > 0) {
							e.setBusinessTimeType(StoreVO.CUSTOMIZE_TIME);
							Long beginTime = Long.valueOf(businessTime.substring(0, index));
							Long endTime = Long.valueOf(businessTime.substring(index + 1));
							e.setBeginTime(beginTime);
							e.setEndTime(endTime);
						}
					}
				}
				if (Objects.nonNull(e.getUid())) {
					User user = userService.queryByUidFromCache(e.getUid());
					if (Objects.nonNull(user)) {
						e.setUserName(user.getName());
					}
				}
			});
		}
		storeVOList.stream().sorted(Comparator.comparing(StoreVO::getCreateTime).reversed()).collect(Collectors.toList());
		return R.ok(storeVOList);
	}

	@Override
	@Transactional
	public R updateStatus(Integer id, Integer usableStatus) {

		Store oldStore = queryByIdFromCache(id);
		if (Objects.isNull(oldStore)) {
			return R.fail("ELECTRICITY.0018", "未找到门店");
		}


		Store store = new Store();
		store.setId(id);
		store.setUpdateTime(System.currentTimeMillis());
		store.setUsableStatus(usableStatus);
		int update = storeMapper.updateById(store);


		DbUtils.dbOperateSuccessThen(update, () -> {
			//更新缓存
			redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE + store.getId(), store);
			return null;
		});
		return R.ok();
	}

	@Override
	public Integer homeOne(List<Integer> storeIdList, Integer tenantId) {
		return storeMapper.homeOne(storeIdList, tenantId);
	}

	@Override
	public R showInfoByDistance(StoreQuery storeQuery) {
		List<StoreVO> storeVOList = storeMapper.showInfoByDistance(storeQuery);
		List<StoreVO> storeVOs = new ArrayList<>();
		if (ObjectUtil.isNotEmpty(storeVOList)) {
			storeVOList.parallelStream().forEach(e -> {

				//营业时间
				if (Objects.nonNull(e.getBusinessTime())) {
					String businessTime = e.getBusinessTime();
					if (Objects.equals(businessTime, StoreVO.ALL_DAY)) {
						e.setBusinessTimeType(StoreVO.ALL_DAY);
						e.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
					} else {
						e.setBusinessTimeType(StoreVO.ILLEGAL_DATA);
						Integer index = businessTime.indexOf("-");
						if (!Objects.equals(index, -1) && index > 0) {
							e.setBusinessTimeType(ElectricityCabinetVO.CUSTOMIZE_TIME);
							Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
							Long beginTime = getTime(totalBeginTime);
							Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
							Long endTime = getTime(totalEndTime);
							e.setBeginTime(totalBeginTime);
							e.setEndTime(totalEndTime);
							Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
							Long now = System.currentTimeMillis();
							if (firstToday + beginTime > now || firstToday + endTime < now) {
								e.setIsBusiness(ElectricityCabinetVO.IS_NOT_BUSINESS);
							} else {
								e.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
							}
						}
					}
				}

				//在线电柜数
				Integer onlineElectricityCabinetCount = 0;
				//满电电池数
				Integer fullyElectricityBatteryCount = 0;
				List<ElectricityCabinet> electricityCabinetList = electricityCabinetService.queryByStoreId(e.getId());
				if (ObjectUtil.isNotEmpty(electricityCabinetList)) {
					for (ElectricityCabinet electricityCabinet : electricityCabinetList) {
						//动态查询在线状态
						boolean result = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
						if (result) {
							onlineElectricityCabinetCount = onlineElectricityCabinetCount + 1;
							Integer fullyElectricityBattery = electricityCabinetService.queryFullyElectricityBattery(electricityCabinet.getId()).get(1);
							fullyElectricityBatteryCount = fullyElectricityBatteryCount + fullyElectricityBattery;
						}
					}
				}
				e.setOnlineElectricityCabinet(onlineElectricityCabinetCount);
				e.setFullyElectricityBattery(fullyElectricityBatteryCount);
				storeVOs.add(e);
			});
		}
		return R.ok(storeVOs.stream().sorted(Comparator.comparing(StoreVO::getDistance)).collect(Collectors.toList()));
	}

	@Override
	public List<Store> queryByFranchiseeId(Integer id) {
		return storeMapper.selectList(new LambdaQueryWrapper<Store>().eq(Store::getFranchiseeId, id).eq(Store::getDelFlag, Store.DEL_NORMAL));
	}

	@Override
	public Store queryByUid(Long uid) {
		return storeMapper.selectOne(new LambdaQueryWrapper<Store>().eq(Store::getUid, uid).eq(Store::getDelFlag, Store.DEL_NORMAL));
	}

	@Override
	public R queryCount(StoreQuery storeQuery) {
		return R.ok(storeMapper.queryCount(storeQuery));
	}

	@Override
	public R queryCountByFranchisee(StoreQuery storeQuery) {
		return R.ok(storeMapper.queryCount(storeQuery));
	}

	@Override
	public List<HashMap<String, String>> homeThree(Long startTimeMilliDay, Long endTimeMilliDay, List<Integer> storeIdList, Integer tenantId) {
		return storeMapper.homeThree(startTimeMilliDay, endTimeMilliDay, storeIdList, tenantId);
	}

	@Override
	public void deleteByUid(Long uid) {
		Store store = queryByUid(uid);
		if (Objects.nonNull(store)) {

			//删除用户
			store.setUpdateTime(System.currentTimeMillis());
			store.setDelFlag(ElectricityCabinet.DEL_DEL);

			int update = storeMapper.updateById(store);
			DbUtils.dbOperateSuccessThen(update, () -> {
				//删除缓存
				redisService.delete(ElectricityCabinetConstant.CACHE_STORE + store.getId());
				return null;
			});
		}
	}

	@Override
	public Integer queryCountByFranchiseeId(Integer id) {
		return storeMapper.selectCount(new LambdaQueryWrapper<Store>().eq(Store::getFranchiseeId,id).eq(Store::getDelFlag,Store.DEL_NORMAL).last("limit 0,1"));
	}

	@Override
	public Integer queryByFanchisee(Long uid) {
		Store store=queryByUid(uid);

		if(Objects.isNull(store)){
			return 0;
		}

		return electricityCabinetService.queryCountByStoreId(store.getId());
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

	private boolean checkParam(StoreAddAndUpdate storeAddAndUpdate, Store store) {
		if (Objects.equals(storeAddAndUpdate.getBusinessTimeType(), ElectricityCabinetAddAndUpdate.ALL_DAY)) {
			store.setBusinessTime(ElectricityCabinetAddAndUpdate.ALL_DAY);
		}
		if (Objects.equals(storeAddAndUpdate.getBusinessTimeType(), ElectricityCabinetAddAndUpdate.CUSTOMIZE_TIME)) {
			if (Objects.isNull(storeAddAndUpdate.getBeginTime()) || Objects.isNull(storeAddAndUpdate.getEndTime())
					|| storeAddAndUpdate.getBeginTime() > storeAddAndUpdate.getEndTime()) {
				return true;
			}
			store.setBusinessTime(storeAddAndUpdate.getBeginTime() + "-" + storeAddAndUpdate.getEndTime());
		}
		if (Objects.isNull(store.getBusinessTime())) {
			return true;
		}
		return false;
	}

}
