package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositPayQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageDepositPayQryReq;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.car.CarRentalPackageDepositPayVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 租车套餐押金缴纳 Controller
 * TODO 权限后补
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
     * @param orderNo
     * @return
     */
    @GetMapping("/syncExemptState")
    public R<Boolean> syncExemptState(String orderNo) {
        // TODO 实现逻辑
        return null;
    }

    /**
     * 条件分页查询
     * @param queryReq 请求参数类
     * @return
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageDepositPayVO>> page(@RequestBody CarRentalPackageDepositPayQryReq queryReq) {
        if (null == queryReq) {
            queryReq = new CarRentalPackageDepositPayQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        queryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalPackageDepositPayQryModel qryModel = new CarRentalPackageDepositPayQryModel();
        BeanUtils.copyProperties(queryReq, qryModel);

        // 调用服务
        List<CarRentalPackageDepositPayPO> depositPayEntityList = carRentalPackageDepositPayService.page(qryModel);
        if (CollectionUtils.isEmpty(depositPayEntityList)) {
            return R.ok();
        }


        // 获取辅助业务信息（用户信息）
        Set<Long> uids = new HashSet<>();
        depositPayEntityList.forEach(carRentalPackageOrder -> {
            uids.add(carRentalPackageOrder.getUid());
        });

        // 用户信息
        Map<Long, UserInfo> userInfoMap = getUserInfoByUidsForMap(uids);

        // 模型转换，封装返回
        List<CarRentalPackageDepositPayVO> depositPayVOList = depositPayEntityList.stream().map(depositPayPO -> {
            CarRentalPackageDepositPayVO depositPayVO = new CarRentalPackageDepositPayVO();
            BeanUtils.copyProperties(depositPayPO, depositPayVO);

            if (!userInfoMap.isEmpty()) {
                UserInfo userInfo = userInfoMap.getOrDefault(depositPayPO.getUid(), new UserInfo());
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
    public R<Integer> count(@RequestBody CarRentalPackageDepositPayQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageDepositPayQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalPackageDepositPayQryModel qryModel = new CarRentalPackageDepositPayQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);

        // 调用服务
        return R.ok(carRentalPackageDepositPayService.count(qryModel));
    }

}
