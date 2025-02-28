package com.xiliulou.electricity.service.impl.installment;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.installment.InstallmentConstants;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.installment.InstallmentRecordMapper;
import com.xiliulou.electricity.query.installment.InstallmentDeductionPlanQuery;
import com.xiliulou.electricity.query.installment.InstallmentPayQuery;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentTerminatingRecordQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.installment.InstallmentBizService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.service.installment.InstallmentTerminatingRecordService;
import com.xiliulou.electricity.utils.InstallmentUtil;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.installment.InstallmentRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_SIGN_CANCEL_LOCK;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_FAIL;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_PAID;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_CANCEL_PAY;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_UNPAID;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_UN_SIGN;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.TERMINATING_RECORD_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.TERMINATING_RECORD_STATUS_REFUSE;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:51
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstallmentRecordServiceImpl implements InstallmentRecordService {

    private final InstallmentRecordMapper installmentRecordMapper;

    private final FranchiseeService franchiseeService;

    private final BatteryMemberCardService batteryMemberCardService;

    private final CarRentalPackageService carRentalPackageService;

    private final InstallmentDeductionPlanService installmentDeductionPlanService;

    private final InstallmentTerminatingRecordService installmentTerminatingRecordService;

    private final RedisService redisService;

    @Override
    public Integer insert(InstallmentRecord installmentRecord) {
        return installmentRecordMapper.insert(installmentRecord);
    }

    @Override
    public Integer update(InstallmentRecord installmentRecord) {
        return installmentRecordMapper.update(installmentRecord);
    }

    @Slave
    @Override
    public R<List<InstallmentRecordVO>> listForPage(InstallmentRecordQuery installmentRecordQuery) {
        List<InstallmentRecord> installmentRecords = installmentRecordMapper.selectPage(installmentRecordQuery);

        if (CollectionUtils.isEmpty(installmentRecords)) {
            return R.ok(ListUtil.empty());
        }

        List<InstallmentRecordVO> installmentRecordVos = installmentRecords.parallelStream().map(installmentRecord -> {
            InstallmentRecordVO installmentRecordVO = new InstallmentRecordVO();
            BeanUtils.copyProperties(installmentRecord, installmentRecordVO);

            // 设置加盟商名称
            Franchisee franchisee = franchiseeService.queryByIdFromCache(installmentRecord.getFranchiseeId());
            installmentRecordVO.setFranchiseeName(Objects.isNull(franchisee) ? null : franchisee.getName());

            // 设置套餐信息
            setPackageMessage(installmentRecordVO, installmentRecord);

            installmentRecordVO.setUnpaidInstallmentNo(installmentRecordVO.getInstallmentNo() - installmentRecordVO.getPaidInstallment());
            return installmentRecordVO;
        }).collect(Collectors.toList());

        return R.ok(installmentRecordVos);
    }

    @Slave
    @Override
    public R<Integer> count(InstallmentRecordQuery installmentRecordQuery) {
        return R.ok(installmentRecordMapper.count(installmentRecordQuery));
    }

    @Override
    public Triple<Boolean, String, InstallmentRecord> generateInstallmentRecord(InstallmentPayQuery query, BatteryMemberCard batteryMemberCard,
                                                                                CarRentalPackagePo carRentalPackagePo, UserInfo userInfo) {
        // 生成分期签约记录订单号
        String externalAgreementNo = OrderIdUtil.generateBusinessOrderId(BusinessType.INSTALLMENT_SIGN, userInfo.getUid());
        InstallmentRecord installmentRecord = new InstallmentRecord();
        installmentRecord.setUid(userInfo.getUid());
        installmentRecord.setExternalAgreementNo(externalAgreementNo);
        installmentRecord.setUserName(null);
        installmentRecord.setMobile(null);
        installmentRecord.setPackageType(query.getPackageType());
        installmentRecord.setPaidAmount(new BigDecimal("0"));
        installmentRecord.setStatus(INSTALLMENT_RECORD_STATUS_UNPAID);
        installmentRecord.setPaidInstallment(0);
        installmentRecord.setCreateTime(System.currentTimeMillis());
        installmentRecord.setUpdateTime(System.currentTimeMillis());

        if (InstallmentConstants.PACKAGE_TYPE_BATTERY.equals(query.getPackageType())) {
            if (Objects.isNull(batteryMemberCard)) {
                return Triple.of(false, null, null);
            }

            Integer installmentNo = batteryMemberCard.getValidDays() / 30;
            installmentRecord.setInstallmentNo(installmentNo);
            installmentRecord.setInstallmentServiceFee(batteryMemberCard.getInstallmentServiceFee());
            installmentRecord.setTenantId(batteryMemberCard.getTenantId());
            installmentRecord.setFranchiseeId(batteryMemberCard.getFranchiseeId());
            installmentRecord.setPackageId(batteryMemberCard.getId());
        }
        return Triple.of(true, null, installmentRecord);
    }

    @Override
    public InstallmentRecord queryRecordWithStatusForUser(Long uid, List<Integer> statuses) {
        return installmentRecordMapper.selectRecordWithStatusForUser(uid, statuses);
    }

    @Slave
    @Override
    public InstallmentRecord queryByExternalAgreementNo(String externalAgreementNo) {
        return installmentRecordMapper.selectByExternalAgreementNo(externalAgreementNo);
    }

    @Slave
    @Override
    public InstallmentRecord queryByExternalAgreementNoWithoutUnpaid(String externalAgreementNo) {
        return installmentRecordMapper.selectByExternalAgreementNoWithoutUnpaid(externalAgreementNo);
    }

    @Slave
    @Override
    public R<InstallmentRecordVO> queryInstallmentRecordForUser(String externalAgreementNo) {
        InstallmentRecordVO installmentRecordVO = new InstallmentRecordVO();
        InstallmentRecord installmentRecord;

        if (!StringUtils.isEmpty(externalAgreementNo)) {
            // 用户端查询指定的签约记录
            installmentRecord = queryByExternalAgreementNoWithoutUnpaid(externalAgreementNo);
        } else {
            // 用户端查询最新签约记录
            Long uid = SecurityUtils.getUid();
            // 初始化状态的签约记录为未支付签约服务费时，不可展示给用户，也不参与业务，若一直不支付，3天后由自动取消机制修改为取消状态
            installmentRecord = queryLatestUsingRecordByUid(uid);
        }

        if (Objects.isNull(installmentRecord)) {
            return R.ok();
        }
        BeanUtils.copyProperties(installmentRecord, installmentRecordVO);

        // 设置套餐信息
        setPackageMessage(installmentRecordVO, installmentRecord);

        // 查询有无逾期代扣计划
        InstallmentDeductionPlanQuery deductionPlanQuery = new InstallmentDeductionPlanQuery();
        deductionPlanQuery.setStatuses(Arrays.asList(DEDUCTION_PLAN_STATUS_INIT, DEDUCTION_PLAN_STATUS_FAIL));
        deductionPlanQuery.setEndTime(System.currentTimeMillis());
        deductionPlanQuery.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());

        List<InstallmentDeductionPlan> deductionPlans = installmentDeductionPlanService.listDeductionPlanByAgreementNo(deductionPlanQuery).getData();
        installmentRecordVO.setOverdue(CollectionUtils.isEmpty(deductionPlans) ? 0 : 1);

        // 查询有无审核中的、被拒绝的解约申请
        InstallmentTerminatingRecordQuery terminatingRecordQuery = new InstallmentTerminatingRecordQuery();
        terminatingRecordQuery.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        terminatingRecordQuery.setStatuses(Arrays.asList(TERMINATING_RECORD_STATUS_INIT, TERMINATING_RECORD_STATUS_REFUSE));

        List<InstallmentTerminatingRecord> terminatingRecords = installmentTerminatingRecordService.listForRecordWithStatus(terminatingRecordQuery);

        if (CollectionUtils.isEmpty(terminatingRecords)) {
            installmentRecordVO.setUnderReview(0);
            return R.ok(installmentRecordVO);
        }

        // 设置有无审核中的或被拒绝的解约申请
        InstallmentTerminatingRecord terminatingRecord = terminatingRecords.get(0);
        if (Objects.equals(terminatingRecord.getStatus(), TERMINATING_RECORD_STATUS_INIT)) {
            installmentRecordVO.setUnderReview(1);
        } else if (Objects.equals(terminatingRecord.getStatus(), TERMINATING_RECORD_STATUS_REFUSE)) {
            installmentRecordVO.setRefused(1);
            installmentRecordVO.setOpinion(terminatingRecord.getOpinion());
        }

        return R.ok(installmentRecordVO);
    }

    @Override
    public R<String> cancel(String externalAgreementNo) {
        InstallmentRecord installmentRecord = installmentRecordMapper.selectByExternalAgreementNoWithoutUnpaid(externalAgreementNo);
        if (Objects.isNull(installmentRecord)) {
            return R.fail("301005", "签约记录不存在");
        }

        if (!redisService.setNx(String.format(CACHE_INSTALLMENT_SIGN_CANCEL_LOCK, installmentRecord.getUid()), "1", 3 * 1000L, false)) {
            return R.fail("301019", "当前套餐正在签约或取消，请稍候再试");
        }

        List<Integer> list = Arrays.asList(INSTALLMENT_RECORD_STATUS_INIT, INSTALLMENT_RECORD_STATUS_UN_SIGN);
        if (Objects.isNull(installmentRecord.getStatus()) || !list.contains(installmentRecord.getStatus())) {
            return R.fail("301016", "该分期套餐已签约成功，不可取消");
        }

        InstallmentRecord installmentRecordUpdate = new InstallmentRecord();
        installmentRecordUpdate.setId(installmentRecord.getId());
        installmentRecordUpdate.setStatus(INSTALLMENT_RECORD_STATUS_CANCEL_PAY);
        installmentRecordUpdate.setUpdateTime(System.currentTimeMillis());
        installmentRecordMapper.update(installmentRecordUpdate);

        return R.ok();
    }

    @Slave
    @Override
    public InstallmentRecord queryLatestUsingRecordByUid(Long uid) {
        return installmentRecordMapper.selectLatestUsingRecordByUid(uid);
    }


    private void setPackageMessage(InstallmentRecordVO installmentRecordVO, InstallmentRecord installmentRecord) {
        if (Objects.equals(installmentRecordVO.getPackageType(), InstallmentConstants.PACKAGE_TYPE_BATTERY)) {
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(installmentRecordVO.getPackageId());
            
            // 根据代扣计划计算签约总金额与未支付金额
            Pair<BigDecimal, BigDecimal> pair = installmentTerminatingRecordService.queryRentPriceAndUnpaidAmount(installmentRecord.getExternalAgreementNo());
            installmentRecordVO.setRentPrice(pair.getLeft());
            installmentRecordVO.setUnpaidAmount(pair.getRight());
            
            if (Objects.isNull(batteryMemberCard)) {
                return;
            }
            
            installmentRecordVO.setPackageName(batteryMemberCard.getName());
            installmentRecordVO.setDownPayment(batteryMemberCard.getDownPayment());
            installmentRecordVO.setValidDays(batteryMemberCard.getValidDays());

            if (Objects.equals(installmentRecordVO.getInstallmentNo(), 1)) {
                return;
            }
            // 计算剩余每期金额
            installmentRecordVO.setRemainingPrice(InstallmentUtil.calculateSuborderAmount(2, installmentRecord, batteryMemberCard));
        } else {
            CarRentalPackagePo carRentalPackagePo = carRentalPackageService.selectById(installmentRecordVO.getPackageId());
            installmentRecordVO.setPackageName(carRentalPackagePo.getName());
        }
    }

}
