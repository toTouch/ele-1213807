package com.xiliulou.electricity.controller.admin;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.sms.SmsService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeBind;
import com.xiliulou.electricity.entity.StoreBind;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.query.ElectricityCabinetAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FranchiseeBindService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreBindService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


/**
 * 换电柜表(TElectricityCabinet)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@RestController
@Slf4j
public class ElectricityCabinetAdminController {
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
    StoreBindService storeBindService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    FranchiseeBindService franchiseeBindService;

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
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "sn", required = false) String sn,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "address", required = false) String address,
                       @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                       @RequestParam(value = "powerStatus", required = false) Integer powerStatus,
                       @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
                .offset(offset)
                .size(size)
                .sn(sn)
                .name(name)
                .address(address)
                .usableStatus(usableStatus)
                .powerStatus(powerStatus)
                .onlineStatus(onlineStatus)
                .beginTime(beginTime)
                .endTime(endTime).build();

        return electricityCabinetService.queryList(electricityCabinetQuery);
    }

    //加盟商列表查询
    @GetMapping(value = "/admin/electricityCabinet/listByFranchisee")
    public R listByFranchisee(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "sn", required = false) String sn,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "address", required = false) String address,
                       @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                       @RequestParam(value = "powerStatus", required = false) Integer powerStatus,
                       @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
                .offset(offset)
                .size(size)
                .sn(sn)
                .name(name)
                .address(address)
                .usableStatus(usableStatus)
                .powerStatus(powerStatus)
                .onlineStatus(onlineStatus)
                .beginTime(beginTime)
                .endTime(endTime).build();

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Franchisee> franchiseeList=franchiseeService.queryByUid(user.getUid());
        if(ObjectUtil.isEmpty(franchiseeList)){
            return R.ok();
        }
        List<FranchiseeBind> franchiseeBinds=new ArrayList<>();
        for (Franchisee franchisee:franchiseeList) {
            List<FranchiseeBind> franchiseeBindList= franchiseeBindService.queryByFranchiseeId(franchisee.getId());
            franchiseeBinds.addAll(franchiseeBindList);
        }
        if(ObjectUtil.isEmpty(franchiseeBinds)){
            return R.ok();
        }
        List<Integer> storeIdList=new ArrayList<>();
        for (FranchiseeBind franchiseeBind:franchiseeBinds) {
            storeIdList.add(franchiseeBind.getStoreId());
        }
        if(ObjectUtil.isEmpty(storeIdList)){
            return R.ok();
        }
        electricityCabinetQuery.setStoreIdList(storeIdList);

        return electricityCabinetService.listByStoreId(electricityCabinetQuery);
    }

    //网点列表查询
    @GetMapping(value = "/admin/electricityCabinet/listByStoreId")
    public R listByStoreId(@RequestParam(value = "size", required = false) Long size,
                              @RequestParam(value = "offset", required = false) Long offset,
                              @RequestParam(value = "sn", required = false) String sn,
                              @RequestParam(value = "name", required = false) String name,
                              @RequestParam(value = "address", required = false) String address,
                              @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                              @RequestParam(value = "powerStatus", required = false) Integer powerStatus,
                              @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
                              @RequestParam(value = "beginTime", required = false) Long beginTime,
                              @RequestParam(value = "endTime", required = false) Long endTime) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
                .offset(offset)
                .size(size)
                .sn(sn)
                .name(name)
                .address(address)
                .usableStatus(usableStatus)
                .powerStatus(powerStatus)
                .onlineStatus(onlineStatus)
                .beginTime(beginTime)
                .endTime(endTime).build();

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<StoreBind> storeBindList=storeBindService.queryByUid(user.getUid());

        if(ObjectUtil.isEmpty(storeBindList)){
            return R.ok();
        }
        List<Integer> storeIdList=new ArrayList<>();
        for (StoreBind storeBind:storeBindList) {
            storeIdList.add(storeBind.getStoreId());
        }
        if(ObjectUtil.isEmpty(storeIdList)){
            return R.ok();
        }

        electricityCabinetQuery.setStoreIdList(storeIdList);
        return electricityCabinetService.listByStoreId(electricityCabinetQuery);
    }

    //柜子负责人列表查询
    @GetMapping(value = "/admin/electricityCabinet/listByUid")
    public R listByUid(@RequestParam(value = "size", required = false) Long size,
                              @RequestParam(value = "offset", required = false) Long offset,
                              @RequestParam(value = "sn", required = false) String sn,
                              @RequestParam(value = "name", required = false) String name,
                              @RequestParam(value = "address", required = false) String address,
                              @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                              @RequestParam(value = "powerStatus", required = false) Integer powerStatus,
                              @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
                              @RequestParam(value = "beginTime", required = false) Long beginTime,
                              @RequestParam(value = "endTime", required = false) Long endTime) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
                .offset(offset)
                .size(size)
                .sn(sn)
                .name(name)
                .address(address)
                .usableStatus(usableStatus)
                .powerStatus(powerStatus)
                .onlineStatus(onlineStatus)
                .beginTime(beginTime)
                .endTime(endTime).build();

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> uidList = new ArrayList<>();
        uidList.add(user.getUid());
        electricityCabinetQuery.setUidList(uidList);


        return electricityCabinetService.listByUid(electricityCabinetQuery);
    }

    //禁用换电柜
    @PostMapping(value = "/admin/electricityCabinet/disable/{id}")
    public R disable(@PathVariable("id") Integer id) {
        return electricityCabinetService.disable(id);
    }


    //重启换电柜
    @PostMapping(value = "/admin/electricityCabinet/reboot/{id}")
    public R reboot(@PathVariable("id") Integer id) {
        return electricityCabinetService.reboot(id);
    }


    //首页一
    @GetMapping(value = "/admin/electricityCabinet/homeOne/{type}")
    public R homeOne(@PathVariable("type") Integer type) {
        return electricityCabinetService.homeOne(type);
    }

    //首页二
    @GetMapping(value = "/admin/electricityCabinet/homeTwo")
    public R homeTwo() {
        return electricityCabinetService.homeTwo();
    }

    //首页三
    @GetMapping(value = "/admin/electricityCabinet/homeThree/{day}")
    public R homeThree(@PathVariable("day") Integer day) {
        return electricityCabinetService.homeThree(day);
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

        params.put("code","1314");
        params.put("address", "i love you");
        smsService.sendSmsCode("15371639767","SMS_183160573", JsonUtil.toJson(params), "西六楼");

    }


}