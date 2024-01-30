package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.asset.AssetAllocateDetailBO;
import com.xiliulou.electricity.mapper.asset.AssetAllocateDetailMapper;
import com.xiliulou.electricity.query.asset.AssetAllocateDetailSaveQueryModel;
import com.xiliulou.electricity.request.asset.AssetAllocateDetailSaveRequest;
import com.xiliulou.electricity.service.asset.AssetAllocateDetailService;
import com.xiliulou.electricity.vo.asset.AssetAllocateDetailVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 资产调拨详情服务
 * @date 2023/11/29 18:01:27
 */
@Service
public class AssetAllocateDetailServiceImpl implements AssetAllocateDetailService {
    
    @Autowired
    private AssetAllocateDetailMapper assetAllocateDetailMapper;
    
    @Override
    public Integer batchInsert(List<AssetAllocateDetailSaveRequest> detailSaveRequestList) {
        List<AssetAllocateDetailSaveQueryModel> detailSaveQueryModelList = detailSaveRequestList.stream().map(item -> {
            AssetAllocateDetailSaveQueryModel assetAllocateDetailSaveQueryModel = new AssetAllocateDetailSaveQueryModel();
            BeanUtils.copyProperties(item, assetAllocateDetailSaveQueryModel);
            
            return assetAllocateDetailSaveQueryModel;
            
        }).collect(Collectors.toList());
        
        return assetAllocateDetailMapper.batchInsert(detailSaveQueryModelList);
    }
    
    @Slave
    @Override
    public List<AssetAllocateDetailVO> listByPage(String orderNo, Integer tenantId) {
        List<AssetAllocateDetailVO> rspList = null;
    
        List<AssetAllocateDetailBO> assetAllocateDetailBOList = assetAllocateDetailMapper.selectListByPage(orderNo, tenantId);
        if (CollectionUtils.isNotEmpty(assetAllocateDetailBOList)) {
            rspList = assetAllocateDetailBOList.stream().map(item -> {
                AssetAllocateDetailVO assetAllocateDetailVO = new AssetAllocateDetailVO();
                BeanUtils.copyProperties(item, assetAllocateDetailVO);
            
                return assetAllocateDetailVO;
            
            }).collect(Collectors.toList());
        }
    
        if (CollectionUtils.isEmpty(rspList)) {
            return Collections.emptyList();
        }
    
        return rspList;
    }
}
