package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.DivisionAccountRecordMapper;
import com.xiliulou.electricity.query.DivisionAccountRecordQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.DivisionAccountConfigRefVO;
import com.xiliulou.electricity.vo.DivisionAccountRecordStatisticVO;
import com.xiliulou.electricity.vo.DivisionAccountRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * (DivisionAccountRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-04-24 16:23:44
 */
@Service("divisionAccountRecordService")
@Slf4j
public class DivisionAccountRecordServiceImpl implements DivisionAccountRecordService {

    private static final ExecutorService divisionAccountExecutorService = XllThreadPoolExecutors
            .newFixedThreadPool("eleDivisionAccount", 4, "ele_division_account");

    @Resource
    private DivisionAccountRecordMapper divisionAccountRecordMapper;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private DivisionAccountConfigService divisionAccountConfigService;

    @Autowired
    private FranchiseeService franchiseeService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private DivisionAccountBatteryMembercardService divisionAccountBatteryMembercardService;

    @Autowired
    private CarMemberCardOrderService carMemberCardOrderService;

    @Autowired
    private ElectricityMemberCardOrderService eleMemberCardOrderService;

    @Autowired
    private ElectricityCabinetService eleCabinetService;

    @Autowired
    private ElectricityCarModelService eleCarModelService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DivisionAccountRecord queryByIdFromDB(Long id) {
        return this.divisionAccountRecordMapper.queryById(id);
    }

    @Slave
    @Override
    public List<DivisionAccountRecordVO> selectByPage(DivisionAccountRecordQuery query) {
        List<DivisionAccountRecord> list = this.divisionAccountRecordMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.stream().map(item -> {
            DivisionAccountRecordVO divisionAccountRecordVO = new DivisionAccountRecordVO();
            BeanUtils.copyProperties(item, divisionAccountRecordVO);

            UserInfo userInfo = userInfoService.queryByUidFromDb(item.getUid());
            divisionAccountRecordVO.setUserName(Objects.nonNull(userInfo) ? userInfo.getName() : "");

            DivisionAccountConfig accountConfig = divisionAccountConfigService.queryByIdFromDB(item.getDivisionAccountConfigId());
            divisionAccountRecordVO.setDivisionAccountConfigName(Objects.nonNull(accountConfig) ? accountConfig.getName() : "");
            return divisionAccountRecordVO;
        }).collect(Collectors.toList());
    }

    @Slave
    @Override
    public Integer selectByPageCount(DivisionAccountRecordQuery query) {
        return this.divisionAccountRecordMapper.selectByPageCount(query);
    }

    /**
     * 新增数据
     *
     * @param divisionAccountRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DivisionAccountRecord insert(DivisionAccountRecord divisionAccountRecord) {
        this.divisionAccountRecordMapper.insertOne(divisionAccountRecord);
        return divisionAccountRecord;
    }

    /**
     * 修改数据
     *
     * @param divisionAccountRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(DivisionAccountRecord divisionAccountRecord) {
        return this.divisionAccountRecordMapper.update(divisionAccountRecord);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.divisionAccountRecordMapper.deleteById(id) > 0;
    }

    @Override
    public List<DivisionAccountRecordStatisticVO> selectStatisticByPage(DivisionAccountRecordQuery query) {
        List<DivisionAccountRecordStatisticVO> list = this.divisionAccountRecordMapper.selectStatisticByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.stream().peek(item -> {
            DivisionAccountConfig divisionAccountConfig = divisionAccountConfigService.queryByIdFromDB(item.getDivisionAccountConfigId());
            if (Objects.isNull(divisionAccountConfig)) {
                return;
            }

            Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
            item.setOperatorName(Objects.nonNull(tenant) ? tenant.getName() : "");

            Franchisee franchisee = franchiseeService.queryByIdFromCache(divisionAccountConfig.getFranchiseeId());
            item.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");

            Store store = storeService.queryByIdFromCache(divisionAccountConfig.getStoreId());
            item.setStoreName(Objects.nonNull(store) ? store.getName() : "");

            item.setDivisionAccountConfigName(divisionAccountConfig.getName());

        }).collect(Collectors.toList());
    }

    @Override
    public Integer selectStatisticByPageCount(DivisionAccountRecordQuery query) {
        return divisionAccountRecordMapper.selectStatisticByPageCount(query);
    }

    @Override
    public void handleBatteryMembercardDivisionAccount(ElectricityMemberCardOrder batteryMemberCardOrder) {
        divisionAccountExecutorService.execute(() -> {

            try {
//                Long storeId = null;
//                if (ElectricityMemberCardOrder.SOURCE_SCAN.equals(batteryMemberCardOrder.getSource()) && Objects.nonNull(batteryMemberCardOrder.getRefId())) {
//                    ElectricityCabinet electricityCabinet = eleCabinetService.queryByIdFromCache(batteryMemberCardOrder.getRefId().intValue());
//                    storeId = Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : null;
//                }

                DivisionAccountConfigRefVO divisionAccountConfigRefVO = divisionAccountConfigService.selectDivisionConfigByRefId(batteryMemberCardOrder.getMemberCardId().longValue(), null, batteryMemberCardOrder.getFranchiseeId(), batteryMemberCardOrder.getTenantId());
                if (Objects.isNull(divisionAccountConfigRefVO)) {
                    log.info("ELE INFO! batteryMemberCardOrder division account fail,not found divisionAccountConfig,orderId={},uid={}", batteryMemberCardOrder.getOrderId(), batteryMemberCardOrder.getUid());
                    return;
                }

                BigDecimal userPayAmount = batteryMemberCardOrder.getPayAmount();
                BigDecimal operatorIncome = BigDecimal.ZERO;
                BigDecimal franchiseeIncome = BigDecimal.ZERO;
                BigDecimal storeIncome = BigDecimal.ZERO;

                BigDecimal operatorRate = divisionAccountConfigRefVO.getOperatorRate();
                BigDecimal franchiseeRate = divisionAccountConfigRefVO.getFranchiseeRate();
                BigDecimal storeRate = divisionAccountConfigRefVO.getStoreRate();

                //二级分帐
                if (DivisionAccountConfig.HIERARCHY_TWO.equals(divisionAccountConfigRefVO.getHierarchy())) {
                    operatorIncome = userPayAmount.multiply(BigDecimal.ZERO.compareTo(divisionAccountConfigRefVO.getOperatorRate()) == 0 ? BigDecimal.ZERO : divisionAccountConfigRefVO.getOperatorRate().divide(BigDecimal.valueOf(100), new MathContext(2, RoundingMode.DOWN)), new MathContext(2, RoundingMode.DOWN));
                    franchiseeIncome = userPayAmount.multiply(BigDecimal.ZERO.compareTo(divisionAccountConfigRefVO.getFranchiseeRate()) == 0 ? BigDecimal.ZERO : divisionAccountConfigRefVO.getFranchiseeRate().divide(BigDecimal.valueOf(100), new MathContext(2, RoundingMode.DOWN)), new MathContext(2, RoundingMode.DOWN));
                } else if (DivisionAccountConfig.HIERARCHY_THREE.equals(divisionAccountConfigRefVO.getHierarchy())) {//三级分帐
                    //扫码 门店、加盟商、运营商分帐
                    if (ElectricityMemberCardOrder.SOURCE_SCAN.equals(batteryMemberCardOrder.getSource())) {
                        operatorIncome = userPayAmount.multiply(BigDecimal.ZERO.compareTo(divisionAccountConfigRefVO.getOperatorRate()) == 0 ? BigDecimal.ZERO : divisionAccountConfigRefVO.getOperatorRate().divide(BigDecimal.valueOf(100), new MathContext(2, RoundingMode.DOWN)), new MathContext(2, RoundingMode.DOWN));
                        franchiseeIncome = userPayAmount.multiply(BigDecimal.ZERO.compareTo(divisionAccountConfigRefVO.getFranchiseeRate()) == 0 ? BigDecimal.ZERO : divisionAccountConfigRefVO.getFranchiseeRate().divide(BigDecimal.valueOf(100), new MathContext(2, RoundingMode.DOWN)), new MathContext(2, RoundingMode.DOWN));
                        storeIncome = userPayAmount.multiply(BigDecimal.ZERO.compareTo(divisionAccountConfigRefVO.getStoreRate()) == 0 ? BigDecimal.ZERO : divisionAccountConfigRefVO.getStoreRate().divide(BigDecimal.valueOf(100), new MathContext(2, RoundingMode.DOWN)), new MathContext(2, RoundingMode.DOWN));
                    } else {//非扫码 加盟商、运营商分帐
                        operatorRate = divisionAccountConfigRefVO.getOperatorRateOther();
                        franchiseeRate = divisionAccountConfigRefVO.getFranchiseeRateOther();

                        operatorIncome = userPayAmount.multiply(BigDecimal.ZERO.compareTo(divisionAccountConfigRefVO.getOperatorRateOther()) == 0 ? BigDecimal.ZERO : divisionAccountConfigRefVO.getOperatorRateOther().divide(BigDecimal.valueOf(100), new MathContext(2, RoundingMode.DOWN)), new MathContext(2, RoundingMode.DOWN));
                        franchiseeIncome = userPayAmount.multiply(BigDecimal.ZERO.compareTo(divisionAccountConfigRefVO.getFranchiseeRateOther()) == 0 ? BigDecimal.ZERO : divisionAccountConfigRefVO.getFranchiseeRateOther().divide(BigDecimal.valueOf(100), new MathContext(2, RoundingMode.DOWN)), new MathContext(2, RoundingMode.DOWN));
                    }
                } else {
                    log.warn("ELE WARN! not found division account hierarchy,id={}", divisionAccountConfigRefVO.getId());
                }

                //保存分帐记录
                DivisionAccountRecord divisionAccountRecord = new DivisionAccountRecord();
                divisionAccountRecord.setMembercardName(batteryMemberCardOrder.getCardName());
                divisionAccountRecord.setUid(batteryMemberCardOrder.getUid());
                divisionAccountRecord.setOrderNo(batteryMemberCardOrder.getOrderId());
                divisionAccountRecord.setPayAmount(batteryMemberCardOrder.getPayAmount());
                divisionAccountRecord.setPayTime(batteryMemberCardOrder.getUpdateTime());
                divisionAccountRecord.setDivisionAccountConfigId(divisionAccountConfigRefVO.getId());
                divisionAccountRecord.setOperatorIncome(operatorIncome);
                divisionAccountRecord.setFranchiseeIncome(franchiseeIncome);
                divisionAccountRecord.setStoreIncome(storeIncome);
                divisionAccountRecord.setOperatorRate(operatorRate);
                divisionAccountRecord.setFranchiseeRate(franchiseeRate);
                divisionAccountRecord.setStoreRate(storeRate);
                divisionAccountRecord.setSource(batteryMemberCardOrder.getSource());
                divisionAccountRecord.setTenantId(batteryMemberCardOrder.getTenantId());
                divisionAccountRecord.setStatus(DivisionAccountRecord.STATUS_SUCCESS);
                divisionAccountRecord.setDelFlag(DivisionAccountRecord.DEL_NORMAL);
                divisionAccountRecord.setCreateTime(System.currentTimeMillis());
                divisionAccountRecord.setUpdateTime(System.currentTimeMillis());
                divisionAccountRecordMapper.insert(divisionAccountRecord);
            } catch (Exception e) {
                log.error("ELE ERROR! batteryMemberCardOrder division account error,orderId={},uid={}", batteryMemberCardOrder.getOrderId(), batteryMemberCardOrder.getUid(), e);
            }
        });
    }

    @Override
    public void handleCarMembercardDivisionAccount(CarMemberCardOrder carMemberCardOrder) {
        divisionAccountExecutorService.execute(() -> {
            try {
                DivisionAccountConfigRefVO divisionAccountConfigRefVO = divisionAccountConfigService.selectDivisionConfigByRefId(carMemberCardOrder.getCarModelId(), null, carMemberCardOrder.getFranchiseeId(), carMemberCardOrder.getTenantId());
                if (Objects.isNull(divisionAccountConfigRefVO)) {
                    log.info("ELE INFO! carMemberCardOrder division account fail,not found divisionAccountConfig,orderId={},uid={}", carMemberCardOrder.getOrderId(), carMemberCardOrder.getUid());
                    return;
                }

                BigDecimal userPayAmount = carMemberCardOrder.getPayAmount();
                BigDecimal operatorIncome = BigDecimal.ZERO;
                BigDecimal franchiseeIncome = BigDecimal.ZERO;
                BigDecimal storeIncome = BigDecimal.ZERO;

                BigDecimal operatorRate = divisionAccountConfigRefVO.getOperatorRate();
                BigDecimal franchiseeRate = divisionAccountConfigRefVO.getFranchiseeRate();
                BigDecimal storeRate = divisionAccountConfigRefVO.getStoreRate();

                //二级分帐
                if (DivisionAccountConfig.HIERARCHY_TWO.equals(divisionAccountConfigRefVO.getHierarchy())) {
                    operatorIncome = userPayAmount.multiply(BigDecimal.ZERO.compareTo(divisionAccountConfigRefVO.getOperatorRate()) == 0 ? BigDecimal.ZERO : divisionAccountConfigRefVO.getOperatorRate().divide(BigDecimal.valueOf(100), new MathContext(2, RoundingMode.DOWN)), new MathContext(2, RoundingMode.DOWN));
                    franchiseeIncome = userPayAmount.multiply(BigDecimal.ZERO.compareTo(divisionAccountConfigRefVO.getFranchiseeRate()) == 0 ? BigDecimal.ZERO : divisionAccountConfigRefVO.getFranchiseeRate().divide(BigDecimal.valueOf(100), new MathContext(2, RoundingMode.DOWN)), new MathContext(2, RoundingMode.DOWN));
                } else if (DivisionAccountConfig.HIERARCHY_THREE.equals(divisionAccountConfigRefVO.getHierarchy())) {//三级分帐
                    operatorIncome = userPayAmount.multiply(BigDecimal.ZERO.compareTo(divisionAccountConfigRefVO.getOperatorRate()) == 0 ? BigDecimal.ZERO : divisionAccountConfigRefVO.getOperatorRate().divide(BigDecimal.valueOf(100), new MathContext(2, RoundingMode.DOWN)), new MathContext(2, RoundingMode.DOWN));
                    franchiseeIncome = userPayAmount.multiply(BigDecimal.ZERO.compareTo(divisionAccountConfigRefVO.getFranchiseeRate()) == 0 ? BigDecimal.ZERO : divisionAccountConfigRefVO.getFranchiseeRate().divide(BigDecimal.valueOf(100), new MathContext(2, RoundingMode.DOWN)), new MathContext(2, RoundingMode.DOWN));
                    storeIncome = userPayAmount.multiply(BigDecimal.ZERO.compareTo(divisionAccountConfigRefVO.getStoreRate()) == 0 ? BigDecimal.ZERO : divisionAccountConfigRefVO.getStoreRate().divide(BigDecimal.valueOf(100), new MathContext(2, RoundingMode.DOWN)), new MathContext(2, RoundingMode.DOWN));
                } else {
                    log.warn("ELE WARN! not found division account hierarchy,id={}", divisionAccountConfigRefVO.getId());
                }

                ElectricityCarModel carModel = eleCarModelService.queryByIdFromCache(carMemberCardOrder.getCarModelId().intValue());

                //保存分帐记录
                DivisionAccountRecord divisionAccountRecord = new DivisionAccountRecord();
                divisionAccountRecord.setMembercardName(Objects.nonNull(carModel)?carModel.getName():"");
                divisionAccountRecord.setUid(carMemberCardOrder.getUid());
                divisionAccountRecord.setOrderNo(carMemberCardOrder.getOrderId());
                divisionAccountRecord.setPayAmount(carMemberCardOrder.getPayAmount());
                divisionAccountRecord.setPayTime(carMemberCardOrder.getUpdateTime());
                divisionAccountRecord.setDivisionAccountConfigId(divisionAccountConfigRefVO.getId());
                divisionAccountRecord.setOperatorIncome(operatorIncome);
                divisionAccountRecord.setFranchiseeIncome(franchiseeIncome);
                divisionAccountRecord.setStoreIncome(storeIncome);
                divisionAccountRecord.setOperatorRate(operatorRate);
                divisionAccountRecord.setFranchiseeRate(franchiseeRate);
                divisionAccountRecord.setStoreRate(storeRate);
                divisionAccountRecord.setTenantId(carMemberCardOrder.getTenantId());
                divisionAccountRecord.setStatus(DivisionAccountRecord.STATUS_SUCCESS);
                divisionAccountRecord.setDelFlag(DivisionAccountRecord.DEL_NORMAL);
                divisionAccountRecord.setCreateTime(System.currentTimeMillis());
                divisionAccountRecord.setUpdateTime(System.currentTimeMillis());
                divisionAccountRecordMapper.insert(divisionAccountRecord);
            } catch (Exception e) {
                log.error("ELE ERROR! carMemberCardOrder division account error,orderId={},uid={}", carMemberCardOrder.getOrderId(), carMemberCardOrder.getUid(), e);
            }
        });
    }

    @Override
    public Triple<Boolean, String, Object> divisionAccountCompensation(String orderId, Integer type) {
        DivisionAccountRecord divisionAccountRecord = this.divisionAccountRecordMapper.selectByOrderId(orderId);
        if (Objects.nonNull(divisionAccountRecord) && Objects
                .equals(divisionAccountRecord.getStatus(), DivisionAccountRecord.STATUS_SUCCESS)) {
            return Triple.of(false, "", "该订单已分帐");
        }

        if (Objects.equals(type, DivisionAccountConfig.TYPE_BATTERY)) {
            ElectricityMemberCardOrder electricityMemberCardOrder = eleMemberCardOrderService.selectByOrderNo(orderId);
            if (Objects.isNull(electricityMemberCardOrder)) {
                return Triple.of(false, "", "订单不存在");
            }

            this.handleBatteryMembercardDivisionAccount(electricityMemberCardOrder);
        }

        if (Objects.equals(type, DivisionAccountConfig.TYPE_CAR)) {
            CarMemberCardOrder carMemberCardOrder = carMemberCardOrderService.selectByOrderId(orderId);
            if (Objects.isNull(carMemberCardOrder)) {
                return Triple.of(false, "", "订单不存在");
            }

            this.handleCarMembercardDivisionAccount(carMemberCardOrder);
        }

        return Triple.of(true, "", "");
    }
}
