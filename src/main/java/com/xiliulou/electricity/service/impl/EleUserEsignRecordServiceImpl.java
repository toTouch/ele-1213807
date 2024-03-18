package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.EleUserEsignRecordMapper;
import com.xiliulou.electricity.query.EleUserEsignRecordQuery;
import com.xiliulou.electricity.service.EleUserEsignRecordService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.EleUserEsignRecordVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author: Kenneth
 * @Date: 2023/7/10 11:30
 * @Description:
 */

@Service
@Slf4j
public class EleUserEsignRecordServiceImpl implements EleUserEsignRecordService {

    @Resource
    private EleUserEsignRecordMapper eleUserEsignRecordMapper;
    @Resource
    private UserDataScopeService userDataScopeService;

    @Override
    public List<EleUserEsignRecordVO> queryUserEsignRecords(EleUserEsignRecordQuery eleUserEsignRecordQuery) {
        Triple<List<Long>, List<Long>, Boolean> triple = assertPermission(SecurityUtils.getUserInfo());
        if (!triple.getRight()){
            return new ArrayList<>();
        }
        eleUserEsignRecordQuery.setFranchiseeIds(triple.getLeft());
        eleUserEsignRecordQuery.setStoreIds(triple.getMiddle());
        
        List<EleUserEsignRecordVO> eleUserEsignRecordVOList = eleUserEsignRecordMapper.selectByPage(eleUserEsignRecordQuery);
        log.info("get user esign record list: {}", eleUserEsignRecordVOList);
        return eleUserEsignRecordVOList;
    }

    @Override
    public Integer queryCount(EleUserEsignRecordQuery eleUserEsignRecordQuery) {
        Triple<List<Long>, List<Long>, Boolean> triple = assertPermission(SecurityUtils.getUserInfo());
        if (!triple.getRight()){
            return NumberConstant.ZERO;
        }
        eleUserEsignRecordQuery.setFranchiseeIds(triple.getLeft());
        eleUserEsignRecordQuery.setStoreIds(triple.getMiddle());
        return eleUserEsignRecordMapper.selectByPageCount(eleUserEsignRecordQuery);
    }
    
    private Triple<List<Long>, List<Long>, Boolean> assertPermission(TokenUser userInfo) {
        List<Long> franchiseeIds = null;
        if (Objects.equals(userInfo.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(userInfo.getUid());
            if (CollUtil.isEmpty(franchiseeIds)) {
                return Triple.of(null, null, false);
            }
        }
        List<Long> storeIds = null;
        if (Objects.equals(userInfo.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(userInfo.getUid());
            if (CollUtil.isEmpty(storeIds)) {
                return Triple.of(null, null, false);
            }
        }
        return Triple.of(franchiseeIds, storeIds, true);
    }
    
}
