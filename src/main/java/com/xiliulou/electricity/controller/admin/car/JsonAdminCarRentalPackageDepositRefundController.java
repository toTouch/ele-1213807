package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPo;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageDepositRefundOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositRefundQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageDepositRefundQryReq;
import com.xiliulou.electricity.query.car.audit.AuditOptReq;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageDepositBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageDepositRefundVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 租车套餐押金退押 Controller
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackageDepositRefund")
public class JsonAdminCarRentalPackageDepositRefundController extends BasicController {

    @Resource
    private CarRenalPackageDepositBizService carRenalPackageDepositResource;

    @Resource
    private CarRentalPackageDepositRefundService carRentalPackageDepositRefundService;

    /**
     * 创建退押
     * @param optModel 操作实体类
     * @return true(成功)、false(失败)
     */
    @PostMapping("/create")
    public R<Boolean> create(@RequestBody CarRentalPackageDepositRefundOptModel optModel) {
        if (!ObjectUtils.allNotNull(optModel, optModel.getUid(), optModel.getRealAmount(), optModel.getDepositPayOrderNo())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        optModel.setTenantId(tenantId);

        return R.ok(carRenalPackageDepositResource.refundDepositCreate(optModel));
    }

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

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRenalPackageDepositResource.approveRefundDepositOrder(optReq.getOrderNo(), false, optReq.getReason(), user.getUid(), null));
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

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRenalPackageDepositResource.approveRefundDepositOrder(optReq.getOrderNo(), true, optReq.getReason(), user.getUid(), optReq.getAmount()));
    }

    /**
     * 条件分页查询
     * @param queryReq 请求参数类
     * @return 退押订单集
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageDepositRefundVo>> page(@RequestBody CarRentalPackageDepositRefundQryReq queryReq) {
        if (null == queryReq) {
            queryReq = new CarRentalPackageDepositRefundQryReq();
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        queryReq.setTenantId(tenantId);

        // 数据权校验
        Triple<List<Integer>, List<Integer>, Boolean> permissionTriple = checkPermissionInteger();
        if (!permissionTriple.getRight()) {
            return R.ok(Collections.emptyList());
        }

        // 转换请求体
        CarRentalPackageDepositRefundQryModel qryModel = new CarRentalPackageDepositRefundQryModel();
        BeanUtils.copyProperties(queryReq, qryModel);
        qryModel.setFranchiseeIdList(permissionTriple.getLeft());
        qryModel.setStoreIdList(permissionTriple.getMiddle());

        // 调用服务
        List<CarRentalPackageDepositRefundPo> depositRefundEntityList = carRentalPackageDepositRefundService.page(qryModel);
        if (CollectionUtils.isEmpty(depositRefundEntityList)) {
            return R.ok(Collections.emptyList());
        }

        // 获取辅助业务信息（用户信息）
        Set<Long> uids = depositRefundEntityList.stream().map(CarRentalPackageDepositRefundPo::getUid).collect(Collectors.toSet());

        // 用户信息
        Map<Long, UserInfo> userInfoMap = getUserInfoByUidsForMap(uids);

        // 模型转换，封装返回
        List<CarRentalPackageDepositRefundVo> depositRefundVoList = depositRefundEntityList.stream().map(depositRefundEntity -> {

            CarRentalPackageDepositRefundVo depositRefundVO = new CarRentalPackageDepositRefundVo();
            BeanUtils.copyProperties(depositRefundEntity, depositRefundVO);

            if (!userInfoMap.isEmpty()) {
                UserInfo userInfo = userInfoMap.getOrDefault(depositRefundEntity.getUid(), new UserInfo());
                depositRefundVO.setUserRelName(userInfo.getName());
                depositRefundVO.setUserPhone(userInfo.getPhone());
            }

            return depositRefundVO;
        }).collect(Collectors.toList());

        return R.ok(depositRefundVoList);
    }

    /**
     * 条件查询总数
     * @param qryReq 请求参数类
     * @return 总数
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalPackageDepositRefundQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageDepositRefundQryReq();
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);

        // 数据权校验
        Triple<List<Integer>, List<Integer>, Boolean> permissionTriple = checkPermissionInteger();
        if (!permissionTriple.getRight()) {
            return R.ok(NumberConstant.ZERO);
        }

        // 转换请求体
        CarRentalPackageDepositRefundQryModel qryModel = new CarRentalPackageDepositRefundQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);
        qryModel.setFranchiseeIdList(permissionTriple.getLeft());
        qryModel.setStoreIdList(permissionTriple.getMiddle());

        // 调用服务
        return R.ok(carRentalPackageDepositRefundService.count(qryModel));
    }

}
