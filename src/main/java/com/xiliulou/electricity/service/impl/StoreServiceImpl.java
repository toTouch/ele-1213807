package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.StoreMapper;
import com.xiliulou.electricity.query.ElectricityCabinetAddAndUpdate;
import com.xiliulou.electricity.query.StoreAddAndUpdate;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.MapVo;
import com.xiliulou.electricity.vo.StoreVO;
import com.xiliulou.electricity.web.query.AdminUserQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
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
            storeTagService.batchInsert(this.buildStoreTags(store,storeAddAndUpdate));

            //保存门店详情
            storeDetailService.insert(this.buildStoreDetail(store,storeAddAndUpdate));

            //保存门店图片
            pictureService.batchInsert(this.buildStorePicture(store,storeAddAndUpdate));
            
            //保存用户数据可见范围
            UserDataScope userDataScope = new UserDataScope();
            userDataScope.setUid(store.getUid());
            userDataScope.setDataId(store.getId());
            userDataScopeService.insert(userDataScope);
    
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
        if(!Objects.equals(oldStore.getTenantId(),TenantContextHolder.getTenantId())){
            return R.ok();
        }
        if (Objects.nonNull(storeAddAndUpdate.getBusinessTimeType())) {
            if (checkParam(storeAddAndUpdate, store)) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }
        }

        store.setUpdateTime(System.currentTimeMillis());
        int update = storeMapper.updateById(store);
        DbUtils.dbOperateSuccessThen(update, () -> {

            //保存门店标签
            storeTagService.deleteByStoreId(store.getId());
            storeTagService.batchInsert(this.buildStoreTags(store,storeAddAndUpdate));

            //保存门店详情
            storeDetailService.deleteByStoreId(store.getId());
            storeDetailService.insert(this.buildStoreDetail(store,storeAddAndUpdate));

            //保存门店图片
            pictureService.deleteByBusinessId(store.getId());
            pictureService.batchInsert(this.buildStorePicture(store,storeAddAndUpdate));

            //更新缓存
            redisService.saveWithHash(CacheConstant.CACHE_STORE + store.getId(), store);
            return null;
        });

        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R delete(Long id) {

        Store store = queryByIdFromCache(id);
        if (Objects.isNull(store)) {
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        if(!Objects.equals(store.getTenantId(),TenantContextHolder.getTenantId())){
            return R.ok();
        }

        //查询门店是否绑定换电柜
        Integer count = electricityCabinetService.queryCountByStoreId(store.getId());

        if (count > 0) {
            return R.fail("门店已绑定换电柜");
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

            //删除门店图片
            pictureService.deleteByBusinessId(store.getId());

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

                //用户
                if (Objects.nonNull(e.getUid())) {
                    User user = userService.queryByUidFromCache(e.getUid());
                    if (Objects.nonNull(user)) {
                        e.setUserName(user.getName());
                    }
                }

                //加盟商
                if (Objects.nonNull(e.getFranchiseeId())) {
                    Franchisee franchisee = franchiseeService.queryByIdFromDB(e.getFranchiseeId());
                    if (Objects.nonNull(franchisee)) {
                        e.setFranchiseeName(franchisee.getName());
                    }
                }
            });
        }
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
        if(!Objects.equals(oldStore.getTenantId(),TenantContextHolder.getTenantId())){
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


    private void checkCupboardStatusAndUpdateDiff(boolean isOnline, ElectricityCabinet electricityCabinet) {
        if (!isOnline && isCupboardAttrIsOnline(electricityCabinet) || isOnline && !isCupboardAttrIsOnline(electricityCabinet)) {
            ElectricityCabinet update = new ElectricityCabinet();
            update.setId(electricityCabinet.getId());
            update.setOnlineStatus(isOnline ? ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS : ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
            electricityCabinetService.idempotentUpdateCupboard(electricityCabinet, update);
        }
    }

    private List<StoreTag> buildStoreTags(Store store,StoreAddAndUpdate storeAddAndUpdate){
        List<StoreTag> list=new ArrayList<>();
        String serviceType = storeAddAndUpdate.getServiceType();
        if(StringUtils.isBlank(serviceType)){
            return list;
        }

        List<StoreTag> storeTags = JsonUtil.fromJsonArray(serviceType, StoreTag.class);
        if(CollectionUtils.isEmpty(storeTags)){
            return list;
        }

        return storeTags.parallelStream().peek(item->{
            item.setStoreId(store.getId());
            item.setStatus(StoreTag.STATUS_ENABLE);
            item.setDelFlag(StoreTag.DEL_NORMAL);
            item.setTenantId(store.getTenantId());
            item.setCreateTime(System.currentTimeMillis());
            item.setUpdateTime(System.currentTimeMillis());
        }).collect(Collectors.toList());
    }

    private StoreDetail buildStoreDetail(Store store,StoreAddAndUpdate storeAddAndUpdate){
        StoreDetail storeDetail=null;
        if (StringUtils.isBlank(storeAddAndUpdate.getDetail())) {
            return storeDetail;
        }
        storeDetail=new StoreDetail();
        storeDetail.setStoreId(store.getId());
        storeDetail.setDetail(storeAddAndUpdate.getDetail());
        storeDetail.setStatus(StoreDetail.STATUS_ENABLE);
        storeDetail.setDelFlag(StoreDetail.DEL_NORMAL);
        storeDetail.setTenantId(store.getTenantId());
        storeDetail.setCreateTime(System.currentTimeMillis());
        storeDetail.setUpdateTime(System.currentTimeMillis());
        return storeDetail;
    }

    private List<Picture> buildStorePicture(Store store,StoreAddAndUpdate storeAddAndUpdate){
        List<Picture> list=new ArrayList<>();
        String pictures = storeAddAndUpdate.getPictureList();
        if(StringUtils.isBlank(pictures)){
            return list;
        }

        List<Picture> pictureList = JsonUtil.fromJsonArray(pictures, Picture.class);
        if(CollectionUtils.isEmpty(pictureList)){
            return list;
        }

        return pictureList.parallelStream().peek(item->{
            Picture picture = new Picture();
            picture.setBusinessId(store.getId());
            picture.setStatus(Picture.STATUS_ENABLE);
            picture.setDelFlag(Picture.DEL_NORMAL);
            picture.setTenantId(store.getTenantId());
            picture.setCreateTime(System.currentTimeMillis());
            picture.setUpdateTime(System.currentTimeMillis());
        }).collect(Collectors.toList());
    }

}
