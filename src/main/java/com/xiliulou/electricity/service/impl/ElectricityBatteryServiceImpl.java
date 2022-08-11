package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
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
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.HomepageBatteryFrequencyQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BorrowExpireBatteryVo;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import com.xiliulou.electricity.vo.HomepageBatteryFrequencyVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
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
            log.error("dataScreen  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Integer count = electricitybatterymapper.selectCount(new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, electricityBattery.getSn())
                .eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL));
        if (count > 0) {
            return R.fail("该电池已被其他租户使用!");
        }
        electricityBattery.setStatus(ElectricityBattery.STOCK_STATUS);
        electricityBattery.setCreateTime(System.currentTimeMillis());
        electricityBattery.setUpdateTime(System.currentTimeMillis());
        electricityBattery.setTenantId(tenantId);
        electricitybatterymapper.insert(electricityBattery);


        Long franchiseeId=null;
        if (Objects.equals(user.getType(), User.TYPE_USER_STORE)) {
            Store store = storeService.queryByUid(user.getUid());
            if (Objects.nonNull(store)) {
                franchiseeId = store.getFranchiseeId();
            }
        }
        if (Objects.equals(user.getType(),User.TYPE_USER_FRANCHISEE)){
            Franchisee franchisee=franchiseeService.queryByUid(user.getUid());
            if (Objects.nonNull(franchisee)) {
                franchiseeId = franchisee.getId();
            }
        }

        if (Objects.nonNull(franchiseeId)){
            FranchiseeBindElectricityBattery franchiseeBindElectricityBattery = new FranchiseeBindElectricityBattery();
            franchiseeBindElectricityBattery.setFranchiseeId(franchiseeId.intValue());
            franchiseeBindElectricityBattery.setElectricityBatteryId(electricityBattery.getId());
            franchiseeBindElectricityBatteryService.insert(franchiseeBindElectricityBattery);
        }

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

		/*List<FranchiseeBindElectricityBattery> franchiseeBindElectricityBatteryList = new ArrayList<>();
		if (Objects.nonNull(electricityBatteryQuery.getFranchiseeId())) {
			franchiseeBindElectricityBatteryList = franchiseeBindElectricityBatteryService.queryByFranchiseeId(electricityBatteryQuery.getFranchiseeId());
		}*/

        for (ElectricityBattery electricityBattery : electricityBatteryList) {

            ElectricityBatteryVO electricityBatteryVO = new ElectricityBatteryVO();
            BeanUtil.copyProperties(electricityBattery, electricityBatteryVO);

            if (Objects.equals(electricityBattery.getStatus(), ElectricityBattery.LEASE_STATUS) && Objects.nonNull(electricityBattery.getUid())) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(electricityBattery.getUid());
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
    @DS("slave_1")
    public R queryNotBindList(Long offset, Long size, Integer franchiseeId) {
        List<ElectricityBattery> electricityBatteryList = electricitybatterymapper.queryNotBindList(offset, size, franchiseeId, TenantContextHolder.getTenantId());
        List<ElectricityBatteryVO> electricityBatteryVOList = new ArrayList<>();

        List<FranchiseeBindElectricityBattery> franchiseeBindElectricityBatteryList = new ArrayList<>();
        if (Objects.nonNull(franchiseeId)) {
            franchiseeBindElectricityBatteryList = franchiseeBindElectricityBatteryService.queryByFranchiseeId(Long.parseLong(franchiseeId + ""));
        }

        for (ElectricityBattery electricityBattery : electricityBatteryList) {
            ElectricityBatteryVO electricityBatteryVO = new ElectricityBatteryVO();
            BeanUtil.copyProperties(electricityBattery, electricityBatteryVO);

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
    public void insert(ElectricityBattery electricityBattery) {
        electricitybatterymapper.insert(electricityBattery);
    }

    @Override
    public ElectricityBatteryVO queryInfoByUid(Long uid) {
        return electricitybatterymapper.selectBatteryInfo(uid);
    }

    @Override
    public Integer querySumCount(ElectricityBatteryQuery electricityBatteryQuery) {
        return electricitybatterymapper.queryCount(electricityBatteryQuery);
    }

    @Override
    public R queryById(Long electricityBatteryId) {
        ElectricityBattery electricityBattery = electricitybatterymapper.selectById(electricityBatteryId);

        ElectricityBatteryVO electricityBatteryVO = new ElectricityBatteryVO();
        BeanUtil.copyProperties(electricityBattery, electricityBatteryVO);

        if (Objects.equals(electricityBattery.getStatus(), ElectricityBattery.LEASE_STATUS) && Objects.nonNull(electricityBattery.getUid())) {
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
        return baseMapper.queryByUid(uid);
    }

    @Override
    public ElectricityBattery queryBySn(String oldElectricityBatterySn) {
        return electricitybatterymapper.selectOne(new LambdaQueryWrapper<ElectricityBattery>().
                eq(ElectricityBattery::getSn, oldElectricityBatterySn));
    }

    @Override
    public ElectricityBatteryVO selectBatteryDetailInfoBySN(String sn) {
        return electricitybatterymapper.selectBatteryDetailInfoBySN(sn);
    }

    @Override
    public List<ElectricityBattery> queryWareHouseByElectricityCabinetId(Integer electricityCabinetId) {
        return electricitybatterymapper.selectList(new LambdaQueryWrapper<ElectricityBattery>().
                eq(ElectricityBattery::getElectricityCabinetId, electricityCabinetId).eq(ElectricityBattery::getStatus, ElectricityBattery.WARE_HOUSE_STATUS).
                eq(ElectricityBattery::getDelFlag,ElectricityBattery.DEL_NORMAL));
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

        Integer offset = 0;
        Integer size = 300;
        while (true) {
            List<BorrowExpireBatteryVo> borrowExpireBatteryList = electricitybatterymapper.queryBorrowExpireBattery(System.currentTimeMillis(), offset, size);
            if (CollectionUtils.isEmpty(borrowExpireBatteryList)) {
                return;
            }
            //将电池按租户id分组
            Map<Integer, List<BorrowExpireBatteryVo>> batteryMaps = borrowExpireBatteryList.stream().collect(Collectors.groupingBy(BorrowExpireBatteryVo::getTenantId));
            //频率
            Long frequency = Long.parseLong(wechatTemplateNotificationConfig.getBatteryTimeoutFrequency()) * 60000;

            batteryMaps.entrySet().parallelStream().forEach(entry -> {
                Integer tenantId = entry.getKey();
                List<BorrowExpireBatteryVo> batteryList = entry.getValue();

                boolean isOutTime = redisService.setNx(CacheConstant.CACHE_ADMIN_ALREADY_NOTIFICATION + tenantId, JSON.toJSONString(batteryList), frequency, false);
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

                if (Objects.isNull(templateConfigEntity) || Objects.isNull(templateConfigEntity.getBatteryOuttimeTemplate())) {
                    log.error("TEMPLATE_CONFIG IS NULL ERROR! tenantId={}", tenantId);
                    return;
                }

                String openStr = wechatTemplateAdminNotification.getOpenIds();
                List<String> openIds = JSON.parseArray(openStr, String.class);
                AppTemplateQuery appTemplateQuery = createAppTemplateQuery(batteryList, tenantId, ele.getMerchantMinProAppId(), ele.getMerchantMinProAppSecert(), templateConfigEntity.getBatteryOuttimeTemplate());

                if (CollectionUtils.isNotEmpty(openIds)) {
                    for (String openId : openIds) {
                        appTemplateQuery.setTouser(openId);
                        appTemplateQuery.setFormId(RandomUtil.randomString(20));
                        weChatAppTemplateService.sendWeChatAppTemplate(appTemplateQuery);
                    }
                }
            });
            offset += size;
        }
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
        return R.ok(electricitybatterymapper.queryBatteryOverview(electricityBatteryQuery));
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
