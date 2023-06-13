package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.sms.SmsService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.dto.ElectricityCabinetOtherSetting;
import com.xiliulou.electricity.entity.EleCabinetCoreData;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.electricity.vo.HomepageBatteryVo;
import com.xiliulou.electricity.vo.HomepageElectricityExchangeFrequencyVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 换电柜表(TElectricityCabinet)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@RestController
@Slf4j
public class JsonAdminElectricityCabinetController extends BaseController {

    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Qualifier("alibabaSmsService")
    @Autowired
    SmsService smsService;

    @Autowired
    StoreService storeService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;

    @Autowired
    RedisService redisService;

    @Autowired
    UserTypeFactory userTypeFactory;

    @Autowired
    EleCabinetCoreDataService eleCabinetCoreDataService;

    @Autowired
    EleOnlineLogService eleOnlineLogService;
    @Autowired
    UserDataScopeService userDataScopeService;


    //新增换电柜
    @PostMapping(value = "/admin/electricityCabinet")
    public R save(
            @RequestBody @Validated(value = CreateGroup.class) ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate) {
        return electricityCabinetService.save(electricityCabinetAddAndUpdate);
    }

    //修改换电柜
    @PutMapping(value = "/admin/electricityCabinet")
    @Log(title = "修改换电柜")
    public R update(
            @RequestBody @Validated(value = UpdateGroup.class) ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate) {
        return electricityCabinetService.edit(electricityCabinetAddAndUpdate);
    }

    @PutMapping(value = "/admin/electricityCabinet/updateAddress")
    @Log(title = "修改换电柜")
    public R updateAddress(@RequestBody @Validated ElectricityCabinetAddressQuery eleCabinetAddressQuery) {
        return returnTripleResult(electricityCabinetService.updateAddress(eleCabinetAddressQuery));
    }

    //删除换电柜
    @DeleteMapping(value = "/admin/electricityCabinet/{id}")
    @Log(title = "删除换电柜")
    public R delete(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return electricityCabinetService.delete(id);
    }

