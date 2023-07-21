package com.xiliulou.electricity.controller.admin.car;

import com.alibaba.fastjson.JSON;
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
import java.util.*;
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
            // TODO mock数据
            String mockString = "[\n" +
                    "    {\n" +
                    "        \"orderNo\":\"3627384938274829\",\n" +
                    "        \"rentalPackageType\":1,\n" +
                    "        \"confine\":0,\n" +
                    "        \"tenancy\":7,\n" +
                    "        \"tenancyUnit\":0,\n" +
                    "        \"rent\":123,\n" +
                    "        \"rentPayment\":100,\n" +
                    "        \"applicableType\":0,\n" +
                    "        \"rentRebate\":1,\n" +
                    "        \"depositPayOrderNo\":\"2637284938274839\",\n" +
                    "        \"lateFee\":288,\n" +
                    "        \"payType\":1,\n" +
                    "        \"couponId\":123456,\n" +
                    "        \"payState\":2,\n" +
                    "        \"useState\":2,\n" +
                    "        \"remark\":\"\",\n" +
                    "        \"createTime\":1689749354000,\n" +
                    "        \"franchiseeName\":\"我是加盟商\",\n" +
                    "        \"userRelName\":\"张三\",\n" +
                    "        \"userPhone\":\"15426374839\",\n" +
                    "        \"carRentalPackageName\":\"我是单车套餐\",\n" +
                    "        \"carModelName\":\"我是车辆型号\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"orderNo\":\"2637485039281738\",\n" +
                    "        \"rentalPackageType\":2,\n" +
                    "        \"confine\":1,\n" +
                    "        \"confineNum\":20,\n" +
                    "        \"tenancy\":99,\n" +
                    "        \"tenancyUnit\":1,\n" +
                    "        \"rentUnitPrice\":23,\n" +
                    "        \"rent\":760,\n" +
                    "        \"rentPayment\":380,\n" +
                    "        \"applicableType\":1,\n" +
                    "        \"rentRebate\":0,\n" +
                    "        \"rentRebateTerm\":7,\n" +
                    "        \"rentRebateEndTime\":1690267754000,\n" +
                    "        \"depositPayOrderNo\":\"2736483948392811\",\n" +
                    "        \"lateFee\":188,\n" +
                    "        \"payType\":2,\n" +
                    "        \"couponId\":1234,\n" +
                    "        \"payState\":2,\n" +
                    "        \"useState\":1,\n" +
                    "        \"remark\":\"我是备注啊\",\n" +
                    "        \"createTime\":1689749354000,\n" +
                    "        \"franchiseeName\":\"我是加盟商啊\",\n" +
                    "        \"userRelName\":\"李四\",\n" +
                    "        \"userPhone\":\"17672637489\",\n" +
                    "        \"carRentalPackageName\":\"我是车电一体套餐\",\n" +
                    "        \"carModelName\":\"我是车辆型号啊\"\n" +
                    "    }\n" +
                    "]";
            return R.ok(JSON.parseArray(mockString, CarRentalPackageOrderVO.class));
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
            return R.ok(Collections.emptyList());
        }

        // 获取辅助业务信息（用户信息、套餐名称、加盟商信息）
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
