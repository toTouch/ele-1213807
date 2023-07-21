package com.xiliulou.electricity.controller.admin.car;

import com.alibaba.fastjson.JSON;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPO;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageDepositRefundOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositRefundQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageDepositRefundQryReq;
import com.xiliulou.electricity.query.car.audit.AuditOptReq;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageDepositBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageDepositRefundVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 租车套餐押金退押 Controller
 * TODO 权限后补
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
    @GetMapping("/create")
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
        if (!ObjectUtils.allNotNull(optReq, optReq.getOrderNo(), optReq.getAmount())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRenalPackageDepositResource.approveRefundDepositOrder(optReq.getOrderNo(), true, null, user.getUid(), optReq.getAmount()));
    }

    /**
     * 条件分页查询
     * @param queryReq 请求参数类
     * @return 退押订单集
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageDepositRefundVO>> page(@RequestBody CarRentalPackageDepositRefundQryReq queryReq) {
        // TODO mock数据
        if (true) {
            String mockString = "[\n" +
                    "    {\n" +
                    "        \"orderNo\":\"111111111\",\n" +
                    "        \"depositPayOrderNo\":\"111111111\",\n" +
                    "        \"applyAmount\":99,\n" +
                    "        \"realAmount\":90,\n" +
                    "        \"payType\":1,\n" +
                    "        \"refundState\":1,\n" +
                    "        \"createTime\":1689749354000,\n" +
                    "        \"remark\":\"\",\n" +
                    "        \"userRelName\":\"张三\",\n" +
                    "        \"userPhone\":\"13384937843\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"orderNo\":\"\",\n" +
                    "        \"depositPayOrderNo\":\"\",\n" +
                    "        \"applyAmount\":0,\n" +
                    "        \"realAmount\":0,\n" +
                    "        \"payType\":0,\n" +
                    "        \"refundState\":0,\n" +
                    "        \"createTime\":1689404354000,\n" +
                    "        \"remark\":\"我是备注\",\n" +
                    "        \"userRelName\":\"李四\",\n" +
                    "        \"userPhone\":\"13243256746\"\n" +
                    "    }\n" +
                    "]";
            return R.ok(JSON.parseArray(mockString, CarRentalPackageDepositRefundVO.class));
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (null == queryReq) {
            queryReq = new CarRentalPackageDepositRefundQryReq();
        }

        queryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalPackageDepositRefundQryModel qryModel = new CarRentalPackageDepositRefundQryModel();
        BeanUtils.copyProperties(queryReq, qryModel);

        // 调用服务
        List<CarRentalPackageDepositRefundPO> depositRefundEntityList = carRentalPackageDepositRefundService.page(qryModel);
        if (CollectionUtils.isEmpty(depositRefundEntityList)) {
            return R.ok(Collections.emptyList());
        }

        // 获取辅助业务信息（用户信息）
        Set<Long> uids = depositRefundEntityList.stream().map(CarRentalPackageDepositRefundPO::getUid).collect(Collectors.toSet());

        // 用户信息
        Map<Long, UserInfo> userInfoMap = getUserInfoByUidsForMap(uids);

        // 模型转换，封装返回
        List<CarRentalPackageDepositRefundVO> depositRefundVoList = depositRefundEntityList.stream().map(depositRefundEntity -> {

            CarRentalPackageDepositRefundVO depositRefundVO = new CarRentalPackageDepositRefundVO();
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
     * @return
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalPackageDepositRefundQryReq qryReq) {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (null == qryReq) {
            qryReq = new CarRentalPackageDepositRefundQryReq();
        }

        qryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalPackageDepositRefundQryModel qryModel = new CarRentalPackageDepositRefundQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);

        // 调用服务
        return R.ok(carRentalPackageDepositRefundService.count(qryModel));
    }

}
