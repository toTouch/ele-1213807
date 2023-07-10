package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalOrderPO;
import com.xiliulou.electricity.model.car.query.CarRentalOrderQryModel;
import com.xiliulou.electricity.query.car.CarRentalOrderQryReq;
import com.xiliulou.electricity.query.car.audit.AuditOptReq;
import com.xiliulou.electricity.service.car.CarRentalOrderService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.car.CarRentalOrderVO;
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
 * 车辆租赁订单操作 Controller
 * @author xiaohui.song
 * TODO 权限后补
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalOrder")
public class JsonAdminCarRentalOrderController extends BasicController {

    @Resource
    private CarRentalOrderService carRentalOrderService;

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
    public R<List<CarRentalOrderVO>> page(@RequestBody CarRentalOrderQryReq queryReq) {
        if (null == queryReq) {
            queryReq = new CarRentalOrderQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        queryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalOrderQryModel qryModel = new CarRentalOrderQryModel();
        BeanUtils.copyProperties(queryReq, qryModel);

        // 调用服务
        R<List<CarRentalOrderPO>> listRes = carRentalOrderService.page(qryModel);
        if (!listRes.isSuccess()) {
            return R.fail(listRes.getErrCode(), listRes.getErrMsg());
        }
        List<CarRentalOrderPO> rentalOrderPOList = listRes.getData();

        // 获取辅助业务信息（用户信息、车辆型号信息、门店信息）
        Set<Long> uids = new HashSet<>();
        Set<Integer> carModelIds = new HashSet<>();
        Set<Long> storeIds = new HashSet<>();
        rentalOrderPOList.forEach(rentalOrderPO -> {
            uids.add(rentalOrderPO.getUid());
            carModelIds.add(rentalOrderPO.getCarModelId());
            storeIds.add(Long.valueOf(rentalOrderPO.getStoreId()));
        });

        // 用户信息
        Map<Long, UserInfo> userInfoMap = getUserInfoByUidsForMap(uids);

        // 车辆型号信息
        Map<Integer, String> carModelMap = getCarModelNameByIdsForMap(carModelIds);

        // 门店信息
        Map<Long, String> storeMap = getStoreNameByIdsForMap(storeIds);

        // 模型转换，封装返回
        List<CarRentalOrderVO> carRentalPackageVOList = rentalOrderPOList.stream().map(rentalOrderPO -> {

            CarRentalOrderVO rentalOrderVO = new CarRentalOrderVO();
            BeanUtils.copyProperties(rentalOrderPO, rentalOrderVO);

            if (!userInfoMap.isEmpty()) {
                UserInfo userInfo = userInfoMap.getOrDefault(rentalOrderPO.getUid(), new UserInfo());
                rentalOrderVO.setUserRelName(userInfo.getName());
                rentalOrderVO.setUserPhone(userInfo.getPhone());
            }

            if (!carModelMap.isEmpty()) {
                rentalOrderVO.setCarModelName(carModelMap.getOrDefault(rentalOrderPO.getCarModelId(), ""));
            }

            if (!storeMap.isEmpty()) {
                rentalOrderVO.setStoreName(storeMap.getOrDefault(Long.valueOf(rentalOrderPO.getStoreId()), ""));
            }

            return rentalOrderVO;
        }).collect(Collectors.toList());

        return R.ok(carRentalPackageVOList);
    }

    /**
     * 条件查询总数
     * @param qryReq 请求参数类
     * @return
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalOrderQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalOrderQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalOrderQryModel qryModel = new CarRentalOrderQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);

        // 调用服务
        return carRentalOrderService.count(qryModel);
    }

}
