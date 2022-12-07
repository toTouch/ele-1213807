package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.sms.SmsService;
import com.xiliulou.core.web.R;
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

import java.util.*;

/**
 * 启用套餐(TEnableMemberCardRecord)实体类
 *
 * @author makejava
 * @since 2022-11-17 16:00:45
 */
@RestController
@Slf4j
public class JsonAdminEnableMemberCardRecordController extends BaseController {


    @Autowired
    EnableMemberCardRecordService enableMemberCardRecordService;

    @Autowired
    UserTypeFactory userTypeFactory;

    @Autowired
    UserDataScopeService userDataScopeService;

    //列表查询
    @GetMapping(value = "/admin/enableMemberCardRecord/list")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "enableType", required = false) Integer enableType,
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
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
            return R.ok(Collections.EMPTY_LIST);
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        EnableMemberCardRecordQuery enableMemberCardRecordQuery=EnableMemberCardRecordQuery.builder()
                .enableType(enableType)
                .beginTime(beginTime)
                .endTime(endTime)
                .offset(offset)
                .size(size)
                .phone(phone)
                .franchiseeIds(franchiseeIds)
                .userName(userName)
                .tenantId(tenantId).build();

        return enableMemberCardRecordService.queryList(enableMemberCardRecordQuery);
    }
    
    
    //列表数量查询
    @GetMapping(value = "/admin/enableMemberCardRecord/queryCount")
    public R queryCount(@RequestParam(value = "userName", required = false) String userName,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "enableType", required = false) Integer enableType,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime,
                        @RequestParam(value = "id", required = false) Integer id) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
            return R.ok(Collections.EMPTY_LIST);
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        EnableMemberCardRecordQuery enableMemberCardRecordQuery=EnableMemberCardRecordQuery.builder()
                .enableType(enableType)
                .beginTime(beginTime)
                .endTime(endTime)
                .phone(phone)
                .franchiseeIds(franchiseeIds)
                .userName(userName)
                .tenantId(tenantId).build();

        return enableMemberCardRecordService.queryCount(enableMemberCardRecordQuery);
    }

}
