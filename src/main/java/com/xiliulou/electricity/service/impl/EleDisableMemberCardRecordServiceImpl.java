package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.EleDisableMemberCardRecordMapper;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.query.ElectricityMemberCardRecordQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

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
    FranchiseeUserInfoService franchiseeUserInfoService;

    @Autowired
    UserInfoService userInfoService;

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
        UserInfo userInfo = userInfoService.selectUserByUid(eleDisableMemberCardRecord.getUid());

        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found user,uid={} ", eleDisableMemberCardRecord.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId={}", eleDisableMemberCardRecord.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime()) || franchiseeUserInfo.getMemberCardExpireTime() < System.currentTimeMillis()) {
            log.error("REVIEW_DISABLE_MEMBER_CARD ERROR member card Expire! userId={}", eleDisableMemberCardRecord.getUid());
            return R.fail("100246", "套餐已过期，无法进行停卡审核");
        }

        EleDisableMemberCardRecord updateEleDisableMemberCardRecord = new EleDisableMemberCardRecord();
        updateEleDisableMemberCardRecord.setId(eleDisableMemberCardRecord.getId());
        updateEleDisableMemberCardRecord.setDisableMemberCardNo(disableMemberCardNo);
        updateEleDisableMemberCardRecord.setStatus(status);
        updateEleDisableMemberCardRecord.setErrMsg(errMsg);
        updateEleDisableMemberCardRecord.setUpdateTime(System.currentTimeMillis());
        updateEleDisableMemberCardRecord.setCardDays((franchiseeUserInfo.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000L / 60 / 60 / 24);
        if (Objects.equals(eleDisableMemberCardRecord.getDisableCardTimeType(), EleDisableMemberCardRecord.DISABLE_CARD_LIMIT_TIME) && Objects.equals(status, EleDisableMemberCardRecord.MEMBER_CARD_DISABLE)) {
            updateEleDisableMemberCardRecord.setDisableDeadline(System.currentTimeMillis() + eleDisableMemberCardRecord.getChooseDays() * (24 * 60 * 60 * 1000L));
        }

        eleDisableMemberCardRecordMapper.updateById(updateEleDisableMemberCardRecord);

        FranchiseeUserInfo updateFranchiseeUserInfo = new FranchiseeUserInfo();
        updateFranchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
        updateFranchiseeUserInfo.setMemberCardDisableStatus(status);
        updateFranchiseeUserInfo.setId(franchiseeUserInfo.getId());
        if (Objects.equals(status, FranchiseeUserInfo.MEMBER_CARD_DISABLE_REVIEW_REFUSE)) {
            updateFranchiseeUserInfo.setMemberCardDisableStatus(FranchiseeUserInfo.MEMBER_CARD_NOT_DISABLE);
        }
        if (Objects.equals(status, FranchiseeUserInfo.MEMBER_CARD_DISABLE)) {
            updateFranchiseeUserInfo.setDisableMemberCardTime(System.currentTimeMillis());
        }

        franchiseeUserInfoService.update(updateFranchiseeUserInfo);
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
