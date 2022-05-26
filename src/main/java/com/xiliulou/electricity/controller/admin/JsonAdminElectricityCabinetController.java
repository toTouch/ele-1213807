package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.sms.SmsService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.HardwareCommand;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.handler.EleHardwareHandlerManager;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.query.ElectricityCabinetAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 换电柜表(TElectricityCabinet)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@RestController
@Slf4j
public class JsonAdminElectricityCabinetController {
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

    //新增换电柜
    @PostMapping(value = "/admin/electricityCabinet")
    public R save(@RequestBody @Validated(value = CreateGroup.class) ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate) {
        return electricityCabinetService.save(electricityCabinetAddAndUpdate);
    }

    //修改换电柜
    @PutMapping(value = "/admin/electricityCabinet")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate) {
        return electricityCabinetService.edit(electricityCabinetAddAndUpdate);
    }

    //删除换电柜
    @DeleteMapping(value = "/admin/electricityCabinet/{id}")
    public R delete(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return electricityCabinetService.delete(id);
    }

    //列表查询
    @GetMapping(value = "/admin/electricityCabinet/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
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
        //如果是查全部则直接跳过
        List<Integer> eleIdList = null;
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByUserType(user);
            if (ObjectUtil.isEmpty(eleIdList)) {
                return R.ok(new ArrayList<>());
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
                .tenantId(tenantId).build();

        return electricityCabinetService.queryList(electricityCabinetQuery);
    }

    //列表数量查询
    @GetMapping(value = "/admin/electricityCabinet/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "address", required = false) String address,
                        @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                        @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        //如果是查全部则直接跳过
        List<Integer> eleIdList = null;
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByUserType(user);
            if (ObjectUtil.isEmpty(eleIdList)) {
                return R.ok();
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
                .tenantId(tenantId).build();

        return electricityCabinetService.queryCount(electricityCabinetQuery);
    }

    //禁启用换电柜
    @PutMapping(value = "/admin/electricityCabinet/updateStatus")
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

    //发送命令
    @PostMapping(value = "/admin/electricityCabinet/command")
    public R sendCommandToEleForOuterV2(@RequestBody EleOuterCommandQuery eleOuterCommandQuery) {
        return electricityCabinetService.sendCommandToEleForOuter(eleOuterCommandQuery);

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
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        //限制解锁权限
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            log.info("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(id);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is offline ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //发送命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(UUID.randomUUID().toString().replace("-", ""))
                .data(dataMap)
                .productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName())
                .command(HardwareCommand.ELE_COMMAND_UNLOCK_CABINET)
                .build();

        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        //删除缓存
        redisService.delete(ElectricityCabinetConstant.UNLOCK_CABINET_CACHE + electricityCabinet.getId());

        redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + id);
        return R.ok();
    }

    //列表查询
    @GetMapping(value = "/admin/electricityCabinet/queryNameList")
    public R queryNameList(@RequestParam("size") Long size,
                           @RequestParam("offset") Long offset) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        //如果是查全部则直接跳过
        List<Integer> eleIdList = null;
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByUserType(user);
            if (ObjectUtil.isEmpty(eleIdList)) {
                return R.ok();
            }
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        return R.ok(electricityCabinetService.queryNameList(size, offset, eleIdList, tenantId));
    }

    //列表查询
    @GetMapping(value = "/admin/electricityCabinet/queryConfig")
    public R queryConfig(@RequestParam("id") Integer id) {

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(id);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        String result = redisService.get(ElectricityCabinetConstant.OTHER_CONFIG_CACHE + electricityCabinet.getId());
        if (StringUtils.isEmpty(result)) {
            return R.ok();
        }
        Map<String, Object> map = JsonUtil.fromJson(result, Map.class);
        return R.ok(map);
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
    public R queryAllElectricityCabinet(@RequestParam("size") Long size,
                                        @RequestParam("offset") Long offset,
                                        @RequestParam(value = "name", required = false) String name) {

        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");

        }

        ElectricityCabinetQuery electricityCabinetQuery=ElectricityCabinetQuery.builder()
                .size(size)
                .offset(offset)
                .name(name).build();

        return electricityCabinetService.queryAllElectricityCabinet(electricityCabinetQuery);
    }

}
