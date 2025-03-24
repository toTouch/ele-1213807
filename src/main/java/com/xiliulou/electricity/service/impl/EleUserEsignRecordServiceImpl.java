package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.EleUserEsignRecord;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.mapper.EleUserEsignRecordMapper;
import com.xiliulou.electricity.query.EleUserEsignRecordQuery;
import com.xiliulou.electricity.service.EleUserEsignRecordService;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.EleUserEsignRecordVO;
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
    private AssertPermissionService assertPermissionService;
    
    @Override
    public List<EleUserEsignRecordVO> queryUserEsignRecords(EleUserEsignRecordQuery eleUserEsignRecordQuery) {
        Triple<List<Long>, List<Long>, Boolean> triple = assertPermissionService.assertPermissionByTriple(SecurityUtils.getUserInfo());
        if (!triple.getRight()) {
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
        Triple<List<Long>, List<Long>, Boolean> triple = assertPermissionService.assertPermissionByTriple(SecurityUtils.getUserInfo());
        if (!triple.getRight()) {
            return NumberConstant.ZERO;
        }
        eleUserEsignRecordQuery.setFranchiseeIds(triple.getLeft());
        eleUserEsignRecordQuery.setStoreIds(triple.getMiddle());
        return eleUserEsignRecordMapper.selectByPageCount(eleUserEsignRecordQuery);
    }

    @Override
    public EleUserEsignRecord queryUserEsignRecordFromDB(Long uid, Long tenantId) {
        
        if (Objects.isNull(uid)) {
            return null;
        }
        
        return eleUserEsignRecordMapper.selectLatestEsignRecordByUser(uid, tenantId);
    }
    
    @Override
    public EleUserEsignRecord queryEsignFinishedRecordByUser(Long uid, Long tenantId) {
        return eleUserEsignRecordMapper.selectEsignFinishedRecordByUser(uid, tenantId);
    }
    
    @Override
    public Integer cancelEsignFlow(Long id) {
        return eleUserEsignRecordMapper.delById(id);
    }
    
}
