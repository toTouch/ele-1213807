package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.core.utils.PhoneUtils;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.RebateRecord;
import com.xiliulou.electricity.enums.merchant.PromotionFeeQueryTypeEnum;
import com.xiliulou.electricity.mapper.merchant.RebateRecordMapper;
import com.xiliulou.electricity.query.merchant.MerchantPromotionEmployeeDetailSpecificsQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionFeeQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionRenewalQueryModel;
import com.xiliulou.electricity.request.merchant.RebateRecordRequest;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.ChannelEmployeeAmountService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.MerchantUserAmountService;
import com.xiliulou.electricity.service.merchant.RebateRecordService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionEmployeeDetailSpecificsVO;
import com.xiliulou.electricity.vo.merchant.MerchantStatisticsUserVO;
import com.xiliulou.electricity.vo.merchant.RebateRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (RebateRecord)表服务实现类
 *
 * @author zzlong
 * @since 2024-02-20 14:31:51
 */
@Service("rebateRecordService")
@Slf4j
public class RebateRecordServiceImpl implements RebateRecordService {
    
    @Resource
    private RebateRecordMapper rebateRecordMapper;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private MerchantService merchantService;
    
    @Autowired
    private MerchantPlaceService merchantPlaceService;
    
    @Autowired
    private ChannelEmployeeAmountService channelEmployeeAmountService;
    
    @Autowired
    private MerchantUserAmountService merchantUserAmountService;
    
    @Override
    public RebateRecord queryById(Long id) {
        return this.rebateRecordMapper.selectById(id);
    }
    
    @Override
    public RebateRecord insert(RebateRecord rebateRecord) {
        this.rebateRecordMapper.insertOne(rebateRecord);
        return rebateRecord;
    }
    
    /**
     * 修改数据
     *
     * @param rebateRecord 实例对象
     * @return 实例对象
     */
    @Override
    public Integer updateById(RebateRecord rebateRecord) {
        return this.rebateRecordMapper.update(rebateRecord);
    }
    
    @Override
    public Integer deleteById(Long id) {
        return this.rebateRecordMapper.deleteById(id);
    }
    
    @Override
    public RebateRecord queryByOrderId(String orderId) {
        return this.rebateRecordMapper.selectByOrderId(orderId);
    }
    
    @Slave
    @Override
    public List<RebateRecord> queryByOriginalOrderId(String originalOrderId) {
        return this.rebateRecordMapper.selectByOriginalOrderId(originalOrderId);
    }
    
    @Slave
    @Override
    public RebateRecord queryLatestByOriginalOrderId(String originalOrderId) {
        return this.rebateRecordMapper.selectLatestByOriginalOrderId(originalOrderId);
    }
    
