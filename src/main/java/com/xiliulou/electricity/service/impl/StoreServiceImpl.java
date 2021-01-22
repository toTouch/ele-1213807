package com.xiliulou.electricity.service.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBind;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.mapper.StoreMapper;
import com.xiliulou.electricity.query.ElectricityCabinetAddAndUpdate;
import com.xiliulou.electricity.query.StoreAddAndUpdate;
import com.xiliulou.electricity.query.BindElectricityCabinetQuery;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBindService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.PageUtil;
import com.xiliulou.electricity.vo.StoreVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 门店表(TStore)表服务实现类
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@Service("storeService")
public class StoreServiceImpl implements StoreService {
    @Resource
    private StoreMapper storeMapper;
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    ElectricityCabinetBindService   electricityCabinetBindService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Store queryByIdFromDB(Integer id) {
        return this.storeMapper.queryById(id);
    }

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
        Store store = storeMapper.queryById(id);
        if (Objects.isNull(store)) {
            return null;
        }
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE + id, store);
        return store;
    }

    @Override
    @Transactional
    public R save(StoreAddAndUpdate storeAddAndUpdate) {
        Store store = new Store();
        BeanUtil.copyProperties(storeAddAndUpdate, store);
        if (Objects.equals(storeAddAndUpdate.getBusinessTimeType(), ElectricityCabinetAddAndUpdate.ALL_DAY)) {
            store.setBusinessTime(ElectricityCabinetAddAndUpdate.ALL_DAY);
        }
        if (Objects.equals(storeAddAndUpdate.getBusinessTimeType(), ElectricityCabinetAddAndUpdate.CUSTOMIZE_TIME)) {
            if (Objects.isNull(storeAddAndUpdate.getBeginTime()) || Objects.isNull(storeAddAndUpdate.getEndTime())
                    || storeAddAndUpdate.getBeginTime() > storeAddAndUpdate.getEndTime()) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }
            store.setBusinessTime(storeAddAndUpdate.getBeginTime() + "-" + storeAddAndUpdate.getEndTime());
        }
        if (Objects.isNull(store.getBusinessTime())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        if (Objects.isNull(store.getUsableStatus())) {
            store.setUsableStatus(Store.STORE_UN_USABLE_STATUS);
        }
        store.setCreateTime(System.currentTimeMillis());
        store.setUpdateTime(System.currentTimeMillis());
        store.setDelFlag(ElectricityCabinet.DEL_NORMAL);
        int insert = storeMapper.insert(store);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //新增缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE + store.getId(), store);
            return null;
        });
        return R.ok();
    }

    @Override
    @Transactional
    public R edit(StoreAddAndUpdate storeAddAndUpdate) {
        Store store = new Store();
        BeanUtil.copyProperties(storeAddAndUpdate, store);
        Store oldStore = queryByIdFromCache(store.getId());
        if (Objects.isNull(oldStore)) {
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        if (Objects.nonNull(storeAddAndUpdate.getBusinessTimeType())) {
            if (Objects.equals(storeAddAndUpdate.getBusinessTimeType(), ElectricityCabinetAddAndUpdate.ALL_DAY)) {
                store.setBusinessTime(ElectricityCabinetAddAndUpdate.ALL_DAY);
            }
            if (Objects.equals(storeAddAndUpdate.getBusinessTimeType(), ElectricityCabinetAddAndUpdate.CUSTOMIZE_TIME)) {
                if (Objects.isNull(storeAddAndUpdate.getBeginTime()) || Objects.isNull(storeAddAndUpdate.getEndTime())
                        || storeAddAndUpdate.getBeginTime() > storeAddAndUpdate.getEndTime()) {
                    return R.fail("ELECTRICITY.0007", "不合法的参数");
                }
                store.setBusinessTime(storeAddAndUpdate.getBeginTime() + "-" + storeAddAndUpdate.getEndTime());
            }
            if (Objects.isNull(store.getBusinessTime())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }
        }
        store.setUpdateTime(System.currentTimeMillis());
        int update = storeMapper.update(store);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE + store.getId(), store);
            return null;
        });
        return R.ok();
    }

    @Override
    @Transactional
    public R delete(Integer id) {
        Store store = queryByIdFromCache(id);
        if (Objects.isNull(store)) {
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        store.setUpdateTime(System.currentTimeMillis());
        store.setDelFlag(ElectricityCabinet.DEL_DEL);
        int update = storeMapper.update(store);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //删除缓存
            redisService.deleteKeys(ElectricityCabinetConstant.CACHE_STORE + id);
            return null;
        });
        return R.ok();
    }

    @Override
    @DS("slave_1")
    public R queryList(StoreQuery storeQuery) {
        Page page = PageUtil.getPage(storeQuery.getOffset(), storeQuery.getSize());


        storeMapper.queryList(page, storeQuery);
        if (ObjectUtil.isEmpty(page.getRecords())) {
            return R.ok(new ArrayList<>());
        }
        List<StoreVO> storeVOList = page.getRecords();
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
            });
        }
        page.setRecords(storeVOList.stream().sorted(Comparator.comparing(StoreVO::getCreateTime).reversed()).collect(Collectors.toList()));
        return R.ok(page);
    }

    @Override
    @Transactional
    public R disable(Integer id) {
        Store oldStore = queryByIdFromCache(id);
        if (Objects.isNull(oldStore)) {
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        Store store = new Store();
        store.setId(id);
        store.setUpdateTime(System.currentTimeMillis());
        store.setUsableStatus(Store.STORE_UN_USABLE_STATUS);
        int update = storeMapper.update(store);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE + store.getId(), store);
            return null;
        });
        return R.ok();
    }

    @Override
    @Transactional
    public R reboot(Integer id) {
        Store oldStore = queryByIdFromCache(id);
        if (Objects.isNull(oldStore)) {
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        Store store = new Store();
        store.setId(id);
        store.setUpdateTime(System.currentTimeMillis());
        store.setUsableStatus(Store.STORE_USABLE_STATUS);
        int update = storeMapper.update(store);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE + store.getId(), store);
            return null;
        });
        return R.ok();
    }


    @Override
    public Integer homeTwoTotal() {
        return storeMapper.selectCount(new LambdaQueryWrapper<Store>().eq(Store::getDelFlag, Store.DEL_NORMAL));
    }

    @Override
    public Integer homeTwoBusiness() {
        List<Store> storeList = storeMapper.selectList(new LambdaQueryWrapper<Store>().eq(Store::getDelFlag, Store.DEL_NORMAL));
        Integer countBusiness = 0;
        if (ObjectUtil.isNotEmpty(storeList)) {
            for (Store store : storeList) {
                //营业时间
                if (Objects.nonNull(store.getBusinessTime())) {
                    String businessTime = store.getBusinessTime();
                    if (Objects.equals(businessTime, StoreVO.ALL_DAY)) {
                        countBusiness = countBusiness + 1;
                    } else {
                        Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                        Long now = System.currentTimeMillis();
                        Long beginTime = Long.valueOf(businessTime.substring(0, businessTime.indexOf("-") - 1));
                        Long endTime = Long.valueOf(businessTime.substring(businessTime.indexOf("-"), businessTime.length() - 1));
                        if (firstToday + beginTime < now && firstToday + endTime > now) {
                            countBusiness = countBusiness + 1;
                        }
                    }
                }
            }
        }
        return countBusiness;
    }


    @Override
    public R rentBattery(StoreQuery storeQuery) {
        List<StoreVO> storeVOList = storeMapper.showInfoByDistance(storeQuery);
        List<StoreVO> storeVOs = new ArrayList<>();
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
                if (Objects.equals(e.getBatteryService(), Store.SUPPORT)) {
                    storeVOs.add(e);
                }
            });
        }
        return R.ok(storeVOs.stream().sorted(Comparator.comparing(StoreVO::getDistance)).collect(Collectors.toList()));
    }

    @Override
    public R rentCar(StoreQuery storeQuery) {
        List<StoreVO> storeVOList = storeMapper.showInfoByDistance(storeQuery);
        List<StoreVO> storeVOs = new ArrayList<>();
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
                if (Objects.equals(e.getCarService(), Store.SUPPORT)) {
                    storeVOs.add(e);
                }
            });
        }
        return R.ok(storeVOs.stream().sorted(Comparator.comparing(StoreVO::getDistance)).collect(Collectors.toList()));
    }

    @Override
    public R bindElectricityCabinet(BindElectricityCabinetQuery bindElectricityCabinetQuery) {
        //先删除
        electricityCabinetBindService.deleteByUid(bindElectricityCabinetQuery.getUid());
        if(ObjectUtil.isEmpty(bindElectricityCabinetQuery.getElectricityCabinetIdList())){
            return R.ok();
        }
        //再新增
        for (Integer electricityCabinetId : bindElectricityCabinetQuery.getElectricityCabinetIdList()) {
            ElectricityCabinetBind electricityCabinetBind=new ElectricityCabinetBind();
            electricityCabinetBind.setUid(bindElectricityCabinetQuery.getUid());
            electricityCabinetBind.setElectricityCabinetId(electricityCabinetId);
            electricityCabinetBindService.insert(electricityCabinetBind);
        }
        return R.ok();
    }
}