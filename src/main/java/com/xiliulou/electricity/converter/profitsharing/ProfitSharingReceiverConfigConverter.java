/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/26
 */

package com.xiliulou.electricity.converter.profitsharing;
import java.math.BigDecimal;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingReceiverConfigModel;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingReceiverConfigOptRequest;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingReceiverConfigQryRequest;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingReceiverConfigDetailsVO;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingReceiverConfigVO;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/26 10:29
 */
public class ProfitSharingReceiverConfigConverter {
    
    
    public static ProfitSharingReceiverConfig optReqToEntity(ProfitSharingReceiverConfigOptRequest request) {
        
        ProfitSharingReceiverConfig profitSharingReceiverConfig = new ProfitSharingReceiverConfig();
        profitSharingReceiverConfig.setId(request.getId());
        profitSharingReceiverConfig.setTenantId(request.getTenantId());
        profitSharingReceiverConfig.setProfitSharingConfigId(request.getProfitSharingConfigId());
        profitSharingReceiverConfig.setAccount(request.getAccount());
        profitSharingReceiverConfig.setReceiverType(request.getReceiverType());
        profitSharingReceiverConfig.setReceiverName(request.getReceiverName());
        profitSharingReceiverConfig.setRelationType(request.getRelationType());
        profitSharingReceiverConfig.setScale(request.getScale());
        profitSharingReceiverConfig.setRemark(request.getRemark());
        return profitSharingReceiverConfig;
        
    }
    
    public static ProfitSharingReceiverConfigDetailsVO qryEntityToDetailsVO(ProfitSharingReceiverConfig receiver) {
        if (Objects.isNull(receiver)) {
            return null;
        }
        ProfitSharingReceiverConfigDetailsVO profitSharingReceiverConfigDetailsVO = new ProfitSharingReceiverConfigDetailsVO();
        profitSharingReceiverConfigDetailsVO.setId(receiver.getId());
        profitSharingReceiverConfigDetailsVO.setFranchiseeId(receiver.getFranchiseeId());
        profitSharingReceiverConfigDetailsVO.setProfitSharingConfigId(receiver.getProfitSharingConfigId());
        profitSharingReceiverConfigDetailsVO.setAccount(receiver.getAccount());
        profitSharingReceiverConfigDetailsVO.setReceiverType(receiver.getReceiverType());
        profitSharingReceiverConfigDetailsVO.setReceiverName(receiver.getReceiverName());
        profitSharingReceiverConfigDetailsVO.setRelationType(receiver.getRelationType());
        profitSharingReceiverConfigDetailsVO.setReceiverStatus(receiver.getReceiverStatus());
        profitSharingReceiverConfigDetailsVO.setScale(receiver.getScale());
        profitSharingReceiverConfigDetailsVO.setRemark(receiver.getRemark());
        return profitSharingReceiverConfigDetailsVO;
        
        
    }
    
    public static ProfitSharingReceiverConfigModel qryRequestToModel(ProfitSharingReceiverConfigQryRequest request) {
        ProfitSharingReceiverConfigModel profitSharingReceiverConfigModel = new ProfitSharingReceiverConfigModel();
        profitSharingReceiverConfigModel.setTenantId(request.getTenantId());
        profitSharingReceiverConfigModel.setProfitSharingConfigId(request.getProfitSharingConfigId());
        profitSharingReceiverConfigModel.setSize(request.getSize());
        profitSharingReceiverConfigModel.setOffset(request.getOffset());
        return profitSharingReceiverConfigModel;
    }
    
    public static List<ProfitSharingReceiverConfigVO> qryEntityToVos(List<ProfitSharingReceiverConfig> configs) {
        return Optional.ofNullable(configs).orElse(Collections.emptyList()).stream().map(v->qryEntityToVo(v)).collect(Collectors.toList());
    }
    
    private static ProfitSharingReceiverConfigVO qryEntityToVo(ProfitSharingReceiverConfig v) {
        ProfitSharingReceiverConfigVO profitSharingReceiverConfigVO = new ProfitSharingReceiverConfigVO();
        profitSharingReceiverConfigVO.setId(v.getId());
        profitSharingReceiverConfigVO.setFranchiseeId(v.getFranchiseeId());
        profitSharingReceiverConfigVO.setProfitSharingConfigId(v.getProfitSharingConfigId());
        profitSharingReceiverConfigVO.setAccount(v.getAccount());
        profitSharingReceiverConfigVO.setReceiverType(v.getReceiverType());
        profitSharingReceiverConfigVO.setReceiverName(v.getReceiverName());
        profitSharingReceiverConfigVO.setRelationType(v.getRelationType());
        profitSharingReceiverConfigVO.setReceiverStatus(v.getReceiverStatus());
        profitSharingReceiverConfigVO.setScale(v.getScale());
        profitSharingReceiverConfigVO.setRemark(v.getRemark());
        profitSharingReceiverConfigVO.setCreateTime(v.getCreateTime());
        return profitSharingReceiverConfigVO;
    }
}
