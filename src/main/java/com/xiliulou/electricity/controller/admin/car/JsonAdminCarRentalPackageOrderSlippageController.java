package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderSlippageQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageOrderSlippageQryReq;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderSlippageVO;
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
 * 租车套餐订单逾期订单 Controller
 * TODO 权限后补
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackageOrderSlippage")
public class JsonAdminCarRentalPackageOrderSlippageController extends JsonAdminCarBasicController {

    @Resource
    private CarRentalPackageOrderSlippageService carRentalPackageOrderSlippageService;

    /**
     * 条件查询列表
     * @param queryReq 请求参数类
     * @return
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageOrderSlippageVO>> page(@RequestBody CarRentalPackageOrderSlippageQryReq queryReq) {
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
        R<List<CarRentalPackageOrderSlippagePO>> listRes = carRentalPackageOrderSlippageService.page(qryModel);
        if (!listRes.isSuccess()) {
            return R.fail(listRes.getErrCode(), listRes.getErrMsg());
        }
        List<CarRentalPackageOrderSlippagePO> slippagePOList = listRes.getData();

        // 获取辅助业务信息（用户信息、加盟商信息）
        Set<Long> uids = new HashSet<>();
        Set<Long> franchiseeIds = new HashSet<>();
        slippagePOList.forEach(slippagePO -> {
            uids.add(slippagePO.getUid());
            franchiseeIds.add(Long.valueOf(slippagePO.getFranchiseeId()));
        });

        // 用户信息
        Map<Long, UserInfo> userInfoMap = getUserInfoByUidsForMap(uids);

        // 加盟商信息
        Map<Long, String> franchiseeNameMap = getFranchiseeNameByIdsForMap(franchiseeIds);

        // 模型转换，封装返回
        List<CarRentalPackageOrderSlippageVO> slippageVOList = slippagePOList.stream().map(slippagePO -> {
            CarRentalPackageOrderSlippageVO slippageVO = new CarRentalPackageOrderSlippageVO();
            BeanUtils.copyProperties(slippagePO, slippageVO);
            if (!userInfoMap.isEmpty()) {
                UserInfo userInfo = userInfoMap.getOrDefault(slippagePO.getUid(), new UserInfo());
                slippageVO.setUserRelName(userInfo.getName());
                slippageVO.setUserPhone(userInfo.getPhone());
            }
            if (!franchiseeNameMap.isEmpty()) {
                slippageVO.setFranchiseeName(franchiseeNameMap.getOrDefault(Long.valueOf(slippagePO.getStoreId()), ""));
            }
            return slippageVO;
        }).collect(Collectors.toList());

        return R.ok(slippageVOList);
    }

    /**
     * 条件查询总数
     * @param qryReq 请求参数类
     * @return
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
        return carRentalPackageOrderSlippageService.count(qryModel);
    }

}
