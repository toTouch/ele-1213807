package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPo;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositPayQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageDepositPayQryReq;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.car.CarRentalPackageDepositPayVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 租车套餐押金缴纳 Controller
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackageDepositPay")
public class JsonAdminCarRentalPackageDepositPayController extends BasicController {

    @Resource
    private CarRentalPackageDepositPayService carRentalPackageDepositPayService;

    /**
     * 根据订单编号同步免押状态
     * @param orderNo 押金缴纳订单编码
     * @return true(成功)、false(失败)
     */
    @GetMapping("/syncFreeState")
    public R<Boolean> syncFreeState(String orderNo) {
        // TODO 实现逻辑
        return null;
    }

    /**
     * 条件分页查询
     * @param queryReq 请求参数类
     * @return 押金缴纳订单集
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageDepositPayVo>> page(@RequestBody CarRentalPackageDepositPayQryReq queryReq) {
        if (null == queryReq) {
            queryReq = new CarRentalPackageDepositPayQryReq();
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
        CarRentalPackageDepositPayQryModel qryModel = new CarRentalPackageDepositPayQryModel();
        BeanUtils.copyProperties(queryReq, qryModel);
        qryModel.setFranchiseeIdList(permissionTriple.getLeft());
        qryModel.setStoreIdList(permissionTriple.getMiddle());

        // 调用服务
        List<CarRentalPackageDepositPayPo> depositPayEntityList = carRentalPackageDepositPayService.page(qryModel);
        if (CollectionUtils.isEmpty(depositPayEntityList)) {
            return R.ok(Collections.emptyList());
        }


        // 获取辅助业务信息（用户信息）
        Set<Long> uids = depositPayEntityList.stream().map(CarRentalPackageDepositPayPo::getUid).collect(Collectors.toSet());

        // 用户信息
        Map<Long, UserInfo> userInfoMap = getUserInfoByUidsForMap(uids);

        // 模型转换，封装返回
        List<CarRentalPackageDepositPayVo> depositPayVOList = depositPayEntityList.stream().map(depositPayEntity -> {
            CarRentalPackageDepositPayVo depositPayVO = new CarRentalPackageDepositPayVo();
            BeanUtils.copyProperties(depositPayEntity, depositPayVO);

            if (!userInfoMap.isEmpty()) {
                UserInfo userInfo = userInfoMap.getOrDefault(depositPayEntity.getUid(), new UserInfo());
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
     * @return 总数
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalPackageDepositPayQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageDepositPayQryReq();
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
        CarRentalPackageDepositPayQryModel qryModel = new CarRentalPackageDepositPayQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);
        qryModel.setFranchiseeIdList(permissionTriple.getLeft());
        qryModel.setStoreIdList(permissionTriple.getMiddle());

        // 调用服务
        return R.ok(carRentalPackageDepositPayService.count(qryModel));
    }

}
