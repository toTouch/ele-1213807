package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderFreezePo;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderFreezeQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageOrderFreezeQryReq;
import com.xiliulou.electricity.query.car.audit.AuditOptReq;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderFreezeService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderFreezeVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
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
     * @return true(成功)、false(失败)
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
     * @return true(成功)、false(失败)
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
     * @return 冻结订单集
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageOrderFreezeVo>> page(@RequestBody CarRentalPackageOrderFreezeQryReq queryReq) {
        if (null == queryReq) {
            queryReq = new CarRentalPackageOrderFreezeQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        queryReq.setTenantId(tenantId);

        // 数据权校验
        Triple<List<Integer>, List<Integer>, Boolean> permissionTriple = checkPermissionInteger();
        if (!permissionTriple.getRight()) {
            return R.ok(Collections.emptyList());
        }

        // 转换请求体
        CarRentalPackageOrderFreezeQryModel qryModel = new CarRentalPackageOrderFreezeQryModel();
        BeanUtils.copyProperties(queryReq, qryModel);
        qryModel.setFranchiseeIdList(permissionTriple.getLeft());
        qryModel.setStoreIdList(permissionTriple.getMiddle());

        // 调用服务
        List<CarRentalPackageOrderFreezePo> freezeEntityList = carRentalPackageOrderFreezeService.page(qryModel);
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
        List<CarRentalPackageOrderFreezeVo> freezeVoList = freezeEntityList.stream().map(freezeEntity -> {

            CarRentalPackageOrderFreezeVo freezeVO = new CarRentalPackageOrderFreezeVo();
            BeanUtils.copyProperties(freezeEntity, freezeVO);

            if (!userInfoMap.isEmpty()) {
                UserInfo userInfo = userInfoMap.getOrDefault(freezeEntity.getUid(), new UserInfo());
                freezeVO.setUserRelName(userInfo.getName());
                freezeVO.setUserPhone(userInfo.getPhone());
            }

            if (!packageNameMap.isEmpty()) {
                freezeVO.setCarRentalPackageName(packageNameMap.getOrDefault(freezeEntity.getRentalPackageId(), ""));
            }

            return freezeVO;
        }).collect(Collectors.toList());

        return R.ok(freezeVoList);
    }

    /**
     * 条件查询总数
     * @param qryReq 请求参数类
     * @return 总数
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalPackageOrderFreezeQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageOrderFreezeQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);

        // 数据权校验
        Triple<List<Integer>, List<Integer>, Boolean> permissionTriple = checkPermissionInteger();
        if (!permissionTriple.getRight()) {
            return R.ok(NumberConstant.ZERO);
        }

        // 转换请求体
        CarRentalPackageOrderFreezeQryModel qryModel = new CarRentalPackageOrderFreezeQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);
        qryModel.setFranchiseeIdList(permissionTriple.getLeft());
        qryModel.setStoreIdList(permissionTriple.getMiddle());

        // 调用服务
        return R.ok(carRentalPackageOrderFreezeService.count(qryModel));
    }

}
