package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.DivisionAccountRecordMapper;
import com.xiliulou.electricity.query.DivisionAccountRecordQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
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
    private DivisionAccountCarModelService divisionAccountCarModelService;

    @Autowired
    private CarMemberCardOrderService carMemberCardOrderService;

    @Autowired
    private ElectricityMemberCardOrderService eleMemberCardOrderService;

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

            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            divisionAccountRecordVO.setUserName(Objects.nonNull(userInfo) ? userInfo.getName() : "");

            DivisionAccountConfig accountConfig = divisionAccountConfigService
                    .queryByIdFromCache(item.getDivisionAccountConfigId());
            divisionAccountRecordVO
                    .setDivisionAccountConfigName(Objects.nonNull(accountConfig) ? accountConfig.getName() : "");
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
            DivisionAccountConfig divisionAccountConfig = divisionAccountConfigService
                    .queryByIdFromCache(item.getDivisionAccountConfigId());
            if (Objects.isNull(divisionAccountConfig)) {
                return;
            }

            Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
            item.setOperatorName(Objects.nonNull(tenant) ? tenant.getName() : "");

            Franchisee franchisee = franchiseeService.queryByIdFromCache(divisionAccountConfig.getFranchiseeId());
            item.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");

            Store store = storeService.queryByIdFromCache(divisionAccountConfig.getStoreId());
            item.setStoreName(Objects.nonNull(store) ? store.getName() : "");

        }).collect(Collectors.toList());
    }

    @Override
    public Integer selectStatisticByPageCount(DivisionAccountRecordQuery query) {
        return divisionAccountRecordMapper.selectStatisticByPageCount(query);
    }

    @Override
    public void handleBatteryMembercardDivisionAccount(ElectricityMemberCardOrder batteryMemberCardOrder) {
        divisionAccountExecutorService.execute(() -> {
            DivisionAccountConfig divisionAccountConfig = divisionAccountConfigService.queryByIdFromCache(
                    divisionAccountBatteryMembercardService
                            .selectByBatteryMembercardId(batteryMemberCardOrder.getMemberCardId().longValue()));
            if (Objects.isNull(divisionAccountConfig)) {
                log.info("ELE INFO! not found divisionAccountConfig,batteryMembercardId={}",
                        batteryMemberCardOrder.getMemberCardId());
                return;
            }

            BigDecimal userPayAmount = batteryMemberCardOrder.getPayAmount();
            BigDecimal operatorIncome = userPayAmount
                    .multiply(BigDecimal.ZERO.compareTo(divisionAccountConfig.getOperatorRate()) == 0 ? BigDecimal.ONE : divisionAccountConfig.getOperatorRate(), new MathContext(2, RoundingMode.DOWN));
            BigDecimal franchiseeIncome = userPayAmount
                    .multiply(BigDecimal.ZERO.compareTo(divisionAccountConfig.getFranchiseeRate()) == 0 ? BigDecimal.ONE : divisionAccountConfig.getFranchiseeRate(), new MathContext(2, RoundingMode.DOWN));
            BigDecimal storeIncome = userPayAmount
                    .multiply(BigDecimal.ZERO.compareTo(divisionAccountConfig.getStoreRate()) == 0 ? BigDecimal.ONE : divisionAccountConfig.getStoreRate(), new MathContext(2, RoundingMode.DOWN));

            //保存分帐记录
            DivisionAccountRecord divisionAccountRecord = new DivisionAccountRecord();
            divisionAccountRecord.setMembercardName(batteryMemberCardOrder.getCardName());
            divisionAccountRecord.setUid(batteryMemberCardOrder.getUid());
            divisionAccountRecord.setOrderNo(batteryMemberCardOrder.getOrderId());
            divisionAccountRecord.setPayAmount(batteryMemberCardOrder.getPayAmount());
            divisionAccountRecord.setPayTime(batteryMemberCardOrder.getUpdateTime());
            divisionAccountRecord.setDivisionAccountConfigId(divisionAccountConfig.getId());
            divisionAccountRecord.setOperatorIncome(operatorIncome);
            divisionAccountRecord.setFranchiseeIncome(franchiseeIncome);
            divisionAccountRecord.setStoreIncome(storeIncome);
            divisionAccountRecord.setTenantId(batteryMemberCardOrder.getTenantId());
            divisionAccountRecord.setStatus(DivisionAccountRecord.STATUS_SUCCESS);
            divisionAccountRecord.setDelFlag(DivisionAccountRecord.DEL_NORMAL);
            divisionAccountRecord.setCreateTime(System.currentTimeMillis());
            divisionAccountRecord.setUpdateTime(System.currentTimeMillis());
            divisionAccountRecordMapper.insert(divisionAccountRecord);
        });
    }

    @Override
    public void handleCarMembercardDivisionAccount(CarMemberCardOrder carMemberCardOrder) {
        divisionAccountExecutorService.execute(() -> {
            DivisionAccountConfig divisionAccountConfig = divisionAccountConfigService.queryByIdFromCache(
                    divisionAccountCarModelService.selectByCarModelId(carMemberCardOrder.getCarModelId()));
            if (Objects.isNull(divisionAccountConfig)) {
                log.info("ELE INFO! not found divisionAccountConfig,batteryMembercardId={}",
                        carMemberCardOrder.getCarModelId());
                return;
            }

            BigDecimal userPayAmount = carMemberCardOrder.getPayAmount();
            BigDecimal operatorIncome = userPayAmount
                    .multiply(divisionAccountConfig.getOperatorRate(), new MathContext(2, RoundingMode.DOWN));
            BigDecimal franchiseeIncome = userPayAmount
                    .multiply(divisionAccountConfig.getFranchiseeRate(), new MathContext(2, RoundingMode.DOWN));
            BigDecimal storeIncome = userPayAmount
                    .multiply(divisionAccountConfig.getStoreRate(), new MathContext(2, RoundingMode.DOWN));

            //保存分帐记录
            DivisionAccountRecord divisionAccountRecord = new DivisionAccountRecord();
            divisionAccountRecord.setMembercardName(carMemberCardOrder.getCardName());
            divisionAccountRecord.setUid(carMemberCardOrder.getUid());
            divisionAccountRecord.setOrderNo(carMemberCardOrder.getOrderId());
            divisionAccountRecord.setPayAmount(carMemberCardOrder.getPayAmount());
            divisionAccountRecord.setPayTime(carMemberCardOrder.getUpdateTime());
            divisionAccountRecord.setDivisionAccountConfigId(divisionAccountConfig.getId());
            divisionAccountRecord.setOperatorIncome(operatorIncome);
            divisionAccountRecord.setFranchiseeIncome(franchiseeIncome);
            divisionAccountRecord.setStoreIncome(storeIncome);
            divisionAccountRecord.setTenantId(carMemberCardOrder.getTenantId());
            divisionAccountRecord.setStatus(DivisionAccountRecord.STATUS_SUCCESS);
            divisionAccountRecord.setDelFlag(DivisionAccountRecord.DEL_NORMAL);
            divisionAccountRecord.setCreateTime(System.currentTimeMillis());
            divisionAccountRecord.setUpdateTime(System.currentTimeMillis());
            divisionAccountRecordMapper.insert(divisionAccountRecord);
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
