package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.RebateRecord;
import com.xiliulou.electricity.mapper.merchant.RebateRecordMapper;
import com.xiliulou.electricity.query.merchant.MerchantPromotionFeeQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionRenewalQueryModel;
import com.xiliulou.electricity.request.merchant.RebateRecordRequest;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.RebateRecordService;
import com.xiliulou.electricity.vo.merchant.RebateRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
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
    private FranchiseeService franchiseeService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private MerchantService merchantService;
    
    @Autowired
    private MerchantPlaceService merchantPlaceService;
    
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
    public RebateRecord queryByOriginalOrderId(String originalOrderId) {
        return this.rebateRecordMapper.selectByOriginalOrderId(originalOrderId);
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
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            rebateRecord.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getMemberCardId());
            rebateRecord.setBatteryMemberCardName(Objects.nonNull(batteryMemberCard) ? batteryMemberCard.getName() : "");
            
            Merchant merchant = merchantService.queryFromCacheById(item.getMerchantId());
            rebateRecord.setMerchantName(Objects.nonNull(merchant) ? merchant.getName() : "");
            
            MerchantPlace merchantPlace = merchantPlaceService.queryFromCacheById(item.getPlaceId());
            rebateRecord.setPlaceName(Objects.nonNull(merchantPlace) ? merchantPlace.getName() : "");
            
            User placeUser = userService.queryByUidFromCache(item.getPlaceUid());
            rebateRecord.setPlaceUserName(Objects.nonNull(placeUser) ? placeUser.getName() : "");
            
            return rebateRecord;
            
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer countByPage(RebateRecordRequest query) {
        return this.rebateRecordMapper.selectByPageCount(query);
    }
    
    @Override
    public BigDecimal sumByStatus(MerchantPromotionFeeQueryModel merchantPromotionFeeQueryModel) {
        return this.rebateRecordMapper.sumByStatus(merchantPromotionFeeQueryModel);
    }
    
    @Override
    public Integer countByTime(MerchantPromotionRenewalQueryModel merchantPromotionRenewalQueryModel) {
        return this.rebateRecordMapper.countByTime(merchantPromotionRenewalQueryModel);
    }
    
}
