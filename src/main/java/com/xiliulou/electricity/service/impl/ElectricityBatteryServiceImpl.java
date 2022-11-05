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
import com.xiliulou.core.web.R;
import com.xiliulou.core.wp.entity.AppTemplateQuery;
import com.xiliulou.core.wp.service.WeChatAppTemplateService;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.config.WechatTemplateNotificationConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.HomepageBatteryFrequencyQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BigEleBatteryVo;
import com.xiliulou.electricity.vo.BorrowExpireBatteryVo;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import com.xiliulou.electricity.vo.HomepageBatteryFrequencyVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

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
    FranchiseeUserInfoService franchiseeUserInfoService;
    @Autowired
    UserOauthBindService userOauthBindService;

    /**
     * 保存电池
     *
     * @param
     * @return
     */
    @Override
    @Transactional
    public R saveElectricityBattery(ElectricityBattery electricityBattery) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Integer count = electricitybatterymapper.selectCount(new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, electricityBattery.getSn())
                .eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL));
        if (count > 0) {
            return R.fail("100224","该电池已被其他租户使用!");
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

        electricityBattery.setFranchiseeId(franchiseeId);
//        electricityBattery.setStatus(ElectricityBattery.STOCK_STATUS);
        electricityBattery.setPhysicsStatus(ElectricityBattery.PHYSICS_STATUS_NOT_WARE_HOUSE);
        electricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_INPUT);
        electricityBattery.setCreateTime(System.currentTimeMillis());
        electricityBattery.setUpdateTime(System.currentTimeMillis());
        electricityBattery.setTenantId(tenantId);
        electricitybatterymapper.insert(electricityBattery);


//        if (Objects.nonNull(franchiseeId)){
//            FranchiseeBindElectricityBattery franchiseeBindElectricityBattery = new FranchiseeBindElectricityBattery();
//            franchiseeBindElectricityBattery.setFranchiseeId(franchiseeId.intValue());
//            franchiseeBindElectricityBattery.setElectricityBatteryId(electricityBattery.getId());
//            franchiseeBindElectricityBatteryService.insert(franchiseeBindElectricityBattery);
//        }

        return R.ok();
    }

    /**
     * 修改电池
     *
     * @param electricityBattery
     * @return
     */
    @Override
    public R update(ElectricityBattery electricityBattery) {
        ElectricityBattery electricityBatteryDb = electricitybatterymapper.selectOne(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getId, electricityBattery.getId())
                        .eq(ElectricityBattery::getTenantId, TenantContextHolder.getTenantId())
                        .eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL));
        if (Objects.isNull(electricityBatteryDb)) {
            log.error("ELE ERROR, not found electricity battery id={}", electricityBattery.getId());
            return R.fail("ELECTRICITY.0020", "电池不存在!");
        }
        
        Integer count = electricitybatterymapper.selectCount(new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, electricityBattery.getSn())
                .eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL).ne(ElectricityBattery::getId, electricityBattery.getId()));
        if (count > 0) {
            return R.fail("电池编号已绑定其他电池!");
        }
        electricityBattery.setUpdateTime(System.currentTimeMillis());
        electricityBattery.setTenantId(TenantContextHolder.getTenantId());
        Integer rows = electricitybatterymapper.update(electricityBattery);
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

            if (Objects.equals(electricityBattery.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE) && Objects.nonNull(electricityBattery.getUid())) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(electricityBattery.getUid());
                if (Objects.nonNull(userInfo)) {
                    electricityBatteryVO.setUserName(userInfo.getName());
                }
            }

            if (Objects.equals(electricityBattery.getPhysicsStatus(), ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE) && Objects.nonNull(electricityBattery.getElectricityCabinetId())) {
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityBattery.getElectricityCabinetId());
                if (Objects.nonNull(electricityCabinet)) {
                    electricityBatteryVO.setElectricityCabinetName(electricityCabinet.getName());
                }
            }

