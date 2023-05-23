package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mysql.cj.x.protobuf.MysqlxExpr;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.StoreMapper;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.MapVo;
import com.xiliulou.electricity.vo.PictureVO;
import com.xiliulou.electricity.vo.SearchVo;
import com.xiliulou.electricity.vo.StoreVO;
import com.xiliulou.electricity.web.query.AdminUserQuery;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    StoreAmountService storeAmountService;
    @Autowired
    RoleService roleService;
    @Autowired
    UserDataScopeService userDataScopeService;
    @Autowired
    StoreTagService storeTagService;
    @Autowired
    StoreDetailService storeDetailService;
    @Autowired
    PictureService pictureService;
    @Autowired
    StorageConfig storageConfig;
    @Qualifier("aliyunOssService")
    @Autowired
    StorageService storageService;
    @Autowired
    ElectricityCarModelService electricityCarModelService;
    @Autowired
    ElectricityConfigService electricityConfigService;

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Store queryByIdFromCache(Long id) {
        Store cacheStore = redisService.getWithHash(CacheConstant.CACHE_STORE + id, Store.class);
        if (Objects.nonNull(cacheStore)) {
            return cacheStore;
        }
        Store store = storeMapper.selectById(id);
        if (Objects.isNull(store)) {
            return null;
        }
        redisService.saveWithHash(CacheConstant.CACHE_STORE + id, store);
        return store;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R save(StoreAddAndUpdate storeAddAndUpdate) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();


        //新增加盟商新增用户
        AdminUserQuery adminUserQuery = new AdminUserQuery();
        BeanUtil.copyProperties(storeAddAndUpdate, adminUserQuery);

        adminUserQuery.setUserType(User.TYPE_USER_NORMAL_ADMIN);
        adminUserQuery.setDataType(User.DATA_TYPE_STORE);
        if (!Objects.equals(tenantId, 1)) {
            //普通租户新增加盟商
            //1、查普通租户加盟商角色
            Long roleId = roleService.queryByName(Role.ROLE_STORE_USER_NAME, tenantId);
            if (Objects.nonNull(roleId)) {
                adminUserQuery.setRoleId(roleId);
            }

        }
        adminUserQuery.setLang(User.DEFAULT_LANG);
        adminUserQuery.setGender(User.GENDER_FEMALE);
        adminUserQuery.setPhone(storeAddAndUpdate.getServicePhone());

        R result = userService.addInnerUser(adminUserQuery);
        if (result.getCode() == 1) {
            return result;
        }

        Long uid = (Long) result.getData();


        Store store = new Store();
        BeanUtil.copyProperties(storeAddAndUpdate, store);
        store.setCid(storeAddAndUpdate.getCityId());

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
        store.setPayType(Store.DEL_NORMAL);
        store.setTenantId(tenantId);
        store.setUid(uid);

        int insert = storeMapper.insert(store);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //新增缓存
            redisService.saveWithHash(CacheConstant.CACHE_STORE + store.getId(), store);

            //新增门店账户
            StoreAmount storeAmount = StoreAmount.builder()
                    .storeId(store.getId())
                    .delFlag(StoreAmount.DEL_NORMAL)
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis())
                    .uid(uid)
                    .balance(BigDecimal.valueOf(0.0))
                    .totalIncome(BigDecimal.valueOf(0.0))
                    .withdraw(BigDecimal.valueOf(0.0))
                    .tenantId(tenantId)
                    .build();
            storeAmountService.insert(storeAmount);

            //保存门店标签
            storeTagService.batchInsert(this.buildStoreTags(store, storeAddAndUpdate));

            //保存门店详情
            storeDetailService.insert(this.buildStoreDetail(store, storeAddAndUpdate));

            //保存用户数据可见范围
            UserDataScope userDataScope = new UserDataScope();
            userDataScope.setUid(store.getUid());
            userDataScope.setDataId(store.getId());
            userDataScopeService.insert(userDataScope);

            return null;
        });

        if (insert > 0) {
            return R.ok(store.getId());
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> edit(StoreAddAndUpdate storeAddAndUpdate) {

        Store store = new Store();
        BeanUtil.copyProperties(storeAddAndUpdate, store);
        Store oldStore = queryByIdFromCache(store.getId());
        if (Objects.isNull(oldStore)) {
            return Triple.of(false,"ELECTRICITY.0018", "未找到门店");
        }
        if (!Objects.equals(oldStore.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true,"",null);
        }
        if (Objects.nonNull(storeAddAndUpdate.getBusinessTimeType())) {
            if (checkParam(storeAddAndUpdate, store)) {
                return Triple.of(false,"ELECTRICITY.0007", "不合法的参数");
            }
        }

        //若修改门店加盟商，需要判断门店是否绑定的有车辆型号
        if(Objects.nonNull(storeAddAndUpdate.getFranchiseeId()) && !Objects.equals(oldStore.getFranchiseeId().intValue(),storeAddAndUpdate.getFranchiseeId())){

            ElectricityCarModelQuery carModelQuery = new ElectricityCarModelQuery();
            carModelQuery.setStoreId(store.getId());
            carModelQuery.setDelFlag(ElectricityCarModel.DEL_NORMAL);
            carModelQuery.setTenantId(store.getTenantId());

            List<ElectricityCarModel> electricityCarModels = electricityCarModelService.selectByQuery(carModelQuery);
            if(!CollectionUtils.isEmpty(electricityCarModels)){
                return Triple.of(false,"100254","门店已绑定车辆型号，请先删除车辆型号");
            }
        }

//        //校验加盟商
//        Triple<Boolean, String, Object> verifyCarModelFranchisee = verifyCarModelFranchisee(storeAddAndUpdate, oldStore);
//        List<ElectricityCarModel> electricityCarModels = null;
//        if (!verifyCarModelFranchisee.getLeft()) {
//            return verifyCarModelFranchisee;
//        } else {
//            electricityCarModels = (List<ElectricityCarModel>) verifyCarModelFranchisee.getRight();
//            if (!CollectionUtils.isEmpty(electricityCarModels)) {
//                electricityCarModelService.updateFranchiseeById(electricityCarModels, storeAddAndUpdate.getFranchiseeId().longValue());
//            }
//        }

        store.setTenantId(TenantContextHolder.getTenantId());
        store.setUpdateTime(System.currentTimeMillis());
        int update = storeMapper.updateById(store);
        DbUtils.dbOperateSuccessThen(update, () -> {

            //保存门店标签
            storeTagService.deleteByStoreId(store.getId());
            storeTagService.batchInsert(this.buildStoreTags(store, storeAddAndUpdate));

            //保存门店详情
            storeDetailService.deleteByStoreId(store.getId());
            storeDetailService.insert(this.buildStoreDetail(store, storeAddAndUpdate));

            //更新缓存
            redisService.delete(CacheConstant.CACHE_STORE + store.getId());
            return null;
        });

        if (update > 0) {
            return Triple.of(true,"",null);
        }
        return Triple.of(false,"ELECTRICITY.0086", "操作失败");
    }

//    private Triple<Boolean, String, Object> verifyCarModelFranchisee(StoreAddAndUpdate storeAddAndUpdate, Store oldStore) {
//        //若修改门店加盟商，需要判断门店是否绑定的有车辆型号
//        if (Objects.nonNull(storeAddAndUpdate.getFranchiseeId()) && !Objects.equals(oldStore.getFranchiseeId().intValue(), storeAddAndUpdate.getFranchiseeId())) {
//            ElectricityCarModelQuery carModelQuery = new ElectricityCarModelQuery();
//            carModelQuery.setStoreId(oldStore.getId());
//            carModelQuery.setDelFlag(ElectricityCarModel.DEL_NORMAL);
//            carModelQuery.setTenantId(oldStore.getTenantId());
//
//            //获取门店下车辆型号
//            List<ElectricityCarModel> electricityCarModels = electricityCarModelService.selectByQuery(carModelQuery);
//            if (CollectionUtils.isEmpty(electricityCarModels)) {
//                return Triple.of(true, "", null);
//            }
//
//            //若开启了加盟商迁移，且门店新绑定的加盟商与迁移的新加盟商一致，则允许修改
//            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
//            if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsMoveFranchisee(), ElectricityConfig.MOVE_FRANCHISEE_OPEN)) {
//
//                FranchiseeMoveInfo franchiseeMoveInfo = JsonUtil.fromJson(electricityConfig.getFranchiseeMoveInfo(), FranchiseeMoveInfo.class);
//                if (Objects.isNull(franchiseeMoveInfo)) {
//                    log.error("ELE ERROR!not found franchiseeMoveInfo,tenantId={}", TenantContextHolder.getTenantId());
//                    return Triple.of(false, "100354", "用户加盟商迁移配置信息不存在");
//                }
//
//                if (Objects.equals(franchiseeMoveInfo.getToFranchiseeId().intValue(), storeAddAndUpdate.getFranchiseeId())) {
//                    //门店新绑定的加盟商与迁移的新加盟商一致，则允许修改
//                    return Triple.of(true, "", electricityCarModels);
//                } else {
//                    return Triple.of(false, "100254", "门店已绑定车辆型号，请先删除车辆型号");
//                }
//            } else {
//                return Triple.of(false, "100254", "门店已绑定车辆型号，请先删除车辆型号");
//            }
//        }
//
//        return Triple.of(true, "", null);
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R delete(Long id) {

        Store store = queryByIdFromCache(id);
        if (Objects.isNull(store)) {
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        if (!Objects.equals(store.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }

        //查询门店是否绑定换电柜
        Integer count = electricityCabinetService.queryCountByStoreId(store.getId());

        if (count > 0) {
            return R.fail("门店已绑定换电柜");
        }

        //查询门店是否绑定车辆型号 TODO 优化
        ElectricityCarModelQuery carModelQuery = new ElectricityCarModelQuery();
        carModelQuery.setStoreId(store.getId());
        carModelQuery.setDelFlag(ElectricityCarModel.DEL_NORMAL);
        carModelQuery.setTenantId(store.getTenantId());
        List<ElectricityCarModel> electricityCarModels = electricityCarModelService.selectByQuery(carModelQuery);
        if(!CollectionUtils.isEmpty(electricityCarModels)){
            return R.fail("100254","门店已绑定车辆型号，请先删除车辆型号");
        }

        store.setUpdateTime(System.currentTimeMillis());
        store.setDelFlag(ElectricityCabinet.DEL_DEL);

        int update = storeMapper.updateById(store);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //删除缓存
            redisService.delete(CacheConstant.CACHE_STORE + id);
            //删除用户
            userService.deleteInnerUser(store.getUid());

            //删除门店账号
            storeAmountService.deleteByStoreId(id);

            //删除门店标签
            storeTagService.deleteByStoreId(store.getId());

            //删除门店详情
            storeDetailService.deleteByStoreId(store.getId());

            return null;
        });


        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    @Override
    @Slave
    public R queryList(StoreQuery storeQuery) {
        List<StoreVO> storeVOList = storeMapper.queryList(storeQuery);
        if (CollectionUtils.isEmpty(storeVOList)) {
            return R.ok(Collections.EMPTY_LIST);
        }

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

            //用户
            if (Objects.nonNull(e.getUid())) {
                User user = userService.queryByUidFromCache(e.getUid());
                if (Objects.nonNull(user)) {
                    e.setUserName(user.getName());
                }
            }

            //加盟商
            if (Objects.nonNull(e.getFranchiseeId())) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(e.getFranchiseeId());
                if (Objects.nonNull(franchisee)) {
                    e.setFranchiseeName(franchisee.getName());
                }
            }

            //标签
            List<StoreTag> storeTags = storeTagService.selectByStoreId(e.getId());
            if (!CollectionUtils.isEmpty(storeTags)) {
                List<String> tags = storeTags.stream().map(StoreTag::getTitle).collect(Collectors.toList());
                e.setServiceType(tags);
            }

            //详情
            StoreDetail storeDetail = storeDetailService.selectByStoreId(e.getId());
            if (Objects.nonNull(storeDetail)) {
                e.setDetail(storeDetail.getDetail());
            }
        });

        storeVOList.stream().sorted(Comparator.comparing(StoreVO::getCreateTime).reversed()).collect(Collectors.toList());
        return R.ok(storeVOList);
    }

    @Override
    @Transactional
    public R updateStatus(Long id, Integer usableStatus) {

        Store oldStore = queryByIdFromCache(id);
        if (Objects.isNull(oldStore)) {
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        if (!Objects.equals(oldStore.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }

        Store store = new Store();
        store.setId(id);
        store.setUpdateTime(System.currentTimeMillis());
        store.setUsableStatus(usableStatus);
        int update = storeMapper.updateById(store);


        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.saveWithHash(CacheConstant.CACHE_STORE + store.getId(), store);
            return null;
        });
        return R.ok();
    }

    @Override
    public Integer homeOne(List<Long> storeIdList, Integer tenantId) {
        return storeMapper.homeOne(storeIdList, tenantId);
    }

    @Override
    public R showInfoByDistance(StoreQuery storeQuery) {
        List<StoreVO> storeVOList = storeMapper.showInfoByDistance(storeQuery);
        List<StoreVO> storeVOs = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(storeVOList)) {
            storeVOList.parallelStream().forEach(e -> {

                //营业时间
                parseBusinessTime(e);

                //满电电池数
                Integer fullyElectricityBatteryCount = 0;
                List<ElectricityCabinet> electricityCabinetList = electricityCabinetService.queryByStoreId(e.getId());
                if (ObjectUtil.isNotEmpty(electricityCabinetList)) {
                    for (ElectricityCabinet electricityCabinet : electricityCabinetList) {
                        Integer fullyElectricityBattery = electricityCabinetService.queryFullyElectricityBattery(electricityCabinet.getId(), null);
                        fullyElectricityBatteryCount = fullyElectricityBatteryCount + fullyElectricityBattery;
                    }
                }
                e.setFullyElectricityBattery(fullyElectricityBatteryCount);
                storeVOs.add(e);
            });
        }
        return R.ok(storeVOs.stream().sorted(Comparator.comparing(StoreVO::getDistance)).collect(Collectors.toList()));
    }

    @Override
    public List<Store> queryByFranchiseeId(Long id) {
        return storeMapper.selectList(new LambdaQueryWrapper<Store>().eq(Store::getFranchiseeId, id).eq(Store::getDelFlag, Store.DEL_NORMAL));
    }

    @Override
    @Slave
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
    public List<Long> queryStoreIdsByProvinceIdOrCityId(Integer tenantId, Integer pid, Integer cid) {
        return storeMapper.queryStoreIdsByProvinceId(tenantId, pid, cid);
    }

    @Override
    public List<MapVo> queryCountGroupByCityId(Integer tenantId, Integer pid) {
        return storeMapper.queryCountGroupByCityId(tenantId, pid);
    }

    @Override
    public List<HashMap<String, String>> homeThree(Long startTimeMilliDay, Long endTimeMilliDay, List<Long> storeIdList, Integer tenantId) {
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
                redisService.delete(CacheConstant.CACHE_STORE + store.getId());
                return null;
            });
        }
    }

    @Override
    public Integer queryCountByFranchiseeId(Long id) {
        return storeMapper.selectCount(new LambdaQueryWrapper<Store>().eq(Store::getFranchiseeId, id).eq(Store::getDelFlag, Store.DEL_NORMAL).last("limit 0,1"));
    }

    @Override
    public Integer queryCountByFranchisee(Long uid) {
        Store store = queryByUid(uid);

        if (Objects.isNull(store)) {
            return 0;
        }

        return electricityCabinetService.queryCountByStoreId(store.getId());
    }

    @Override
    public void updateById(Store store) {
        int update = storeMapper.update(store);


        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.delete(CacheConstant.CACHE_STORE + store.getId());
            return null;
        });
    }

    @Override
    public List<MapVo> queryCountGroupByProvinceId(Integer tenantId) {
        return storeMapper.queryCountGroupByProvinceId(tenantId);
    }

    @Override
    public List<Store> selectByFranchiseeId(Long id) {
        return storeMapper.selectList(new LambdaQueryWrapper<Store>().eq(Store::getFranchiseeId, id).eq(Store::getDelFlag, Store.DEL_NORMAL));
    }

    @Override
    public Integer queryCountForHomePage(StoreQuery storeQuery) {
        return storeMapper.queryCount(storeQuery);
    }

    @Override
    public List<Long> queryStoreIdByFranchiseeId(List<Long> id) {
        return storeMapper.queryStoreIdByFranchiseeId(id);
    }

    @Override
    public List<Store> selectByFranchiseeIds(List<Long> franchiseeIds) {
        return storeMapper.selectList(new LambdaQueryWrapper<Store>().in(Store::getFranchiseeId, franchiseeIds).eq(Store::getDelFlag, Store.DEL_NORMAL));
    }

    @Override
    public Triple<Boolean, String, Object> selectListByQuery(StoreQuery storeQuery) {
        List<Store> stores = storeMapper.selectListByQuery(storeQuery);
        if (CollectionUtils.isEmpty(stores)) {
            return Triple.of(true, "", Collections.EMPTY_LIST);
        }

        return Triple.of(true, "", stores);
    }

    @Override
    public List<Store> selectByStoreIds(List<Long> storeIds) {
        return storeMapper.selectList(new LambdaQueryWrapper<Store>().in(Store::getId, storeIds).eq(Store::getDelFlag, Store.DEL_NORMAL));
    }

    /**
     * 检查是否有门店绑定加盟商
     * @param id
     * @param tenantId
     * @return
     */
    @Override
    public Integer isStoreBindFranchinsee(Long id, Integer tenantId) {
        return storeMapper.isStoreBindFranchinsee(id, tenantId);
    }


    @Override
    public Store queryFromCacheByProductAndDeviceName(String productKey, String deviceName) {
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELE ERROR! not found electricityCabinet,productKey={},deviceName={}", productKey, deviceName);
            return null;
        }

        if (Objects.isNull(electricityCabinet.getStoreId())) {
            log.error("ELE ERROR! not found store,electricityCabinetId={}", electricityCabinet.getId());
            return null;
        }
        return this.queryByIdFromCache(electricityCabinet.getStoreId());
    }

    @Override
    public List<StoreVO> selectListByDistance(StoreQuery storeQuery) {
        List<StoreVO> list = storeMapper.selectListByDistance(storeQuery);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }

        return list.parallelStream().map(item -> {

            List<Picture> pictures = pictureService.selectByByBusinessId(item.getId());
            if (!CollectionUtils.isEmpty(pictures)) {
                item.setPictureList(pictureService.pictureParseVO(pictures));
            }

            return item;
        }).collect(Collectors.toList());

    }

    @Override
    public StoreVO selectDetailById(Long id) {
        StoreVO storeVO = new StoreVO();

        Store store = this.queryByIdFromCache(id);
        if (Objects.isNull(store) || !Objects.equals(TenantContextHolder.getTenantId(), store.getTenantId())) {
            return storeVO;
        }

        BeanUtils.copyProperties(store, storeVO);

        List<Picture> pictures = pictureService.selectByByBusinessId(id);
        if (!CollectionUtils.isEmpty(pictures)) {
            storeVO.setPictureList(pictureService.pictureParseVO(pictures));
        }

        StoreDetail storeDetail = storeDetailService.selectByStoreId(id);
        if (Objects.nonNull(storeDetail)) {
            storeVO.setDetail(storeDetail.getDetail());
        }

        List<StoreTag> storeTags = storeTagService.selectByStoreId(id);
        if (!CollectionUtils.isEmpty(storeTags)) {
            List<String> tags = storeTags.stream().map(StoreTag::getTitle).collect(Collectors.toList());
            storeVO.setServiceType(tags);
        }

        parseBusinessTime(storeVO);

        return storeVO;
    }
    
    @Override
    public R storeSearch(Long size, Long offset, String name , Integer tenantId) {
        List<SearchVo> voList = storeMapper.storeSearch(size, offset, name , tenantId);
        return R.ok(voList);
    }
    

    @Slave
    @Override
    public List<StoreVO> selectByAddress(StoreQuery storeQuery) {
        List<StoreVO> stores = storeMapper.selectByAddress(storeQuery);
        if(CollectionUtils.isEmpty(stores)){
            return Collections.EMPTY_LIST;
        }
        return stores.parallelStream().map(item -> {

            List<Picture> pictures = pictureService.selectByByBusinessId(item.getId());
            if (!CollectionUtils.isEmpty(pictures)) {
                item.setPictureList(pictureService.pictureParseVO(pictures));
            }

            return item;
        }).collect(Collectors.toList());
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

    private boolean isCupboardAttrIsOnline(ElectricityCabinet electricityCabinet) {
        return ElectricityCabinet.IOT_STATUS_ONLINE.equalsIgnoreCase(electricityCabinet.getOnlineStatus().toString());
    }




    private List<StoreTag> buildStoreTags(Store store, StoreAddAndUpdate storeAddAndUpdate) {
        List<StoreTag> list = new ArrayList<>();
        String serviceType = storeAddAndUpdate.getServiceType();
        if (StringUtils.isBlank(serviceType)) {
            return list;
        }

        List<String> storeTags = JsonUtil.fromJsonArray(serviceType, String.class);
        if (CollectionUtils.isEmpty(storeTags)) {
            return list;
        }

        for (int i = 0; i < storeTags.size(); i++) {
            StoreTag storeTag = new StoreTag();
            storeTag.setSeq(i);
            storeTag.setTitle(storeTags.get(i));
            storeTag.setStoreId(store.getId());
            storeTag.setStatus(StoreTag.STATUS_ENABLE);
            storeTag.setDelFlag(StoreTag.DEL_NORMAL);
            storeTag.setTenantId(store.getTenantId());
            storeTag.setCreateTime(System.currentTimeMillis());
            storeTag.setUpdateTime(System.currentTimeMillis());
            list.add(storeTag);
        }

        return list;
    }

    private StoreDetail buildStoreDetail(Store store, StoreAddAndUpdate storeAddAndUpdate) {
        StoreDetail storeDetail = null;
        if (StringUtils.isBlank(storeAddAndUpdate.getDetail())) {
            return storeDetail;
        }
        storeDetail = new StoreDetail();
        storeDetail.setStoreId(store.getId());
        storeDetail.setDetail(storeAddAndUpdate.getDetail());
        storeDetail.setStatus(StoreDetail.STATUS_ENABLE);
        storeDetail.setDelFlag(StoreDetail.DEL_NORMAL);
        storeDetail.setTenantId(store.getTenantId());
        storeDetail.setCreateTime(System.currentTimeMillis());
        storeDetail.setUpdateTime(System.currentTimeMillis());
        return storeDetail;
    }

    private void parseBusinessTime(StoreVO storeVO) {
        if (Objects.nonNull(storeVO.getBusinessTime())) {
            String businessTime = storeVO.getBusinessTime();
            if (Objects.equals(businessTime, StoreVO.ALL_DAY)) {
                storeVO.setBusinessTimeType(StoreVO.ALL_DAY);
                storeVO.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
            } else {
                storeVO.setBusinessTimeType(StoreVO.ILLEGAL_DATA);
                Integer index = businessTime.indexOf("-");
                if (!Objects.equals(index, -1) && index > 0) {
                    storeVO.setBusinessTimeType(ElectricityCabinetVO.CUSTOMIZE_TIME);
                    Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
                    Long beginTime = getTime(totalBeginTime);
                    Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
                    Long endTime = getTime(totalEndTime);
                    storeVO.setBeginTime(totalBeginTime);
                    storeVO.setEndTime(totalEndTime);
                    Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                    Long now = System.currentTimeMillis();
                    if (firstToday + beginTime > now || firstToday + endTime < now) {
                        storeVO.setIsBusiness(ElectricityCabinetVO.IS_NOT_BUSINESS);
                    } else {
                        storeVO.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
                    }
                }
            }
        }
    }

}