    @Slave
    @Override
    public List<RebateRecordVO> listByPage(RebateRecordRequest query) {
        List<RebateRecord> list = this.rebateRecordMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        return list.stream().map(item -> {
            RebateRecordVO rebateRecord = new RebateRecordVO();
            BeanUtils.copyProperties(item, rebateRecord);
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            rebateRecord.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");
            
            Merchant merchant = merchantService.queryByIdFromCache(item.getMerchantId());
            rebateRecord.setMerchantName(Objects.nonNull(merchant) ? merchant.getName() : "");
            
            MerchantPlace merchantPlace = merchantPlaceService.queryByIdFromCache(item.getPlaceId());
            rebateRecord.setPlaceName(Objects.nonNull(merchantPlace) ? merchantPlace.getName() : "");
            
            User placeUser = userService.queryByUidFromCache(item.getPlaceUid());
            rebateRecord.setPlaceUserName(Objects.nonNull(placeUser) ? placeUser.getName() : "");
            
            User channel = userService.queryByUidFromCache(item.getChanneler());
            rebateRecord.setChannelerName(Objects.nonNull(channel) ? channel.getName() : "");
            
            return rebateRecord;
            
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer countByPage(RebateRecordRequest query) {
        return this.rebateRecordMapper.selectByPageCount(query);
    }
    
    @Slave
    @Override
    public List<RebateRecord> listCurrentMonthRebateRecord( Long merchantId, long startTime, long endTime, int offset, int size) {
        return this.rebateRecordMapper.selectCurrentMonthRebateRecord( merchantId, startTime, endTime, offset, size);
    }
    
    @Slave
    @Override
    public List<RebateRecord> listRebatedByUid(Long uid, Long memberCardId, Long merchantId, String currentLevel) {
        return this.rebateRecordMapper.selectRebatedByUid(uid, memberCardId, merchantId, currentLevel);
    }
    
    @Override
    public Integer existsExpireRebateRecordByOriginalOrderId(String originalOrderId) {
        return this.rebateRecordMapper.existsExpireRebateRecordByOriginalOrderId(originalOrderId);
    }
    
    @Override
    @Slave
    public List<MerchantStatisticsUserVO> listRenewal(MerchantPromotionRenewalQueryModel renewalQueryModel) {
        return this.rebateRecordMapper.selectListRenewal(renewalQueryModel);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRebate(RebateRecord rebateRecord) {
        BigDecimal merchantRebate = rebateRecord.getMerchantRebate();
        BigDecimal channelerRebate = rebateRecord.getChannelerRebate();
        
        //未结算
        if (Objects.equals(rebateRecord.getStatus(), MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE)) {
            //商户返利
            merchantUserAmountService.addAmount(merchantRebate, rebateRecord.getMerchantUid(), rebateRecord.getTenantId().longValue());
            
            //渠道员返利
            channelEmployeeAmountService.addAmount(channelerRebate, rebateRecord.getChanneler(), rebateRecord.getTenantId().longValue());
        }
        
        RebateRecord rebateRecordUpdate = new RebateRecord();
        rebateRecordUpdate.setId(rebateRecord.getId());
        rebateRecordUpdate.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED);
        rebateRecordUpdate.setSettleTime(System.currentTimeMillis());
        rebateRecordUpdate.setUpdateTime(System.currentTimeMillis());
        applicationContext.getBean(RebateRecordService.class).updateById(rebateRecordUpdate);
    }
    
    @Override
    public void settleRebateRecordTask() {
        int offset = 0;
        int size = 200;
        
        long startTime = DateUtils.getDayStartTimeByLocalDate(LocalDate.now()) - 24 * 60 * 60 * 1000L;
        long endTime = DateUtils.getDayStartTimeByLocalDate(LocalDate.now());
        
        while (true) {
            List<RebateRecord> list = this.rebateRecordMapper.selectNotSettleListByLimit(startTime, endTime, offset, size);
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            
            list.forEach(item -> {
                try {
                    applicationContext.getBean(RebateRecordService.class).handleRebate(item);
                } catch (Exception e) {
                    log.error("HANDLE REBATE ERROR!orderId={}", item.getOrderId(), e);
                }
            });
            
        }
    }
    
    @Slave
    @Override
    public BigDecimal sumByStatus(MerchantPromotionFeeQueryModel merchantPromotionFeeQueryModel) {
        if (Objects.equals(PromotionFeeQueryTypeEnum.CHANNEL_EMPLOYEE.getCode(), merchantPromotionFeeQueryModel.getType())) {
            return this.rebateRecordMapper.sumEmployeeIncomeByStatus(merchantPromotionFeeQueryModel);
        } else {
            if (Objects.equals(PromotionFeeQueryTypeEnum.MERCHANT_AND_MERCHANT_EMPLOYEE.getCode(), merchantPromotionFeeQueryModel.getType())) {
                merchantPromotionFeeQueryModel.setType(PromotionFeeQueryTypeEnum.MERCHANT.getCode());
            }
            return this.rebateRecordMapper.sumMerchantIncomeByStatus(merchantPromotionFeeQueryModel);
        }
    }
    
    
    @Slave
    @Override
    public Integer countByTime(MerchantPromotionRenewalQueryModel merchantPromotionRenewalQueryModel) {
        return this.rebateRecordMapper.countByTime(merchantPromotionRenewalQueryModel);
    }
    
    @Slave
    @Override
    public List<MerchantPromotionEmployeeDetailSpecificsVO> selectListPromotionDetail(MerchantPromotionEmployeeDetailSpecificsQueryModel queryModel) {
        List<RebateRecord> recordList = this.rebateRecordMapper.selectListPromotionDetail(queryModel);
        if (CollectionUtils.isEmpty(recordList)) {
            return Collections.emptyList();
        }
        
        return recordList.parallelStream().map(item -> {
            MerchantPromotionEmployeeDetailSpecificsVO specificsVO = new MerchantPromotionEmployeeDetailSpecificsVO();
            BeanUtils.copyProperties(item, specificsVO);
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getMemberCardId());
            specificsVO.setBatteryMemberCardName(Objects.nonNull(batteryMemberCard) ? batteryMemberCard.getName() : "");
            
            //手机号掩码
            if (StringUtils.isNotBlank(specificsVO.getPhone())) {
                specificsVO.setPhone(PhoneUtils.mobileEncrypt(specificsVO.getPhone()));
            }
            specificsVO.setUserName(item.getName());
            return specificsVO;
        }).sorted(Comparator.comparing(MerchantPromotionEmployeeDetailSpecificsVO::getRebateTime).reversed()).collect(Collectors.toList());
    }
    
}