    //列表查询
    @GetMapping(value = "/admin/electricityCabinet/list")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "address", required = false) String address,
                       @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                       @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "id", required = false) Integer id) {
        if (Objects.isNull(size) || size < 0 || size > 50) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }

            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .address(address)
                .usableStatus(usableStatus)
                .onlineStatus(onlineStatus)
                .beginTime(beginTime)
                .endTime(endTime)
                .eleIdList(eleIdList)
                .id(id)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return electricityCabinetService.queryList(electricityCabinetQuery);
    }


    //列表数量查询
    @GetMapping(value = "/admin/electricityCabinet/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "address", required = false) String address,
                        @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                        @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime,
                        @RequestParam(value = "sn",required = false) String sn,
                        @RequestParam(value = "modelId",required = false) Integer modelId) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }

            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
                .name(name)
                .address(address)
                .usableStatus(usableStatus)
                .onlineStatus(onlineStatus)
                .beginTime(beginTime)
                .endTime(endTime)
                .eleIdList(eleIdList)
                .modelId(modelId)
                .sn(sn)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return electricityCabinetService.queryCount(electricityCabinetQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/electricityCabinet/list/super")
    public R queryListSuper(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                            @RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "address", required = false) String address,
                            @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                            @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
                            @RequestParam(value = "beginTime", required = false) Long beginTime,
                            @RequestParam(value = "endTime", required = false) Long endTime,
                            @RequestParam(value = "id", required = false) Integer id) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (user.getTenantId() != 1) {
            return R.fail("权限不足");
        }


        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder().offset(offset).size(size)
                .name(name).address(address).usableStatus(usableStatus).onlineStatus(onlineStatus).beginTime(beginTime)
                .endTime(endTime).eleIdList(null).id(id).tenantId(null).build();

        return electricityCabinetService.queryList(electricityCabinetQuery);
    }

    //列表数量查询
    @GetMapping(value = "/admin/electricityCabinet/queryCount/super")
    public R queryCountSuper(@RequestParam(value = "name", required = false) String name,
                             @RequestParam(value = "address", required = false) String address,
                             @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                             @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
                             @RequestParam(value = "beginTime", required = false) Long beginTime,
                             @RequestParam(value = "endTime", required = false) Long endTime) {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (user.getTenantId() != 1) {
            return R.fail("权限不足");
        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder().name(name).address(address)
                .usableStatus(usableStatus).onlineStatus(onlineStatus).beginTime(beginTime).endTime(endTime)
                .eleIdList(null).build();

        return electricityCabinetService.queryCount(electricityCabinetQuery);
    }

    @PutMapping("/admin/cabinet/onLineStatus/{id}")
    public R updateOnlineStatus(@PathVariable("id") Long id) {
        return returnTripleResult(electricityCabinetService.updateOnlineStatus(id));
    }

    /**
     * 查询空仓、有电池数量
     */
    @GetMapping("/admin/cabinet/battery/statistics/{id}")
    public R batteryStatistics(@PathVariable("id") Long id) {
        return R.ok(electricityCabinetService.batteryStatistics(id));
    }

    //禁启用换电柜
    @PutMapping(value = "/admin/electricityCabinet/updateStatus")
    @Log(title = "禁/启用换电柜")
    public R updateStatus(@RequestParam("id") Integer id, @RequestParam("usableStatus") Integer usableStatus) {
        return electricityCabinetService.updateStatus(id, usableStatus);
    }

    //首页一
    @GetMapping(value = "/admin/electricityCabinet/homeOne")
    public R homeOne(@RequestParam(value = "beginTime", required = false) Long beginTime,
                     @RequestParam(value = "endTime", required = false) Long endTime) {
        //不传查全部
        if (Objects.isNull(beginTime)) {
            beginTime = 0L;
        }
        if (Objects.isNull(endTime)) {
            endTime = System.currentTimeMillis();
        }
        return electricityCabinetService.homeOne(beginTime, endTime);
    }

    //首页三
    @GetMapping(value = "/admin/electricityCabinet/homeTwo")
    public R homeThree(@RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {
        //不传查近七天
        if (Objects.isNull(beginTime)) {
            beginTime = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7L;
        }
        if (Objects.isNull(endTime)) {
            endTime = System.currentTimeMillis();
        }
        return electricityCabinetService.homeTwo(beginTime, endTime);
    }

    //type 1--用户
    //     2--柜机
    //     3--换电次数
    //     4--门店
    //首页三
    @GetMapping(value = "/admin/electricityCabinet/homeThree")
    public R homeThree(@RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "type", required = false) Integer type) {
        //不传查近七天
        if (Objects.isNull(beginTime)) {
            beginTime = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7L;
        }
        if (Objects.isNull(endTime)) {
            endTime = System.currentTimeMillis();
        }
        //不传默认查用户
        if (Objects.isNull(type)) {
            type = 1;
        }
        return electricityCabinetService.homeThree(beginTime, endTime, type);
    }

    // TODO: 2022/10/15 危险接口
    //发送命令
    @PostMapping(value = "/admin/electricityCabinet/command")
    public R sendCommandToEleForOuterV2(@RequestBody EleOuterCommandQuery eleOuterCommandQuery) {
        return electricityCabinetService.sendCommandToEleForOuter(eleOuterCommandQuery);

    }

    @PostMapping(value = "/admin/electricityCabinet/command/super")
    public R sendCommandToEleForOuterV2Super(@RequestBody EleOuterCommandQuery eleOuterCommandQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (user.getTenantId() != 1) {
            return R.fail("权限不足");
        }
        return electricityCabinetService.sendCommandToEleForOuterSuper(eleOuterCommandQuery);

    }


    //查看开门命令
    @GetMapping("/admin/electricityCabinet/open/check")
    public R checkOpenSession(@RequestParam("sessionId") String sessionId) {
        if (StrUtil.isEmpty(sessionId)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return electricityCabinetService.checkOpenSessionId(sessionId);
    }


    //短信测试
    @GetMapping("/outer/sendMessage")
    public void sendMessage() {
        HashMap<String, Object> params = Maps.newHashMap();
     /*   params.put("code", "1314");
        smsService.sendSmsCode("15371639767", "SMS_185846411", JsonUtil.toJson(params), "西六楼");*/

        params.put("code", "1314");
        params.put("address", "i love you");
        smsService.sendSmsCode("15371639767", "SMS_183160573", JsonUtil.toJson(params), "西六楼");

    }

    //解锁电柜
    @PostMapping(value = "/admin/electricityCabinet/unlockCabinet")
    public R unlockCabinet(@RequestParam("id") Integer id) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //限制解锁权限
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER) && !Objects.equals(user.getType(), User.TYPE_USER_NORMAL_ADMIN)) {
            log.info("USER TYPE ERROR! not found operate service! userType={}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(id);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        if (!Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }

        //发送命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(UUID.randomUUID().toString().replace("-", "")).data(dataMap)
                .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName())
                .command(ElectricityIotConstant.ELE_COMMAND_UNLOCK_CABINET).build();

        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        //删除缓存
        redisService.delete(CacheConstant.UNLOCK_CABINET_CACHE + electricityCabinet.getId());

        redisService.delete(CacheConstant.ORDER_ELE_ID + id);
        return R.ok();
    }

    //列表查询
    @GetMapping(value = "/admin/electricityCabinet/queryNameList")
    public R queryNameList(@RequestParam("size") Long size, @RequestParam("offset") Long offset) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }

            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        return R.ok(electricityCabinetService.queryNameList(size, offset, eleIdList, TenantContextHolder.getTenantId()));
    }

    /**
     * 读取柜机配置信息
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/admin/electricityCabinet/queryConfig")
    public R queryConfig(@RequestParam("id") Integer id) {

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(id);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        if (!Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }

//        String result = redisService.get(CacheConstant.OTHER_CONFIG_CACHE + electricityCabinet.getId());
//        if (StringUtils.isEmpty(result)) {
//            return R.ok();
//        }
//        Map<String, Object> map = JsonUtil.fromJson(result, Map.class);
        ElectricityCabinetOtherSetting otherSetting = redisService.getWithHash(CacheConstant.OTHER_CONFIG_CACHE_V_2 + electricityCabinet.getId(), ElectricityCabinetOtherSetting.class);

        return R.ok(otherSetting);
    }

    //列表查询
    @GetMapping(value = "/admin/electricityCabinet/{id}")
    public R queryById(@PathVariable("id") Integer id) {
        return electricityCabinetService.queryById(id);
    }

    /**
     * 查询换电柜所属加盟商
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/admin/cabinetBelongFranchisee/{id}")
    public R queryCabinetBelongFranchisee(@PathVariable("id") Integer id) {
        return electricityCabinetService.queryCabinetBelongFranchisee(id);
    }

    /**
     * admin查看所有换电柜
     *
     * @return
     */
    @GetMapping(value = "/admin/electricityCabinet/allCabinet")
    public R queryAllElectricityCabinet(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
            @RequestParam(value = "name", required = false) String name) {

        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin()) {
            log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");

        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder().size(size).offset(offset)
                .name(name).build();

        return electricityCabinetService.queryAllElectricityCabinet(electricityCabinetQuery);
    }
    
    
