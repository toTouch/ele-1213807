package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderRentRefundPO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderRentRefundQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageOrderRentRefundQryReq;
import com.xiliulou.electricity.query.car.audit.AuditOptReq;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderRentRefundService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderRentRefundVO;
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
 * 租车套餐订单退租订单 Controller
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackageOrderRentRefund")
public class JsonAdminCarRentalPackageOrderRentRefundController extends BasicController {

    @Resource
    private CarRentalPackageOrderBizService carRentalPackageOrderBizService;

    @Resource
    private CarRentalPackageOrderRentRefundService carRentalPackageOrderRentRefundService;

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

        return R.ok(carRentalPackageOrderBizService.approveRefundRentOrder(optReq.getOrderNo(), false, optReq.getReason(), user.getUid()));
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

        return R.ok(carRentalPackageOrderBizService.approveRefundRentOrder(optReq.getOrderNo(), true, optReq.getReason(), user.getUid()));
    }

    /**
     * 条件查询列表
     * @param queryReq 请求参数类
     * @return 退租订单集
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageOrderRentRefundVO>> page(@RequestBody CarRentalPackageOrderRentRefundQryReq queryReq) {
        if (null == queryReq) {
            queryReq = new CarRentalPackageOrderRentRefundQryReq();
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
        CarRentalPackageOrderRentRefundQryModel qryModel = new CarRentalPackageOrderRentRefundQryModel();
        BeanUtils.copyProperties(queryReq, qryModel);
        qryModel.setFranchiseeIdList(permissionTriple.getLeft());
        qryModel.setStoreIdList(permissionTriple.getMiddle());

        // 调用服务
        List<CarRentalPackageOrderRentRefundPO> refundPOList = carRentalPackageOrderRentRefundService.page(qryModel);
        if (CollectionUtils.isEmpty(refundPOList)) {
            return R.ok(Collections.emptyList());
        }

        // 获取辅助业务信息（用户信息、租车套餐信息）
        Set<Long> uids = new HashSet<>();
        Set<Long> rentalPackageIdIds = new HashSet<>();
        refundPOList.forEach(refundPO -> {
            uids.add(refundPO.getUid());
            rentalPackageIdIds.add(refundPO.getRentalPackageId());
        });

        // 用户信息
        Map<Long, UserInfo> userInfoMap = getUserInfoByUidsForMap(uids);

        // 租车套餐信息
        Map<Long, String> carRentalPackageNameMap = getCarRentalPackageNameByIdsForMap(rentalPackageIdIds);

        // 模型转换，封装返回
        List<CarRentalPackageOrderRentRefundVO> rentRefundVOList = refundPOList.stream().map(rentRefundPO -> {

            CarRentalPackageOrderRentRefundVO rentRefundVO = new CarRentalPackageOrderRentRefundVO();
            BeanUtils.copyProperties(rentRefundPO, rentRefundVO);

            if (!userInfoMap.isEmpty()) {
                UserInfo userInfo = userInfoMap.getOrDefault(rentRefundPO.getUid(), new UserInfo());
                rentRefundVO.setUserRelName(userInfo.getName());
                rentRefundVO.setUserPhone(userInfo.getPhone());
            }

            if (!carRentalPackageNameMap.isEmpty()) {
                rentRefundVO.setCarRentalPackageName(carRentalPackageNameMap.getOrDefault(rentRefundPO.getRentalPackageId(), ""));
            }

            return rentRefundVO;
        }).collect(Collectors.toList());

        return R.ok(rentRefundVOList);
    }

    /**
     * 条件查询总数
     * @param qryReq 请求参数类
     * @return 总数
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalPackageOrderRentRefundQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageOrderRentRefundQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);

        // 数据权校验
        Triple<List<Integer>, List<Integer>, Boolean> permissionTriple = checkPermissionInteger();
        if (!permissionTriple.getRight()) {
            return R.ok(0);
        }

        // 转换请求体
        CarRentalPackageOrderRentRefundQryModel qryModel = new CarRentalPackageOrderRentRefundQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);
        qryModel.setFranchiseeIdList(permissionTriple.getLeft());
        qryModel.setStoreIdList(permissionTriple.getMiddle());

        // 调用服务
        return R.ok(carRentalPackageOrderRentRefundService.count(qryModel));
    }

}
