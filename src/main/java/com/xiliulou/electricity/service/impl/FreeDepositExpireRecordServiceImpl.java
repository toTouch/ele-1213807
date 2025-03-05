package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FreeDepositExpireRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.FreeDepositExpireRecordMapper;
import com.xiliulou.electricity.query.FreeDepositExpireRecordQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FreeDepositExpireRecordService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.FreeDepositExpireRecordVO;
import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author : renhang
 * @description FreeDepositExpireRecordServiceImpl
 * @date : 2025-02-25 14:03
 **/
@Service
public class FreeDepositExpireRecordServiceImpl implements FreeDepositExpireRecordService {

    @Resource
    private AssertPermissionService assertPermissionService;

    @Resource
    private FreeDepositExpireRecordMapper freeDepositExpireRecordMapper;

    @Resource
    private FranchiseeService franchiseeService;

    @Resource
    private UserService userService;

    @Override
    public List<FreeDepositExpireRecordVO> selectByPage(FreeDepositExpireRecordQuery query) {
        checkPermission();
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()) {
            return new ArrayList<>();
        }
        query.setFranchiseeIds(pair.getRight());

        List<FreeDepositExpireRecord> records = freeDepositExpireRecordMapper.selectByPage(query);
        if (CollUtil.isEmpty(records)) {
            return CollUtil.newArrayList();
        }

        return records.stream().map(record -> {
            FreeDepositExpireRecordVO freeDepositExpireRecordVO = BeanUtil.copyProperties(record, FreeDepositExpireRecordVO.class);
            Franchisee franchisee = franchiseeService.queryByIdFromCache(record.getFranchiseeId());
            freeDepositExpireRecordVO.setFranchiseeName(Optional.ofNullable(franchisee).map(Franchisee::getName).orElse(null));
            User user = userService.queryByUidFromCache(record.getOperateUid());
            freeDepositExpireRecordVO.setOperateName(Optional.ofNullable(user).map(User::getName).orElse(null));
            return freeDepositExpireRecordVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Integer queryCount(FreeDepositExpireRecordQuery query) {
        checkPermission();
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()) {
            return NumberConstant.ZERO;
        }
        query.setFranchiseeIds(pair.getRight());
        return freeDepositExpireRecordMapper.selectByCount(query);
    }

    @Override
    public void offLineDeal(List<Long> ids) {
        checkPermission();
        Integer count = freeDepositExpireRecordMapper.selectByIds(ids);
        if (!Objects.equals(count, ids.size())) {
            throw new BizException("402040", "不存在的免押到期记录,请检查");
        }
        freeDepositExpireRecordMapper.updateStatus(ids, SecurityUtils.getUid());
    }

    @Override
    public void editRemark(Long id, String remark) {
        checkPermission();
        Integer count = freeDepositExpireRecordMapper.selectByIds(CollUtil.newArrayList(id));
        if (!Objects.equals(count, NumberConstant.ONE)) {
            throw new BizException("402040", "不存在的免押到期记录,请检查");
        }
        freeDepositExpireRecordMapper.updateRemark(id, remark);
    }


    private void checkPermission() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            throw new BizException("ELECTRICITY.0066", "用户权限不足");
        }
    }
}