/*    //核心板上报数据分页
    @GetMapping(value = "/admin/electricityCabinet/core_data_list")
    public R queryEleCabinetCoreDataList(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
            @RequestParam(value = "id", required = false) Integer id) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        EleCabinetCoreDataQuery eleCabinetCoreDataQuery = EleCabinetCoreDataQuery.builder().offset(offset).size(size)
                .id(id).tenantId(TenantContextHolder.getTenantId()).build();

        List<EleCabinetCoreData> eleCabinetCoreData = eleCabinetCoreDataService
                .selectListByQuery(eleCabinetCoreDataQuery);
        return R.ok(eleCabinetCoreData);
    }*/

    //核心板上报数据详情
    @GetMapping(value = "/admin/electricityCabinet/core_data_list/{electricityCabinetId}")
    public R queryEleCabinetCoreDataList(@PathVariable(value = "electricityCabinetId") Integer electricityCabinetId) {

        EleCabinetCoreData eleCabinetCoreData = eleCabinetCoreDataService.selectByEleCabinetId(electricityCabinetId);
        return R.ok(eleCabinetCoreData);
    }

    //首页营业额统计
    @GetMapping(value = "/admin/electricityCabinet/homepageTurnover")
    public R homepageTurnover() {
        return electricityCabinetService.homepageTurnover();
    }

    //首页押金统计
    @GetMapping(value = "/admin/electricityCabinet/homepageDeposit")
    public R homepageDeposit() {
        return electricityCabinetService.homepageDeposit();
    }

    //首页概述详情统计
    @GetMapping(value = "/admin/electricityCabinet/homepageOverviewDetail")
    public R homepageOverviewDetail() {
        return electricityCabinetService.homepageOverviewDetail();
    }

    //首页收益分析
    @GetMapping(value = "/admin/electricityCabinet/homepageBenefitAnalysis")
    public R homepageBenefitAnalysis(@RequestParam(value = "beginTime", required = false) Long beginTime,
                                     @RequestParam(value = "endTime", required = false) Long endTime) {
        return electricityCabinetService.homepageBenefitAnalysis(beginTime, endTime);
    }

    //首页用户分析
    @GetMapping(value = "/admin/electricityCabinet/homepageUserAnalysis")
    public R homepageUserAnalysis(@RequestParam(value = "beginTime", required = false) Long beginTime,
                                  @RequestParam(value = "endTime", required = false) Long endTime) {
        return electricityCabinetService.homepageUserAnalysis(beginTime, endTime);
    }

    //首页柜机分析
    @GetMapping(value = "/admin/electricityCabinet/homepageElectricityCabinetAnalysis")
    public R homepageElectricityCabinetAnalysis() {
        return electricityCabinetService.homepageElectricityCabinetAnalysis();
    }

    //首页换电频次
    @GetMapping(value = "/admin/electricityCabinet/homepageExchangeOrderFrequency")
    public R homepageExchangeOrderFrequency(@RequestParam(value = "beginTime", required = false) Long beginTime,
                                            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam("size") Long size,
                                            @RequestParam("offset") Long offset,
                                            @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId) {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }

        List<Integer> eleIdList = null;
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userDataType:{}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (org.springframework.util.CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(new HomepageElectricityExchangeFrequencyVo());
            }

            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(new HomepageElectricityExchangeFrequencyVo());
            }
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery = HomepageElectricityExchangeFrequencyQuery.builder()
                .beginTime(beginTime)
                .endTime(endTime)
                .size(size)
                .offset(offset)
                .electricityCabinetId(electricityCabinetId)
                .tenantId(tenantId)
                .eleIdList(eleIdList)
                .franchiseeIds(franchiseeIds).build();

        return electricityCabinetService.homepageExchangeOrderFrequency(homepageElectricityExchangeFrequencyQuery);
    }

    //首页电池分析
    @GetMapping(value = "/admin/electricityCabinet/homepageBatteryAnalysis")
    public R homepageBatteryAnalysis(@RequestParam(value = "beginTime", required = false) Long beginTime,
                                     @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam("size") Long size,
                                     @RequestParam("offset") Long offset,
                                     @RequestParam(value = "batterySn", required = false) String batterySn) {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }

        List<Integer> eleIdList = null;
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userDataType:{}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (org.springframework.util.CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(new HomepageBatteryVo());
            }

            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(new HomepageBatteryVo());
            }
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery = HomepageBatteryFrequencyQuery.builder()
                .batterySn(batterySn)
                .beginTime(beginTime)
                .endTime(endTime)
                .offset(offset)
                .size(size)
                .franchiseeIds(franchiseeIds)
                .tenantId(tenantId).build();

        return electricityCabinetService.homepageBatteryAnalysis(homepageBatteryFrequencyQuery);
    }

    /**
     * ota操作： 1--下载新  2-- 同步  3--升级
     */
    @PostMapping("/admin/electricityCabinet/ota/command")
    public R otaCommand(@RequestParam("eid") Integer eid, @RequestParam("operateType") Integer operateType,
                        @RequestParam(value = "cellNos", required = false) List<Integer> cellNos) {
        return electricityCabinetService.otaCommand(eid, operateType, cellNos);
    }

    /**
     * ota操作检查
     */
    @GetMapping("/admin/electricityCabinet/ota/check")
    public R checkOtaSession(@RequestParam("sessionId") String sessionId) {
        if (StrUtil.isEmpty(sessionId)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return electricityCabinetService.checkOtaSession(sessionId);
    }

    @GetMapping("/admin/electricityCabinet/onlineLogList")
    public R getOnlineLogList(@RequestParam("size") Integer size, @RequestParam("offset") Integer offset,
                              @RequestParam(value = "status", required = false) String status, @RequestParam("eleId") Integer eleId) {
        if (size < 0 || size > 50) {
            size = 50;
        }

        if (offset < 0) {
            offset = 0;
        }

        return eleOnlineLogService.queryOnlineLogList(size, offset, status, eleId);
    }


    @GetMapping("/admin/electricityCabinet/onlineLogCount")
    public R getOnlineLogCount(@RequestParam(value = "status", required = false) String status,
                               @RequestParam("eleId") Integer eleId) {
        return eleOnlineLogService.queryOnlineLogCount(status, eleId);
    }

    /**
     * 列表页搜索接口
     * @return
     */
    @GetMapping("/admin/electricityCabinet/search")
    public R search(@RequestParam("size") long size, @RequestParam("offset") long offset,
                    @RequestParam(value = "name", required = false) String name) {

        if (size < 0 || size > 50) {
            size = 20;
        }

        if (offset < 0) {
            offset = 0;
        }

        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().size(size).offset(offset)
                .name(name).tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(electricityCabinetService.eleCabinetSearch(cabinetQuery));
    }


    @GetMapping("/admin/electricityCabinet/queryName")
    public R queryName(@RequestParam(value = "eleId", required = false) Integer eleId,
                       @RequestParam(value = "name", required = false) String name) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Integer> eleIdList = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE) || Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userDataType={}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }

            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        ElectricityCabinetQuery query = new ElectricityCabinetQuery();
        query.setId(eleId);
        query.setName(name);
        query.setEleIdList(eleIdList);
        query.setTenantId(TenantContextHolder.getTenantId());

        return R.ok(electricityCabinetService.selectByQuery(query));
    }

    @GetMapping("/admin/electricityCabinet/superAdminQueryName")
    public R superAdminQueryName(@RequestParam(value = "eleId", required = false) Integer eleId,
                                 @RequestParam(value = "name", required = false) String name) {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }
        ElectricityCabinetQuery query = new ElectricityCabinetQuery();
        query.setId(eleId);
        query.setName(name);

        return R.ok(electricityCabinetService.superAdminSelectByQuery(query));
    }

    /**
     * 根据经纬度获取柜机列表
     *
     * @return
     */
    @GetMapping("/admin/electricityCabinet/listByLongitudeAndLatitude")
    public R selectEleCabinetListByLongitudeAndLatitude(@RequestParam(value = "id", required = false) Integer id,
                                                        @RequestParam(value = "name", required = false) String name) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }

            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder()
                .id(id)
                .name(name)
                .tenantId(TenantContextHolder.getTenantId())
                .eleIdList(eleIdList)
                .build();

        return electricityCabinetService.selectEleCabinetListByLongitudeAndLatitude(cabinetQuery);
    }


    /**
     * 获取上传柜机照片所需的签名
     */
    @GetMapping(value = "/admin/acquire/upload/cabiet/file/sign")
    public R getUploadCabinetFileSign() {
        return electricityCabinetService.acquireIdcardFileSign();
    }

    @GetMapping(value = "/admin/electricityCabinet/batchOperate/list")
    public R batchOperateList(@RequestParam("size") Long size,
                              @RequestParam("offset") Long offset,
                              @RequestParam(value = "name", required = false) String name,
                              @RequestParam(value = "modelId", required = false) Integer modelId,
                              @RequestParam(value = "sn", required = false) String sn) {
        if (Objects.isNull(size) || size < 0 || size > 50) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }

            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
                .size(size)
                .offset(offset)
                .name(name)
                .modelId(modelId)
                .sn(sn)
                .eleIdList(eleIdList)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return electricityCabinetService.batchOperateList(electricityCabinetQuery);
    }

    /**
     * 查询租户下是否有该换电柜 按三元组
     */
    @GetMapping(value = "/admin/electricityCabinet/existsElectricityCabinet")
    public R existsElectricityCabinet(@RequestParam("productKey") String productKey, @RequestParam("deviceName") String deviceName) {
        return returnTripleResult(electricityCabinetService.existsElectricityCabinet(productKey, deviceName));
    }

    /**
     * 柜机数据导出
     */
    @GetMapping(value = "/admin/electricityCabinet/exportExcel")
    public void exportExcel(@RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "address", required = false) String address,
                            @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                            @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
                            @RequestParam(value = "beginTime", required = false) Long beginTime,
                            @RequestParam(value = "endTime", required = false) Long endTime,
                            @RequestParam(value = "id", required = false) Integer id, HttpServletResponse response) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new CustomBusinessException("用户不存在");
        }

        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            throw new CustomBusinessException("用户权限不足");
        }

        ElectricityCabinetQuery query = ElectricityCabinetQuery.builder()
                .name(name)
                .address(address)
                .usableStatus(usableStatus)
                .onlineStatus(onlineStatus)
                .beginTime(beginTime)
                .endTime(endTime)
                .id(id)
                .tenantId(TenantContextHolder.getTenantId()).build();

        electricityCabinetService.exportExcel(query, response);
    }


}
