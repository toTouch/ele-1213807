package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageOrderQryReq;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
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
 * 租车套餐订单表 Controller
 * TODO 权限后补
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackageOrder")
public class JsonAdminCarRentalPackageOrderController extends BasicController {

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    /**
     * 条件查询列表
     * @param queryReq 请求参数类
     * @return
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageOrderVO>> page(@RequestBody CarRentalPackageOrderQryReq queryReq) {
        if (null == queryReq) {
            queryReq = new CarRentalPackageOrderQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        queryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalPackageOrderQryModel qryModel = new CarRentalPackageOrderQryModel();
        BeanUtils.copyProperties(queryReq, qryModel);

        // 调用服务
        List<CarRentalPackageOrderPO> carRentalPackageOrderPOList = carRentalPackageOrderService.page(qryModel);
        if (CollectionUtils.isEmpty(carRentalPackageOrderPOList)) {
            return R.ok();
        }

        // 获取辅助业务信息（用户信息、套餐名称、加盟商信息、柜机信息）
        Set<Long> uids = new HashSet<>();
        Set<Long> rentalPackageIds = new HashSet<>();
        Set<Long> franchiseeIds = new HashSet<>();
        carRentalPackageOrderPOList.forEach(carRentalPackageOrder -> {
            uids.add(carRentalPackageOrder.getUid());
            rentalPackageIds.add(carRentalPackageOrder.getRentalPackageId());
            franchiseeIds.add(Long.valueOf(carRentalPackageOrder.getFranchiseeId()));
        });

        // 用户信息
        Map<Long, UserInfo> userInfoMap = getUserInfoByUidsForMap(uids);

        // 套餐信息
        Map<Long, String> carRentalPackageMap = getCarRentalPackageNameByIdsForMap(rentalPackageIds);

        // 加盟商信息
        Map<Long, String> franchiseeMap = getFranchiseeNameByIdsForMap(franchiseeIds);

        // 模型转换，封装返回
        List<CarRentalPackageOrderVO> carRentalPackageVOList = carRentalPackageOrderPOList.stream().map(carRentalPackageOrder -> {

            CarRentalPackageOrderVO carRentalPackageOrderVO = new CarRentalPackageOrderVO();
            BeanUtils.copyProperties(carRentalPackageOrder, carRentalPackageOrderVO);

            if (!userInfoMap.isEmpty()) {
                UserInfo userInfo = userInfoMap.getOrDefault(carRentalPackageOrder.getUid(), new UserInfo());
                carRentalPackageOrderVO.setUserRelName(userInfo.getName());
                carRentalPackageOrderVO.setUserPhone(userInfo.getPhone());
            }

            if (!carRentalPackageMap.isEmpty()) {
                carRentalPackageOrderVO.setCarRentalPackageName(carRentalPackageMap.getOrDefault(carRentalPackageOrder.getRentalPackageId(), ""));
            }

            if (!franchiseeMap.isEmpty()) {
                carRentalPackageOrderVO.setFranchiseeName(franchiseeMap.getOrDefault(Long.valueOf(carRentalPackageOrder.getFranchiseeId()), ""));
            }

            return carRentalPackageOrderVO;
        }).collect(Collectors.toList());

        return R.ok(carRentalPackageVOList);
    }

    /**
     * 条件查询总数
     * @param qryReq 请求参数类
     * @return
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalPackageOrderQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageOrderQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalPackageOrderQryModel qryModel = new CarRentalPackageOrderQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);

        // 调用服务
        return R.ok(carRentalPackageOrderService.count(qryModel));
    }
}
