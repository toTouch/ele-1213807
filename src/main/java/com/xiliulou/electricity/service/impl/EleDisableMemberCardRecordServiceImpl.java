package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleDisableMemberCardRecord;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.EleDisableMemberCardRecordMapper;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.query.ElectricityMemberCardRecordQuery;
import com.xiliulou.electricity.service.EleDisableMemberCardRecordService;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.HRP
 * @create: 2022-05-21 10:54
 **/
@Service
@Slf4j
public class EleDisableMemberCardRecordServiceImpl extends ServiceImpl<ElectricityMemberCardOrderMapper, ElectricityMemberCardOrder> implements EleDisableMemberCardRecordService {

    @Resource
    EleDisableMemberCardRecordMapper eleDisableMemberCardRecordMapper;

    @Autowired
    UserInfoService userInfoService;
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;

    @Override
    public int save(EleDisableMemberCardRecord eleDisableMemberCardRecord) {
        return eleDisableMemberCardRecordMapper.insert(eleDisableMemberCardRecord);
    }

    @Override
    public R list(ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery) {
        return R.ok(eleDisableMemberCardRecordMapper.queryList(electricityMemberCardRecordQuery));
    }

    @Override
    public EleDisableMemberCardRecord queryCreateTimeMaxEleDisableMemberCardRecord(Long uid, Integer tenantId) {
        return eleDisableMemberCardRecordMapper.queryCreateTimeMaxEleDisableMemberCardRecord(uid, tenantId);
    }

    @Override
    public R reviewDisableMemberCard(String disableMemberCardNo, String errMsg, Integer status) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordMapper.selectOne(new LambdaQueryWrapper<EleDisableMemberCardRecord>().eq(EleDisableMemberCardRecord::getDisableMemberCardNo, disableMemberCardNo).eq(EleDisableMemberCardRecord::getTenantId, tenantId));

        if (Objects.isNull(eleDisableMemberCardRecord)) {
            log.error("REVIEW_DISABLE_MEMBER_CARD ERROR ,NOT FOUND DISABLE_MEMBER_CARD ORDER_NO={}", disableMemberCardNo);
            return R.fail("未找到停卡订单!");
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(eleDisableMemberCardRecord.getUid());

        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found user,uid={} ", eleDisableMemberCardRecord.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

//        //是否缴纳押金，是否绑定电池
//        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
//
//        //未找到用户
//        if (Objects.isNull(franchiseeUserInfo)) {
//            log.error("payDeposit  ERROR! not found user! userId={}", eleDisableMemberCardRecord.getUid());
//            return R.fail("ELECTRICITY.0001", "未找到用户");
//        }
        //判断用户套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("REVIEW_DISABLE_MEMBER_CARD ERROR! user haven't memberCard uid={}", userInfo.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        //未找到用户
        if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            log.error("REVIEW_DISABLE_MEMBER_CARD ERROR member card Expire! userId={}", eleDisableMemberCardRecord.getUid());
            return R.fail("100246", "套餐已过期，无法进行停卡审核");
        }

        EleDisableMemberCardRecord updateEleDisableMemberCardRecord = new EleDisableMemberCardRecord();
        updateEleDisableMemberCardRecord.setId(eleDisableMemberCardRecord.getId());
        updateEleDisableMemberCardRecord.setDisableMemberCardNo(disableMemberCardNo);
        updateEleDisableMemberCardRecord.setStatus(status);
        updateEleDisableMemberCardRecord.setErrMsg(errMsg);
        updateEleDisableMemberCardRecord.setUpdateTime(System.currentTimeMillis());
        updateEleDisableMemberCardRecord.setCardDays((userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000L / 60 / 60 / 24);
        if (Objects.equals(eleDisableMemberCardRecord.getDisableCardTimeType(), EleDisableMemberCardRecord.DISABLE_CARD_LIMIT_TIME) && Objects.equals(status, EleDisableMemberCardRecord.MEMBER_CARD_DISABLE)) {
            updateEleDisableMemberCardRecord.setDisableDeadline(System.currentTimeMillis() + eleDisableMemberCardRecord.getChooseDays() * (24 * 60 * 60 * 1000L));
        }

        eleDisableMemberCardRecordMapper.updateById(updateEleDisableMemberCardRecord);


        UserBatteryMemberCard updateUserBatteryMemberCard=new UserBatteryMemberCard();
        updateUserBatteryMemberCard.setUid(userBatteryMemberCard.getUid());
        updateUserBatteryMemberCard.setMemberCardStatus(status);
        updateUserBatteryMemberCard.setUpdateTime(System.currentTimeMillis());
        if (Objects.equals(status, UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW_REFUSE)) {
            updateUserBatteryMemberCard.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
        }
        if (Objects.equals(status, UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            updateUserBatteryMemberCard.setDisableMemberCardTime(System.currentTimeMillis());
        }

//        FranchiseeUserInfo updateFranchiseeUserInfo = new FranchiseeUserInfo();
//        updateFranchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
//        updateFranchiseeUserInfo.setMemberCardDisableStatus(status);
//        updateFranchiseeUserInfo.setId(franchiseeUserInfo.getId());
//        if (Objects.equals(status, FranchiseeUserInfo.MEMBER_CARD_DISABLE_REVIEW_REFUSE)) {
//            updateFranchiseeUserInfo.setMemberCardDisableStatus(FranchiseeUserInfo.MEMBER_CARD_NOT_DISABLE);
//        }
//        if (Objects.equals(status, FranchiseeUserInfo.MEMBER_CARD_DISABLE)) {
//            updateFranchiseeUserInfo.setDisableMemberCardTime(System.currentTimeMillis());
//        }

        userBatteryMemberCardService.updateByUid(updateUserBatteryMemberCard);
        return R.ok();
    }

    @Override
    public R queryCount(ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery) {
        return R.ok(eleDisableMemberCardRecordMapper.queryCount(electricityMemberCardRecordQuery));
    }

    @Override
    public List<EleDisableMemberCardRecord> queryDisableCardExpireRecord(Integer offset, Integer size, Long nowTime) {
        return eleDisableMemberCardRecordMapper.queryDisableCardExpireRecord(offset, size, nowTime);
    }

    @Override
    public int updateBYId(EleDisableMemberCardRecord eleDisableMemberCardRecord) {
        return eleDisableMemberCardRecordMapper.updateById(eleDisableMemberCardRecord);
    }
}
