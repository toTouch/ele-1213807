package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.EleDisableMemberCardRecordMapper;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.query.ElectricityMemberCardRecordQuery;
import com.xiliulou.electricity.service.*;
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
    public R reviewDisableMemberCard(String disableMemberCardNo, String errMsg, Integer status) {
        EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordMapper.selectOne(new LambdaQueryWrapper<EleDisableMemberCardRecord>().eq(EleDisableMemberCardRecord::getDisableMemberCardNo, disableMemberCardNo));

        if (Objects.isNull(eleDisableMemberCardRecord)) {
            log.error("REVIEW_DISABLE_MEMBER_CARD ERROR ,NOT FOUND DISABLE_MEMBER_CARD ORDER_NO:{}", disableMemberCardNo);
            return R.fail("未找到停卡订单!");
        }

        //用户
        UserInfo userInfo = userInfoService.selectUserByUid(eleDisableMemberCardRecord.getUid());

        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found user,uid:{} ", eleDisableMemberCardRecord.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", eleDisableMemberCardRecord.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        EleDisableMemberCardRecord updateEleDisableMemberCardRecord = new EleDisableMemberCardRecord();
        updateEleDisableMemberCardRecord.setDisableMemberCardNo(disableMemberCardNo);
        updateEleDisableMemberCardRecord.setStatus(status);
        updateEleDisableMemberCardRecord.setErrMsg(errMsg);
        updateEleDisableMemberCardRecord.setUpdateTime(System.currentTimeMillis());
        updateEleDisableMemberCardRecord.setCardDays((franchiseeUserInfo.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000L / 60 / 60 / 24);

        eleDisableMemberCardRecordMapper.update(updateEleDisableMemberCardRecord, new LambdaQueryWrapper<EleDisableMemberCardRecord>().eq(EleDisableMemberCardRecord::getDisableMemberCardNo, disableMemberCardNo));

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
}
