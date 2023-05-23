package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.core.wp.entity.AppTemplateQuery;
import com.xiliulou.core.wp.service.WeChatAppTemplateService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.config.WechatTemplateNotificationConfig;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.dto.bms.BatteryInfoDto;
import com.xiliulou.electricity.dto.bms.BatteryTrackDto;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.EleBatteryQuery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.HomepageBatteryFrequencyQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.retrofit.BatteryPlatRetrofitService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.AESUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.electricity.web.query.battery.BatteryBatchOperateQuery;
import com.xiliulou.electricity.web.query.battery.BatteryInfoQuery;
import com.xiliulou.electricity.web.query.battery.BatteryLocationTrackQuery;
import com.xiliulou.electricity.web.query.battery.BatteryModifyQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 换电柜电池表(ElectricityBattery)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
@Service
@Slf4j
public class ElectricityBatteryServiceImpl extends ServiceImpl<ElectricityBatteryMapper, ElectricityBattery>
        implements ElectricityBatteryService {

    @Resource
    private ElectricityBatteryMapper electricitybatterymapper;

    @Autowired
    StoreService storeService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Autowired
    WechatTemplateNotificationConfig wechatTemplateNotificationConfig;

    @Autowired
    RedisService redisService;

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

    @Autowired
    UserOauthBindService userOauthBindService;

    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;

    @Autowired
    RentBatteryOrderService rentBatteryOrderService;

    @Autowired
    ElectricityConfigService electricityConfigService;

    @Autowired
    BatteryGeoService geoService;

    @Autowired
    BatteryModelService batteryModelService;


    @Autowired
    BatteryPlatRetrofitService batteryPlatRetrofitService;

    @Autowired
    TenantService tenantService;

    /**
     * 保存电池
     *
     * @param
     * @return
     */
    @Override
    @Transactional
    public R saveElectricityBattery(EleBatteryQuery query) {

        Integer tenantId = TenantContextHolder.getTenantId();

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Integer count = electricitybatterymapper.selectCount(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, query.getSn())
                        .eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL));
        if (count > 0) {
            return R.fail("100224", "该电池SN已存在!无法重复创建");
        }


        Long franchiseeId = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            Store store = storeService.queryByUid(user.getUid());
            if (Objects.nonNull(store)) {
                franchiseeId = store.getFranchiseeId();
            }
        }
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
            if (Objects.nonNull(franchisee)) {
                franchiseeId = franchisee.getId();
            }
        }

        Pair<Boolean, String> result = callBatteryPlatSaveSn(Collections.singletonList(query.getSn()), query.getIsNeedSync());
        if (!result.getKey()) {
            return R.fail("200005", result.getRight());
        }

        ElectricityBattery saveBattery = new ElectricityBattery();
        BeanUtils.copyProperties(query, saveBattery);
        saveBattery.setFranchiseeId(franchiseeId);
        saveBattery.setPhysicsStatus(ElectricityBattery.PHYSICS_STATUS_NOT_WARE_HOUSE);
        saveBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_INPUT);
        saveBattery.setCreateTime(System.currentTimeMillis());
        saveBattery.setUpdateTime(System.currentTimeMillis());
        saveBattery.setTenantId(tenantId);
        electricitybatterymapper.insert(saveBattery);

        return R.ok();
    }

    private Pair<Boolean, String> callBatteryPlatSaveSn(List<String> list, Integer isNeedSync) {
        if (Objects.isNull(isNeedSync)) {
            return Pair.of(true, null);
        }

        Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(tenant)) {
            return Pair.of(false, "租户信息不能为空");
        }

        Map<String, String> headers = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        headers.put(CommonConstant.INNER_HEADER_APP, CommonConstant.APP_SAAS);
        headers.put(CommonConstant.INNER_HEADER_TIME, time);
        headers.put(CommonConstant.INNER_HEADER_INNER_TOKEN, AESUtils.encrypt(time, CommonConstant.APP_SAAS_AES_KEY));
        headers.put(CommonConstant.INNER_TENANT_ID, tenant.getCode());

        BatteryBatchOperateQuery batteryBatchOperateQuery = new BatteryBatchOperateQuery();
        batteryBatchOperateQuery.setJsonBatterySnList(JsonUtil.toJson(list));


        R r = batteryPlatRetrofitService.batchSave(headers, batteryBatchOperateQuery);
        if (!r.isSuccess()) {
            log.error("CALL BATTERY ERROR! msg={},uid={}", r.getErrMsg(), SecurityUtils.getUid());
            return Pair.of(false, r.getErrMsg());
        }
        return Pair.of(true, null);
    }

    private Pair<Boolean, String> callBatteryPlatDeleteSn(List<String> list, Integer isNeedSync) {
        if (Objects.isNull(isNeedSync)) {
            return Pair.of(true, null);
        }


        Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(tenant)) {
            return Pair.of(false, "租户信息不能为空");
        }

        Map<String, String> headers = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        headers.put(CommonConstant.INNER_HEADER_APP, CommonConstant.APP_SAAS);
        headers.put(CommonConstant.INNER_HEADER_TIME, time);
        headers.put(CommonConstant.INNER_HEADER_INNER_TOKEN, AESUtils.encrypt(time, CommonConstant.APP_SAAS_AES_KEY));
        headers.put(CommonConstant.INNER_TENANT_ID, tenant.getCode());

        BatteryBatchOperateQuery batteryBatchOperateQuery = new BatteryBatchOperateQuery();
        batteryBatchOperateQuery.setJsonBatterySnList(JsonUtil.toJson(list));
        R r = batteryPlatRetrofitService.batchDelete(headers, batteryBatchOperateQuery);
        if (!r.isSuccess()) {
            log.error("CALL BATTERY ERROR! msg={},uid={}", r.getErrMsg(), SecurityUtils.getUid());
            return Pair.of(false, r.getErrMsg());
        }
        return Pair.of(true, null);
    }

    private Pair<Boolean, String> callBatteryPlatModify(String newSn, String oldSn, Integer isNeedSync) {
        if (Objects.isNull(isNeedSync)) {
            return Pair.of(true, null);
        }

        Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(tenant)) {
            return Pair.of(false, "租户信息不能为空");
        }

        Map<String, String> headers = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        headers.put(CommonConstant.INNER_HEADER_APP, CommonConstant.APP_SAAS);
        headers.put(CommonConstant.INNER_HEADER_TIME, time);
        headers.put(CommonConstant.INNER_HEADER_INNER_TOKEN, AESUtils.encrypt(time, CommonConstant.APP_SAAS_AES_KEY));
        headers.put(CommonConstant.INNER_TENANT_ID, tenant.getCode());

        BatteryModifyQuery query = new BatteryModifyQuery();
        query.setNewSn(newSn);
        query.setOriginalSn(oldSn);
        R r = batteryPlatRetrofitService.modifyBatterySn(headers, query);
        if (!r.isSuccess()) {
            log.error("CALL BATTERY ERROR! msg={},uid={}", r.getErrMsg(), SecurityUtils.getUid());
            return Pair.of(false, r.getErrMsg());
        }
        return Pair.of(true, null);
    }

    @Override
    public Triple<Boolean, String, Object> queryBatteryLocationTrack(Long uid, Long beginTime, Long endTime) {
        String sn = electricitybatterymapper.querySnByUid(uid);
        if (StrUtil.isEmpty(sn)) {
            return Triple.of(true, null, null);
        }

        BatteryLocationTrackQuery query = new BatteryLocationTrackQuery();
        query.setSn(sn);
        query.setBeginTime(beginTime);
        query.setEndTime(endTime);

        Triple<Boolean, String, List<BatteryTrackDto>> result = callBatteryServiceQueryBatteryTrack(query);
        if (!result.getLeft() || Objects.isNull(result.getRight())) {
            log.error("CALL BATTERY ERROR! uid={},msg={}", uid, result.getMiddle());
            return Triple.of(false, "200005", result.getMiddle());
        }


        return Triple.of(true, null, result.getRight());
    }

    private Triple<Boolean, String, List<BatteryTrackDto>> callBatteryServiceQueryBatteryTrack(BatteryLocationTrackQuery batteryLocationTrackQuery) {
        Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(tenant)) {
            return Triple.of(false, "租户信息不能为空", null);
        }

        Map<String, String> headers = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        headers.put(CommonConstant.INNER_HEADER_APP, CommonConstant.APP_SAAS);
        headers.put(CommonConstant.INNER_HEADER_TIME, time);
        headers.put(CommonConstant.INNER_HEADER_INNER_TOKEN, AESUtils.encrypt(time, CommonConstant.APP_SAAS_AES_KEY));
        headers.put(CommonConstant.INNER_TENANT_ID, tenant.getCode());

        R<List<BatteryTrackDto>> r = batteryPlatRetrofitService.queryBatteryTrack(headers, batteryLocationTrackQuery);
        if (!r.isSuccess()) {
            log.error("CALL BATTERY ERROR! msg={},uid={}", r.getErrMsg(), SecurityUtils.getUid());
            return Triple.of(false, r.getErrMsg(), null);
        }
        return Triple.of(true, null, r.getData());
    }


    private Triple<Boolean, String, BatteryInfoDto> callBatteryServiceQueryBatteryInfo(BatteryInfoQuery batteryInfoQuery) {
        Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(tenant)) {
            return Triple.of(false, "租户信息不能为空", null);
        }

        Map<String, String> headers = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        headers.put(CommonConstant.INNER_HEADER_APP, CommonConstant.APP_SAAS);
        headers.put(CommonConstant.INNER_HEADER_TIME, time);
        headers.put(CommonConstant.INNER_HEADER_INNER_TOKEN, AESUtils.encrypt(time, CommonConstant.APP_SAAS_AES_KEY));
        headers.put(CommonConstant.INNER_TENANT_ID, tenant.getCode());

        R<BatteryInfoDto> r = batteryPlatRetrofitService.queryBatteryInfo(headers, batteryInfoQuery);
        if (!r.isSuccess()) {
            log.error("CALL BATTERY ERROR! msg={},uid={}", r.getErrMsg(), SecurityUtils.getUid());
            return Triple.of(false, r.getErrMsg(), null);
        }
        return Triple.of(true, null, r.getData());


    }


    /**
     * 修改电池
     *
     * @param electricityBattery
     * @return
     */
    @Override
    public Integer update(ElectricityBattery electricityBattery) {
        return electricitybatterymapper.update(electricityBattery);
    }

    @Override
    public R updateForAdmin(EleBatteryQuery eleQuery) {
        ElectricityBattery electricityBatteryDb = electricitybatterymapper.selectOne(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getId, eleQuery.getId())
                        .eq(ElectricityBattery::getTenantId, TenantContextHolder.getTenantId())
                        .eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL));
        if (Objects.isNull(electricityBatteryDb)) {
            log.error("ELE ERROR, not found electricity battery id={}", eleQuery.getId());
            return R.fail("ELECTRICITY.0020", "电池不存在!");
        }

        Integer count = electricitybatterymapper.selectCount(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, eleQuery.getSn())
                        .eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL)
                        .ne(ElectricityBattery::getId, eleQuery.getId()));
        if (count > 0) {
            return R.fail("电池编号已绑定其他电池!");
        }

        if (!eleQuery.getSn().equalsIgnoreCase(electricityBatteryDb.getSn())) {
            Pair<Boolean, String> result = callBatteryPlatModify(eleQuery.getSn(), electricityBatteryDb.getSn(), eleQuery.getIsNeedSync());
            if (!result.getKey()) {
                return R.fail("200005", result.getRight());
            }
        }

        ElectricityBattery updateBattery = new ElectricityBattery();
        BeanUtil.copyProperties(eleQuery, updateBattery);
        updateBattery.setUpdateTime(System.currentTimeMillis());
        updateBattery.setTenantId(TenantContextHolder.getTenantId());
        Integer rows = electricitybatterymapper.update(updateBattery);
        if (rows > 0) {
            redisService.delete(CacheConstant.CACHE_BT_ATTR + electricityBatteryDb.getSn());
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
    @Slave
    public R queryList(ElectricityBatteryQuery electricityBatteryQuery, Long offset, Long size) {
        List<ElectricityBattery> electricityBatteryList = electricitybatterymapper.queryList(electricityBatteryQuery,
                offset, size);
        if (ObjectUtil.isEmpty(electricityBatteryList)) {
            return R.ok(CollectionUtils.EMPTY_COLLECTION);
        }

        List<ElectricityBatteryVO> electricityBatteryVOList = new ArrayList<>();

        /*List<FranchiseeBindElectricityBattery> franchiseeBindElectricityBatteryList = new ArrayList<>();
        if (Objects.nonNull(electricityBatteryQuery.getFranchiseeId())) {
            franchiseeBindElectricityBatteryList = franchiseeBindElectricityBatteryService.queryByFranchiseeId(electricityBatteryQuery.getFranchiseeId());
        }*/

        for (ElectricityBattery electricityBattery : electricityBatteryList) {

            ElectricityBatteryVO electricityBatteryVO = new ElectricityBatteryVO();
            BeanUtil.copyProperties(electricityBattery, electricityBatteryVO);

            if (Objects.equals(electricityBattery.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE)
                    && Objects.nonNull(electricityBattery.getUid())) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(electricityBattery.getUid());
                if (Objects.nonNull(userInfo)) {
                    electricityBatteryVO.setUserName(userInfo.getName());
                }
            }

            if (Objects.equals(electricityBattery.getPhysicsStatus(), ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE)
                    && Objects.nonNull(electricityBattery.getElectricityCabinetId())) {
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(
                        electricityBattery.getElectricityCabinetId());
                if (Objects.nonNull(electricityCabinet)) {
                    electricityBatteryVO.setElectricityCabinetName(electricityCabinet.getName());
                }
            }

            Franchisee franchisee = franchiseeService.queryByIdFromDB(electricityBattery.getFranchiseeId());
            electricityBatteryVO.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());

            electricityBatteryVOList.add(electricityBatteryVO);
        }
        return R.ok(electricityBatteryVOList);
    }

    @Override
    @Slave
    public R queryBindListByPage(Long offset, Long size, Long franchiseeId) {

        List<ElectricityBatteryVO> batteryVOList = new ArrayList<>();

        //没有绑定加盟商的电池
        List<ElectricityBattery> notBindList = electricitybatterymapper.queryNotBindList(offset, size,
                TenantContextHolder.getTenantId());
        //当前加盟商绑定的电池
        List<ElectricityBattery> bindList = electricitybatterymapper.queryBindList(offset, size, franchiseeId,
                TenantContextHolder.getTenantId());

        if (CollectionUtils.isNotEmpty(notBindList)) {
            notBindList.forEach(item -> {
                ElectricityBatteryVO batteryVO = new ElectricityBatteryVO();
                BeanUtils.copyProperties(item, batteryVO);
                batteryVO.setIsBind(false);
                batteryVOList.add(batteryVO);
            });
        }

        if (CollectionUtils.isNotEmpty(bindList)) {
            bindList.forEach(item -> {
                ElectricityBatteryVO batteryVO = new ElectricityBatteryVO();
                BeanUtils.copyProperties(item, batteryVO);
                batteryVO.setIsBind(true);
                batteryVOList.add(batteryVO);
            });
        }

        return R.ok(batteryVOList);
    }

    @Override
    public void insert(ElectricityBattery electricityBattery) {
        electricitybatterymapper.insert(electricityBattery);
    }

    @Override
    public Triple<Boolean, String, Object> queryInfoByUid(Long uid, Integer isNeedLocation) {
        String sn = electricitybatterymapper.querySnByUid(uid);
        if (StrUtil.isEmpty(sn)) {
            return Triple.of(true, null, null);
        }

        BatteryInfoQuery batteryInfoQuery = new BatteryInfoQuery();
        batteryInfoQuery.setSn(sn);

        //为空也需要查询路径，兼容旧版本
        if (Objects.isNull(isNeedLocation) || Objects.equals(isNeedLocation, BatteryInfoQuery.NEED)) {
            batteryInfoQuery.setNeedLocation(BatteryInfoQuery.NEED);

        }

        Triple<Boolean, String, BatteryInfoDto> result = callBatteryServiceQueryBatteryInfo(batteryInfoQuery);
        if (!result.getLeft()) {
            log.error("CALL BATTERY ERROR! uid={},msg={}", uid, result.getMiddle());
            return Triple.of(false, "200005", result.getMiddle());
        }

        if (Objects.isNull(result.getRight())) {
            log.error("BATTERY ERROR! not found bms'battery! uid={}", uid);
            return Triple.of(false, "200006", "该电池未录入电池服务平台");
        }

        ElectricityUserBatteryVo userBatteryVo = new ElectricityUserBatteryVo();
        userBatteryVo.setBatteryA(result.getRight().getBatteryA());
        userBatteryVo.setBatteryV(result.getRight().getBatteryV());
        userBatteryVo.setSn(sn);
        userBatteryVo.setLatitude(result.getRight().getLatitude());
        userBatteryVo.setLongitude(result.getRight().getLongitude());
        userBatteryVo.setPower(Double.valueOf(result.getRight().getSoc()));
        userBatteryVo.setUpdateTime(result.getRight().getUpdateTime());
        return Triple.of(true, null, userBatteryVo);
    }

    @Override
    public Integer querySumCount(ElectricityBatteryQuery electricityBatteryQuery) {
        return electricitybatterymapper.queryCount(electricityBatteryQuery);
    }

    @Override
    public BigEleBatteryVo queryMaxPowerByElectricityCabinetId(Integer electricityCabinetId) {
        return electricitybatterymapper.queryMaxPowerByElectricityCabinetId(electricityCabinetId);
    }

    @Override
    public R queryById(Long electricityBatteryId) {
        ElectricityBattery electricityBattery = electricitybatterymapper.selectById(electricityBatteryId,
                TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityBattery)) {
            log.error("ELE ERROR, not found electricity battery id={}", electricityBatteryId);
            return R.fail("ELECTRICITY.0020", "电池不存在!");
        }

        ElectricityBatteryVO electricityBatteryVO = new ElectricityBatteryVO();
        BeanUtil.copyProperties(electricityBattery, electricityBatteryVO);

        if (Objects.equals(electricityBattery.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE)
                && Objects.nonNull(electricityBattery.getUid())) {
            UserInfo userInfo = userInfoService.queryByUidFromCache(electricityBattery.getUid());
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
     * @param isNeedSync
     * @return
     */
    @Override
    public R deleteElectricityBattery(Long id, Integer isNeedSync) {
        ElectricityBattery electricityBattery = electricitybatterymapper.selectOne(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getId, id)
                        .eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL)
                        .eq(ElectricityBattery::getTenantId, TenantContextHolder.getTenantId()));
        if (Objects.isNull(electricityBattery)) {
            log.error("ELE ERROR ,not found electricitybattery,batteryId={}", id);
            return R.fail("100225", "未找到电池!");
        }

        if (ObjectUtil.equal(ElectricityBattery.BUSINESS_STATUS_LEASE, electricityBattery.getBusinessStatus())) {
            log.error("ELE ERROR ,electricity_battery is using,batteryId={}", id);
            return R.fail("100226", "电池正在租用中,无法删除!");
        }

        Pair<Boolean, String> result = callBatteryPlatDeleteSn(Collections.singletonList(electricityBattery.getSn()), isNeedSync);
        if (!result.getKey()) {
            return R.fail("200005", result.getRight());
        }

        int raws = electricitybatterymapper.deleteById(id, TenantContextHolder.getTenantId());
        geoService.deleteBySn(electricityBattery.getSn());
        if (raws > 0) {
            redisService.delete(CacheConstant.CACHE_BT_ATTR + electricityBattery.getSn());
            return R.ok();
        } else {
            return R.fail("100227", "删除失败!");
        }
    }

    @Override
    public ElectricityBattery queryByBindSn(String initElectricityBatterySn) {
        return electricitybatterymapper.selectOne(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, initElectricityBatterySn));
    }

    /**
     * 获取个人电池
     *
     * @param uid
     * @return
     */
    @Override
    public ElectricityBattery queryByUid(Long uid) {
        return baseMapper.queryByUid(uid);
    }

    @Override
    public ElectricityBattery queryBySnFromDb(String oldElectricityBatterySn) {
        return electricitybatterymapper.selectOne(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, oldElectricityBatterySn));
    }

    /**
     * 只会查处部分电池属性（ID，tenantId,sn）
     *
     * @param sn
     * @return
     */
    @Override
    public ElectricityBattery queryPartAttrBySnFromCache(String sn) {
        ElectricityBattery existsBt = redisService.getWithHash(CacheConstant.CACHE_BT_ATTR + sn,
                ElectricityBattery.class);
        if (Objects.nonNull(existsBt)) {
            return existsBt;
        }
        ElectricityBattery dbBattery = electricitybatterymapper.queryPartAttrBySn(sn);
        if (Objects.isNull(dbBattery)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_BT_ATTR + sn, dbBattery);
        return dbBattery;
    }

    @Override
    @Slave
    public ElectricityBattery queryBySnFromDb(String oldElectricityBatterySn, Integer tenantId) {
        return electricitybatterymapper.selectOne(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, oldElectricityBatterySn)
                        .eq(ElectricityBattery::getTenantId, tenantId));
    }

    @Override
    public ElectricityBatteryVO selectBatteryDetailInfoBySN(String sn) {
        return electricitybatterymapper.selectBatteryDetailInfoBySN(sn);
    }

    @Override
    public List<ElectricityBattery> queryWareHouseByElectricityCabinetId(Integer electricityCabinetId) {
        return electricitybatterymapper.selectList(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getElectricityCabinetId,
                                electricityCabinetId)
                        .eq(ElectricityBattery::getPhysicsStatus, ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE)
                        .eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL));
    }

    /**
     * 更新电池绑定的用户
     *
     * @param electricityBattery
     * @return
     */
    @Override
    public Integer updateBatteryUser(ElectricityBattery electricityBattery) {
        return electricitybatterymapper.updateBatteryUser(electricityBattery);
    }

    /**
     * 更新电池状态
     *
     * @param electricityBattery
     * @return
     */
    @Override
    public Integer updateBatteryStatus(ElectricityBattery electricityBattery) {
        return electricitybatterymapper.updateBatteryStatus(electricityBattery);
    }

    @Override
    public R queryCount(ElectricityBatteryQuery electricityBatteryQuery) {
        return R.ok(electricitybatterymapper.queryCount(electricityBatteryQuery));
    }

    @Override
    public R batteryOutTimeInfo(Long tenantId) {
        String json = redisService.get(CacheConstant.CACHE_ADMIN_ALREADY_NOTIFICATION + tenantId);
        List<BorrowExpireBatteryVo> list = null;
        if (StrUtil.isNotBlank(json)) {
            list = JSON.parseArray(json, BorrowExpireBatteryVo.class);
        }
        return R.ok(list);
    }

    @Override
    public void handlerLowBatteryReminder() {
        Integer size = 300;
        Integer offset = 0;

        String batteryLevel = wechatTemplateNotificationConfig.getBatteryLevel();
        Long lowBatteryFrequency = Long.parseLong(wechatTemplateNotificationConfig.getLowBatteryFrequency()) * 60000;
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");

        while (true) {
            List<ElectricityBattery> borrowExpireBatteryList = electricitybatterymapper.queryLowBattery(offset, size,
                    batteryLevel);

            if (CollectionUtils.isEmpty(borrowExpireBatteryList)) {
                return;
            }

            borrowExpireBatteryList.parallelStream().forEach(electricityBattery -> {
                Long uid = electricityBattery.getUid();
                //                Long uid = null;
                //                FranchiseeUserInfo franchiseeUserInfo=franchiseeUserInfoService.selectByNowBattery(electricityBattery.getSn());
                //                if(Objects.nonNull(franchiseeUserInfo)){
                //                    UserInfo userInfo = userInfoService.queryByIdFromDB(franchiseeUserInfo.getUserInfoId());
                //                    if(Objects.nonNull(userInfo)){
                //                        uid = userInfo.getUid();
                //                    }
                //                }

                Integer tenantId = electricityBattery.getTenantId();
                boolean isOutTime = redisService.setNx(CacheConstant.CACHE_LOW_BATTERY_NOTIFICATION + uid, "ok",
                        lowBatteryFrequency, false);
                if (!isOutTime) {
                    return;
                }

                UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(uid, tenantId);
                if (Objects.isNull(userOauthBind)) {
                    log.error("USER_OAUTH_BIND IS NULL uid={},tenantId={}", uid, tenantId);
                    return;
                }
                String openId = userOauthBind.getThirdId();

                BaseMapper<ElectricityPayParams> mapper = electricityPayParamsService.getBaseMapper();
                QueryWrapper<ElectricityPayParams> wrapper = new QueryWrapper<>();
                wrapper.eq("tenant_id", tenantId);
                ElectricityPayParams ele = mapper.selectOne(wrapper);
                if (Objects.isNull(ele)) {
                    log.error("ELECTRICITY_PAY_PARAMS IS NULL ERROR! tenantId={}", tenantId);
                    return;
                }

                TemplateConfigEntity templateConfigEntity = templateConfigService.queryByTenantIdFromCache(tenantId);

                if (Objects.isNull(templateConfigEntity) || Objects.isNull(
                        templateConfigEntity.getBatteryOuttimeTemplate())) {
                    log.error("TEMPLATE_CONFIG IS NULL ERROR! tenantId={}", tenantId);
                    return;
                }

                AppTemplateQuery appTemplateQuery = new AppTemplateQuery();
                appTemplateQuery.setAppId(ele.getMerchantMinProAppId());
                appTemplateQuery.setSecret(ele.getMerchantMinProAppSecert());
                appTemplateQuery.setTouser(openId);
                appTemplateQuery.setFormId(RandomUtil.randomString(20));
                appTemplateQuery.setTemplateId(templateConfigEntity.getElectricQuantityRemindTemplate());
                appTemplateQuery.setPage("/pages/start/index");
                Map<String, Object> data = new HashMap<>(3);

                data.put("character_string1", electricityBattery.getPower() + "%");
                data.put("character_string2", electricityBattery.getSn());
                //data.put("keyword3", sdf.format(new Date(System.currentTimeMillis())));
                data.put("thing3", "当前电量较低，请及时换电。");

                appTemplateQuery.setData(data);
                log.info("LOW BATTERY POWER MESSAGE TO USER uid={}, sn={}", uid, electricityBattery.getSn());

                weChatAppTemplateService.sendWeChatAppTemplate(appTemplateQuery);
            });

            offset += size;
        }
    }

    @Override
    public void handlerBatteryNotInCabinetWarning() {

        //        Integer offset = 0;
        //        Integer size = 300;
        //        while (true) {
        //            List<BorrowExpireBatteryVo> borrowExpireBatteryList = electricitybatterymapper.queryBorrowExpireBattery(System.currentTimeMillis(), offset, size);
        //            if (CollectionUtils.isEmpty(borrowExpireBatteryList)) {
        //                return;
        //            }
        //            //将电池按租户id分组
        //            Map<Integer, List<BorrowExpireBatteryVo>> batteryMaps = borrowExpireBatteryList.stream().collect(Collectors.groupingBy(BorrowExpireBatteryVo::getTenantId));
        //            //频率
        //            Long frequency = Long.parseLong(wechatTemplateNotificationConfig.getBatteryTimeoutFrequency()) * 60000;
        //
        //            batteryMaps.entrySet().parallelStream().forEach(entry -> {
        //                Integer tenantId = entry.getKey();
        //                List<BorrowExpireBatteryVo> batteryList = entry.getValue();
        //
        //                boolean isOutTime = redisService.setNx(CacheConstant.CACHE_ADMIN_ALREADY_NOTIFICATION + tenantId, JSON.toJSONString(batteryList), frequency, false);
        //                if (!isOutTime) {
        //                    return;
        //                }
        //
        //                WechatTemplateAdminNotification wechatTemplateAdminNotification = wechatTemplateAdminNotificationService.queryByTenant(tenantId);
        //                if (Objects.isNull(wechatTemplateAdminNotification)) {
        //                    log.error("WECHAT_TEMPLATE_ADMIN_NOTIFICATION IS NULL ERROR! tenantId={}", tenantId);
        //                    return;
        //                }
        //
        //                BaseMapper<ElectricityPayParams> mapper = electricityPayParamsService.getBaseMapper();
        //                QueryWrapper<ElectricityPayParams> wrapper = new QueryWrapper<>();
        //                wrapper.eq("tenant_id", tenantId);
        //                ElectricityPayParams ele = mapper.selectOne(wrapper);
        //
        //                if (Objects.isNull(ele)) {
        //                    log.error("ELECTRICITY_PAY_PARAMS IS NULL ERROR! tenantId={}", tenantId);
        //                    return;
        //                }
        //
        //                TemplateConfigEntity templateConfigEntity = templateConfigService.queryByTenantIdFromCache(tenantId);
        //
        //                if (Objects.isNull(templateConfigEntity) || Objects.isNull(templateConfigEntity.getBatteryOuttimeTemplate())) {
        //                    log.error("TEMPLATE_CONFIG IS NULL ERROR! tenantId={}", tenantId);
        //                    return;
        //                }
        //
        //                String openStr = wechatTemplateAdminNotification.getOpenIds();
        //                List<String> openIds = JSON.parseArray(openStr, String.class);
        //                AppTemplateQuery appTemplateQuery = createAppTemplateQuery(batteryList, tenantId, ele.getMerchantMinProAppId(), ele.getMerchantMinProAppSecert(), templateConfigEntity.getBatteryOuttimeTemplate());
        //
        //                if (CollectionUtils.isNotEmpty(openIds)) {
        //                    for (String openId : openIds) {
        //                        appTemplateQuery.setTouser(openId);
        //                        appTemplateQuery.setFormId(RandomUtil.randomString(20));
        //                        weChatAppTemplateService.sendWeChatAppTemplate(appTemplateQuery);
        //                    }
        //                }
        //            });
        //            offset += size;
        //        }
    }

    @Override
    public List<HomepageBatteryFrequencyVo> homepageBatteryAnalysis(
            HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery) {
        return electricitybatterymapper.homepageBatteryAnalysis(homepageBatteryFrequencyQuery);
    }

    @Override
    public List<HomepageBatteryFrequencyVo> homepageBatteryAnalysisCount(
            HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery) {
        return electricitybatterymapper.homepageBatteryAnalysisCount(homepageBatteryFrequencyQuery);
    }

    @Override
    public R queryBatteryOverview(ElectricityBatteryQuery electricityBatteryQuery) {
        List<ElectricityBattery> electricityBatteryList = electricitybatterymapper.queryBatteryOverview(
                electricityBatteryQuery);

        if (ObjectUtil.isEmpty(electricityBatteryList)) {
            return R.ok(electricityBatteryList);
        }

        List<ElectricityBatteryVO> electricityBatteryVOList = new ArrayList<>();

        /*List<FranchiseeBindElectricityBattery> franchiseeBindElectricityBatteryList = new ArrayList<>();
        if (Objects.nonNull(electricityBatteryQuery.getFranchiseeId())) {
            franchiseeBindElectricityBatteryList = franchiseeBindElectricityBatteryService.queryByFranchiseeId(electricityBatteryQuery.getFranchiseeId());
        }*/

        for (ElectricityBattery electricityBattery : electricityBatteryList) {

            ElectricityBatteryVO electricityBatteryVO = new ElectricityBatteryVO();
            BeanUtil.copyProperties(electricityBattery, electricityBatteryVO);

            if (Objects.equals(electricityBattery.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE)
                    && Objects.nonNull(electricityBattery.getUid())) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(electricityBattery.getUid());
                if (Objects.nonNull(userInfo)) {
                    electricityBatteryVO.setUserName(userInfo.getName());
                }
            }

            if (Objects.equals(electricityBattery.getPhysicsStatus(), ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE)
                    && Objects.nonNull(electricityBattery.getElectricityCabinetId())) {
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(
                        electricityBattery.getElectricityCabinetId());
                if (Objects.nonNull(electricityCabinet)) {
                    electricityBatteryVO.setElectricityCabinetName(electricityCabinet.getName());
                }
            }

            Franchisee franchisee = franchiseeService.queryByElectricityBatteryId(electricityBattery.getId());
            if (Objects.nonNull(franchisee)) {
                electricityBatteryVO.setFranchiseeName(franchisee.getName());
            }

            //用于电池绑定问题
            /*electricityBatteryVO.setIsBind(false);

            if (ObjectUtil.isNotEmpty(franchiseeBindElectricityBatteryList)) {
                for (FranchiseeBindElectricityBattery franchiseeBindElectricityBattery : franchiseeBindElectricityBatteryList) {
                    if (Objects.equals(franchiseeBindElectricityBattery.getElectricityBatteryId(), electricityBattery.getId())) {
                        electricityBatteryVO.setIsBind(true);
                    }
                }
            }*/

            electricityBatteryVOList.add(electricityBatteryVO);
        }
        return R.ok(electricityBatteryVOList);
    }

    @Override
    public R batteryStatistical(ElectricityBatteryQuery electricityBatteryQuery) {
        return R.ok(electricitybatterymapper.batteryStatistical(electricityBatteryQuery));
    }

    /**
     * 电池绑定/解绑加盟商
     *
     * @param batteryQuery
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R bindFranchisee(BindElectricityBatteryQuery batteryQuery) {
        //先解绑电池加盟商
        ElectricityBattery updateBattery = new ElectricityBattery();
        updateBattery.setFranchiseeId(null);
        updateBattery.setTenantId(TenantContextHolder.getTenantId());
        updateBattery.setUpdateTime(System.currentTimeMillis());
        electricitybatterymapper.unbindFranchiseeId(batteryQuery.getFranchiseeId(), updateBattery);

        //再绑定加盟商
        if (CollectionUtils.isEmpty(batteryQuery.getElectricityBatteryIdList())) {
            return R.ok();
        }

        electricitybatterymapper.bindFranchiseeId(batteryQuery);
        return R.ok();
    }

    @Override
    public List<ElectricityBattery> selectByBatteryIds(List<Long> batteryIds) {
        return electricitybatterymapper.selectByBatteryIds(batteryIds);
    }

    @Override
    public ElectricityBattery selectByBatteryIdAndFranchiseeId(Long batteryId, Long franchiseeId) {
        return electricitybatterymapper.selectOne(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getId, batteryId)
                        .eq(ElectricityBattery::getFranchiseeId, franchiseeId));
    }

    @Override
    public List<ElectricityBattery> selectBatteryInfoByBatteryName(ElectricityBatteryQuery batteryQuery) {
        return electricitybatterymapper.selectBatteryInfoByBatteryName(batteryQuery);
    }

    /**
     * 检查是否有电池绑定加盟商
     *
     * @param id
     * @param tenantId
     * @return
     */
    @Override
    public Integer isFranchiseeBindBattery(Long id, Integer tenantId) {
        return electricitybatterymapper.isFranchiseeBindBattery(id, tenantId);
    }

    /**
     * 检查用户是否绑定有电池
     *
     * @return
     */
    @Override
    public Integer isUserBindBattery(Long uid, Integer tenantId) {
        return electricitybatterymapper.isUserBindBattery(uid, tenantId);
    }


    @Override
    public Integer insertBatch(List<ElectricityBattery> saveList) {
        return electricitybatterymapper.insertBatch(saveList);
    }

    @Override
    public ElectricityBattery queryUserAttrBySnFromDb(String sn) {
        return electricitybatterymapper.queryUserAttrBySn(sn);
    }


    /**
     * 迁移用户所属加盟商  获取用户电池型号
     *
     * @return
     */
    @Deprecated
    public Triple<Boolean, String, Object> selectUserLatestBatteryType() {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(
                TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            log.error("ELE ERROR!not found electricityConfig,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "000001", "系统异常");
        }

        if (!Objects.equals(electricityConfig.getIsMoveFranchisee(), ElectricityConfig.MOVE_FRANCHISEE_OPEN)) {
            log.error("ELE ERROR!not found open move franchisee,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100353", "未启用加盟商迁移");
        }

        String batteryType = null;

        //1.查询当前用户最新换电订单
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderService.selectLatestByUid(
                SecurityUtils.getUid(), TenantContextHolder.getTenantId());
        if (Objects.nonNull(electricityCabinetOrder)) {
            String batterySn = electricityCabinetOrder.getNewElectricityBatterySn();
            batteryType = BatteryConstant.parseBatteryModelByBatteryName(batterySn);
        } else {
            //查询当前用户最新的租电订单
            RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.selectLatestByUid(SecurityUtils.getUid(),
                    TenantContextHolder.getTenantId());
            String batterySn = rentBatteryOrder.getElectricityBatterySn();
            batteryType = BatteryConstant.parseBatteryModelByBatteryName(batterySn);
        }

        if (StringUtils.isBlank(batteryType)) {
            log.error("ELE ERROR!not found user batteryType,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100352", "未找到用户电池型号");
        }

        Integer batteryModel = BatteryConstant.acquireBatteryModel(batteryType);
        if (Objects.isNull(batteryModel)) {
            log.error("ELE ERROR!not found user batteryModel,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100352", "未找到用户电池型号");
        }

        return Triple.of(true, "", batteryModel);
    }

    @Override
    public Triple<Boolean, String, Object> queryBatteryInfoBySn(String sn) {
        ElectricityBattery electricityBattery = queryBySnFromDb(sn, TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityBattery)) {
            return Triple.of(true, null, null);
        }

        ElectricityBatteryVO electricityBatteryVO = new ElectricityBatteryVO();
        BeanUtil.copyProperties(electricityBattery, electricityBatteryVO);

        if (Objects.equals(electricityBattery.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE)
                && Objects.nonNull(electricityBattery.getUid())) {
            UserInfo userInfo = userInfoService.queryByUidFromCache(electricityBattery.getUid());
            if (Objects.nonNull(userInfo)) {
                electricityBatteryVO.setUserName(userInfo.getName());
            }
        }

        if (Objects.equals(electricityBattery.getPhysicsStatus(), ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE)
                && Objects.nonNull(electricityBattery.getElectricityCabinetId())) {
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(
                    electricityBattery.getElectricityCabinetId());
            if (Objects.nonNull(electricityCabinet)) {
                electricityBatteryVO.setElectricityCabinetName(electricityCabinet.getName());
            }
        }

        Franchisee franchisee = franchiseeService.queryByElectricityBatteryId(electricityBattery.getId());
        if (Objects.nonNull(franchisee)) {
            electricityBatteryVO.setFranchiseeName(franchisee.getName());
        }

        return Triple.of(true, null, electricityBatteryVO);

    }

    @Override
    public Triple<Boolean, String, Object> queryBatteryMapList(Integer offset, Integer size, List<Long> franchiseeIds) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        return Triple.of(true, null, electricitybatterymapper.queryPartAttrList(offset, size, franchiseeIds,
                TenantContextHolder.getTenantId()));
    }


}
