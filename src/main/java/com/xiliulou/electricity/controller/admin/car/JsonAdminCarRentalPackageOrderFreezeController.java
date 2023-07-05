package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderFreezePO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderFreezeQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageOrderFreezeQryReq;
import com.xiliulou.electricity.query.car.audit.AuditOptReq;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderFreezeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderFreezeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 租车套餐订单冻结表 Controller
 * TODO 权限后补
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackageOrderFreeze")
public class JsonAdminCarRentalPackageOrderFreezeController extends JsonAdminCarBasicController {

    @Resource
    private CarRentalPackageOrderFreezeService carRentalPackageOrderFreezeService;


    /**
     * 审核拒绝
     * @param optReq 审核操作数据
     * @return
     */
    @PostMapping("/auditReject")
    public R<Boolean> auditReject(@RequestBody AuditOptReq optReq) {
        // TODO 实现逻辑
        return null;
    }

    /**
     * 审核通过
     * @param optReq 审核操作数据
     * @return
     */
    @PostMapping("/approved")
    public R<Boolean> approved(@RequestBody AuditOptReq optReq) {
        // TODO 实现逻辑
        return null;
    }

    /**
     * 条件查询列表
     * @param queryReq 请求参数类
     * @return
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageOrderFreezeVO>> page(@RequestBody CarRentalPackageOrderFreezeQryReq queryReq) {
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
        R<List<CarRentalPackageOrderFreezePO>> listRes = carRentalPackageOrderFreezeService.page(qryModel);
        if (!listRes.isSuccess()) {
            return R.fail(listRes.getErrCode(), listRes.getErrMsg());
        }
        List<CarRentalPackageOrderFreezePO> freezePOList = listRes.getData();

        // 获取辅助业务信息（用户信息、套餐信息）
        Set<Long> uids = new HashSet<>();
        Set<Long> rentalPackageIds = new HashSet<>();
        freezePOList.forEach(freezePO -> {
            uids.add(freezePO.getUid());
            rentalPackageIds.add(freezePO.getRentalPackageId());
        });

        // 用户信息
        Map<Long, UserInfo> userInfoMap = getUserInfoByUidsForMap(uids);

        // 套餐信息
        Map<Long, String> packageNameMap = getCarRentalPackageNameByIdsForMap(rentalPackageIds);

        // 模型转换，封装返回
        List<CarRentalPackageOrderFreezeVO> freezeVOList = freezePOList.stream().map(freezePO -> {
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
        return carRentalPackageOrderFreezeService.count(qryModel);
    }

}
