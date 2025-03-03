package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantJoinRecordConstant;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantAttr;
import com.xiliulou.electricity.entity.merchant.MerchantLevel;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.model.MerchantModify;
import com.xiliulou.electricity.mq.model.MerchantUpgrade;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.service.merchant.MerchantLevelService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.vo.merchant.MerchantLevelVO;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.MERCHANT_UPGRADE_TOPIC, consumerGroup = MqConsumerConstant.MERCHANT_UPGRADE_CONSUMER_GROUP, consumeThreadMax = 3)
public class MerchantUpgradeConsumer implements RocketMQListener<String> {
    
    @Autowired
    private RocketMqService rocketMqService;
    
    @Autowired
    private MerchantService merchantService;
    
    @Autowired
    private MerchantAttrService merchantAttrService;
    
    @Autowired
    private MerchantLevelService merchantLevelService;
    
    @Autowired
    private UserInfoExtraService userInfoExtraService;
    
    @Autowired
    private MerchantJoinRecordService merchantJoinRecordService;
    
    @Autowired
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Override
    public void onMessage(String message) {
        MDC.put(CommonConstant.TRACE_ID, IdUtil.fastSimpleUUID());
        
        log.info("MERCHANT UPGRADE CONSUMER INFO!received msg={}", message);
        MerchantUpgrade merchantUpgrade = null;
        
        try {
            merchantUpgrade = JsonUtil.fromJson(message, MerchantUpgrade.class);
            
            if (Objects.isNull(merchantUpgrade) || Objects.isNull(merchantUpgrade.getUid())) {
                return;
            }
            
            UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(merchantUpgrade.getUid());
            if (Objects.isNull(userInfoExtra) || Objects.isNull(userInfoExtra.getMerchantId())) {
                return;
            }
            
            Merchant merchant = merchantService.queryByIdFromCache(userInfoExtra.getMerchantId());
            if (Objects.isNull(merchant)) {
                return;
            }
            
            if (Objects.equals(MerchantConstant.DISABLE, merchant.getStatus())) {
                log.warn("MERCHANT UPGRADE CONSUMER WARN!merchant is disable,merchantId={}", userInfoExtra.getMerchantId());
                return;
            }
            
            if (!Objects.equals(MerchantConstant.open, merchant.getAutoUpGrade())) {
                log.info("MERCHANT UPGRADE CONSUMER INFO!merchant is not auto upgrade,merchantId={}", userInfoExtra.getMerchantId());
                return;
            }
            
            MerchantAttr merchantAttr = merchantAttrService.queryByFranchiseeIdFromCache(merchant.getFranchiseeId());
            if (Objects.isNull(merchantAttr)) {
                log.warn("MERCHANT UPGRADE CONSUMER WARN!merchantAttr is null,merchantId={}", userInfoExtra.getMerchantId());
                return;
            }
            
            List<MerchantLevelVO> merchantLevelList = merchantLevelService.list(merchantAttr.getTenantId(), merchant.getFranchiseeId());
            if (CollectionUtils.isEmpty(merchantLevelList)) {
                log.warn("MERCHANT UPGRADE CONSUMER WARN!merchantLevelList is null,merchantId={}", userInfoExtra.getMerchantId());
                return;
            }
            
            /**
             * 商户升级条件：拉新人数、续费人数（退租不算），包括已删除用户
             */
            
            //升级条件：拉新人数
            if (Objects.equals(MerchantConstant.UPGRADE_CONDITION_INVITATION, merchantAttr.getUpgradeCondition())) {
                //拉新人数
                Integer invitationNumber = merchantJoinRecordService.countByMerchantIdAndStatus(merchant.getId(), MerchantJoinRecordConstant.STATUS_SUCCESS);
                log.info("MERCHANT UPGRADE INVITATION INFO!invitationNumber={},merchantId={}", invitationNumber, userInfoExtra.getMerchantId());
                if (Objects.isNull(invitationNumber)) {
                    return;
                }
                
                MerchantLevelVO merchantLevel = null;
                
                for (MerchantLevelVO merchantLevelVO : merchantLevelList) {
                    if (Objects.nonNull(merchantLevelVO.getInvitationUserCount()) && merchantLevelVO.getInvitationUserCount() > 0 && invitationNumber >= merchantLevelVO
                            .getInvitationUserCount()) {
                        merchantLevel = merchantLevelVO;
                        break;
                    }
                }
                
                modifyMerchantLevel(userInfoExtra, merchant, merchantLevel);
            }
            
            //升级条件：续费人数
            if (Objects.equals(MerchantConstant.UPGRADE_CONDITION_RENEWAL, merchantAttr.getUpgradeCondition())) {
                //续费人数
                Integer renewalNumber = userBatteryMemberCardService.queryRenewalNumberByMerchantId(merchant.getId(), merchantAttr.getTenantId());
                log.info("MERCHANT UPGRADE RENEWAL INFO!renewalNumber={},merchantId={}", renewalNumber, userInfoExtra.getMerchantId());
                if (Objects.isNull(renewalNumber)) {
                    return;
                }
                
                MerchantLevelVO merchantLevel = null;
                
                for (MerchantLevelVO merchantLevelVO : merchantLevelList) {
                    if (Objects.nonNull(merchantLevelVO.getRenewalUserCount()) && merchantLevelVO.getRenewalUserCount() > 0 && renewalNumber >= merchantLevelVO
                            .getRenewalUserCount()) {
                        merchantLevel = merchantLevelVO;
                        break;
                    }
                }
                
                modifyMerchantLevel(userInfoExtra, merchant, merchantLevel);
            }
            
            //升级条件：全部
            if (Objects.equals(MerchantConstant.UPGRADE_CONDITION_ALL, merchantAttr.getUpgradeCondition())) {
                //拉新人数
                Integer invitationNumber = merchantJoinRecordService.countByMerchantIdAndStatus(merchant.getId(), MerchantJoinRecordConstant.STATUS_SUCCESS);
                log.info("MERCHANT UPGRADE INVITATION INFO!invitationNumber={},merchantId={}", invitationNumber, userInfoExtra.getMerchantId());
                if (Objects.isNull(invitationNumber)) {
                    return;
                }
                
                //续费人数
                Integer renewalNumber = userBatteryMemberCardService.queryRenewalNumberByMerchantId(merchant.getId(), merchantAttr.getTenantId());
                log.info("MERCHANT UPGRADE RENEWAL INFO!renewalNumber={},merchantId={}", renewalNumber, userInfoExtra.getMerchantId());
                if (Objects.isNull(renewalNumber)) {
                    return;
                }
                
                MerchantLevelVO merchantLevel = null;
                
                for (MerchantLevelVO merchantLevelVO : merchantLevelList) {
                    if (Objects.nonNull(merchantLevelVO.getInvitationUserCount()) && merchantLevelVO.getInvitationUserCount() > 0 && invitationNumber >= merchantLevelVO
                            .getInvitationUserCount() && Objects.nonNull(merchantLevelVO.getRenewalUserCount()) && merchantLevelVO.getRenewalUserCount() > 0
                            && renewalNumber >= merchantLevelVO.getRenewalUserCount()) {
                        merchantLevel = merchantLevelVO;
                        break;
                    }
                }
                
                modifyMerchantLevel(userInfoExtra, merchant, merchantLevel);
            }
            
        } catch (Exception e) {
            log.error("MERCHANT UPGRADE CONSUMER ERROR!msg={}", message, e);
        }
    }
    
