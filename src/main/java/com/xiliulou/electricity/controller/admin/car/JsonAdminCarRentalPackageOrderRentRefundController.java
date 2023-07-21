package com.xiliulou.electricity.controller.admin.car;

import com.alibaba.fastjson.JSON;
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
 * TODO 权限后补
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
     * @return
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
     * @return
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
     * @return
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageOrderRentRefundVO>> page(@RequestBody CarRentalPackageOrderRentRefundQryReq queryReq) {
        if (null == queryReq) {
            queryReq = new CarRentalPackageOrderRentRefundQryReq();
            // TODO mock数据
            String mockString = "[\n" +
                    "    {\n" +
                    "        \"orderNo\":\"7373849302938472\",\n" +
                    "        \"rentalPackageOrderNo\":\"6273849982273617\",\n" +
                    "        \"rentalPackageType\":1,\n" +
                    "        \"residue\":123,\n" +
                    "        \"residueUnit\":0,\n" +
                    "        \"refundAmount\":760,\n" +
                    "        \"refundState\":1,\n" +
                    "        \"rentUnitPrice\":320,\n" +
                    "        \"rentPayment\":1110,\n" +
                    "        \"remark\":\"备注\",\n" +
                    "        \"createTime\":1690267754000,\n" +
                    "        \"updateTime\":1690267754000,\n" +
                    "        \"userRelName\":\"张三\",\n" +
                    "        \"userPhone\":\"17689876263\",\n" +
                    "        \"carRentalPackageName\":\"单车套餐\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"orderNo\":\"7362839482738163\",\n" +
                    "        \"rentalPackageOrderNo\":\"6273837982273617\",\n" +
                    "        \"rentalPackageType\":2,\n" +
                    "        \"residue\":234,\n" +
                    "        \"residueUnit\":1,\n" +
                    "        \"refundAmount\":220,\n" +
                    "        \"refundState\":2,\n" +
                    "        \"rentUnitPrice\":230,\n" +
                    "        \"rentPayment\":2220,\n" +
                    "        \"remark\":\"备注\",\n" +
                    "        \"createTime\":1690267754000,\n" +
                    "        \"updateTime\":1690267754000,\n" +
                    "        \"userRelName\":\"李四\",\n" +
                    "        \"userPhone\":\"166273898256\",\n" +
                    "        \"carRentalPackageName\":\"车电一体套餐\"\n" +
                    "    }\n" +
                    "]";
            return R.ok(JSON.parseArray(mockString, CarRentalPackageOrderRentRefundVO.class));
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        queryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalPackageOrderRentRefundQryModel qryModel = new CarRentalPackageOrderRentRefundQryModel();
        BeanUtils.copyProperties(queryReq, qryModel);

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
     * @return
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalPackageOrderRentRefundQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageOrderRentRefundQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalPackageOrderRentRefundQryModel qryModel = new CarRentalPackageOrderRentRefundQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);

        // 调用服务
        return R.ok(carRentalPackageOrderRentRefundService.count(qryModel));
    }

}
