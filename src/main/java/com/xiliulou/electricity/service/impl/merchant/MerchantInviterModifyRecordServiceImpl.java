package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.merchant.MerchantInviterModifyRecordConstant;
import com.xiliulou.electricity.constant.merchant.MerchantJoinRecordConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantInviterModifyRecord;
import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;
import com.xiliulou.electricity.enums.UserInfoActivitySourceEnum;
import com.xiliulou.electricity.enums.merchant.MerchantInviterSourceEnum;
import com.xiliulou.electricity.mapper.merchant.MerchantInviterModifyRecordMapper;
import com.xiliulou.electricity.query.merchant.MerchantInviterModifyRecordQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantInviterModifyRecordRequest;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.MerchantInviterModifyRecordService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.merchant.MerchantInviterModifyRecordVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 邀请人修改记录
 * @date 2024/3/28 09:34:26
 */
@Service
public class MerchantInviterModifyRecordServiceImpl implements MerchantInviterModifyRecordService {
    
    @Resource
    private MerchantInviterModifyRecordMapper merchantInviterModifyRecordMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    private MerchantService merchantService;
    
    @Resource
    private MerchantJoinRecordService merchantJoinRecordService;
    
    @Override
    public Integer insertOne(MerchantInviterModifyRecord record) {
        return merchantInviterModifyRecordMapper.insertOne(record);
    }
    
    @Slave
    @Override
    public List<MerchantInviterModifyRecordVO> listByPage(MerchantInviterModifyRecordRequest request) {
        MerchantInviterModifyRecordQueryModel queryModel = new MerchantInviterModifyRecordQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        
        List<MerchantInviterModifyRecord> merchantInviterModifyRecordList = merchantInviterModifyRecordMapper.selectPage(queryModel);
        if (CollectionUtils.isEmpty(merchantInviterModifyRecordList)) {
            return Collections.emptyList();
        }
        
        return merchantInviterModifyRecordList.stream().map(item -> {
            MerchantInviterModifyRecordVO recordVO = new MerchantInviterModifyRecordVO();
            BeanUtils.copyProperties(item, recordVO);
            
            Integer inviterSource = MerchantInviterSourceEnum.MERCHANT_INVITER_SOURCE_USER_FOR_VO.getCode();
            if (Objects.equals(item.getOldInviterSource(), UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode())) {
                inviterSource = MerchantInviterSourceEnum.MERCHANT_INVITER_SOURCE_MERCHANT_FOR_VO.getCode();
                String inviterName = "";
                Merchant merchant1 = merchantService.queryByUid(item.getOldInviterUid());
                if (Objects.nonNull(merchant1)) {
                    // 邀请人是商户
                    inviterName = merchant1.getName();
                } else {
                    // 邀请人是场地员工
                    MerchantJoinRecord merchantJoinRecord = merchantJoinRecordService.queryRemoveSuccessRecord(item.getUid(), item.getOldInviterUid(), item.getTenantId());
                    if (Objects.nonNull(merchantJoinRecord) && Objects.equals(merchantJoinRecord.getInviterType(),
                            MerchantJoinRecordConstant.INVITER_TYPE_MERCHANT_PLACE_EMPLOYEE)) {
                        Merchant merchant2 = merchantService.queryByIdFromCache(merchantJoinRecord.getMerchantId());
                        if (Objects.nonNull(merchant2)) {
                            inviterName = merchant2.getName();
                        }
                    }
                }
                
                recordVO.setOldInviterName(inviterName);
            }
    
            // 流失用户操作人为系统
            if (Objects.equals(item.getOperator(), NumberConstant.ZERO_L)) {
                recordVO.setOperator(MerchantInviterModifyRecordConstant.LOST_USER_OPERATOR);
            } else {
                recordVO.setOperator(Optional.ofNullable(userService.queryByUidFromCache(item.getOperator())).orElse(new User()).getName());
            }
            
            recordVO.setOldInviterSource(inviterSource);
            recordVO.setOperateTime(item.getCreateTime());
            
            return recordVO;
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer countTotal(MerchantInviterModifyRecordRequest request) {
        MerchantInviterModifyRecordQueryModel queryModel = new MerchantInviterModifyRecordQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        
        return merchantInviterModifyRecordMapper.countTotal(queryModel);
    }
    
    @Override
    @Slave
    public boolean existsModifyRecordByUid(Long uid) {
        Integer count = merchantInviterModifyRecordMapper.existsModifyRecordByUid(uid);
        if (Objects.nonNull(count)) {
            return true;
        }
        
        return false;
    }
}
