package com.xiliulou.electricity.controller.admin.car;

import com.alibaba.fastjson.JSON;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderSlippageQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageOrderSlippageQryReq;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderSlippageVO;
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
 * 租车套餐订单逾期订单 Controller
 * TODO 权限后补
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackageOrderSlippage")
public class JsonAdminCarRentalPackageOrderSlippageController extends BasicController {

    @Resource
    private CarRentalPackageOrderSlippageService carRentalPackageOrderSlippageService;

    /**
     * 条件查询列表
     * @param queryReq 请求参数类
     * @return 逾期订单集
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageOrderSlippageVO>> page(@RequestBody CarRentalPackageOrderSlippageQryReq queryReq) {
        // TODO mock数据
        if (true) {
            String mockString = "[\n" +
                    "    {\n" +
                    "        \"orderNo\":\"2637284938717263\",\n" +
                    "        \"type\":1,\n" +
                    "        \"carSn\":\"172839212\",\n" +
                    "        \"batterySn\":\"7382042\",\n" +
                    "        \"lateFee\":122,\n" +
                    "        \"lateFeeStartTime\":1690267754000,\n" +
                    "        \"payState\":1,\n" +
                    "        \"createTime\":1690267754000,\n" +
                    "        \"franchiseeName\":\"加盟商名称\",\n" +
                    "        \"userRelName\":\"张三\",\n" +
                    "        \"userPhone\":\"18726279384\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"orderNo\":\"9837283400912893\",\n" +
                    "        \"type\":2,\n" +
                    "        \"carSn\":\"98264929s8\",\n" +
                    "        \"batterySn\":\"sjshd8292\",\n" +
                    "        \"lateFee\":230,\n" +
                    "        \"lateFeeStartTime\":1690267754000,\n" +
                    "        \"lateFeePay\":256,\n" +
                    "        \"payState\":2,\n" +
                    "        \"createTime\":1690267754000,\n" +
                    "        \"franchiseeName\":\"加盟商名称\",\n" +
                    "        \"userRelName\":\"李四\",\n" +
                    "        \"userPhone\":\"198273683167\"\n" +
                    "    }\n" +
                    "]";
            return R.ok(JSON.parseArray(mockString, CarRentalPackageOrderSlippageVO.class));
        }
        if (null == queryReq) {
            queryReq = new CarRentalPackageOrderSlippageQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        queryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalPackageOrderSlippageQryModel qryModel = new CarRentalPackageOrderSlippageQryModel();
        BeanUtils.copyProperties(queryReq, qryModel);

        // 调用服务
        List<CarRentalPackageOrderSlippagePO> slippageEntityList = carRentalPackageOrderSlippageService.page(qryModel);
        if (CollectionUtils.isEmpty(slippageEntityList)) {
            return R.ok(Collections.emptyList());
        }

        // 获取辅助业务信息（用户信息、加盟商信息）
        Set<Long> uids = new HashSet<>();
        Set<Long> franchiseeIds = new HashSet<>();
        slippageEntityList.forEach(slippagePO -> {
            uids.add(slippagePO.getUid());
            franchiseeIds.add(Long.valueOf(slippagePO.getFranchiseeId()));
        });

        // 用户信息
        Map<Long, UserInfo> userInfoMap = getUserInfoByUidsForMap(uids);

        // 加盟商信息
        Map<Long, String> franchiseeNameMap = getFranchiseeNameByIdsForMap(franchiseeIds);

        // 模型转换，封装返回
        List<CarRentalPackageOrderSlippageVO> slippageVOList = slippageEntityList.stream().map(slippageEntity -> {

            CarRentalPackageOrderSlippageVO slippageVo = new CarRentalPackageOrderSlippageVO();
            BeanUtils.copyProperties(slippageEntity, slippageVo);

            if (!userInfoMap.isEmpty()) {
                UserInfo userInfo = userInfoMap.getOrDefault(slippageEntity.getUid(), new UserInfo());
                slippageVo.setUserRelName(userInfo.getName());
                slippageVo.setUserPhone(userInfo.getPhone());
            }

            if (!franchiseeNameMap.isEmpty()) {
                slippageVo.setFranchiseeName(franchiseeNameMap.getOrDefault(Long.valueOf(slippageEntity.getStoreId()), ""));
            }

            return slippageVo;
        }).collect(Collectors.toList());

        return R.ok(slippageVOList);
    }

    /**
     * 条件查询总数
     * @param qryReq 请求参数类
     * @return 总数
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalPackageOrderSlippageQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageOrderSlippageQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalPackageOrderSlippageQryModel qryModel = new CarRentalPackageOrderSlippageQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);

        // 调用服务
        return R.ok(carRentalPackageOrderSlippageService.count(qryModel));
    }

}
