package com.xiliulou.electricity.service.impl.profitsharing;

import com.google.common.collect.Maps;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.profitsharing.ProfitSharingOrderTypeUnfreezeBO;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.profitsharing.ProfitSharingOrderDetailConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingOrderDetailMapper;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingOrderMapper;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingOrderDetailQueryModel;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingOrderDetailPageRequest;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderDetailService;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingOrderDetailVO;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingReceiverConfigDetailsVO;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分账订单明细表(ProfitSharingOrderDetail)表服务实现类
 *
 * @author maxiaodong
 * @since 2024-08-22 17:00:36
 */
@Service
public class ProfitSharingOrderDetailServiceImpl implements ProfitSharingOrderDetailService {
    
    @Resource
    private ProfitSharingOrderDetailMapper profitSharingOrderDetailMapper;
    
    @Resource
    private ProfitSharingOrderMapper profitSharingOrderMapper;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Override
    @Slave
    public Integer countTotal(ProfitSharingOrderDetailPageRequest profitSharingOrderPageRequest) {
        ProfitSharingOrderDetailQueryModel queryModel = new ProfitSharingOrderDetailQueryModel();
        BeanUtils.copyProperties(profitSharingOrderPageRequest, queryModel);
        
        return profitSharingOrderDetailMapper.countTotal(queryModel);
    }
    
    @Override
    @Slave
    public List<ProfitSharingOrderDetailVO> listByPage(ProfitSharingOrderDetailPageRequest profitSharingOrderPageRequest) {
        List<ProfitSharingOrderDetailVO> resList = new ArrayList<>();
        ProfitSharingOrderDetailQueryModel queryModel = new ProfitSharingOrderDetailQueryModel();
        BeanUtils.copyProperties(profitSharingOrderPageRequest, queryModel);
        
        List<ProfitSharingOrderDetail> profitSharingOrderDetailList = this.profitSharingOrderDetailMapper.selectListByPage(queryModel);
        if (ObjectUtils.isEmpty(profitSharingOrderDetailList)) {
            return resList;
        }
        
        // 查询分账订单主表的信息
        List<Long> profitSharingOrderIdList = profitSharingOrderDetailList.parallelStream().map(ProfitSharingOrderDetail::getProfitSharingOrderId).collect(Collectors.toList());
        List<ProfitSharingOrder> profitSharingOrderList = profitSharingOrderMapper.selectListByIds(profitSharingOrderIdList);
        Map<Long, ProfitSharingOrder> profitSharingOrderMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(profitSharingOrderList)) {
            profitSharingOrderMap = profitSharingOrderList.stream().collect(Collectors.toMap(ProfitSharingOrder::getId, Function.identity(), (v1, v2) -> v1));
        }
        
        for (ProfitSharingOrderDetail profitSharingOrderDetail : profitSharingOrderDetailList) {
            ProfitSharingOrderDetailVO profitSharingOrderDetailVO = new ProfitSharingOrderDetailVO();
            BeanUtils.copyProperties(profitSharingOrderDetail, profitSharingOrderDetailVO);
            
            // 订单号，订单金额
            ProfitSharingOrder profitSharingOrder = profitSharingOrderMap.get(profitSharingOrderDetail.getProfitSharingOrderId());
            if (Objects.nonNull(profitSharingOrder)) {
                profitSharingOrderDetailVO.setOrderNo(profitSharingOrder.getOrderNo());
                profitSharingOrderDetailVO.setAmount(profitSharingOrder.getAmount());
                profitSharingOrderDetailVO.setType(profitSharingOrder.getType());
                profitSharingOrderDetailVO.setBusinessOrderNo(profitSharingOrder.getBusinessOrderNo());
            }
            
            // 分账接收方
            if (Objects.equals(profitSharingOrderDetail.getFranchiseeId(), NumberConstant.ZERO_L)) {
                profitSharingOrderDetailVO.setProfitSharingOutAccount(ProfitSharingOrderDetailConstant.PROFIT_SHARING_OUT_ACCOUNT_DEFAULT);
            } else {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(profitSharingOrderDetail.getFranchiseeId());
                profitSharingOrderDetailVO.setProfitSharingOutAccount(Objects.nonNull(franchisee) ? franchisee.getName() : null);
            }
            
            resList.add(profitSharingOrderDetailVO);
        }
        
        return resList;
    }
    
    @Override
    public int batchInsert(List<ProfitSharingOrderDetail> profitSharingOrderDetailList) {
        return profitSharingOrderDetailMapper.batchInsert(profitSharingOrderDetailList);
    }
    
    @Override
    public int insert(ProfitSharingOrderDetail profitSharingOrderDetail) {
        return profitSharingOrderDetailMapper.insert(profitSharingOrderDetail);
    }
    
    @Override
    @Slave
    public boolean existsNotUnfreezeByThirdOrderNo(String thirdOrderNo) {
        Integer count = profitSharingOrderDetailMapper.existsNotUnfreezeByThirdOrderNo(thirdOrderNo);
        if (Objects.nonNull(count)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    @Slave
    public boolean existsNotCompleteByThirdOrderNo(String thirdOrderNo) {
        Integer count = profitSharingOrderDetailMapper.existsNotCompleteByThirdOrderNo(thirdOrderNo);
        if (Objects.nonNull(count)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    @Slave
    public boolean existsFailByThirdOrderNo(String thirdOrderNo) {
        Integer failCount = profitSharingOrderDetailMapper.existsFailByThirdOrderNo(thirdOrderNo);
        if (Objects.nonNull(failCount)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public int updateUnfreezeStatusByThirdOrderNo(String thirdOrderNo, Integer status, Integer unfreezeStatus, List<Integer> businessTypeList, long updateTime) {
        return profitSharingOrderDetailMapper.updateUnfreezeStatusByThirdOrderNo(thirdOrderNo, status, unfreezeStatus, businessTypeList, updateTime);
    }
    
    @Override
    @Slave
    public List<ProfitSharingOrderTypeUnfreezeBO> listOrderTypeUnfreeze(Integer tenantId, Long startId, Integer size) {
        return profitSharingOrderDetailMapper.selectListOrderTypeUnfreeze(tenantId, startId, size);
    }
    
    @Override
    public int updateUnfreezeOrderById(ProfitSharingOrderDetail profitSharingOrderDetailUpdate) {
        return profitSharingOrderDetailMapper.updateUnfreezeOrderById(profitSharingOrderDetailUpdate);
    }
    
    @Override
    @Slave
    public List<ProfitSharingOrderDetail> listFailByThirdOrderNo(String thirdTradeOrderNo) {
        return profitSharingOrderDetailMapper.selectListFailByThirdOrderNo(thirdTradeOrderNo);
    }
    
    
    @Slave
    @Override
    public List<ProfitSharingOrderDetail> queryListByProfitSharingOrderIds(Integer tenantId, List<Long> ids) {
        return profitSharingOrderDetailMapper.selectListByProfitSharingOrderIds(tenantId, ids);
    }
    
    
    
    
    
}
