package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.dto.DivisionAccountOrderDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderRentRefundPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.DivisionAccountEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.DivisionAccountRecordMapper;
import com.xiliulou.electricity.query.DivisionAccountRecordQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderRentRefundService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.DivisionAccountAmountVO;
import com.xiliulou.electricity.vo.DivisionAccountConfigRefVO;
import com.xiliulou.electricity.vo.DivisionAccountRecordStatisticVO;
import com.xiliulou.electricity.vo.DivisionAccountRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.MDC;
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

    protected XllThreadPoolExecutorService divisionAccountExecutorService = XllThreadPoolExecutors
            .newFixedThreadPool("DIVISION_ACCOUNT_THREAD_POOL", 4, "division_account_thread");

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
    BatteryMemberCardService batteryMemberCardService;

    @Autowired
    private ElectricityCabinetService eleCabinetService;

    @Autowired
    private ElectricityCarModelService eleCarModelService;

    @Autowired
    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Autowired
    private CarRentalPackageService carRentalPackageService;

    @Autowired
    private BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;

    @Autowired
    private CarRentalPackageOrderRentRefundService carRentalPackageOrderRentRefundService;

    @Autowired
    RedisService redisService;

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

            Store store = storeService.queryByIdFromCache(item.getStoreId());
            item.setStoreName(Objects.nonNull(store) ? store.getName() : "");

            item.setDivisionAccountConfigName(divisionAccountConfig.getName());

        }).collect(Collectors.toList());
    }

    @Override
    public Integer selectStatisticByPageCount(DivisionAccountRecordQuery query) {
        return divisionAccountRecordMapper.selectStatisticByPageCount(query);
    }

    @Deprecated
    @Override
    public void handleBatteryMembercardDivisionAccount(ElectricityMemberCardOrder batteryMemberCardOrder) {
        divisionAccountExecutorService.execute(() -> {

            log.info("DIVISION ACCOUNT INFO!batteryMemberCardOrder,orderId={},uid={}", batteryMemberCardOrder.getOrderId(), batteryMemberCardOrder.getUid());

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
                divisionAccountRecord.setFranchiseeId(batteryMemberCardOrder.getFranchiseeId());
                divisionAccountRecord.setStoreId(batteryMemberCardOrder.getStoreId());
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
    @Transactional(rollbackFor = Exception.class)
    public void handleDivisionAccountByPackage(DivisionAccountOrderDTO divisionAccountOrderDTO){
        String orderNo = divisionAccountOrderDTO.getOrderNo();
        Integer type = divisionAccountOrderDTO.getType();
        MDC.put(CommonConstant.TRACE_ID, divisionAccountOrderDTO.getTraceId());

        String value = orderNo + "_" + type;
        if (!redisService
                .setNx(CacheConstant.CACHE_DIVISION_ACCOUNT_PACKAGE_PURCHASE_KEY + value, value, 10 * 1000L, false)) {
            //return Triple.of(false, "000000", "操作频繁，请稍后再试！");
            log.error("Division Account by package error, operations frequently, order number = {}, package type = {}", orderNo, type);
            return;
        }

        try{
            if(DivisionAccountBatteryMembercard.TYPE_BATTERY.equals(type)){
                ElectricityMemberCardOrder electricityMemberCardOrder = eleMemberCardOrderService.selectByOrderNo(orderNo);
                if(Objects.isNull(electricityMemberCardOrder)){
                    log.error("Division Account error, Not found for electricity member card, order number = {}", orderNo);
                    return;
                }

                log.info("Division Account flow Start for purchase battery package, order number = {}, package type = {}, uid = {}", electricityMemberCardOrder.getOrderId(), type, electricityMemberCardOrder.getUid());

                DivisionAccountConfigRefVO divisionAccountConfigRefVO = divisionAccountConfigService.selectDivisionConfigByRefId(electricityMemberCardOrder.getMemberCardId().longValue(), null, electricityMemberCardOrder.getFranchiseeId(), electricityMemberCardOrder.getTenantId());
                if (Objects.isNull(divisionAccountConfigRefVO)) {
                    log.info("Division Account info, Division account for electricity member card, not found division account config info, orderId = {}, uid = {}", electricityMemberCardOrder.getOrderId(), electricityMemberCardOrder.getUid());
                    return;
                }

                DivisionAccountAmountVO divisionAccountAmountVO = calculateBenefitsByBatteryDA(electricityMemberCardOrder.getPayAmount(), electricityMemberCardOrder.getSource(), divisionAccountConfigRefVO);
                if(Objects.isNull(divisionAccountAmountVO)){
                    log.error("calculate Division Account amount error");
                    return;
                }
                //获取套餐信息
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrder.getMemberCardId());
                log.info("Division Account flow get franchisee id and store id, franchisee id = {}, store id = {}", divisionAccountConfigRefVO.getFranchiseeId(), divisionAccountConfigRefVO.getStoreId());

                //保存分帐记录
                DivisionAccountRecord divisionAccountRecord = new DivisionAccountRecord();
                divisionAccountRecord.setMembercardName(batteryMemberCard.getName());
                divisionAccountRecord.setUid(electricityMemberCardOrder.getUid());
                divisionAccountRecord.setOrderNo(electricityMemberCardOrder.getOrderId());
                divisionAccountRecord.setPayAmount(electricityMemberCardOrder.getPayAmount());
                divisionAccountRecord.setPayTime(electricityMemberCardOrder.getUpdateTime());
                divisionAccountRecord.setDivisionAccountConfigId(divisionAccountConfigRefVO.getId());
                divisionAccountRecord.setOperatorIncome(divisionAccountAmountVO.getOperatorIncome());
                divisionAccountRecord.setFranchiseeIncome(divisionAccountAmountVO.getFranchiseeIncome());
                divisionAccountRecord.setStoreIncome(divisionAccountAmountVO.getStoreIncome());
                divisionAccountRecord.setOperatorRate(divisionAccountAmountVO.getOperatorRate());
                divisionAccountRecord.setFranchiseeRate(divisionAccountAmountVO.getFranchiseeRate());
                divisionAccountRecord.setStoreRate(divisionAccountAmountVO.getStoreRate());
                divisionAccountRecord.setSource(electricityMemberCardOrder.getSource());
                divisionAccountRecord.setTenantId(electricityMemberCardOrder.getTenantId());
                divisionAccountRecord.setFranchiseeId(divisionAccountConfigRefVO.getFranchiseeId());
                divisionAccountRecord.setStoreId(divisionAccountConfigRefVO.getStoreId());
                divisionAccountRecord.setStatus(DivisionAccountRecord.STATUS_SUCCESS);
                divisionAccountRecord.setDelFlag(DivisionAccountRecord.DEL_NORMAL);
                divisionAccountRecord.setType(DivisionAccountRecord.TYPE_PURCHASE);

                if(BatteryMemberCard.YES.equals(batteryMemberCard.getIsRefund())){
                    divisionAccountRecord.setDivisionAccountStatus(DivisionAccountRecord.DA_STATUS_FREEZE);
                }else{
                    divisionAccountRecord.setDivisionAccountStatus(DivisionAccountRecord.DA_STATUS_NORMAL);
                }
                divisionAccountRecord.setCreateTime(System.currentTimeMillis());
                divisionAccountRecord.setUpdateTime(System.currentTimeMillis());
                divisionAccountRecordMapper.insert(divisionAccountRecord);

                log.info("Division Account flow end, purchase battery package, order number = {}, package type = {}, uid = {}", electricityMemberCardOrder.getOrderId(), type, electricityMemberCardOrder.getUid());

            } else {
                CarRentalPackageOrderPo carRentalPackageOrderPO = carRentalPackageOrderService.selectByOrderNo(orderNo);
                if(Objects.isNull(carRentalPackageOrderPO)){
                    log.error("Division Account error, Not found for car rental package, order number = {}", orderNo);
                    return;
                }

                log.info("Division Account flow start for car rental,  car rental or car with battery package order, order number = {}, uid = {}", orderNo, carRentalPackageOrderPO.getUid());

                DivisionAccountConfigRefVO divisionAccountConfigRefVO = divisionAccountConfigService.selectDivisionConfigByRefId(carRentalPackageOrderPO.getRentalPackageId(), null, carRentalPackageOrderPO.getFranchiseeId().longValue(), carRentalPackageOrderPO.getTenantId());
                if (Objects.isNull(divisionAccountConfigRefVO)) {
                    log.error("Division Account error, Division account for car rental or car-electricity package, not found division account config info, orderId = {},uid = {}", orderNo, carRentalPackageOrderPO.getUid());
                    return;
                }

                DivisionAccountAmountVO CarRentalAmountVO = calculateBenefitsByCarRentalPackage(carRentalPackageOrderPO.getRentPayment(), divisionAccountConfigRefVO);
                //获取租车/车电一体套餐信息
                CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(carRentalPackageOrderPO.getRentalPackageId());

                //保存分帐记录
                DivisionAccountRecord divisionAccountRecord = new DivisionAccountRecord();
                divisionAccountRecord.setMembercardName(carRentalPackagePO.getName());
                divisionAccountRecord.setUid(carRentalPackageOrderPO.getUid());
                divisionAccountRecord.setOrderNo(carRentalPackageOrderPO.getOrderNo());
                divisionAccountRecord.setPayAmount(carRentalPackageOrderPO.getRentPayment());
                divisionAccountRecord.setPayTime(carRentalPackageOrderPO.getUpdateTime());
                divisionAccountRecord.setDivisionAccountConfigId(divisionAccountConfigRefVO.getId());
                divisionAccountRecord.setOperatorIncome(CarRentalAmountVO.getOperatorIncome());
                divisionAccountRecord.setFranchiseeIncome(CarRentalAmountVO.getFranchiseeIncome());
                divisionAccountRecord.setStoreIncome(CarRentalAmountVO.getStoreIncome());
                divisionAccountRecord.setOperatorRate(CarRentalAmountVO.getOperatorRate());
                divisionAccountRecord.setFranchiseeRate(CarRentalAmountVO.getFranchiseeRate());
                divisionAccountRecord.setStoreRate(CarRentalAmountVO.getStoreRate());

                divisionAccountRecord.setTenantId(carRentalPackageOrderPO.getTenantId());
                divisionAccountRecord.setFranchiseeId(divisionAccountConfigRefVO.getFranchiseeId());
                divisionAccountRecord.setStoreId(divisionAccountConfigRefVO.getStoreId());
                divisionAccountRecord.setStatus(DivisionAccountRecord.STATUS_SUCCESS);
                divisionAccountRecord.setDelFlag(DivisionAccountRecord.DEL_NORMAL);
                divisionAccountRecord.setType(DivisionAccountRecord.TYPE_PURCHASE);

                if(YesNoEnum.YES.getCode().equals(carRentalPackagePO.getRentRebate())){
                    divisionAccountRecord.setDivisionAccountStatus(DivisionAccountRecord.DA_STATUS_FREEZE);
                }else{
                    divisionAccountRecord.setDivisionAccountStatus(DivisionAccountRecord.DA_STATUS_NORMAL);
                }
                divisionAccountRecord.setCreateTime(System.currentTimeMillis());
                divisionAccountRecord.setUpdateTime(System.currentTimeMillis());
                divisionAccountRecordMapper.insert(divisionAccountRecord);

                log.info("Division Account flow end for car rental package, orderId = {}, package type, uid = {}", orderNo, type, carRentalPackageOrderPO.getUid());

            }
        }
        catch(Exception e){
            log.error("Division Account error, Division account for purchase package error, order number = {}, package type = {}", orderNo, type, e);
            throw new BizException("100000", e.getMessage());
        }finally {
            redisService.delete(CacheConstant.CACHE_DIVISION_ACCOUNT_PACKAGE_PURCHASE_KEY + value);
            MDC.clear();
        }
    }

    /**
     * 计算换电套餐分账金额
     * @param userPayAmount
     * @param orderSource
     * @param divisionAccountConfigRefVO
     * @return
     */
    private DivisionAccountAmountVO calculateBenefitsByBatteryDA(BigDecimal userPayAmount, Integer orderSource,DivisionAccountConfigRefVO divisionAccountConfigRefVO){

        DivisionAccountAmountVO divisionAccountAmountVO = new DivisionAccountAmountVO();

        BigDecimal operatorIncome = BigDecimal.ZERO;
        BigDecimal franchiseeIncome = BigDecimal.ZERO;
        BigDecimal storeIncome = BigDecimal.ZERO;

        BigDecimal operatorRate = divisionAccountConfigRefVO.getOperatorRate();
        BigDecimal franchiseeRate = divisionAccountConfigRefVO.getFranchiseeRate();
        BigDecimal storeRate = divisionAccountConfigRefVO.getStoreRate();

        if (DivisionAccountConfig.HIERARCHY_TWO.equals(divisionAccountConfigRefVO.getHierarchy())) {
            //二级分帐
            operatorIncome = getAmountByRate(userPayAmount, operatorRate);
            franchiseeIncome = getAmountByRate(userPayAmount, franchiseeRate);
        } else if (DivisionAccountConfig.HIERARCHY_THREE.equals(divisionAccountConfigRefVO.getHierarchy())) {
            //三级分帐
            //扫码 门店、加盟商、运营商分帐
            //TODO 代码待删除，3.0版本后 不需要分柜机和非柜机，该处的代码注释掉。
            /*if (ElectricityMemberCardOrder.SOURCE_SCAN.equals(orderSource)) {
                operatorIncome = getAmountByRate(userPayAmount, operatorRate);
                franchiseeIncome = getAmountByRate(userPayAmount, franchiseeRate);
                storeIncome = getAmountByRate(userPayAmount, storeRate);
            } else {
                //非扫码 加盟商、运营商分帐
                operatorRate = divisionAccountConfigRefVO.getOperatorRateOther();
                franchiseeRate = divisionAccountConfigRefVO.getFranchiseeRateOther();

                operatorIncome = getAmountByRate(userPayAmount, operatorRate);
                franchiseeIncome = getAmountByRate(userPayAmount, franchiseeRate);
            }*/
            operatorIncome = getAmountByRate(userPayAmount, operatorRate);
            franchiseeIncome = getAmountByRate(userPayAmount, franchiseeRate);
            storeIncome = getAmountByRate(userPayAmount, storeRate);

        } else {
            log.warn("Division Account error, Division account for electricity member card, not found division account hierarchy, DA config id = {}", divisionAccountConfigRefVO.getId());
            return null;
        }

        divisionAccountAmountVO.setOperatorIncome(operatorIncome);
        divisionAccountAmountVO.setFranchiseeIncome(franchiseeIncome);
        divisionAccountAmountVO.setStoreIncome(storeIncome);
        divisionAccountAmountVO.setOperatorRate(operatorRate);
        divisionAccountAmountVO.setFranchiseeRate(franchiseeRate);
        divisionAccountAmountVO.setStoreRate(storeRate);

        return divisionAccountAmountVO;
    }

    /**
     * 计算租车套餐和车电一体套餐分账金额
     * @param userPayAmount
     * @param divisionAccountConfigRefVO
     * @return
     */
    private DivisionAccountAmountVO calculateBenefitsByCarRentalPackage(BigDecimal userPayAmount, DivisionAccountConfigRefVO divisionAccountConfigRefVO){

        DivisionAccountAmountVO divisionAccountAmountVO = new DivisionAccountAmountVO();
        BigDecimal operatorIncome = BigDecimal.ZERO;
        BigDecimal franchiseeIncome = BigDecimal.ZERO;
        BigDecimal storeIncome = BigDecimal.ZERO;

        BigDecimal operatorRate = divisionAccountConfigRefVO.getOperatorRate();
        BigDecimal franchiseeRate = divisionAccountConfigRefVO.getFranchiseeRate();
        BigDecimal storeRate = divisionAccountConfigRefVO.getStoreRate();

        if (DivisionAccountConfig.HIERARCHY_TWO.equals(divisionAccountConfigRefVO.getHierarchy())) {
            //二级分帐
            operatorIncome = getAmountByRate(userPayAmount, operatorRate);
            franchiseeIncome = getAmountByRate(userPayAmount, franchiseeRate);
        } else if (DivisionAccountConfig.HIERARCHY_THREE.equals(divisionAccountConfigRefVO.getHierarchy())) {
            //三级分帐
            operatorIncome = getAmountByRate(userPayAmount, operatorRate);
            franchiseeIncome = getAmountByRate(userPayAmount, franchiseeRate);
            storeIncome = getAmountByRate(userPayAmount, storeRate);
        } else {
            log.warn("Division account for car rental or car-electricity package error, not found division account hierarchy, DA config id = {}", divisionAccountConfigRefVO.getId());
            return null;
        }

        divisionAccountAmountVO.setOperatorIncome(operatorIncome);
        divisionAccountAmountVO.setFranchiseeIncome(franchiseeIncome);
        divisionAccountAmountVO.setStoreIncome(storeIncome);
        divisionAccountAmountVO.setOperatorRate(operatorRate);
        divisionAccountAmountVO.setFranchiseeRate(franchiseeRate);
        divisionAccountAmountVO.setStoreRate(storeRate);

        return divisionAccountAmountVO;

    }

    /**
     * 处理退款时的分账业务
     * @param divisionAccountOrderDTO 退款订单号及套餐类型信息对象
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRefundDivisionAccountByPackage(DivisionAccountOrderDTO divisionAccountOrderDTO){
        String orderNo = divisionAccountOrderDTO.getOrderNo();
        Integer type = divisionAccountOrderDTO.getType();
        MDC.put(CommonConstant.TRACE_ID, divisionAccountOrderDTO.getTraceId());

        String value = orderNo + "_" + type;
        if (!redisService
                .setNx(CacheConstant.CACHE_DIVISION_ACCOUNT_PACKAGE_REFUND_KEY + value, value, 10 * 1000L, false)) {
            log.error("Division Account by refund error, operations frequently, order number = {}, package type = {}", orderNo, type);
            return;
        }

        try{

            if(DivisionAccountBatteryMembercard.TYPE_BATTERY.equals(type)){
                BatteryMembercardRefundOrder batteryMembercardRefundOrder = batteryMembercardRefundOrderService.selectByRefundOrderNo(orderNo);

                if(Objects.isNull(batteryMembercardRefundOrder)) {
                    log.error("Refund Division Account error, Not found for battery member card refund order, refund order number = {}, package type = {}", orderNo, type);
                    return;
                }
                log.info("Refund Division Account flow Start, electricity member card order, refund order id = {}, uid = {}", batteryMembercardRefundOrder.getId(), batteryMembercardRefundOrder.getUid());
                ElectricityMemberCardOrder electricityMemberCardOrder = eleMemberCardOrderService.selectByOrderNo(batteryMembercardRefundOrder.getMemberCardOrderNo());
                if(Objects.isNull(electricityMemberCardOrder)){
                    log.error("Refund Division Account error, Not found purchase battery package order data, refund order number = {}", orderNo);
                    return;
                }

                //退租时,需要查询出之前购买时的分账记录，按照购买时的分账记录，无需按照比例将退款返给用户，直接按照购买时的记录退款。产品已确定需求
                DivisionAccountRecord divisionAccountRecord = this.divisionAccountRecordMapper.selectByOrderId(batteryMembercardRefundOrder.getMemberCardOrderNo());

                //检查购买的分账记录是否存在，如果购买记录不存在，则返回。
                if(Objects.isNull(divisionAccountRecord)){
                    log.error("Refund Division Account error, Not found division account record for purchase battery package, refund order number = {}", orderNo);
                    return;
                }

                //保存分帐记录
                DivisionAccountRecord refundDivisionAccountRecord = new DivisionAccountRecord();
                refundDivisionAccountRecord.setMembercardName(electricityMemberCardOrder.getCardName());
                refundDivisionAccountRecord.setUid(batteryMembercardRefundOrder.getUid());
                refundDivisionAccountRecord.setOrderNo(batteryMembercardRefundOrder.getRefundOrderNo());
                refundDivisionAccountRecord.setPayAmount(batteryMembercardRefundOrder.getRefundAmount());
                refundDivisionAccountRecord.setPayTime(batteryMembercardRefundOrder.getUpdateTime());
                refundDivisionAccountRecord.setDivisionAccountConfigId(divisionAccountRecord.getDivisionAccountConfigId());
                refundDivisionAccountRecord.setOperatorIncome(divisionAccountRecord.getOperatorIncome());
                refundDivisionAccountRecord.setFranchiseeIncome(divisionAccountRecord.getFranchiseeIncome());
                refundDivisionAccountRecord.setStoreIncome(divisionAccountRecord.getStoreIncome());
                refundDivisionAccountRecord.setOperatorRate(divisionAccountRecord.getOperatorRate());
                refundDivisionAccountRecord.setFranchiseeRate(divisionAccountRecord.getFranchiseeRate());
                refundDivisionAccountRecord.setStoreRate(divisionAccountRecord.getStoreRate());
                refundDivisionAccountRecord.setSource(electricityMemberCardOrder.getSource());
                refundDivisionAccountRecord.setTenantId(batteryMembercardRefundOrder.getTenantId());
                refundDivisionAccountRecord.setFranchiseeId(divisionAccountRecord.getFranchiseeId());
                refundDivisionAccountRecord.setStoreId(divisionAccountRecord.getStoreId());
                refundDivisionAccountRecord.setStatus(DivisionAccountRecord.STATUS_SUCCESS);
                refundDivisionAccountRecord.setDelFlag(DivisionAccountRecord.DEL_NORMAL);
                refundDivisionAccountRecord.setType(DivisionAccountRecord.TYPE_REFUND);
                refundDivisionAccountRecord.setDivisionAccountStatus(DivisionAccountRecord.DA_STATUS_NORMAL);
                refundDivisionAccountRecord.setCreateTime(System.currentTimeMillis());
                refundDivisionAccountRecord.setUpdateTime(System.currentTimeMillis());
                divisionAccountRecordMapper.insert(refundDivisionAccountRecord);

                //退租后，需要将之前购买的分账记录状态置为无效
                updateDARecordStatus(divisionAccountRecord, DivisionAccountRecord.DA_STATUS_INVALIDITY);

                log.info("Refund Division Account flow end, orderId = {}, package type = {}, uid = {}", electricityMemberCardOrder.getOrderId(), type, electricityMemberCardOrder.getUid());

            } else {
                //处理租车和车电一体的退租分账记录
                handleRefundDivisionAccountByCarRentalPackage(orderNo);

            }

        }catch (Exception e){
            log.error("Division Account for refund error, Division account for refund package error, order number = {}, package type = {}", orderNo, type, e);
            throw new BizException("100001", e.getMessage());
        }finally {
            redisService.delete(CacheConstant.CACHE_DIVISION_ACCOUNT_PACKAGE_REFUND_KEY + value);
            MDC.clear();
        }

    }

    /**
     * 更新超过七天的分账记录状态为冻结的记录，将分账状态更新为正常
     */
    @Override
    public void updateDivisionAccountStatusForFreezeOrder() {
        Long exceedTime = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L;
        List<DivisionAccountRecord> divisionAccountRecords = divisionAccountRecordMapper.selectDAFreezeStatusRecordsByTime(exceedTime);
        if(CollectionUtils.isNotEmpty(divisionAccountRecords)){
            log.info("update division account records = {}", JsonUtil.toJson(divisionAccountRecords));

            for(DivisionAccountRecord divisionAccountRecord : divisionAccountRecords){
               //只要查到超过七天的状态还是冻结态的记录，则将其修改为正常，因为如果有退单的记录，这个状态就会被置为失效。所以能查到的肯定是未退款的记录
                updateDARecordStatus(divisionAccountRecord, DivisionAccountRecord.DA_STATUS_NORMAL);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDARecordStatus(DivisionAccountRecord divisionAccountRecord, Integer status){
        DivisionAccountRecord accountRecord = new DivisionAccountRecord();
        accountRecord.setId(divisionAccountRecord.getId());
        accountRecord.setDivisionAccountStatus(status);
        accountRecord.setUpdateTime(System.currentTimeMillis());
        divisionAccountRecordMapper.updateDAStatus(accountRecord);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleRefundDivisionAccountByCarRentalPackage(String orderNo){
        CarRentalPackageOrderRentRefundPo carRentalPackageOrderRentRefundPO = carRentalPackageOrderRentRefundService.selectByOrderNo(orderNo);
        if(Objects.isNull(carRentalPackageOrderRentRefundPO)){
            log.error("Refund Division Account error, Not found for car rental package order, refund order number = {}", orderNo);
            return;
        }

        log.info("Refund Division Account Start, car rental package order, refund order id = {}, uid = {}", carRentalPackageOrderRentRefundPO.getId(), carRentalPackageOrderRentRefundPO.getUid());
        CarRentalPackageOrderPo carRentalPackageOrderPO = carRentalPackageOrderService.selectByOrderNo(carRentalPackageOrderRentRefundPO.getRentalPackageOrderNo());

        //退租时,需要查询出之前购买时的分账记录，按照购买时的分账记录，无需按照比例将退款返给用户，直接按照购买时的记录退款。产品已确定需求
        DivisionAccountRecord divisionAccountRecord = this.divisionAccountRecordMapper.selectByOrderId(carRentalPackageOrderPO.getOrderNo());
        if(Objects.isNull(divisionAccountRecord)){
            log.error("Refund Division Account error, Not found division account record for purchase car rental package , refund order number = {}", orderNo);
            return;
        }

        //检查套餐信息是否存在。
        CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(carRentalPackageOrderPO.getRentalPackageId());
        if(Objects.isNull(carRentalPackagePO)){
            log.error("Refund Division Account error, Not found purchase car rental package data, refund order number = {}", orderNo);
            return;
        }

        //不需要再根据之前的分账设置去计算退款金额，直接按购买时的分账金额全部退回。同时将构面分账记录状态设置为失效状态。如果后续分账时只需查找分账状态为正常且创建时间大于7天的记录即可。
        //保存分帐记录
        DivisionAccountRecord refundDivisionAccountRecord = new DivisionAccountRecord();
        refundDivisionAccountRecord.setMembercardName(carRentalPackagePO.getName());
        refundDivisionAccountRecord.setUid(carRentalPackageOrderRentRefundPO.getUid());
        refundDivisionAccountRecord.setOrderNo(carRentalPackageOrderRentRefundPO.getOrderNo());
        refundDivisionAccountRecord.setPayAmount(carRentalPackageOrderRentRefundPO.getRefundAmount());
        refundDivisionAccountRecord.setPayTime(carRentalPackageOrderRentRefundPO.getUpdateTime());
        refundDivisionAccountRecord.setDivisionAccountConfigId(divisionAccountRecord.getDivisionAccountConfigId());
        refundDivisionAccountRecord.setOperatorIncome(divisionAccountRecord.getOperatorIncome());
        refundDivisionAccountRecord.setFranchiseeIncome(divisionAccountRecord.getFranchiseeIncome());
        refundDivisionAccountRecord.setStoreIncome(divisionAccountRecord.getStoreIncome());
        refundDivisionAccountRecord.setOperatorRate(divisionAccountRecord.getOperatorRate());
        refundDivisionAccountRecord.setFranchiseeRate(divisionAccountRecord.getFranchiseeRate());
        refundDivisionAccountRecord.setStoreRate(divisionAccountRecord.getStoreRate());

        refundDivisionAccountRecord.setTenantId(carRentalPackageOrderRentRefundPO.getTenantId());
        refundDivisionAccountRecord.setFranchiseeId(divisionAccountRecord.getFranchiseeId());
        refundDivisionAccountRecord.setStoreId(divisionAccountRecord.getStoreId());
        refundDivisionAccountRecord.setStatus(DivisionAccountRecord.STATUS_SUCCESS);
        refundDivisionAccountRecord.setDelFlag(DivisionAccountRecord.DEL_NORMAL);
        refundDivisionAccountRecord.setType(DivisionAccountRecord.TYPE_REFUND);
        refundDivisionAccountRecord.setDivisionAccountStatus(DivisionAccountRecord.DA_STATUS_NORMAL);
        refundDivisionAccountRecord.setCreateTime(System.currentTimeMillis());
        refundDivisionAccountRecord.setUpdateTime(System.currentTimeMillis());
        divisionAccountRecordMapper.insert(refundDivisionAccountRecord);

        //退租后，需要将之前购买的分账记录状态置为无效
        updateDARecordStatus(divisionAccountRecord, DivisionAccountRecord.DA_STATUS_INVALIDITY);

        log.info("Refund Division Account end, car rental package order, refund order id = {}, uid = {}", carRentalPackageOrderRentRefundPO.getId(), carRentalPackageOrderRentRefundPO.getUid());
    }

    private BigDecimal getAmountByRate(BigDecimal userPayAmount, BigDecimal rate){
       /* BigDecimal ratePercent = BigDecimal.ZERO.compareTo(rate) == 0 ? BigDecimal.ZERO : rate.divide(BigDecimal.valueOf(100), new MathContext(2, RoundingMode.DOWN));
        return userPayAmount.multiply(ratePercent, new MathContext(2, RoundingMode.DOWN));*/
        log.info("Calculate user pay amount by division account rate, user pay amount = {}, rate = {}", userPayAmount, rate);
        BigDecimal ratePercent = BigDecimal.ZERO.compareTo(rate) == 0 ? BigDecimal.ZERO : rate.divide(BigDecimal.valueOf(100)).setScale(2, RoundingMode.DOWN);
        return userPayAmount.multiply(ratePercent).setScale(2, RoundingMode.DOWN);
    }

    @Deprecated
    @Override
    public void handleCarMembercardDivisionAccount(CarMemberCardOrder carMemberCardOrder) {
        divisionAccountExecutorService.execute(() -> {

            log.info("DIVISION ACCOUNT INFO!carMemberCardOrder,orderId={},uid={}", carMemberCardOrder.getOrderId(), carMemberCardOrder.getUid());

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
                divisionAccountRecord.setFranchiseeId(carMemberCardOrder.getFranchiseeId());
                divisionAccountRecord.setStoreId(carMemberCardOrder.getStoreId());
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

    @Override
    public void asyncHandleDivisionAccount(DivisionAccountOrderDTO divisionAccountOrderDTO) {

        divisionAccountExecutorService.execute(() -> {

            if(DivisionAccountEnum.DA_TYPE_PURCHASE.getCode().equals(divisionAccountOrderDTO.getDivisionAccountType())){
                //处理购买套餐时的分账业务
                handleDivisionAccountByPackage(divisionAccountOrderDTO);

            } else if(DivisionAccountEnum.DA_TYPE_REFUND.getCode().equals(divisionAccountOrderDTO.getDivisionAccountType())){
                //处理退租时的分账业务
                handleRefundDivisionAccountByPackage(divisionAccountOrderDTO);
            }

        });
    }
}
