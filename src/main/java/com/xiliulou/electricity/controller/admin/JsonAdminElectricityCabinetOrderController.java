package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
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
public class JsonAdminElectricityCabinetOrderController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    @Autowired
    UserTypeFactory userTypeFactory;

    //换电柜订单查询
    @GetMapping("/admin/electricityCabinetOrder/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "status", required = false) String status,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "source", required = false) Integer source,
                       @RequestParam(value = "paymentMethod", required = false) Integer paymentMethod,
                       @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName,
                       @RequestParam(value = "oldCellNo", required = false) Integer oldCellNo) {

        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }


        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

//        List<Integer> eleIdList = null;
//        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
//                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
//            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
//            if (Objects.isNull(userTypeService)) {
//                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
//                return R.fail("ELECTRICITY.0066", "用户权限不足");
//            }
//            eleIdList = userTypeService.getEleIdListByUserType(user);
//            if (ObjectUtil.isEmpty(eleIdList)) {
//                return R.ok(new ArrayList<>());
//            }
//        }

        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByDataType(user);
        }

        if (CollectionUtils.isEmpty(eleIdList)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder()
                .offset(offset)
                .size(size)
                .orderId(orderId)
                .phone(phone)
                .status(status)
                .beginTime(beginTime)
                .endTime(endTime)
                .paymentMethod(paymentMethod)
                .eleIdList(eleIdList)
                .source(source)
                .electricityCabinetName(electricityCabinetName)
                .oldCellNo(oldCellNo)
                .tenantId(TenantContextHolder.getTenantId()).build();
        return electricityCabinetOrderService.queryList(electricityCabinetOrderQuery);
    }

    //换电柜订单查询
    @GetMapping("/admin/electricityCabinetOrder/queryCount")
    public R queryCount(@RequestParam(value = "orderId", required = false) String orderId,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "status", required = false) String status,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime,
                        @RequestParam(value = "source", required = false) Integer source,
                        @RequestParam(value = "paymentMethod", required = false) Integer paymentMethod,
                        @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName,
                        @RequestParam(value = "oldCellNo", required = false) Integer oldCellNo) {


        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

//        List<Integer> eleIdList = null;
//        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER) && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
//            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
//            if (Objects.isNull(userTypeService)) {
//                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
//                return R.fail("ELECTRICITY.0066", "用户权限不足");
//            }
//            eleIdList = userTypeService.getEleIdListByUserType(user);
//            if (ObjectUtil.isEmpty(eleIdList)) {
//                return R.ok();
//            }
//        }

        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByDataType(user);
        }

        if (CollectionUtils.isEmpty(eleIdList)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder()
                .orderId(orderId)
                .phone(phone)
                .status(status)
                .beginTime(beginTime)
                .endTime(endTime)
                .paymentMethod(paymentMethod)
                .eleIdList(eleIdList)
                .source(source)
                .electricityCabinetName(electricityCabinetName)
                .oldCellNo(oldCellNo)
                .tenantId(TenantContextHolder.getTenantId()).build();
        return electricityCabinetOrderService.queryCount(electricityCabinetOrderQuery);
    }

    //结束异常订单
    @PutMapping(value = "/admin/electricityCabinetOrder/endOrder")
    public R endOrder(@RequestParam("orderId") String orderId) {
        return electricityCabinetOrderService.endOrder(orderId);
    }

    //换电柜订单导出报表
    @GetMapping("/admin/electricityCabinetOrder/exportExcel")
    public void exportExcel(@RequestParam(value = "orderId", required = false) String orderId,
                            @RequestParam(value = "phone", required = false) String phone,
                            @RequestParam(value = "status", required = false) String status,
                            @RequestParam(value = "beginTime", required = false) Long beginTime,
                            @RequestParam(value = "endTime", required = false) Long endTime, HttpServletResponse response) {

        Double days = (Double.valueOf(endTime - beginTime)) / 1000 / 3600 / 24;
        if (days > 33) {
            throw new CustomBusinessException("搜索日期不能大于33天");
        }

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user ");
            throw new CustomBusinessException("查不到订单");
        }

//        List<Integer> eleIdList = null;
//        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER) && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
//            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
//            if (Objects.isNull(userTypeService)) {
//                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
//                throw new CustomBusinessException("查不到订单");
//            }
//            eleIdList = userTypeService.getEleIdListByUserType(user);
//            if (ObjectUtil.isEmpty(eleIdList)) {
//                throw new CustomBusinessException("查不到订单");
//            }
//        }

        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getDataType());
                throw new CustomBusinessException("查不到订单");
            }
            eleIdList = userTypeService.getEleIdListByDataType(user);
        }

        if (CollectionUtils.isEmpty(eleIdList)) {
            throw new CustomBusinessException("查不到订单");
        }

        ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder()
                .orderId(orderId)
                .phone(phone)
                .status(status)
                .beginTime(beginTime)
                .endTime(endTime)
                .eleIdList(eleIdList)
                .tenantId(TenantContextHolder.getTenantId()).build();
        electricityCabinetOrderService.exportExcel(electricityCabinetOrderQuery, response);
    }

    //测试订单查询
    @PutMapping(value = "/admin/electricityCabinetOrder/test")
    public R test(@RequestParam("eleId") Integer eleId, @RequestParam("cellNo") Integer cellNo) {
        return R.ok(electricityCabinetOrderService.queryByCellNoAndEleId(eleId, cellNo));
    }

}
