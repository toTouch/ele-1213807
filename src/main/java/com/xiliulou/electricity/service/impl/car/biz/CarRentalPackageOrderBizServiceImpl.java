package com.xiliulou.electricity.service.impl.car.biz;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CarRenalCacheConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.domain.car.CarInfoDO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.*;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderBuyOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderFreezeQryModel;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.*;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderVO;
import com.xiliulou.electricity.vo.car.CarVO;
import com.xiliulou.electricity.vo.rental.RentalPackageVO;
import com.xiliulou.mq.service.RocketMqService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租车套餐购买业务聚合 BizServiceImpl
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageOrderBizServiceImpl implements CarRentalPackageOrderBizService {

    @Resource
    private ElectricityBatteryService batteryService;

    @Resource
    private CarRentalPackageOrderFreezeService carRentalPackageOrderFreezeService;

    @Resource
    private CarRentalPackageOrderSlippageService carRentalPackageOrderSlippageService;

    @Resource
    private CarRentalPackageOrderRentRefundService carRentalPackageOrderRentRefundService;

    @Resource
    private CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;

    @Resource
    private ElectricityCarService carService;

    @Resource
    private UserCarService userCarService;

    @Resource
    private UserBizService userBizService;

    @Resource
    private RocketMqService rocketMqService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserCouponService userCouponService;

    @Resource
    private UserOauthBindService userOauthBindService;

    @Resource
    private ElectricityPayParamsService electricityPayParamsService;

    @Resource
    private ElectricityTradeOrderService electricityTradeOrderService;

    @Resource
    private CarRentalPackageDepositPayService carRentalPackageDepositPayService;

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    @Resource
    private CarRentalPackageBizService carRentalPackageBizService;

    @Resource
    private RedisService redisService;

    @Resource
    private CarRentalPackageCarBatteryRelService carRentalPackageCarBatteryRelService;

    @Resource
    private CarRentalPackageService carRentalPackageService;

    /**
     * 审批冻结申请单
     *
     * @param freezeRentOrderNo 冻结申请单编码
     * @param approveFlag       审批标识，true(同意)；false(驳回)
     * @param apploveDesc       审批意见
     * @param apploveUid        审批人
     * @return
     */
    @Override
    public Boolean approveFreezeRentOrder(String freezeRentOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid) {
        if (ObjectUtils.allNotNull(freezeRentOrderNo, approveFlag, apploveUid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        CarRentalPackageOrderFreezePO freezeEntity = carRentalPackageOrderFreezeService.selectByOrderNo(freezeRentOrderNo);
        if (ObjectUtils.isEmpty(freezeEntity) || !RentalPackageOrderFreezeStatusEnum.PENDING_APPROVAL.getCode().equals(freezeEntity.getStatus())) {
            log.error("approveFreezeRentOrder faild. not find car_rental_package_order_freeze or status error. freezeRentOrderNo is {}", freezeRentOrderNo);
            throw new BizException("300000", "数据有误");
        }

        CarRentalPackageOrderPO packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(freezeEntity.getRentalPackageOrderNo());

        // 审核通过之后，生成滞纳金
        CarRentalPackageOrderSlippagePO slippageInsertEntity = null;
        CarRentalPackageMemberTermPO memberTermEntity = null;
        if (approveFlag) {
            slippageInsertEntity = buildCarRentalPackageOrderSlippage(freezeEntity.getUid(), packageOrderEntity);
            memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(freezeEntity.getTenantId(), freezeEntity.getUid());
            if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.APPLY_FREEZE.getCode().equals(memberTermEntity.getStatus())) {
                log.error("approveFreezeRentOrder faild. not find car_rental_package_member_term or status error. freezeRentOrderNo is {}, uid is {}", freezeRentOrderNo, freezeEntity.getUid());
                throw new BizException("300000", "数据有误");
            }
        }

        // TX 事务落库
        saveApproveFreezeRentOrderTx(freezeRentOrderNo, approveFlag, apploveDesc, apploveUid, freezeEntity, slippageInsertEntity, memberTermEntity);

        return true;
    }

    /**
     * 审核冻结申请单事务处理
     * @param freezeRentOrderNo 冻结申请单编码
     * @param approveFlag       审批标识，true(同意)；false(驳回)
     * @param apploveDesc       审批意见
     * @param apploveUid        审批人
     * @param freezeEntity      冻结申请单DB数据
     * @param slippageInsertEntity  滞纳金订单
     * @param memberTermEntity  会员期限DB数据
     * @return void
     * @author xiaohui.song
     **/
    @Transactional(rollbackFor = Exception.class)
    public void saveApproveFreezeRentOrderTx(String freezeRentOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid,
                                             CarRentalPackageOrderFreezePO freezeEntity, CarRentalPackageOrderSlippagePO slippageInsertEntity, CarRentalPackageMemberTermPO memberTermEntity) {

        // 1. 更新冻结申请单状态
        CarRentalPackageOrderFreezePO freezeUpdateEntity = new CarRentalPackageOrderFreezePO();
        freezeUpdateEntity.setOrderNo(freezeRentOrderNo);
        freezeUpdateEntity.setAuditTime(System.currentTimeMillis());
        freezeUpdateEntity.setRemark(apploveDesc);
        freezeUpdateEntity.setUpdateUid(apploveUid);

        if (approveFlag) {
            freezeUpdateEntity.setStatus(RentalPackageOrderFreezeStatusEnum.AUDIT_PASS.getCode());
            carRentalPackageOrderFreezeService.updateByOrderNo(freezeUpdateEntity);

            // 2. 更新会员期限信息
            CarRentalPackageMemberTermPO memberTermUpdateEntity = new CarRentalPackageMemberTermPO();
            memberTermUpdateEntity.setStatus(MemberTermStatusEnum.FREEZE.getCode());
            memberTermUpdateEntity.setId(memberTermEntity.getId());
            memberTermUpdateEntity.setUpdateUid(apploveUid);

            // 计算总的订单到期时间及当前订单到期时间
            // 计算规则：审核通过的时间 + 申请期限
            // Long extendTime = freezeUpdateEntity.getAuditTime() + (freezeEntity.getApplyTerm() * TimeConstant.DAY_MILLISECOND);
            // 计算规则：原有的时间 + 申请期限
            Long extendTime = (freezeEntity.getApplyTerm() * TimeConstant.DAY_MILLISECOND);
            memberTermUpdateEntity.setDueTime(memberTermEntity.getDueTime() + extendTime);
            memberTermUpdateEntity.setDueTimeTotal(memberTermEntity.getDueTimeTotal() + extendTime);

            carRentalPackageMemberTermService.updateById(memberTermUpdateEntity);

            // 3. 保存滞纳金订单
            if (ObjectUtils.isNotEmpty(slippageInsertEntity)) {
                carRentalPackageOrderSlippageService.insert(slippageInsertEntity);
            }
        } else {
            // 1. 更新冻结申请单状态
            freezeUpdateEntity.setStatus(RentalPackageOrderFreezeStatusEnum.AUDIT_REJECT.getCode());
            carRentalPackageOrderFreezeService.updateByOrderNo(freezeUpdateEntity);

            // 2. 更新会员期限信息
            carRentalPackageMemberTermService.updateStatusByUidAndTenantId(freezeEntity.getTenantId(), freezeEntity.getUid(), MemberTermStatusEnum.NORMAL.getCode(), apploveUid);
        }

    }

    /**
     * 审批退租申请单
     *
     * @param refundRentOrderNo 退租申请单编码
     * @param approveFlag       审批标识，true(同意)；false(驳回)
     * @param apploveDesc       审批意见
     * @param apploveUid        审批人
     * @return
     */
    @Override
    public Boolean approveRefundRentOrder(String refundRentOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid) {
        if (ObjectUtils.allNotNull(refundRentOrderNo, approveFlag, apploveUid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        CarRentalPackageOrderRentRefundPO rentRefundEntity = carRentalPackageOrderRentRefundService.selectByOrderNo(refundRentOrderNo);
        if (ObjectUtils.isEmpty(rentRefundEntity) || !RefundStateEnum.PENDING_APPROVAL.getCode().equals(rentRefundEntity.getRefundState())) {
            log.error("approveRefundRentOrder faild. not find car_rental_package_order_rent_refund or status error. refundRentOrderNo is {}", refundRentOrderNo);
            throw new BizException("300000", "数据有误");
        }

        // TX 事务落库
        saveApproveRefundRentOrderTx(refundRentOrderNo, approveFlag, apploveDesc, apploveUid, rentRefundEntity);

        if (approveFlag) {
            // 非 0 元退租
            if (BigDecimal.ZERO.compareTo(rentRefundEntity.getRefundAmount()) > 0) {
                // TODO 异步处理
                // TODO 该订单赠送的优惠券，直接置为已失效
                // TODO 分账数据，分账金额需要撤回
                // TODO 活动相关的数据
            }
        }

        return true;
    }

    /**
     * 审核退租申请单事务处理
     * @param refundRentOrderNo 退租申请单编码
     * @param approveFlag       审批标识，true(同意)；false(驳回)
     * @param apploveDesc       审批意见
     * @param apploveUid        审批人
     * @param rentRefundEntity  退租申请单DB数据
     * @return void
     * @author xiaohui.song
     **/
    @Transactional(rollbackFor = Exception.class)
    public void saveApproveRefundRentOrderTx(String refundRentOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid, CarRentalPackageOrderRentRefundPO rentRefundEntity) {

        CarRentalPackageOrderRentRefundPO rentRefundUpdateEntity = new CarRentalPackageOrderRentRefundPO();
        rentRefundUpdateEntity.setOrderNo(refundRentOrderNo);
        rentRefundUpdateEntity.setAuditTime(System.currentTimeMillis());
        rentRefundUpdateEntity.setRemark(apploveDesc);
        rentRefundUpdateEntity.setUpdateUid(apploveUid);

        if (approveFlag) {
            // 1. 更新退租申请单状态
            rentRefundUpdateEntity.setRefundState(RefundStateEnum.AUDIT_PASS.getCode());
            // 0 元退租
            if (BigDecimal.ZERO.compareTo(rentRefundEntity.getRefundAmount()) == 0) {
                rentRefundUpdateEntity.setRefundState(RefundStateEnum.SUCCESS.getCode());
            }
            carRentalPackageOrderRentRefundService.updateByOrderNo(rentRefundUpdateEntity);

            // 2. 更新会员期限
            carRentalPackageMemberTermService.rentRefundByUidAndPackageOrderNo(rentRefundEntity.getUid(), refundRentOrderNo, apploveUid);
        } else {
            // 1. 更新退租申请单状态
            rentRefundUpdateEntity.setRefundState(RefundStateEnum.AUDIT_REJECT.getCode());
            carRentalPackageOrderRentRefundService.updateByOrderNo(rentRefundUpdateEntity);

            // 2. 更新会员期限
            carRentalPackageMemberTermService.updateStatusByUidAndTenantId(rentRefundEntity.getTenantId(), rentRefundEntity.getUid(), MemberTermStatusEnum.NORMAL.getCode(), apploveUid);
        }
    }

    /**
     * 启用用户冻结订单<br />
     * 自动启用
     *
     * @param offset 偏移量
     * @param size 取值数量
     */
    @Override
    public void enableFreezeRentOrderAuto(Integer offset, Integer size) {
        // 初始化定义
        offset = ObjectUtils.isEmpty(offset) ? 0: offset;
        size = ObjectUtils.isEmpty(size) ? 500: size;

        boolean lookFlag = true;

        CarRentalPackageOrderFreezeQryModel qryModel = new CarRentalPackageOrderFreezeQryModel();
        qryModel.setStatus(RentalPackageOrderFreezeStatusEnum.AUDIT_PASS.getCode());
        qryModel.setSize(size);

        while (lookFlag) {
            qryModel.setOffset(offset);

            List<CarRentalPackageOrderFreezePO> pageEntityList = carRentalPackageOrderFreezeService.page(qryModel);
            if (CollectionUtils.isEmpty(pageEntityList)) {
                lookFlag = false;
                break;
            }

            // 当前时间
            long nowTime = System.currentTimeMillis();
            // 比对时间，进行数据处理
            for (CarRentalPackageOrderFreezePO freezeEntity : pageEntityList) {

                Integer applyTerm = freezeEntity.getApplyTerm();
                Long auditTime = freezeEntity.getAuditTime();
                // 到期时间
                long expireTime = auditTime + (TimeConstant.DAY_MILLISECOND * applyTerm);

                if (nowTime < expireTime) {
                    continue;
                }

                // 二次保险
                CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByUidAndPackageOrderNo(freezeEntity.getTenantId(), freezeEntity.getUid(), freezeEntity.getRentalPackageOrderNo());
                if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.FREEZE.getCode().equals(memberTermEntity.getStatus())) {
                    continue;
                }

                // TODO  此处后续需要优化，目前是循环调用IO，需要考虑批量调用，批量调用的时候，需要考虑在更新的时候，数据状态不一致的情况
                // 事务处理
                enableFreezeRentOrderTx(freezeEntity.getTenantId(), freezeEntity.getUid(), freezeEntity.getRentalPackageOrderNo(), true, null);

            }
            offset += size;
        }

    }

    /**
     * 启用用户冻结订单申请<br />
     * 手动启用
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param packageOrderNo 购买订单编码
     * @param optUid 操作人ID
     * @return true(成功)、false(失败)
     */
    @Override
    public Boolean enableFreezeRentOrder(Integer tenantId, Long uid, String packageOrderNo, Long optUid) {
        if (!ObjectUtils.allNotNull(tenantId, uid, packageOrderNo, optUid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询套餐会员期限
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("CarRentalPackageOrderBizServiceImpl.cancelFreezeRentOrder, memberTermEntity not found. uid is {}, tenantId is {}", uid, tenantId);
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        if (!MemberTermStatusEnum.FREEZE.getCode().equals(memberTermEntity.getStatus())) {
            throw new BizException("300002", "租车会员状态异常");
        }

        if (!memberTermEntity.getRentalPackageOrderNo().equals(packageOrderNo)) {
            throw new BizException("300020", "订单编码不匹配");
        }

        // 是否存在滞纳金（仅限租车套餐产生的滞纳金）
        if (carRentalPackageOrderSlippageService.isExitUnpaid(tenantId, uid)) {
            throw new BizException("300001", "存在滞纳金，请先缴纳");
        }

        // TX 事务
        enableFreezeRentOrderTx(tenantId, uid, packageOrderNo, false, optUid);

        return true;
    }

    /**
     * 启用冻结订单，TX事务处理<br />
     * 非对外
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param packageOrderNo 购买订单编码
     * @param autoEnable 自动启用标识，true(自动)，false(手动提前启用)
     * @param optUid 操作人ID(可为空)
     */
    @Transactional(rollbackFor = Exception.class)
    public void enableFreezeRentOrderTx(Integer tenantId, Long uid, String packageOrderNo, Boolean autoEnable, Long optUid) {
        // 1. 更改订单冻结表数据
        carRentalPackageOrderFreezeService.enableFreezeRentOrderByUidAndPackageOrderNo(packageOrderNo, uid, autoEnable, optUid);

        // 2. 更改会员期限表数据
        carRentalPackageMemberTermService.updateStatusByUidAndTenantId(tenantId, uid, MemberTermStatusEnum.NORMAL.getCode(), optUid);
    }

    /**
     * 撤销用户冻结订单申请
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param packageOrderNo  购买订单编码
     * @return
     */
    @Override
    public Boolean revokeFreezeRentOrder(Integer tenantId, Long uid, String packageOrderNo) {
        if (!ObjectUtils.allNotNull(tenantId, uid, packageOrderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询套餐会员期限
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("CarRentalPackageOrderBizServiceImpl.cancelFreezeRentOrder, memberTermEntity not found. uid is {}, tenantId is {}", uid, tenantId);
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        if (!MemberTermStatusEnum.APPLY_FREEZE.getCode().equals(memberTermEntity.getStatus())) {
            throw new BizException("300002", "租车会员状态异常");
        }

        if (!memberTermEntity.getRentalPackageOrderNo().equals(packageOrderNo)) {
            throw new BizException("300020", "订单编码不匹配");
        }

        // 二次保险保底查询
        CarRentalPackageOrderFreezePO freezeEntity = carRentalPackageOrderFreezeService.selectPendingApprovalByUid(tenantId, uid);
        if (ObjectUtils.isEmpty(freezeEntity) || !freezeEntity.getRentalPackageOrderNo().equals(packageOrderNo)) {
            throw new BizException("300020", "订单编码不匹配");
        }

        // 撤销冻结申请
        return carRentalPackageOrderFreezeService.revokeByOrderNo(freezeEntity.getOrderNo(), uid);
    }

    /**
     * 根据用户ID及订单编码进行冻结订单申请
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param packageOrderNo  套餐购买订单编号
     * @param applyTerm 申请期限(天)
     * @param applyReason 申请理由
     * @return
     */
    @Override
    public Boolean freezeRentOrder(Integer tenantId, Long uid, String packageOrderNo, Integer applyTerm, String applyReason) {
        if (!ObjectUtils.allNotNull(tenantId, uid, packageOrderNo, applyTerm, applyReason)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询套餐会员期限
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            throw new BizException("300002", "租车会员状态异常");
        }

        // 查询套餐购买订单信息
        CarRentalPackageOrderPO packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(packageOrderNo);

        // 判定套餐是否允许冻结
        if (ObjectUtils.isEmpty(packageOrderEntity)) {
            throw new BizException("300008", "未找到租车套餐购买订单");
        }

        if(PayStateEnum.SUCCESS.getCode().equals(packageOrderEntity.getPayState())) {
            throw new BizException("300014", "订单支付异常");
        }

        if(UseStateEnum.UN_USED.getCode().equals(packageOrderEntity.getUseState())) {
            throw new BizException("300015", "订单状态异常");
        }

        // 生成冻结申请
        Long residue = calculateResidue(packageOrderEntity.getConfine(), memberTermEntity.getResidue(), packageOrderEntity.getUseBeginTime().longValue(), packageOrderEntity.getTenancy(), packageOrderEntity.getTenancyUnit());
        CarRentalPackageOrderFreezePO freezeEntity = buildCarRentalPackageOrderFreeze(uid, packageOrderEntity, applyTerm, residue, applyReason);

        // TX 事务
        saveFreezeInfoTx(freezeEntity, tenantId, uid);

        return true;
    }

    /**
     * 冻结套餐，最终TX事务保存落库<br />
     * 非对外
     * @param freezeEntity 冻结订单
     * @param tenantId 租户ID
     * @param uid 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveFreezeInfoTx(CarRentalPackageOrderFreezePO freezeEntity, Integer tenantId, Long uid) {
        // 保存冻结记录
        carRentalPackageOrderFreezeService.insert(freezeEntity);

        // 更新会员期限
        carRentalPackageMemberTermService.updateStatusByUidAndTenantId(tenantId, uid, MemberTermStatusEnum.APPLY_FREEZE.getCode(), uid);

    }

    /**
     * 生成冻结订单
     * @param uid 用户ID
     * @param packageOrderEntity 套餐购买订单
     * @param applyTerm 申请期限(天)
     * @param residue 余量
     * @return
     */
    private CarRentalPackageOrderFreezePO buildCarRentalPackageOrderFreeze(Long uid, CarRentalPackageOrderPO packageOrderEntity, Integer applyTerm, Long residue, String applyReason) {
        CarRentalPackageOrderFreezePO freezeEntity = new CarRentalPackageOrderFreezePO();
        freezeEntity.setUid(uid);
        freezeEntity.setRentalPackageOrderNo(packageOrderEntity.getOrderNo());
        freezeEntity.setRentalPackageId(packageOrderEntity.getRentalPackageId());
        freezeEntity.setRentalPackageType(packageOrderEntity.getRentalPackageType());
        freezeEntity.setResidue(residue);
        freezeEntity.setLateFee(packageOrderEntity.getLateFee());
        freezeEntity.setApplyTerm(applyTerm);
        freezeEntity.setApplyReason(applyReason);
        freezeEntity.setApplyTime(System.currentTimeMillis());
        freezeEntity.setStatus(RentalPackageOrderFreezeStatusEnum.PENDING_APPROVAL.getCode());
        freezeEntity.setTenantId(packageOrderEntity.getTenantId());
        freezeEntity.setFranchiseeId(packageOrderEntity.getFranchiseeId());
        freezeEntity.setStoreId(packageOrderEntity.getStoreId());
        freezeEntity.setCreateUid(uid);

        // 设置余量单位
        if (RenalPackageConfineEnum.NUMBER.getCode().equals(packageOrderEntity.getConfine())) {
            freezeEntity.setResidueUnit(RentalUnitEnum.NUMBER.getCode());
        }
        if (RenalPackageConfineEnum.NO.getCode().equals(packageOrderEntity.getConfine())) {
            freezeEntity.setResidueUnit(packageOrderEntity.getTenancyUnit());
        }

        return freezeEntity;
    }

    /**
     * 生成逾期订单
     * @return
     */
    private CarRentalPackageOrderSlippagePO buildCarRentalPackageOrderSlippage(Long uid, CarRentalPackageOrderPO packageOrderEntity) {
        // 初始化标识
        boolean createFlag = false;

        // 查询是否未归还设备
        // 1. 车辆
        UserCar userCar = userCarService.selectByUidFromCache(uid);
        if (ObjectUtils.isNotEmpty(userCar) && ObjectUtils.isNotEmpty(userCar.getSn()) ) {
            createFlag = true;
        }

        // 2. 根据套餐类型，是否查询电池
        ElectricityBattery battery = null;
        if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(packageOrderEntity.getRentalPackageType())) {
            battery = batteryService.queryByUid(uid);
            if (ObjectUtils.isNotEmpty(battery)) {
                createFlag = true;
            }
        }

        // 不会生成滞纳金记录
        if (!createFlag) {
            return null;
        }

        // 生成实体记录
        CarRentalPackageOrderSlippagePO slippageEntity = new CarRentalPackageOrderSlippagePO();
        slippageEntity.setUid(uid);
        slippageEntity.setRentalPackageOrderNo(packageOrderEntity.getOrderNo());
        slippageEntity.setType(SlippageTypeEnum.FREEZE.getCode());
        slippageEntity.setLateFee(packageOrderEntity.getLateFee());
        slippageEntity.setLateFeeStartTime(System.currentTimeMillis());
        slippageEntity.setPayState(PayStateEnum.UNPAID.getCode());
        slippageEntity.setTenantId(packageOrderEntity.getTenantId());
        slippageEntity.setFranchiseeId(packageOrderEntity.getFranchiseeId());
        slippageEntity.setStoreId(packageOrderEntity.getStoreId());
        slippageEntity.setCreateUid(uid);

        // 记录设备信息
        if (ObjectUtils.isNotEmpty(userCar)) {
            slippageEntity.setCarSn(userCar.getSn());
        }
        if (ObjectUtils.isNotEmpty(battery)) {
            slippageEntity.setBatterySn(battery.getSn());
        }

        return slippageEntity;
    }


    /**
     * 根据用户ID及订单编码，退租购买的订单
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param packageOrderNo  套餐购买订单编号
     * @param optUid  操作人ID
     * @return
     */
    @Override
    public Boolean refundRentOrder(Integer tenantId, Long uid, String packageOrderNo, Long optUid) {
        if (!ObjectUtils.allNotNull(tenantId, uid, packageOrderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询套餐购买订单
        CarRentalPackageOrderPO packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(packageOrderNo);
        if (ObjectUtils.isEmpty(packageOrderEntity) || ObjectUtils.notEqual(tenantId, packageOrderEntity.getTenantId()) || ObjectUtils.notEqual(uid, packageOrderEntity.getUid())) {
            throw new BizException("300008", "未找到租车套餐购买订单");
        }

        if (ObjectUtils.notEqual(YesNoEnum.NO.getCode(), packageOrderEntity.getRentRebate())) {
            throw new BizException("300012", "订单不允许退租");
        } else {
            if (System.currentTimeMillis() >= packageOrderEntity.getRentRebateEndTime()) {
                throw new BizException("300013", "订单超过可退期限");
            }
        }

        if (ObjectUtils.notEqual(PayStateEnum.SUCCESS.getCode(), packageOrderEntity.getPayState())) {
            throw new BizException("300014", "订单支付异常");
        }

        if (UseStateEnum.notRefundCodes().contains(packageOrderEntity.getUseState())) {
            throw new BizException("300015", "订单状态异常");
        }

        // 购买的时候，赠送的优惠券是否被使用，若为使用中、已使用，则不允许退租
        UserCoupon userCoupon = userCouponService.selectBySourceOrderId(packageOrderEntity.getOrderNo());
        if (ObjectUtils.isNotEmpty(userCoupon)) {
            Integer status = userCoupon.getStatus();
            if (UserCoupon.STATUS_IS_BEING_VERIFICATION.equals(status) || UserCoupon.STATUS_IS_BEING_VERIFICATION.equals(status)) {
                throw new BizException("300016", "订单赠送优惠券状态异常");
            }
        }

        CarRentalPackageMemberTermPO memberTermUpdateEntity = null;
        if (UseStateEnum.IN_USE.getCode().equals(packageOrderEntity.getUseState())) {
            if (carRentalPackageOrderService.isExitUnUseByUid(tenantId, uid)) {
                throw new BizException("300017", "存在未使用的订单");
            }
            // 查询设备信息，存在设备，不允许退租
            UserCar userCar = userCarService.selectByUidFromCache(uid);
            if (ObjectUtils.isNotEmpty(userCar) && ObjectUtils.isNotEmpty(userCar.getSn()) ) {
                throw new BizException("300018", "存在未归还的车辆");
            }
            if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(packageOrderEntity.getRentalPackageType())) {
                ElectricityBattery battery = batteryService.queryByUid(uid);
                if (ObjectUtils.isNotEmpty(battery)) {
                    throw new BizException("300019", "存在未归还的电池");
                }
            }
            memberTermUpdateEntity = buildRentRefundRentalPackageMemberTerm(tenantId, uid, optUid);
        }

        // 计算实际应退金额及余量
        Pair<BigDecimal, Long> refundAmountPair = calculateRefundAmount(packageOrderEntity, tenantId, uid);

        // 生成租金退款审核订单
        CarRentalPackageOrderRentRefundPO rentRefundOrderEntity = buildRentRefundOrder(packageOrderEntity, refundAmountPair.getLeft(), uid, refundAmountPair.getRight(), optUid);

        // TX 事务管理
        saveRentRefundOrderInfoTx(rentRefundOrderEntity, memberTermUpdateEntity);

        return true;
    }

    /**
     * 退租申请单的事务处理
     * @param rentRefundOrderEntity 退租申请单
     * @param memberTermUpdateEntity 会员期限数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRentRefundOrderInfoTx(CarRentalPackageOrderRentRefundPO rentRefundOrderEntity, CarRentalPackageMemberTermPO memberTermUpdateEntity) {
        carRentalPackageOrderRentRefundService.insert(rentRefundOrderEntity);
        if (ObjectUtils.isNotEmpty(memberTermUpdateEntity)) {
            carRentalPackageMemberTermService.updateById(memberTermUpdateEntity);
        }
    }

    /**
     * 退租申请，构建会员期限更新数据
     * @param tenantId
     * @param uid
     * @param optUid
     * @return
     */
    private CarRentalPackageMemberTermPO buildRentRefundRentalPackageMemberTerm(Integer tenantId, Long uid, Long optUid) {
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            log.error("buildRentRefundRentalPackageMemberTerm faild. not find car_rental_package_member_term or status error. uid is {}", uid);
            throw new BizException("300000", "数据有误");
        }

        CarRentalPackageMemberTermPO memberTermUpdateEntity = new CarRentalPackageMemberTermPO();
        memberTermUpdateEntity.setId(memberTermEntity.getId());
        memberTermUpdateEntity.setStatus(MemberTermStatusEnum.APPLY_RENT_REFUND.getCode());
        memberTermUpdateEntity.setUpdateUid(ObjectUtils.isEmpty(optUid) ? uid : optUid);

        return memberTermUpdateEntity;
    }

    /**
     * 计算实际应退金额
     * @param packageOrderEntity 购买的套餐订单
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return Pair L： 金额, R：余量
     */
    private Pair<BigDecimal, Long> calculateRefundAmount(CarRentalPackageOrderPO packageOrderEntity, Integer tenantId, Long uid) {
        // 定义实际应返金额
        BigDecimal refundAmount = null;

        // TODO 余量数字已经抽取了一个方法，此处需要优化
        Long residueNum = null;
        // 实际支付金额
        BigDecimal rentPayment = packageOrderEntity.getRentPayment();

        // 判定订单状态
        // 使用中，计算金额
        if (UseStateEnum.IN_USE.getCode().equals(packageOrderEntity.getUseState())) {
            // 查询套餐会员期限
            CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
            // 退款规则
            // 1. 若限制次数，则根据次数计算退款金额
            if (RenalPackageConfineEnum.NUMBER.getCode().equals(packageOrderEntity.getConfine())) {
                // 查询当前套餐的余量
                long residue = memberTermEntity.getResidue().longValue();
                // 余量为 0，则退款金额为 0
                if (residue == 0) {
                    refundAmount = BigDecimal.ZERO;
                    return Pair.of(refundAmount, residue);
                }
                // 余量非 0
                long confineNum = packageOrderEntity.getConfineNum().longValue();

                return Pair.of(diffAmount((confineNum - residue), packageOrderEntity.getRentUnitPrice(), rentPayment), residue);
            }

            // 2. 若不限制，则根据时间单位（天、分钟）计算退款金额
            if (RenalPackageConfineEnum.NO.getCode().equals(packageOrderEntity.getConfine())) {
                long nowTime = System.currentTimeMillis();
                long useBeginTime = packageOrderEntity.getUseBeginTime().longValue();

                if (RentalUnitEnum.DAY.getCode().equals(packageOrderEntity.getTenancyUnit())) {
                    // 已使用天数
                    long diffDay = DateUtils.diffDay(useBeginTime, nowTime);

                    return Pair.of(diffAmount(diffDay, packageOrderEntity.getRentUnitPrice(), rentPayment), packageOrderEntity.getTenancy().intValue() - diffDay);
                }

                if (RentalUnitEnum.MINUTE.getCode().equals(packageOrderEntity.getTenancyUnit())) {
                    // 已使用分钟数
                    long diffMinute = DateUtils.diffMinute(useBeginTime, nowTime);

                    return Pair.of(diffAmount(diffMinute, packageOrderEntity.getRentUnitPrice(), rentPayment), packageOrderEntity.getTenancy().intValue() - diffMinute);
                }
            }
        }

        // 未使用，实际支付的租金金额
        if (UseStateEnum.UN_USED.getCode().equals(packageOrderEntity.getUseState())) {
            refundAmount = rentPayment;
            if (RenalPackageConfineEnum.NUMBER.getCode().equals(packageOrderEntity.getConfine())) {
                residueNum = packageOrderEntity.getConfineNum();
            }
            if (RenalPackageConfineEnum.NO.getCode().equals(packageOrderEntity.getConfine())) {
                residueNum = Long.valueOf(packageOrderEntity.getTenancy());
            }
        }

        return Pair.of(refundAmount, residueNum);
    }

    /**
     * 计算套餐订单剩余量
     * @param confine 套餐订单是否限制
     * @param memberResidue 会员余量
     * @param useBeginTime 套餐订单开始时间时间
     * @param tenancy 租期
     * @param tenancyUnit 租期单位
     * @return
     */
    private Long calculateResidue(Integer confine, Long memberResidue, long useBeginTime, Integer tenancy, Integer tenancyUnit) {
        // 1. 若限制次数，则根据次数计算退款金额
        if (RenalPackageConfineEnum.NUMBER.getCode().equals(confine)) {
            return memberResidue;
        }

        // 2. 若不限制，则根据时间单位（天、分钟）计算退款金额
        if (RenalPackageConfineEnum.NO.getCode().equals(confine)) {
            long nowTime = System.currentTimeMillis();

            if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                // 已使用天数
                long diffDay = DateUtils.diffDay(useBeginTime, nowTime);

                return Long.valueOf(tenancy.intValue() - diffDay);

            }

            if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                // 已使用分钟数
                long diffMinute = DateUtils.diffMinute(useBeginTime, nowTime);

                return Long.valueOf(tenancy.intValue() - diffMinute);
            }
        }

        return null;
    }

    /**
     * 计算差额
     * @param diffTime 差额时间
     * @param rentUnitPrice 单价
     * @param rentPayment 实际支付金额
     * @return
     */
    private BigDecimal diffAmount(long diffTime, BigDecimal rentUnitPrice, BigDecimal rentPayment) {
        BigDecimal diffAmount = BigDecimal.ZERO;
        // 应付款 计算规则：套餐单价 * 差额时间
        BigDecimal shouldPayAmount = NumberUtil.mul(diffTime, rentUnitPrice).setScale(2, RoundingMode.HALF_UP);
        // 计算实际应返金额
        if (rentPayment.compareTo(shouldPayAmount) > 0) {
            diffAmount = NumberUtil.sub(rentPayment, shouldPayAmount);
        }
        return diffAmount;
    }

    /**
     * 生成租金退款订单信息
     * @param packageOrderEntity 套餐购买订单
     * @param refundAmount 应退金额
     * @param uid 用户ID
     * @param residue 余量
     * @param optUid 操作人ID
     * @return com.xiliulou.electricity.entity.car.CarRentalPackageOrderRentRefundPO
     * @author xiaohui.song
     **/
    private CarRentalPackageOrderRentRefundPO buildRentRefundOrder(CarRentalPackageOrderPO packageOrderEntity, BigDecimal refundAmount, Long uid, Long residue, Long optUid) {
        CarRentalPackageOrderRentRefundPO rentRefundOrderEntity = new CarRentalPackageOrderRentRefundPO();
        rentRefundOrderEntity.setUid(uid);
        rentRefundOrderEntity.setRentalPackageOrderNo(packageOrderEntity.getOrderNo());
        rentRefundOrderEntity.setRentalPackageId(packageOrderEntity.getRentalPackageId());
        rentRefundOrderEntity.setRentalPackageType(packageOrderEntity.getRentalPackageType());
        rentRefundOrderEntity.setResidue(residue);
        rentRefundOrderEntity.setRefundAmount(refundAmount);
        rentRefundOrderEntity.setRefundState(RefundStateEnum.PENDING_APPROVAL.getCode());
        rentRefundOrderEntity.setRentUnitPrice(packageOrderEntity.getRentUnitPrice());
        rentRefundOrderEntity.setRentPayment(packageOrderEntity.getRentPayment());
        rentRefundOrderEntity.setTenantId(packageOrderEntity.getTenantId());
        rentRefundOrderEntity.setFranchiseeId(packageOrderEntity.getFranchiseeId());
        rentRefundOrderEntity.setStoreId(packageOrderEntity.getStoreId());
        rentRefundOrderEntity.setCreateUid(ObjectUtils.isEmpty(optUid) ? uid : optUid);
        rentRefundOrderEntity.setDelFlag(DelFlagEnum.OK.getCode());
        // 设置余量单位
        if (RenalPackageConfineEnum.NUMBER.getCode().equals(packageOrderEntity.getConfine())) {
            rentRefundOrderEntity.setResidueUnit(RentalUnitEnum.NUMBER.getCode());
        }
        if (RenalPackageConfineEnum.NO.getCode().equals(packageOrderEntity.getConfine())) {
            rentRefundOrderEntity.setResidueUnit(packageOrderEntity.getTenancyUnit());
        }
        return rentRefundOrderEntity;
    }

    /**
     * 根据用户ID查询正在使用的套餐信息<br />
     * 复合查询，车辆信息、门店信息、GPS信息、电池信息、保险信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return com.xiliulou.core.web.R<com.xiliulou.electricity.vo.rental.RentalPackageVO>
     * @author xiaohui.song
     **/
    @Override
    public R<RentalPackageVO> queryUseRentalPackageOrderByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 1. 查询会员期限信息
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            return R.ok();
        }

        // 2. 查询套餐信息
        CarRentalPackagePO carRentalPackageEntity = carRentalPackageService.selectById(memberTermEntity.getRentalPackageId());

        // 3. 查询用户车辆信息
        UserCar userCar = userCarService.selectByUidFromCache(uid);

        // 4. 查询车辆相关信息
        CarInfoDO carInfoDO = carService.queryByCarId(tenantId, userCar.getCid());

        // 5. TODO 查询保险信息，志龙
        // 车电一体
        if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(memberTermEntity.getRentalPackageType())) {
            // 6. TODO 电池消息，志龙

        }

        // 7. 滞纳金信息
        BigDecimal lateFeeAmount = carRenalPackageSlippageBizService.queryCarPackageUnpaidAmountByUid(tenantId, uid);

        // 构建返回信息
        RentalPackageVO rentalPackageVO = buildRentalPackageVO(memberTermEntity, carRentalPackageEntity, carInfoDO, lateFeeAmount);

        return R.ok(rentalPackageVO);
    }

    private RentalPackageVO buildRentalPackageVO(CarRentalPackageMemberTermPO memberTermEntity, CarRentalPackagePO carRentalPackageEntity, CarInfoDO carInfoDO, BigDecimal lateFeeAmount) {
        // 构建返回值
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            return null;
        }
        RentalPackageVO rentalPackageVO = new RentalPackageVO();
        rentalPackageVO.setStatus(memberTermEntity.getStatus());
        rentalPackageVO.setDeadlineTime(memberTermEntity.getDueTimeTotal());
        rentalPackageVO.setLateFeeAmount(lateFeeAmount);

        // 套餐订单信息
        CarRentalPackageOrderVO carRentalPackageOrderVO = new CarRentalPackageOrderVO();
        carRentalPackageOrderVO.setOrderNo(memberTermEntity.getRentalPackageOrderNo());
        carRentalPackageOrderVO.setRentalPackageType(carRentalPackageEntity.getType());
        carRentalPackageOrderVO.setConfine(carRentalPackageEntity.getConfine());
        carRentalPackageOrderVO.setConfineNum(carRentalPackageEntity.getConfineNum());
        carRentalPackageOrderVO.setTenancy(carRentalPackageEntity.getTenancy());
        carRentalPackageOrderVO.setTenancyUnit(carRentalPackageEntity.getTenancyUnit());
        carRentalPackageOrderVO.setRent(carRentalPackageEntity.getRent());
        carRentalPackageOrderVO.setCarRentalPackageName(carRentalPackageEntity.getName());
        // 赋值套餐订单信息
        rentalPackageVO.setCarRentalPackageOrder(carRentalPackageOrderVO);

        // 车辆信息
        if (ObjectUtils.isNotEmpty(carInfoDO)) {
            CarVO carVO = new CarVO();
            carVO.setCarSn(carInfoDO.getCarSn());
            carVO.setStoreName(carInfoDO.getStoreName());
            carVO.setLatitude(carInfoDO.getLatitude());
            carVO.setLongitude(carInfoDO.getLongitude());
            // 赋值车辆信息
            rentalPackageVO.setCar(carVO);
        }

        return rentalPackageVO;
    }

    /**
     * 租车套餐订单
     *
     * @param packageOrderNo  租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return
     */
    @Override
    public Boolean cancelRentalPackageOrder(String packageOrderNo, Integer tenantId, Long uid) {
        // 1. 处理租车套餐购买订单
        CarRentalPackageOrderPO carRentalPackageOrderEntity = carRentalPackageOrderService.selectByOrderNo(packageOrderNo);
        if (ObjectUtil.isEmpty(carRentalPackageOrderEntity)) {
            log.error("CancelRentalPackageOrder failed, not found car_rental_package_order, order_no is {}", packageOrderNo);
            throw new BizException("300008", "未找到租车套餐购买订单");
        }

        // 订单支付状态不匹配
        if (ObjectUtil.notEqual(PayStateEnum.UNPAID.getCode(), carRentalPackageOrderEntity.getPayState())) {
            log.error("CancelRentalPackageOrder failed, car_rental_package_order processed, order_no is {}", packageOrderNo);
            throw new BizException("300009", "租车套餐购买订单已处理");
        }

        // 更改套餐购买订单的支付状态
        carRentalPackageOrderService.updatePayStateByOrderNo(packageOrderNo, PayStateEnum.CANCEL.getCode());

        // 2. 处理租车套餐押金缴纳订单
        String depositPayOrderNo = carRentalPackageOrderEntity.getDepositPayOrderNo();
        CarRentalPackageDepositPayPO depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity)) {
            log.error("CancelRentalPackageOrder failed, not found car_rental_package_deposit_pay, order_no is {}", depositPayOrderNo);
            throw new BizException("300010", "未找到租车套餐押金缴纳订单");
        }

        // 判定押金缴纳订单是否需要更改支付状态
        if (ObjectUtil.equal(PayStateEnum.UNPAID.getCode(), depositPayEntity.getPayState())) {
            carRentalPackageDepositPayService.updatePayStateByOrderNo(depositPayOrderNo, PayStateEnum.CANCEL.getCode());
        }

        // 3. 处理租车套餐会员期限
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("CancelRentalPackageOrder failed, not found car_rental_package_member_term, uid is {}", uid);
            throw new BizException("300011", "未找到租车会员记录信息");
        }

        // 待生效的数据，直接删除
        if (MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            carRentalPackageMemberTermService.delByUidAndTenantId(tenantId, uid, uid);
        }

        // 4. TODO 处理用户押金支付信息（订单取消）


        // 5. 处理用户优惠券的使用状态
        userCouponService.updateStatusByOrderId(packageOrderNo, OrderTypeEnum.CAR_BUY_ORDER.getCode(), UserCoupon.STATUS_UNUSED);

        // 7. TODO 处理保险购买订单

        return true;
    }

    /**
     * 租车套餐订单，购买/续租
     * @param buyOptModel
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R buyRentalPackageOrder(CarRentalPackageOrderBuyOptModel buyOptModel, HttpServletRequest request) {
        // 参数校验
        Integer tenantId = buyOptModel.getTenantId();
        Long uid = buyOptModel.getUid();
        Long buyRentalPackageId = buyOptModel.getRentalPackageId();

        if (!ObjectUtils.allNotNull(tenantId, uid, buyRentalPackageId)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 获取加锁 KEY
        String buyLockKey = String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_BUY_UID_KEY, uid);

        try {
            // 加锁
            if (!redisService.setNx(buyLockKey, uid.toString(), 10 * 1000L, false)) {
                return R.fail("ELECTRICITY.0034", "操作频繁");
            }

            // 1 获取用户信息
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.error("CheckBuyPackageCommon failed. Not found user. uid is {} ", uid);
                throw new BizException("ELECTRICITY.0001", "未找到用户");
            }

            // 1.1 用户可用状态
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.error("CheckBuyPackageCommon failed. User is unUsable. uid is {} ", uid);
                throw new BizException("ELECTRICITY.0024", "用户已被禁用");
            }

            // 1.2 用户实名认证状态
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.error("CheckBuyPackageCommon failed. User not auth. uid is {}", uid);
                throw new BizException("ELECTRICITY.0041", "用户尚未实名认证");
            }

            // 2. 判定滞纳金
            if (carRenalPackageSlippageBizService.isExitUnpaid(tenantId, uid)) {
                log.error("CheckBuyPackageCommon failed. Late fee not paid. uid is {}", uid);
                throw new BizException("300001", "存在滞纳金，请先缴纳");
            }

            // 3. 支付相关
            ElectricityPayParams payParamsEntity = electricityPayParamsService.queryFromCache(tenantId);
            if (Objects.isNull(payParamsEntity)) {
                log.error("BuyRentalPackageOrder failed. Not found pay_params. uid is {}", uid);
                throw new BizException("100234", "未配置支付参数");
            }

            // 4. 三方授权相关
            UserOauthBind userOauthBindEntity = userOauthBindService.queryUserOauthBySysId(uid, tenantId);
            if (Objects.isNull(userOauthBindEntity) || Objects.isNull(userOauthBindEntity.getThirdId())) {
                log.error("BuyRentalPackageOrder failed. Not found useroauthbind or thirdid is null. uid is {}", uid);
                throw new BizException("100235", "未找到用户的第三方授权信息");
            }

            // 初始化押金金额
            BigDecimal deposit = null;
            Integer userTenantId = userInfo.getTenantId();
            Long userFranchiseeId = userInfo.getFranchiseeId();
            Long userStoreId = userInfo.getStoreId();

            // 5. 获取租车套餐会员期限信息
            CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
            // 若非空，则押金必定缴纳
            if (ObjectUtils.isNotEmpty(memberTermEntity)) {
                // 5.1 用户套餐会员限制状态异常
                if (!MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
                    log.error("BuyRentalPackageOrder failed. member_term status wrong. uid is {}, status is {}", uid, memberTermEntity.getStatus());
                    return R.fail("300002", "租车会员状态异常");
                }
                // 从会员期限中获取押金金额
                deposit = memberTermEntity.getDeposit();
            }

            // 6. 获取套餐信息
            // 6.1 套餐不存在
            CarRentalPackagePO buyPackageEntity = carRentalPackageService.selectById(buyRentalPackageId);
            if (ObjectUtils.isEmpty(buyPackageEntity)) {
                log.error("BuyRentalPackageOrder failed. Package does not exist, buyRentalPackageId is {}", buyRentalPackageId);
                return R.fail("300003", "套餐不存在");
            }

            // 6.2 套餐上下架状态
            if (UpDownEnum.DOWN.getCode().equals(buyPackageEntity.getStatus())) {
                log.error("BuyRentalPackageOrder failed. Package status is down. buyRentalPackageId is {}", buyRentalPackageId);
                return R.fail("300004", "套餐已下架");
            }

            // 6.3 判定用户是否是老用户，然后和套餐的适用类型做比对
            Boolean oldUserFlag = userBizService.isOldUser(tenantId, uid);
            if (oldUserFlag && !ApplicableTypeEnum.oldUserApplicable().contains(buyPackageEntity.getApplicableType())) {
                log.error("BuyRentalPackageOrder failed. Package type mismatch. Buy package type is {}, user is old", buyPackageEntity.getApplicableType());
                return R.fail("300005", "套餐不匹配");
            }

            // 7. 判定套餐互斥
            // 7.1 车或者电与车电一体互斥
            if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(buyPackageEntity.getType()) &&
                    (!UserInfo.BATTERY_DEPOSIT_STATUS_NO.equals(userInfo.getBatteryDepositStatus()) || !UserInfo.CAR_DEPOSIT_STATUS_NO.equals(userInfo.getCarDepositStatus()))) {
                log.error("BuyRentalPackageOrder failed. Package type mismatch. Buy package type is {}, user package type is battery", buyPackageEntity.getType());
                return R.fail("300005", "套餐不匹配");
            }

            if (ObjectUtils.isNotEmpty(memberTermEntity)) {
                // 此处代表用户名下有租车套餐（单车或车电一体）
                // 7.2 用户名下的套餐类型和即将购买的套餐类型不一致
                if (!memberTermEntity.getRentalPackageType().equals(buyPackageEntity.getType())) {
                    log.error("BuyRentalPackageOrder failed. Package type mismatch. Buy package type is {}, user package type is {}", buyPackageEntity.getType(), memberTermEntity.getRentalPackageType());
                    return R.fail("300005", "套餐不匹配");
                }
                userTenantId = memberTermEntity.getTenantId();
                userFranchiseeId = Long.valueOf(memberTermEntity.getFranchiseeId());
                userStoreId = Long.valueOf(memberTermEntity.getStoreId());
            }

            // 7.3 用户归属和套餐归属不一致(租户、加盟商、门店)，拦截
            if (ObjectUtils.notEqual(userStoreId, UserInfo.VIRTUALLY_STORE_ID)) {
                if (ObjectUtils.notEqual(userTenantId, buyPackageEntity.getTenantId()) || ObjectUtils.notEqual(userFranchiseeId, Long.valueOf(buyPackageEntity.getFranchiseeId()))
                        || ObjectUtils.notEqual(userStoreId, Long.valueOf(buyPackageEntity.getStoreId()))) {
                    log.error("BuyRentalPackageOrder failed. Package belong mismatch. ");
                    return R.fail("300005", "套餐不匹配");
                }
            } else {
                if (ObjectUtils.notEqual(userTenantId, buyPackageEntity.getTenantId()) || ObjectUtils.notEqual(userFranchiseeId, Long.valueOf(buyPackageEntity.getFranchiseeId()))) {
                    log.error("BuyRentalPackageOrder failed. Package belong mismatch. ");
                    return R.fail("300005", "套餐不匹配");
                }
            }

            // 7.4 类型一致、归属一致，比对：型号（车或者车、电） + 押金
            if (ObjectUtils.isNotEmpty(memberTermEntity)) {
                if (buyPackageEntity.getDeposit().compareTo(deposit) != 0) {
                    log.error("BuyRentalPackageOrder failed. Package deposit mismatch. ");
                    return R.fail("300005", "套餐不匹配");
                }
                // 比对型号
                String rentalPackageOrderNo = memberTermEntity.getRentalPackageOrderNo();
                CarRentalPackageOrderPO oriPackageOrderEntity = null;
                if (StringUtils.isNotBlank(rentalPackageOrderNo)) {
                    // 未退租
                    // 根据套餐购买订单编号，获取套餐购买订单表，读取其中的套餐快照信息
                    oriPackageOrderEntity = carRentalPackageOrderService.selectByOrderNo(rentalPackageOrderNo);

                } else{
                    // 退租未退押
                    // 查找最后一个购买的订单信息
                    oriPackageOrderEntity = carRentalPackageOrderService.seletLastByUid(tenantId, uid);
                }

                // 比对车辆型号
                CarRentalPackagePO oriCarRentalPackageEntity = carRentalPackageService.selectById(oriPackageOrderEntity.getRentalPackageId());
                if (!oriCarRentalPackageEntity.getCarModelId().equals(buyPackageEntity.getCarModelId())) {
                    log.error("BuyRentalPackageOrder failed. Package carModelId mismatch. ");
                    return R.fail("300005", "套餐不匹配");
                }
                // 车电一体，比对电池型号
                if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(oriCarRentalPackageEntity.getType())) {
                    List<String> oriBatteryList = carRentalPackageCarBatteryRelService.selectByRentalPackageId(oriCarRentalPackageEntity.getId()).stream().map(CarRentalPackageCarBatteryRelPO::getBatteryModelType).collect(Collectors.toList());
                    List<String> buyBatteryList = carRentalPackageCarBatteryRelService.selectByRentalPackageId(buyPackageEntity.getId()).stream().map(CarRentalPackageCarBatteryRelPO::getBatteryModelType).collect(Collectors.toList());
                    if (!buyBatteryList.containsAll(oriBatteryList)) {
                        log.error("BuyRentalPackageOrder failed. Package battery mismatch. ");
                        return R.fail("300005", "套餐不匹配");
                    }
                }
            }

            // 检测结束，进入购买阶段，
            // 1）押金处理
            // 待新增的押金信息，肯定没有走免押
            CarRentalPackageDepositPayPO depositPayInsertEntity = null;
            // 押金缴纳订单编码
            String depositPayOrderNo = null;
            CarRentalPackageDepositPayPO depositPayEntity = carRentalPackageDepositPayService.selectUnRefundCarDeposit(tenantId, uid);
            // 没有押金订单，此时肯定也没有申请免押，因为免押是另外的线路，在下订单之前就已经生成记录了
            if (ObjectUtils.isEmpty(depositPayEntity)) {
                if (YesNoEnum.YES.getCode().equals(buyOptModel.getDepositType())) {
                    // 免押
                    return R.fail("300006", "未缴纳押金");
                }
                // 生成押金缴纳订单，准备 insert
                depositPayInsertEntity = buildCarRentalPackageDepositPay(tenantId, uid, buyPackageEntity.getDeposit(), YesNoEnum.NO.getCode(), buyPackageEntity.getFranchiseeId(), buyPackageEntity.getStoreId(), buyPackageEntity.getType());
                depositPayOrderNo = depositPayInsertEntity.getOrderNo();
            } else {
                // 存在押金信息，但是不匹配
                if ((YesNoEnum.YES.getCode().equals(buyOptModel.getDepositType()) && !PayTypeEnum.EXEMPT.getCode().equals(depositPayEntity.getPayType()))
                        || YesNoEnum.NO.getCode().equals(buyOptModel.getDepositType()) && PayTypeEnum.EXEMPT.getCode().equals(depositPayEntity.getPayType())) {
                    // 免押
                    return R.fail("300007", "请选择对应的押金缴纳方式");
                }
                depositPayOrderNo = depositPayEntity.getOrderNo();
            }

            // 判定 depositPayInsertEntity 是否需要新增
            if (!ObjectUtils.isEmpty(depositPayInsertEntity)) {
                carRentalPackageDepositPayService.insert(depositPayInsertEntity);
                deposit = depositPayInsertEntity.getDeposit();
            } else {
                deposit = BigDecimal.ZERO;
            }

            // 2）保险处理 TODO 志龙接口
            InsuranceOrder insuranceOrderInsertEntity = buildInsuranceOrder(uid);
            // 保险费用初始化
            BigDecimal insuranceAmount = BigDecimal.ZERO;

            // 3）支付金额处理
            // 优惠券只抵扣租金
            Triple<BigDecimal, List<Long>, Boolean> couponTriple = carRentalPackageBizService.calculatePaymentAmount(buyPackageEntity.getRent(), buyOptModel.getUserCouponIds(), uid);
            // 实际支付租金金额
            BigDecimal rentPaymentAmount = couponTriple.getLeft();
            // 实际支付总金额（租金 + 押金 + 保险）
            BigDecimal paymentAmount = rentPaymentAmount.add(deposit).add(insuranceAmount);

            // 4）生成租车套餐订单，准备 insert
            CarRentalPackageOrderPO carRentalPackageOrder = buildCarRentalPackageOrder(buyPackageEntity, rentPaymentAmount, tenantId, uid, depositPayOrderNo);
            carRentalPackageOrderService.insert(carRentalPackageOrder);

            // 5）租车套餐会员期限处理
            if (ObjectUtils.isEmpty(memberTermEntity)) {
                // 生成租车套餐会员期限表信息，准备 Insert
                CarRentalPackageMemberTermPO memberTermInsertEntity = buildCarRentalPackageMemberTerm(tenantId, uid, buyPackageEntity, carRentalPackageOrder);
                carRentalPackageMemberTermService.insert(memberTermInsertEntity);
            }

            // 6）更改用户优惠券状态使用中
            // 使用的优惠券
            List<Long> userCouponIds = couponTriple.getMiddle();
            List<UserCoupon> userCouponList = buildUserCouponList(userCouponIds, UserCoupon.STATUS_IS_BEING_VERIFICATION, carRentalPackageOrder.getOrderNo(), OrderTypeEnum.CAR_BUY_ORDER.getCode());
            userCouponService.batchUpdateUserCoupon(userCouponList);

            // 7）支付零元的处理
            if (BigDecimal.ZERO.compareTo(paymentAmount) == 0) {
                // 无须唤起支付，走支付回调的逻辑，抽取方法，直接调用
                handBuyRentalPackageOrderSuccess(carRentalPackageOrder.getOrderNo(), tenantId, uid);
                return R.ok();
            }

            // 唤起支付
            CommonPayOrder commonPayOrder = CommonPayOrder.builder()
                    .orderId(carRentalPackageOrder.getOrderNo())
                    .uid(uid)
                    .payAmount(paymentAmount)
                    .orderType(CallBackEnums.CAR_RENAL_PACKAGE_ORDER.getCode())
                    .attach(CallBackEnums.CAR_RENAL_PACKAGE_ORDER.getDesc())
                    .description("租车套餐购买收费")
                    .tenantId(tenantId).build();

            WechatJsapiOrderResultDTO resultDTO =
                    electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, payParamsEntity, userOauthBindEntity.getThirdId(), request);

            return R.ok(resultDTO);
        } catch (Exception e) {
            log.error("BuyRentalPackageOrder failed. ", e);
        } finally {
            redisService.delete(buyLockKey);
        }

        return R.ok();
    }

    /**
     * 支付成功之后的逻辑<br />
     * 此处逻辑不包含回调处理，是回调逻辑中的一处子逻辑<br />
     * 调用此方法需要慎重
     * @param orderNo 租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> handBuyRentalPackageOrderSuccess(String orderNo, Integer tenantId, Long uid) {
        // 1. 处理租车套餐购买订单
        CarRentalPackageOrderPO carRentalPackageOrderEntity = carRentalPackageOrderService.selectByOrderNo(orderNo);
        if (ObjectUtil.isEmpty(carRentalPackageOrderEntity)) {
            log.error("NotifyCarRenalPackageOrder failed, not found car_rental_package_order, order_no is {}", orderNo);
            return Pair.of(false, "未找到租车套餐购买订单");
        }

        // 订单支付状态不匹配
        if (ObjectUtil.notEqual(PayStateEnum.UNPAID.getCode(), carRentalPackageOrderEntity.getPayState())) {
            log.error("NotifyCarRenalPackageOrder failed, car_rental_package_order processed, order_no is {}", orderNo);
            return Pair.of(false, "租车套餐购买订单已处理");
        }

        // 若名下只有这一条数据，则及时变为使用中，记录开始使用时间，若不止这一条，则只更新支付状态
        Integer unUseNum = carRentalPackageOrderService.countByUnUseByUid(tenantId, uid);
        if (unUseNum.intValue() > 1) {
            // 更改套餐购买订单的支付状态
            carRentalPackageOrderService.updatePayStateByOrderNo(orderNo, PayStateEnum.SUCCESS.getCode());
        } else {
            // 更改支付状态、使用状态、开始使用时间
            carRentalPackageOrderService.updateStateByOrderNo(orderNo, PayStateEnum.SUCCESS.getCode(), UseStateEnum.IN_USE.getCode());
        }

        // 2. 处理租车套餐押金缴纳订单
        String depositPayOrderNo = carRentalPackageOrderEntity.getDepositPayOrderNo();
        CarRentalPackageDepositPayPO depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity)) {
            log.error("NotifyCarRenalPackageOrder failed, not found car_rental_package_deposit_pay, order_no is {}", depositPayOrderNo);
            return Pair.of(false, "未找到租车套餐押金缴纳订单");
        }

        // 判定押金缴纳订单是否需要更改支付状态
        if (ObjectUtil.equal(PayStateEnum.UNPAID.getCode(), depositPayEntity.getPayState())) {
            carRentalPackageDepositPayService.updatePayStateByOrderNo(depositPayOrderNo, PayStateEnum.SUCCESS.getCode());
        }

        // 3. 处理租车套餐会员期限
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("NotifyCarRenalPackageOrder failed, not found car_rental_package_member_term, uid is {}", uid);
            return Pair.of(false, "未找到租车会员记录信息");
        }

        // 待生效的数据，直接更改状态
        if (MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            carRentalPackageMemberTermService.updateStatusById(memberTermEntity.getId(), MemberTermStatusEnum.NORMAL.getCode(), null);
        }

        // 正常的数据，更改总计到期时间、总计套餐余量
        if (MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            CarRentalPackageMemberTermPO memberTermUpdateEntity = new CarRentalPackageMemberTermPO();
            memberTermUpdateEntity.setId(memberTermEntity.getId());

            // 计算总到期时间
            Integer tenancy = carRentalPackageOrderEntity.getTenancy();
            Integer tenancyUnit = carRentalPackageOrderEntity.getTenancyUnit();
            long dueTime = System.currentTimeMillis();
            if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                dueTime = dueTime + (tenancy * TimeConstant.DAY_MILLISECOND);
            }
            if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                dueTime = dueTime + (tenancy * 1000);
            }
            memberTermUpdateEntity.setDueTimeTotal(memberTermEntity.getDueTimeTotal() + dueTime);

            // 套餐购买总次数
            memberTermUpdateEntity.setPayCount(memberTermEntity.getPayCount() + 1);

            carRentalPackageMemberTermService.updateById(memberTermUpdateEntity);
        }

        // 4. 处理用户押金支付信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("NotifyCarRenalPackageOrder failed, not found user_info, uid is {}", uid);
            return Pair.of(false, "未找到用户信息");
        }

        if (YesNoEnum.NO.getCode().equals(userInfo.getCarBatteryDepositStatus()) || UserInfo.CAR_DEPOSIT_STATUS_NO.equals(userInfo.getCarDepositStatus())) {
            LambdaUpdateWrapper<UserInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserInfo::getUid, uid).eq(UserInfo::getTenantId, tenantId)
                    .set(UserInfo::getUpdateTime, System.currentTimeMillis());
            if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(carRentalPackageOrderEntity.getRentalPackageType())) {
                updateWrapper.set(UserInfo::getCarBatteryDepositStatus, YesNoEnum.YES.getCode());
            } else {
                updateWrapper.set(UserInfo::getCarDepositStatus, UserInfo.CAR_DEPOSIT_STATUS_YES);
            }
            userInfoService.update(updateWrapper);
        }

        // 5. 处理用户套餐购买次数叠加
        UserInfo userInfoUpdateEntity = new UserInfo();
        userInfoUpdateEntity.setUid(uid);
        userInfoUpdateEntity.setPayCount(userInfo.getPayCount() + 1);
        userInfoService.updateByUid(userInfoUpdateEntity);


        // 6. 处理用户优惠券的使用状态
        userCouponService.updateStatusByOrderId(orderNo, OrderTypeEnum.CAR_BUY_ORDER.getCode(), UserCoupon.STATUS_USED);


        // 6. TODO 车辆断启电

        rocketMqService.sendAsyncMsg("topic", "msg");
        // 7. TODO 处理保险购买订单
        // 8. TODO 处理分账
        // 9. TODO 处理活动
        return Pair.of(true, userInfo.getPhone());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> handBuyRentalPackageOrderFailed(String orderNo, Integer tenantId, Long uid) {
        // 1. 处理租车套餐购买订单
        CarRentalPackageOrderPO carRentalPackageOrderEntity = carRentalPackageOrderService.selectByOrderNo(orderNo);
        if (ObjectUtil.isEmpty(carRentalPackageOrderEntity)) {
            log.error("NotifyCarRenalPackageOrder failed, not found car_rental_package_order, order_no is {}", orderNo);
            return Pair.of(false, "未找到租车套餐购买订单");
        }

        // 订单支付状态不匹配
        if (ObjectUtil.notEqual(PayStateEnum.UNPAID.getCode(), carRentalPackageOrderEntity.getPayState())) {
            log.error("NotifyCarRenalPackageOrder failed, car_rental_package_order processed, order_no is {}", orderNo);
            return Pair.of(false, "租车套餐购买订单已处理");
        }

        // 更改套餐购买订单的支付状态
        carRentalPackageOrderService.updatePayStateByOrderNo(orderNo, PayStateEnum.FAILED.getCode());

        // 2. 处理租车套餐押金缴纳订单
        String depositPayOrderNo = carRentalPackageOrderEntity.getDepositPayOrderNo();
        CarRentalPackageDepositPayPO depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity)) {
            log.error("NotifyCarRenalPackageOrder failed, not found car_rental_package_deposit_pay, order_no is {}", depositPayOrderNo);
            return Pair.of(false, "未找到租车套餐押金缴纳订单");
        }

        // 判定押金缴纳订单是否需要更改支付状态
        if (ObjectUtil.equal(PayStateEnum.UNPAID.getCode(), depositPayEntity.getPayState())) {
            carRentalPackageDepositPayService.updatePayStateByOrderNo(depositPayOrderNo, PayStateEnum.FAILED.getCode());
        }

        // 3. 处理租车套餐会员期限
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("NotifyCarRenalPackageOrder failed, not found car_rental_package_member_term, uid is {}", uid);
            return Pair.of(false, "未找到租车会员记录信息");
        }

        // 待生效的数据，直接删除
        if (MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            carRentalPackageMemberTermService.delByUidAndTenantId(tenantId, uid, uid);
        }

        // 4. 处理用户押金支付信息（保持原样，不做处理）

        // 5. 处理用户优惠券的使用状态
        userCouponService.updateStatusByOrderId(orderNo, OrderTypeEnum.CAR_BUY_ORDER.getCode(), UserCoupon.STATUS_UNUSED);

        // 7. TODO 处理保险购买订单

        return Pair.of(true, null);
    }

    /**
     * 构建租车套餐会员期限信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param packageEntity 租车套餐信息
     * @param carRentalPackageOrderEntity 租车套餐订单信息
     * @return 将要新增的租车会员期限信息
     */
    private CarRentalPackageMemberTermPO buildCarRentalPackageMemberTerm(Integer tenantId, Long uid, CarRentalPackagePO packageEntity, CarRentalPackageOrderPO carRentalPackageOrderEntity) {
        CarRentalPackageMemberTermPO carRentalPackageMemberTermEntity = new CarRentalPackageMemberTermPO();
        carRentalPackageMemberTermEntity.setUid(uid);
        carRentalPackageMemberTermEntity.setRentalPackageOrderNo(carRentalPackageOrderEntity.getOrderNo());
        carRentalPackageMemberTermEntity.setRentalPackageId(packageEntity.getId());
        carRentalPackageMemberTermEntity.setRentalPackageType(packageEntity.getType());
        carRentalPackageMemberTermEntity.setRentalPackageConfine(packageEntity.getConfine());
        carRentalPackageMemberTermEntity.setResidue(packageEntity.getConfineNum());
        carRentalPackageMemberTermEntity.setStatus(MemberTermStatusEnum.PENDING_EFFECTIVE.getCode());
        carRentalPackageMemberTermEntity.setDeposit(carRentalPackageOrderEntity.getDeposit());
        carRentalPackageMemberTermEntity.setTenantId(tenantId);
        carRentalPackageMemberTermEntity.setFranchiseeId(packageEntity.getFranchiseeId());
        carRentalPackageMemberTermEntity.setStoreId(packageEntity.getStoreId());
        carRentalPackageMemberTermEntity.setCreateUid(uid);
        carRentalPackageMemberTermEntity.setUpdateUid(uid);
        carRentalPackageMemberTermEntity.setCreateTime(System.currentTimeMillis());
        carRentalPackageMemberTermEntity.setUpdateTime(System.currentTimeMillis());
        carRentalPackageMemberTermEntity.setDelFlag(DelFlagEnum.OK.getCode());

        // 计算到期时间
        Integer tenancy = packageEntity.getTenancy();
        Integer tenancyUnit = packageEntity.getTenancyUnit();
        long dueTime = System.currentTimeMillis();
        if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
            dueTime = dueTime + (tenancy * TimeConstant.DAY_MILLISECOND);
        }
        if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
            dueTime = dueTime + (tenancy * 1000);
        }

        carRentalPackageMemberTermEntity.setDueTime(dueTime);
        carRentalPackageMemberTermEntity.setDueTimeTotal(dueTime);

        return carRentalPackageMemberTermEntity;
    }

    /**
     * 构建保险订单信息
     * @return
     */
    private InsuranceOrder buildInsuranceOrder(Long uid) {
        // TODO 赋值具体值
        InsuranceOrder insuranceOrder = new InsuranceOrder();
        return insuranceOrder;
    }

    /**
     * 构建押金订单信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param deposit 押金
     * @param freeDeposit 免押
     * @param franchiseeId 加盟商ID
     * @param storeId 门店ID
     * @param rentalPackageType 套餐类型
     * @return 待新增的押金缴纳订单
     */
    private CarRentalPackageDepositPayPO buildCarRentalPackageDepositPay(Integer tenantId, Long uid, BigDecimal deposit, Integer freeDeposit, Integer franchiseeId, Integer storeId, Integer rentalPackageType) {
        CarRentalPackageDepositPayPO carRentalPackageDepositPayEntity = new CarRentalPackageDepositPayPO();
        carRentalPackageDepositPayEntity.setUid(uid);
        carRentalPackageDepositPayEntity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT, uid));
        carRentalPackageDepositPayEntity.setRentalPackageType(rentalPackageType);
        carRentalPackageDepositPayEntity.setType(DepositTypeEnum.NORMAL.getCode());
        carRentalPackageDepositPayEntity.setChangeAmount(BigDecimal.ZERO);
        carRentalPackageDepositPayEntity.setDeposit(deposit);
        carRentalPackageDepositPayEntity.setFreeDeposit(freeDeposit);
        carRentalPackageDepositPayEntity.setPayType(PayTypeEnum.ON_LINE.getCode());
        carRentalPackageDepositPayEntity.setPayState(PayStateEnum.UNPAID.getCode());
        carRentalPackageDepositPayEntity.setTenantId(tenantId);
        carRentalPackageDepositPayEntity.setFranchiseeId(franchiseeId);
        carRentalPackageDepositPayEntity.setStoreId(storeId);
        carRentalPackageDepositPayEntity.setCreateUid(uid);
        carRentalPackageDepositPayEntity.setUpdateUid(uid);
        carRentalPackageDepositPayEntity.setCreateTime(System.currentTimeMillis());
        carRentalPackageDepositPayEntity.setUpdateTime(System.currentTimeMillis());
        carRentalPackageDepositPayEntity.setDelFlag(DelFlagEnum.OK.getCode());

        return carRentalPackageDepositPayEntity;
    }

    /**
     * 构建用户优惠券使用信息
     * @param userCouponIds 用户优惠券ID
     * @param status 状态
     * @param orderNo 订单编号
     * @param orderIdType 订单类型
     * @return
     */
    private List<UserCoupon> buildUserCouponList(List<Long> userCouponIds, Integer status, String orderNo, Integer orderIdType) {
        return userCouponIds.stream().map(userCouponId -> {
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setId(userCouponId);
            userCoupon.setOrderId(orderNo);
            userCoupon.setOrderIdType(orderIdType);
            userCoupon.setStatus(status);
            userCoupon.setUpdateTime(System.currentTimeMillis());
            return userCoupon;
        }).collect(Collectors.toList());
    }

    /**
     * 构建租车套餐订单购买信息
     * @param packagePO 套餐信息
     * @param rentPayment 租金(支付价格)
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param depositPayOrderNo 押金缴纳订单编号
     * @return
     */
    private CarRentalPackageOrderPO buildCarRentalPackageOrder(CarRentalPackagePO packagePO, BigDecimal rentPayment, Integer tenantId, Long uid, String depositPayOrderNo) {

        CarRentalPackageOrderPO carRentalPackageOrderEntity = new CarRentalPackageOrderPO();
        carRentalPackageOrderEntity.setUid(uid);
        carRentalPackageOrderEntity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_MEMBERCARD, uid));
        carRentalPackageOrderEntity.setRentalPackageId(packagePO.getId());
        carRentalPackageOrderEntity.setRentalPackageType(packagePO.getType());
        carRentalPackageOrderEntity.setConfine(packagePO.getConfine());
        carRentalPackageOrderEntity.setConfineNum(packagePO.getConfineNum());
        carRentalPackageOrderEntity.setTenancy(packagePO.getTenancy());
        carRentalPackageOrderEntity.setTenancyUnit(packagePO.getTenancyUnit());
        carRentalPackageOrderEntity.setRentUnitPrice(packagePO.getRentUnitPrice());
        carRentalPackageOrderEntity.setRent(packagePO.getRent());
        carRentalPackageOrderEntity.setRentPayment(rentPayment);
        carRentalPackageOrderEntity.setApplicableType(packagePO.getApplicableType());
        carRentalPackageOrderEntity.setRentRebate(packagePO.getRentRebate());
        carRentalPackageOrderEntity.setRentRebateTerm(packagePO.getRentRebateTerm());
        carRentalPackageOrderEntity.setRentRebateEndTime(TimeConstant.DAY_MILLISECOND * packagePO.getRentRebateTerm() + System.currentTimeMillis());
        carRentalPackageOrderEntity.setDeposit(packagePO.getDeposit());
        carRentalPackageOrderEntity.setDepositPayOrderNo(depositPayOrderNo);
        carRentalPackageOrderEntity.setLateFee(packagePO.getLateFee());
        carRentalPackageOrderEntity.setPayType(PayTypeEnum.ON_LINE.getCode());
        carRentalPackageOrderEntity.setCouponId(packagePO.getCouponId());
        carRentalPackageOrderEntity.setPayState(PayStateEnum.UNPAID.getCode());
        carRentalPackageOrderEntity.setUseState(UseStateEnum.UN_USED.getCode());
        carRentalPackageOrderEntity.setTenantId(tenantId);
        carRentalPackageOrderEntity.setFranchiseeId(packagePO.getFranchiseeId());
        carRentalPackageOrderEntity.setStoreId(packagePO.getStoreId());
        carRentalPackageOrderEntity.setCreateUid(uid);
        carRentalPackageOrderEntity.setUpdateUid(uid);
        carRentalPackageOrderEntity.setCreateTime(System.currentTimeMillis());
        carRentalPackageOrderEntity.setUpdateTime(System.currentTimeMillis());
        carRentalPackageOrderEntity.setDelFlag(DelFlagEnum.OK.getCode());

        return carRentalPackageOrderEntity;
    }
}
