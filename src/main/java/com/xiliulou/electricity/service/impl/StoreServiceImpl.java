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
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.StoreBind;
import com.xiliulou.electricity.entity.StoreBindElectricityCabinet;
import com.xiliulou.electricity.mapper.StoreMapper;
import com.xiliulou.electricity.query.ElectricityCabinetAddAndUpdate;
import com.xiliulou.electricity.query.StoreAddAndUpdate;
import com.xiliulou.electricity.query.StoreBindElectricityCabinetQuery;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.StoreBindElectricityCabinetService;
import com.xiliulou.electricity.service.StoreBindService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.PageUtil;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.StoreVO;
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
    StoreBindElectricityCabinetService storeBindElectricityCabinetService;
    @Autowired
    StoreBindService storeBindService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;

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
        //TODO 判断用户存不存在
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
            //新增门店绑定
            if (Objects.nonNull(storeAddAndUpdate.getUid())) {
                StoreBind storeBind = new StoreBind();
                storeBind.setStoreId(store.getId());
                storeBind.setUid(storeAddAndUpdate.getUid());
                storeBindService.insert(storeBind);
            }
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
        //TODO 判断用户存不存在
        store.setUpdateTime(System.currentTimeMillis());
        int update = storeMapper.update(store);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE + store.getId(), store);
            //先删除再新增
            storeBindService.deleteByStoreId(store.getId());
            if (Objects.nonNull(storeAddAndUpdate.getUid())) {
                StoreBind storeBind = new StoreBind();
                storeBind.setStoreId(store.getId());
                storeBind.setUid(storeAddAndUpdate.getUid());
                storeBindService.insert(storeBind);
            }
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
            //删除绑定
            storeBindService.deleteByStoreId(store.getId());
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
                StoreBind storeBind=storeBindService.queryByStoreId(e.getId());
                if(Objects.nonNull(storeBind)){
                    e.setUid(storeBind.getUid());
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
                Integer onlineElectricityCabinetCount=0;
                //满电电池数
                Integer fullyElectricityBatteryCount=0;
                List<StoreBindElectricityCabinet> storeBindElectricityCabinetList=storeBindElectricityCabinetService.queryByStoreId(e.getId());
                if(ObjectUtil.isNotEmpty(storeBindElectricityCabinetList)){
                    for (StoreBindElectricityCabinet storeBindElectricityCabinet:storeBindElectricityCabinetList) {
                        ElectricityCabinet electricityCabinet=electricityCabinetService.queryByIdFromCache(storeBindElectricityCabinet.getElectricityCabinetId());
                        if(Objects.nonNull(electricityCabinet)){
                            //动态查询在线状态
                            boolean result = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
                            if (result) {
                                onlineElectricityCabinetCount=onlineElectricityCabinetCount+1;
                                Integer fullyElectricityBattery = electricityCabinetService.queryFullyElectricityBattery(electricityCabinet.getId()).get(1);
                                fullyElectricityBatteryCount = fullyElectricityBatteryCount + fullyElectricityBattery;
                            }
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
    public R bindElectricityCabinet(StoreBindElectricityCabinetQuery storeBindElectricityCabinetQuery) {
        //先删除
        storeBindElectricityCabinetService.deleteByStoreId(storeBindElectricityCabinetQuery.getStoreId());
        if (ObjectUtil.isEmpty(storeBindElectricityCabinetQuery.getElectricityCabinetIdList())) {
            return R.ok();
        }
        //再新增
        for (Integer electricityCabinetId : storeBindElectricityCabinetQuery.getElectricityCabinetIdList()) {
            StoreBindElectricityCabinet storeBindElectricityCabinet = new StoreBindElectricityCabinet();
            storeBindElectricityCabinet.setStoreId(storeBindElectricityCabinetQuery.getStoreId());
            storeBindElectricityCabinet.setElectricityCabinetId(electricityCabinetId);
            storeBindElectricityCabinetService.insert(storeBindElectricityCabinet);
        }
        return R.ok();
    }


    @Override
    public R listByFranchisee(StoreQuery storeQuery) {
        Page page = PageUtil.getPage(storeQuery.getOffset(), storeQuery.getSize());
        storeMapper.listByFranchisee(page, storeQuery);
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
    public R getElectricityCabinetList(Integer id) {
        return R.ok(storeBindElectricityCabinetService.queryByStoreId(id));
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
}