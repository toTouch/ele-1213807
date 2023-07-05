package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositRefundQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageDepositRefundQryReq;
import com.xiliulou.electricity.query.car.audit.AuditOptReq;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.car.CarRentalPackageDepositPayVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 租车套餐押金退押 Controller
 * TODO 权限后补
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackageDepositRefund")
public class JsonAdminCarRentalPackageDepositRefundController extends JsonAdminCarBasicController {

    @Resource
    private CarRentalPackageDepositRefundService carRentalPackageDepositRefundService;


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
     * 条件分页查询
     * @param queryReq 请求参数类
     * @return
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageDepositPayVO>> page(@RequestBody CarRentalPackageDepositRefundQryReq queryReq) {
        if (null == queryReq) {
            queryReq = new CarRentalPackageDepositRefundQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        queryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalPackageDepositRefundQryModel qryModel = new CarRentalPackageDepositRefundQryModel();
        BeanUtils.copyProperties(queryReq, qryModel);

        // 调用服务
        R<List<CarRentalPackageDepositRefundPO>> listRes = carRentalPackageDepositRefundService.page(qryModel);
        if (!listRes.isSuccess()) {
            return R.fail(listRes.getErrCode(), listRes.getErrMsg());
        }
        List<CarRentalPackageDepositRefundPO> depositRefundPOList = listRes.getData();

        // 获取辅助业务信息（用户信息）
        Set<Long> uids = depositRefundPOList.stream().map(CarRentalPackageDepositRefundPO::getUid).collect(Collectors.toSet());

        // 用户信息
        Map<Long, UserInfo> userInfoMap = getUserInfoByUidsForMap(uids);

        // 模型转换，封装返回
        List<CarRentalPackageDepositPayVO> depositPayVOList = depositRefundPOList.stream().map(depositRefundPO -> {

            CarRentalPackageDepositPayVO depositPayVO = new CarRentalPackageDepositPayVO();
            BeanUtils.copyProperties(depositRefundPO, depositPayVO);

            if (!userInfoMap.isEmpty()) {
                UserInfo userInfo = userInfoMap.getOrDefault(depositRefundPO.getUid(), new UserInfo());
                depositPayVO.setUserRelName(userInfo.getName());
                depositPayVO.setUserPhone(userInfo.getPhone());
            }

            return depositPayVO;
        }).collect(Collectors.toList());

        return R.ok(depositPayVOList);
    }

    /**
     * 条件查询总数
     * @param qryReq 请求参数类
     * @return
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalPackageDepositRefundQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageDepositRefundQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalPackageDepositRefundQryModel qryModel = new CarRentalPackageDepositRefundQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);

        // 调用服务
        return carRentalPackageDepositRefundService.count(qryModel);
    }

}
