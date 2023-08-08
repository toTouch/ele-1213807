package com.xiliulou.electricity.controller.user.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderRentRefundPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderBuyOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageOrderQryReq;
import com.xiliulou.electricity.reqparam.opt.carpackage.FreezeRentOrderOptReq;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderVo;
import com.xiliulou.electricity.vo.rental.RefundRentOrderHintVo;
import com.xiliulou.electricity.vo.rental.RentalPackageVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 租车套餐订单相关的 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/renalPackageOrder")
public class JsonUserCarRenalPackageOrderController extends BasicController {

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Resource
    private CarRentalPackageOrderBizService carRentalPackageOrderBizService;

    /**
     * 启用冻结套餐订单申请
     * @param packageOrderNo 购买订单编号
     * @return true(成功)、false(失败)
     */
    @GetMapping("/enableFreezeRentOrder")
    public R<Boolean> enableFreezeRentOrder(String packageOrderNo) {
        if (StringUtils.isBlank(packageOrderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Boolean enableFreezeFlag = carRentalPackageOrderBizService.enableFreezeRentOrder(tenantId, user.getUid(), packageOrderNo, user.getUid());

        return R.ok(enableFreezeFlag);
    }

    /**
     * 撤销冻结套餐订单申请
     * @param packageOrderNo 购买订单编号
     * @return true(成功)、false(失败)
     */
    @GetMapping("/revokeFreezeRentOrder")
    public R<Boolean> revokeFreezeRentOrder(String packageOrderNo) {
        if (StringUtils.isBlank(packageOrderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Boolean cancelFreezeFlag = carRentalPackageOrderBizService.revokeFreezeRentOrder(tenantId, user.getUid(), packageOrderNo);

        return R.ok(cancelFreezeFlag);
    }

    /**
     * 冻结套餐订单申请
     * @param freezeRentOrderoptReq 请求操作数据模型
     * @return true(成功)、false(失败)
     */
    @PostMapping("/freezeRentOrder")
    public R<Boolean> freezeRentOrder(@RequestBody FreezeRentOrderOptReq freezeRentOrderoptReq) {
        if (!ObjectUtils.allNotNull(freezeRentOrderoptReq, freezeRentOrderoptReq.getApplyReason(), freezeRentOrderoptReq.getApplyTerm(), freezeRentOrderoptReq.getApplyTerm())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Boolean freezeFlag = carRentalPackageOrderBizService.freezeRentOrder(tenantId, user.getUid(), freezeRentOrderoptReq.getPackageOrderNo(), freezeRentOrderoptReq.getApplyTerm(), freezeRentOrderoptReq.getApplyReason(), SystemDefinitionEnum.WX_APPLET);

        return R.ok(freezeFlag);

    }

    /**
     * 用户根据套餐购买订单编码进行订单退租申请
     * @param packageOrderNo 购买订单编码
     * @return true(成功)、false(失败)
     */
    @GetMapping("/refundRentOrder")
    public R<Boolean> refundRentOrder(String packageOrderNo) {
        if (StringUtils.isBlank(packageOrderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Boolean refundFlag = carRentalPackageOrderBizService.refundRentOrder(tenantId, user.getUid(), packageOrderNo, user.getUid());

        return R.ok(refundFlag);
    }

    /**
     * 退租提示
     * @param packageOrderNo 购买订单编码
     * @return 提示模型
     */
    @GetMapping("/refundRentOrderHint")
    public R<RefundRentOrderHintVo> refundRentOrderHint(String packageOrderNo) {
        if (StringUtils.isBlank(packageOrderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRentalPackageOrderBizService.refundRentOrderHint(tenantId, user.getUid(), packageOrderNo));
    }

    /**
     * 套餐购买订单-条件查询列表
     * @param qryReq 请求参数类
     * @return 套餐购买订单集
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageOrderVo>> page(@RequestBody CarRentalPackageOrderQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageOrderQryReq();
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        // 赋值租户、用户
        qryReq.setTenantId(tenantId);
        qryReq.setUid(user.getUid());

        // 转换请求体
        CarRentalPackageOrderQryModel qryModel = new CarRentalPackageOrderQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);

        // 调用服务
        List<CarRentalPackageOrderPo> carRentalPackageOrderEntityList = carRentalPackageOrderService.page(qryModel);
        if (CollectionUtils.isEmpty(carRentalPackageOrderEntityList)) {
            return R.ok(Collections.emptyList());
        }

        // 获取辅助业务信息
        Set<Long> rentalPackageIds = new HashSet<>();
        Set<String> rentalPackageOrderNos = new HashSet<>();

        carRentalPackageOrderEntityList.forEach(carRentalPackageOrderEntity -> {
            rentalPackageIds.add(carRentalPackageOrderEntity.getRentalPackageId());
            rentalPackageOrderNos.add(carRentalPackageOrderEntity.getOrderNo());
        });

        // 套餐名称信息
        Map<Long, CarRentalPackagePo> carRentalPackageMap = getCarRentalPackageByIdsForMap(rentalPackageIds);

        // 车辆型号ID集
        Set<Integer> carModelIds = carRentalPackageMap.values().stream().map(CarRentalPackagePo::getCarModelId).collect(Collectors.toSet());

        // 车辆型号名称信息
        Map<Integer, String> carModelNameMap = getCarModelNameByIdsForMap(carModelIds);

        // 查询套餐购买订单对应的退款订单信息
        Map<String, CarRentalPackageOrderRentRefundPo> rentRefundMap = queryCarRentalRentRefundOrderByRentalOrderNos(rentalPackageOrderNos);

        // 查询会员信息
        CarRentalPackageMemberTermPo memberTerm = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, user.getUid());

        long nowTime = System.currentTimeMillis();

        // 模型转换，封装返回
        List<CarRentalPackageOrderVo> carRentalPackageVOList = new ArrayList<>();
        for(CarRentalPackageOrderPo carRentalPackageOrder : carRentalPackageOrderEntityList) {
            CarRentalPackageOrderVo carRentalPackageOrderVo = new CarRentalPackageOrderVo();
            carRentalPackageOrderVo.setOrderNo(carRentalPackageOrder.getOrderNo());
            carRentalPackageOrderVo.setRentalPackageType(carRentalPackageOrder.getRentalPackageType());
            carRentalPackageOrderVo.setConfine(carRentalPackageOrder.getConfine());
            carRentalPackageOrderVo.setConfineNum(carRentalPackageOrder.getConfineNum());
            carRentalPackageOrderVo.setTenancy(carRentalPackageOrder.getTenancy());
            carRentalPackageOrderVo.setTenancyUnit(carRentalPackageOrder.getTenancyUnit());
            carRentalPackageOrderVo.setRent(carRentalPackageOrder.getRent());
            carRentalPackageOrderVo.setPayState(carRentalPackageOrder.getPayState());
            carRentalPackageOrderVo.setUseState(carRentalPackageOrder.getUseState());
            carRentalPackageOrderVo.setCreateTime(carRentalPackageOrder.getCreateTime());
            carRentalPackageOrderVo.setRentRebate(carRentalPackageOrder.getRentRebate());
            // 赋值业务属性信息
            carRentalPackageOrderVo.setCarRentalPackageName(carRentalPackageMap.getOrDefault(carRentalPackageOrder.getRentalPackageId(), new CarRentalPackagePo()).getName());
            carRentalPackageOrderVo.setBatteryVoltage(carRentalPackageMap.getOrDefault(carRentalPackageOrder.getRentalPackageId(), new CarRentalPackagePo()).getBatteryVoltage());
            carRentalPackageOrderVo.setCarModelName(carModelNameMap.getOrDefault(carRentalPackageMap.getOrDefault(carRentalPackageOrder.getRentalPackageId(), new CarRentalPackagePo()).getCarModelId(), ""));

            // 二次判定是否可退
            if (YesNoEnum.NO.getCode().equals(carRentalPackageOrder.getRentRebate())) {
                carRentalPackageOrderVo.setRentRebate(YesNoEnum.NO.getCode());
                carRentalPackageVOList.add(carRentalPackageOrderVo);
                continue;
            }

            // 判定可退截止时间
            Integer rentRebate = carRentalPackageOrder.getRentRebateEndTime().longValue() >= nowTime ? YesNoEnum.YES.getCode() : YesNoEnum.NO.getCode();
            if (YesNoEnum.NO.getCode().equals(rentRebate)) {
                carRentalPackageOrderVo.setRentRebate(YesNoEnum.NO.getCode());
                carRentalPackageVOList.add(carRentalPackageOrderVo);
                continue;
            }

            // 使用状态判定
            Integer useState = carRentalPackageOrder.getUseState();
            if (UseStateEnum.EXPIRED.getCode().equals(useState) || UseStateEnum.RETURNED.getCode().equals(useState)) {
                carRentalPackageOrderVo.setRentRebate(YesNoEnum.NO.getCode());
                carRentalPackageVOList.add(carRentalPackageOrderVo);
                continue;
            }

            // 对使用中的订单，进行二次处理
            if (ObjectUtils.isNotEmpty(memberTerm) && UseStateEnum.IN_USE.getCode().equals(carRentalPackageOrder.getUseState())
                    && ObjectUtils.isNotEmpty(memberTerm.getDueTime()) && memberTerm.getDueTime() <= System.currentTimeMillis()) {
                carRentalPackageOrderVo.setUseState(UseStateEnum.EXPIRED.getCode());
                carRentalPackageOrderVo.setRentRebate(YesNoEnum.NO.getCode());
                carRentalPackageVOList.add(carRentalPackageOrderVo);
                continue;
            }

            // 支付状态判定
            Integer payState = carRentalPackageOrder.getPayState();
            if (!PayStateEnum.SUCCESS.getCode().equals(payState)) {
                carRentalPackageOrderVo.setRentRebate(YesNoEnum.NO.getCode());
                carRentalPackageVOList.add(carRentalPackageOrderVo);
                continue;
            }


            // 集成退款订单的状态，综合判定
            CarRentalPackageOrderRentRefundPo rentRefundOrderEntity = rentRefundMap.get(carRentalPackageOrder.getOrderNo());
            if (ObjectUtils.isEmpty(rentRefundOrderEntity)) {
                carRentalPackageVOList.add(carRentalPackageOrderVo);
                continue;
            }

            // 退款单状态
            Integer refundState = rentRefundOrderEntity.getRefundState();
            if (RefundStateEnum.AUDIT_REJECT.getCode().equals(refundState) || RefundStateEnum.FAILED.getCode().equals(refundState)) {
                carRentalPackageOrderVo.setRentRebate(YesNoEnum.YES.getCode());
            } else {
                carRentalPackageOrderVo.setRentRebate(YesNoEnum.NO.getCode());
            }

            carRentalPackageVOList.add(carRentalPackageOrderVo);
        }

        return R.ok(carRentalPackageVOList);
    }

    /**
     * 套餐购买订单-条件查询总数
     * @param qryReq 请求参数类
     * @return 总数
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalPackageOrderQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageOrderQryReq();
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        // 赋值租户、用户
        qryReq.setTenantId(tenantId);
        qryReq.setUid(user.getUid());

        // 转换请求体
        CarRentalPackageOrderQryModel qryModel = new CarRentalPackageOrderQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);

        // 调用服务
        return R.ok(carRentalPackageOrderService.count(qryModel));
    }

    /**
     * 查询用户正在使用的租车套餐信息（单车、车电一体）<br />
     * 复合查询，车辆信息、门店信息、GPS信息、电池信息、保险信息
     * @return com.xiliulou.core.web.R<com.xiliulou.electricity.vo.rental.RentalPackageVO>
     * @author xiaohui.song
     **/
    @GetMapping("/queryUseRentalPackageOrder")
    public R<RentalPackageVO> queryUseRentalPackageOrder() {

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return carRentalPackageOrderBizService.queryUseRentalPackageOrderByUid(tenantId, user.getUid());
    }

    /**
     * <code>C</code>端用户取消套餐订单<br />
     * 若订单号码不传入，则取消最后一笔未支付的订单，若传入，则按照传入的订单编码进行取消<br />
     * 用于如下情况
     * <pre>
     *     1. 用户未支付（主动终止流程，未曾真正调用微信支付系统，在订单中心，会有“取消订单”的入口，此时有订单编码）
     *     1. 用户未支付（主动终止流程，点击直接页面的“关闭X”，会有一个回调，此时没有订单编码，所以取消最后一笔）
     * </pre>
     * @param packageOrderNo 购买套餐订单编号
     * @return com.xiliulou.core.web.R<java.lang.Boolean>
     * @author xiaohui.song
     **/
    @GetMapping("/cancelRentalPackageOrder")
    public R<Boolean> cancelRentalPackageOrder(String packageOrderNo) {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRentalPackageOrderBizService.cancelRentalPackageOrder(packageOrderNo, tenantId, user.getUid()));
    }

    /**
     * <code>C</code>端用户购买租车套餐订单
     * @param buyOptModel 购买参数模型
     * @param request HTTP请求
     * @return com.xiliulou.core.web.R
     * @author xiaohui.song
     */
    @PostMapping("/buyRentalPackageOrder")
    public R<?> buyRentalPackageOrder(@RequestBody CarRentalPackageOrderBuyOptModel buyOptModel, HttpServletRequest request) {
        // 参数基本校验
        if (!ObjectUtils.allNotNull(buyOptModel, buyOptModel.getRentalPackageId(), buyOptModel.getFranchiseeId(), buyOptModel.getStoreId())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        // 从上下文中重新赋值参数
        buyOptModel.setTenantId(tenantId);
        buyOptModel.setUid(user.getUid());
        buyOptModel.setPayType(PayTypeEnum.ON_LINE.getCode());

        return carRentalPackageOrderBizService.buyRentalPackageOrder(buyOptModel, request);
    }

}