    private void modifyMerchantLevel(UserInfoExtra userInfoExtra, Merchant merchant, MerchantLevelVO merchantLevel) {
        if (Objects.isNull(merchantLevel)) {
            log.warn("MERCHANT UPGRADE CONSUMER WARN!merchantLevel is null,merchantId={}", userInfoExtra.getMerchantId());
            return;
        }
        
        if (Objects.equals(merchant.getMerchantGradeId(), merchantLevel.getId())) {
            return;
        }
        
        //商户将要升到的等级
        int nextLevel = Integer.parseInt(merchantLevel.getLevel());
        //商户当前的等级
        MerchantLevel merchantCurrentLevel = merchantLevelService.queryById(merchant.getMerchantGradeId());
        //防止商户自动降级
        if (Objects.nonNull(merchantCurrentLevel) && StrUtil.isNotBlank(merchantCurrentLevel.getLevel()) && nextLevel >= Integer.parseInt(merchantCurrentLevel.getLevel())) {
            return;
        }
        
        //更新商户等级
        Merchant merchantUpdate = new Merchant();
        merchantUpdate.setId(merchant.getId());
        merchantUpdate.setMerchantGradeId(merchantLevel.getId());
        merchantUpdate.setUpdateTime(System.currentTimeMillis());
        merchantService.updateById(merchantUpdate);
        
        //发送商户升级MQ  重新计算差额
        MerchantModify merchantModify = new MerchantModify();
        merchantModify.setMerchantId(merchant.getId());
        merchantModify.setUid(userInfoExtra.getUid());
        merchantModify.setMessageId(IdUtil.fastSimpleUUID());
        rocketMqService.sendAsyncMsg(MqProducerConstant.MERCHANT_MODIFY_TOPIC, JsonUtil.toJson(merchantModify));
    }
}