//            Franchisee franchisee = franchiseeService.queryByElectricityBatteryId(electricityBattery.getId());
//            if (Objects.nonNull(franchisee)) {
//                electricityBatteryVO.setFranchiseeName(franchisee.getName());
//            }

            Franchisee franchisee = franchiseeService.queryByIdFromDB(electricityBattery.getFranchiseeId());
            electricityBatteryVO.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());


            electricityBatteryVOList.add(electricityBatteryVO);
        }
        return R.ok(electricityBatteryVOList);
    }

    @Override
    @DS("slave_1")
    public R queryBindListByPage(Long offset, Long size, Long franchiseeId) {

        List<ElectricityBatteryVO> batteryVOList = new ArrayList<>();

        //没有绑定加盟商的电池
        List<ElectricityBattery> notBindList = electricitybatterymapper.queryNotBindList(offset, size,TenantContextHolder.getTenantId());
        //当前加盟商绑定的电池
        List<ElectricityBattery> bindList = electricitybatterymapper.queryBindList(offset, size, franchiseeId, TenantContextHolder.getTenantId());

        if(CollectionUtils.isNotEmpty(notBindList)){
            notBindList.forEach(item->{
                ElectricityBatteryVO batteryVO = new ElectricityBatteryVO();
                BeanUtils.copyProperties(item,batteryVO);
                batteryVO.setIsBind(false);
                batteryVOList.add(batteryVO);
            });
        }

        if(CollectionUtils.isNotEmpty(bindList)){
            bindList.forEach(item->{
                ElectricityBatteryVO batteryVO = new ElectricityBatteryVO();
                BeanUtils.copyProperties(item,batteryVO);
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
    public ElectricityBatteryVO queryInfoByUid(Long uid) {
//        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
//        if(Objects.isNull(userInfo)){
//            log.error("ELE ERROR! not found userInfo,uid={}",uid);
//        }
//
//        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUid(userInfo.getUid());
//        if(Objects.isNull(franchiseeUserInfo)){
//            log.error("ELE ERROR! not found franchiseeUserInfo,uid={}",uid);
//        }
//
//        ElectricityBatteryVO electricityBatteryVO = electricitybatterymapper.selectBatteryDetailInfoBySN(franchiseeUserInfo.getNowElectricityBatterySn());
        ElectricityBatteryVO electricityBatteryVO = electricitybatterymapper.selectBatteryInfo(uid);
        if (Objects.isNull(electricityBatteryVO)) {
            return electricityBatteryVO;
        }

        //前端显示值替换
        if (Objects.nonNull(electricityBatteryVO.getSumA())) {
            electricityBatteryVO.setBatteryChargeA(electricityBatteryVO.getSumA() < 0 ? 0 : electricityBatteryVO.getSumA());
        }


        if (Objects.nonNull(electricityBatteryVO.getSumV())) {
            electricityBatteryVO.setBatteryV(electricityBatteryVO.getSumV() < 0 ? 0 : electricityBatteryVO.getSumV());
        }

        return electricityBatteryVO;
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
        ElectricityBattery electricityBattery = electricitybatterymapper.selectById(electricityBatteryId,TenantContextHolder.getTenantId());
        if(Objects.isNull(electricityBattery)){
            log.error("ELE ERROR, not found electricity battery id={}", electricityBatteryId);
            return R.fail("ELECTRICITY.0020", "电池不存在!");
        }
        
        ElectricityBatteryVO electricityBatteryVO = new ElectricityBatteryVO();
        BeanUtil.copyProperties(electricityBattery, electricityBatteryVO);

        if (Objects.equals(electricityBattery.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE) && Objects.nonNull(electricityBattery.getUid())) {
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
     * @return
     */
    @Override
    public R deleteElectricityBattery(Long id) {
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
            return R.fail("100226","电池正在租用中,无法删除!");
        }

        int raws = electricitybatterymapper.deleteById(id,TenantContextHolder.getTenantId());
        if (raws > 0) {
            return R.ok();
        } else {
            return R.fail("100227","删除失败!");
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
        return baseMapper.queryByUid(uid);
    }

    @Override
    public ElectricityBattery queryBySn(String oldElectricityBatterySn) {
        return electricitybatterymapper.selectOne(new LambdaQueryWrapper<ElectricityBattery>().
                eq(ElectricityBattery::getSn, oldElectricityBatterySn));
    }

    @Override
    public ElectricityBattery queryBySn(String oldElectricityBatterySn, Integer tenantId) {
        return electricitybatterymapper.selectOne(new LambdaQueryWrapper<ElectricityBattery>().
                eq(ElectricityBattery::getSn, oldElectricityBatterySn).eq(ElectricityBattery::getTenantId, tenantId));
    }

    @Override
    public ElectricityBatteryVO selectBatteryDetailInfoBySN(String sn) {
        return electricitybatterymapper.selectBatteryDetailInfoBySN(sn);
    }

    @Override
    public List<ElectricityBattery> queryWareHouseByElectricityCabinetId(Integer electricityCabinetId) {
        return electricitybatterymapper.selectList(new LambdaQueryWrapper<ElectricityBattery>().
                eq(ElectricityBattery::getElectricityCabinetId, electricityCabinetId).eq(ElectricityBattery::getPhysicsStatus, ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE).
                eq(ElectricityBattery::getDelFlag,ElectricityBattery.DEL_NORMAL));
    }
    

    /**
     * 更新电池绑定的用户
     * @param electricityBattery
     * @return
     */
    @Override
    public Integer updateBatteryUser(ElectricityBattery electricityBattery) {
        return electricitybatterymapper.updateBatteryUser(electricityBattery);
    }

    /**
     * 更新电池状态
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
            List<ElectricityBattery> borrowExpireBatteryList = electricitybatterymapper.queryLowBattery(offset, size, batteryLevel);

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
                boolean isOutTime = redisService.setNx(CacheConstant.CACHE_LOW_BATTERY_NOTIFICATION + uid, "ok", lowBatteryFrequency, false);
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

                if (Objects.isNull(templateConfigEntity) || Objects.isNull(templateConfigEntity.getBatteryOuttimeTemplate())) {
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
    public List<HomepageBatteryFrequencyVo> homepageBatteryAnalysis(HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery) {
        return electricitybatterymapper.homepageBatteryAnalysis(homepageBatteryFrequencyQuery);
    }

    @Override
    public List<HomepageBatteryFrequencyVo> homepageBatteryAnalysisCount(HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery) {
        return electricitybatterymapper.homepageBatteryAnalysisCount(homepageBatteryFrequencyQuery);
    }

    @Override
    public R queryBatteryOverview(ElectricityBatteryQuery electricityBatteryQuery) {
        List<ElectricityBattery> electricityBatteryList = electricitybatterymapper.queryBatteryOverview(electricityBatteryQuery);

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

            if (Objects.equals(electricityBattery.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE) && Objects.nonNull(electricityBattery.getUid())) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(electricityBattery.getUid());
                if (Objects.nonNull(userInfo)) {
                    electricityBatteryVO.setUserName(userInfo.getName());
                }
            }

            if (Objects.equals(electricityBattery.getPhysicsStatus(), ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE) && Objects.nonNull(electricityBattery.getElectricityCabinetId())) {
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityBattery.getElectricityCabinetId());
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
    public R batteryStatistical(Integer tenantId) {
        return R.ok(electricitybatterymapper.batteryStatistical(tenantId));
    }

    /**
     * 电池绑定/解绑加盟商
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
        return electricitybatterymapper.selectOne( new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getId, batteryId)
                .eq(ElectricityBattery::getFranchiseeId, franchiseeId));
    }
    
    @Override
    public List<ElectricityBattery> selectBatteryInfoByBatteryName(ElectricityBatteryQuery batteryQuery) {
        return electricitybatterymapper.selectBatteryInfoByBatteryName(batteryQuery);
    }
    
    @Override
    public boolean checkBatteryIsExchange(String batteryName, Double fullyCharged) {
        boolean result = Boolean.FALSE;
        ElectricityBattery electricityBattery = this.queryBySn(batteryName);
        if (Objects.isNull(batteryName)) {
            return result;
        }
    
        result = electricityBattery.getPower() >= fullyCharged ? Boolean.TRUE : Boolean.FALSE;
        return result;
    }
    
    private AppTemplateQuery createAppTemplateQuery(List<BorrowExpireBatteryVo> batteryList, Integer tenantId, String appId, String appSecret, String batteryOuttimeTemplate) {
        AppTemplateQuery appTemplateQuery = new AppTemplateQuery();
        appTemplateQuery.setAppId(appId);
        appTemplateQuery.setSecret(appSecret);
        appTemplateQuery.setTemplateId(batteryOuttimeTemplate);
        appTemplateQuery.setPage("/pages/start/template?tenantId=" + tenantId);
        //发送内容
        appTemplateQuery.setData(createData(batteryList));
        return appTemplateQuery;
    }

    private Map<String, Object> createData(List<BorrowExpireBatteryVo> batteryList) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd号 HH:mm");

        Map<String, Object> data = new HashMap<>(2);

        data.put("time1", dateFormat.format(new Date()));

        data.put("number2", String.valueOf(batteryList.size()));

        return data;
    }

}
