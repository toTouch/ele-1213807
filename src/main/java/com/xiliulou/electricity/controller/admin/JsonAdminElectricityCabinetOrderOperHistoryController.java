package com.xiliulou.electricity.controller.admin;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ElectricityCabinetOrderOperHistoryQuery;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;


/**
 * 订单表(TElectricityCabinetOrder)表控制层
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
@RestController
@Slf4j
public class JsonAdminElectricityCabinetOrderOperHistoryController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;
    @Autowired
    UserTypeFactory userTypeFactory;

    //换电柜历史记录查询
    @GetMapping("/admin/electricityCabinetOrderOperHistory/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "type", required = false) Integer type,
                       @RequestParam(value = "orderType", required = false) Integer orderType,
                       @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
                       @RequestParam(value = "cellNo", required = false) Integer cellNo,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {

        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
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


        List<Integer> eleIdList = null;
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByUserType(user);
            if(Objects.isNull(eleIdList)){
                return R.ok();
            }
        }

        ElectricityCabinetOrderOperHistoryQuery electricityCabinetOrderOperHistoryQuery = ElectricityCabinetOrderOperHistoryQuery.builder()
                .offset(offset)
                .size(size)
                .orderId(orderId)
                .status(status)
                .type(type)
                .orderType(orderType)
                .electricityCabinetId(electricityCabinetId)
                .cellNo(cellNo)
                .beginTime(beginTime)
                .endTime(endTime)
                .eleIdList(eleIdList)
                .tenantId(tenantId).build();
        return electricityCabinetOrderOperHistoryService.queryList(electricityCabinetOrderOperHistoryQuery);
    }

    //换电柜历史记录查询
    @GetMapping("/admin/electricityCabinetOrderOperHistory/queryCount")
    public R queryCount(@RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "orderType", required = false) Integer orderType,
            @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "cellNo", required = false) Integer cellNo,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {


        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }


        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        List<Integer> eleIdList = null;
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByUserType(user);
            if(Objects.isNull(eleIdList)){
                return R.ok();
            }
        }

        ElectricityCabinetOrderOperHistoryQuery electricityCabinetOrderOperHistoryQuery = ElectricityCabinetOrderOperHistoryQuery.builder()
                .orderId(orderId)
                .status(status)
                .type(type)
                .orderType(orderType)
                .electricityCabinetId(electricityCabinetId)
                .cellNo(cellNo)
                .beginTime(beginTime)
                .endTime(endTime)
                .eleIdList(eleIdList)
                .tenantId(tenantId).build();
        return electricityCabinetOrderOperHistoryService.queryCount(electricityCabinetOrderOperHistoryQuery);
    }


}
