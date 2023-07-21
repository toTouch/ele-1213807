package com.xiliulou.electricity.controller.admin.car;

import com.alibaba.fastjson.JSON;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderFreezePO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderFreezeQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageOrderFreezeQryReq;
import com.xiliulou.electricity.query.car.audit.AuditOptReq;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderFreezeService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderFreezeVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 租车套餐订单冻结表 Controller
 * TODO 权限后补
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackageOrderFreeze")
public class JsonAdminCarRentalPackageOrderFreezeController extends BasicController {

    @Resource
    private CarRentalPackageOrderBizService carRentalPackageOrderBizService;

    @Resource
    private CarRentalPackageOrderFreezeService carRentalPackageOrderFreezeService;


    /**
     * 审核拒绝
     * @param optReq 审核操作数据
     * @return
     */
    @PostMapping("/auditReject")
    public R<Boolean> auditReject(@RequestBody AuditOptReq optReq) {
        if (!ObjectUtils.allNotNull(optReq, optReq.getOrderNo(), optReq.getReason())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRentalPackageOrderBizService.approveFreezeRentOrder(optReq.getOrderNo(), false, optReq.getReason(), user.getUid()));
    }

    /**
     * 审核通过
     * @param optReq 审核操作数据
     * @return
     */
    @PostMapping("/approved")
    public R<Boolean> approved(@RequestBody AuditOptReq optReq) {
        if (!ObjectUtils.allNotNull(optReq, optReq.getOrderNo())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRentalPackageOrderBizService.approveFreezeRentOrder(optReq.getOrderNo(), true, optReq.getReason(), user.getUid()));
    }

    /**
     * 条件查询列表
     * @param queryReq 请求参数类
     * @return
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageOrderFreezeVO>> page(@RequestBody CarRentalPackageOrderFreezeQryReq queryReq) {
        // TODO mock数据
        if (true) {
            String mockString = "[\n" +
                    "    {\n" +
                    "        \"orderNo\":\"7364728293847382\",\n" +
                    "        \"rentalPackageType\":1,\n" +
                    "        \"residue\":123,\n" +
                    "        \"residueUnit\":0,\n" +
                    "        \"status\":1,\n" +
                    "        \"remark\":\"备注\",\n" +
                    "        \"lateFee\":230,\n" +
                    "        \"createTime\":1690267754000,\n" +
                    "        \"updateTime\":1690267754000,\n" +
                    "        \"applyTerm\":5,\n" +
                    "        \"applyTime\":1690267754000,\n" +
                    "        \"applyReason\":\"申请原因\",\n" +
                    "        \"userRelName\":\"张三\",\n" +
                    "        \"userPhone\":\"18726372897\",\n" +
                    "        \"carRentalPackageName\":\"单车套餐\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"orderNo\":\"8762738492837162\",\n" +
                    "        \"rentalPackageType\":2,\n" +
                    "        \"residue\":987,\n" +
                    "        \"residueUnit\":1,\n" +
                    "        \"status\":2,\n" +
                    "        \"remark\":\"备注啊\",\n" +
                    "        \"lateFee\":22,\n" +
                    "        \"createTime\":1690267754000,\n" +
                    "        \"updateTime\":1690267754000,\n" +
                    "        \"applyTerm\":56,\n" +
                    "        \"realTerm\":4,\n" +
                    "        \"applyTime\":1690267754000,\n" +
                    "        \"auditTime\":1690267754000,\n" +
                    "        \"enableTime\":1690267754000,\n" +
                    "        \"applyReason\":\"李四\",\n" +
                    "        \"userRelName\":\"李四\",\n" +
                    "        \"userPhone\":\"16273627876\",\n" +
                    "        \"carRentalPackageName\":\"车电一体套餐\"\n" +
                    "    }\n" +
                    "]";
            return R.ok(JSON.parseArray(mockString, CarRentalPackageOrderFreezeVO.class));
        }
        if (null == queryReq) {
            queryReq = new CarRentalPackageOrderFreezeQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        queryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalPackageOrderFreezeQryModel qryModel = new CarRentalPackageOrderFreezeQryModel();
        BeanUtils.copyProperties(queryReq, qryModel);

        // 调用服务
        List<CarRentalPackageOrderFreezePO> freezeEntityList = carRentalPackageOrderFreezeService.page(qryModel);
        if (CollectionUtils.isEmpty(freezeEntityList)) {
            return R.ok(Collections.emptyList());
        }

        // 获取辅助业务信息（用户信息、套餐信息）
        Set<Long> uids = new HashSet<>();
        Set<Long> rentalPackageIds = new HashSet<>();
        freezeEntityList.forEach(freezePO -> {
            uids.add(freezePO.getUid());
            rentalPackageIds.add(freezePO.getRentalPackageId());
        });

        // 用户信息
        Map<Long, UserInfo> userInfoMap = getUserInfoByUidsForMap(uids);

        // 套餐信息
        Map<Long, String> packageNameMap = getCarRentalPackageNameByIdsForMap(rentalPackageIds);

        // 模型转换，封装返回
        List<CarRentalPackageOrderFreezeVO> freezeVOList = freezeEntityList.stream().map(freezePO -> {

            CarRentalPackageOrderFreezeVO freezeVO = new CarRentalPackageOrderFreezeVO();
            BeanUtils.copyProperties(freezePO, freezeVO);

            if (!userInfoMap.isEmpty()) {
                UserInfo userInfo = userInfoMap.getOrDefault(freezePO.getUid(), new UserInfo());
                freezeVO.setUserRelName(userInfo.getName());
                freezeVO.setUserPhone(userInfo.getPhone());
            }

            if (!packageNameMap.isEmpty()) {
                freezeVO.setCarRentalPackageName(packageNameMap.getOrDefault(Long.valueOf(freezePO.getStoreId()), ""));
            }

            return freezeVO;
        }).collect(Collectors.toList());

        return R.ok(freezeVOList);
    }

    /**
     * 条件查询总数
     * @param qryReq 请求参数类
     * @return
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalPackageOrderFreezeQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageOrderFreezeQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalPackageOrderFreezeQryModel qryModel = new CarRentalPackageOrderFreezeQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);

        // 调用服务
        return R.ok(carRentalPackageOrderFreezeService.count(qryModel));
    }

}
