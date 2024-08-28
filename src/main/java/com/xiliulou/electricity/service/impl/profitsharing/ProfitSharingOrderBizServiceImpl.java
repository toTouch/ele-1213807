package com.xiliulou.electricity.service.impl.profitsharing;

import com.xiliulou.electricity.constant.DateFormatConstant;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingBusinessTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailUnfreezeStatusEnum;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderBizService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderDetailService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingTradeMixedOrderService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.pay.base.exception.ProfitSharingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/8/28 14:29
 * @desc
 */
@Service
@Slf4j
public class ProfitSharingOrderBizServiceImpl implements ProfitSharingOrderBizService {
    @Resource
    private TenantService tenantService;
    
    @Resource
    private ProfitSharingOrderService profitSharingOrderService;
    
    @Resource
    private ProfitSharingOrderDetailService profitSharingOrderDetailService;
    
    @Resource
    private ProfitSharingTradeMixedOrderService profitSharingTradeMixedOrderService;
    
    @Override
    public void doUnfreezeTask() {
        Integer startTenantId = 0;
        Integer size = 200;
        
        while (true) {
            List<Integer> tenantIds = tenantService.queryIdListByStartId(startTenantId, size);
            if (CollectionUtils.isEmpty(tenantIds)) {
                break;
            }
            
            dealWithTenantIds(tenantIds);
            
            startTenantId = tenantIds.get(tenantIds.size() - 1);
        }
    }
    
    private void dealWithTenantIds(List<Integer> tenantIds) {
        // 两个月前的第一天
        long startTime = DateUtils.getBeforeMonthFirstDayTimestamp(DateFormatConstant.LAST_MONTH);
        tenantIds.stream().forEach(tenantId -> {
            Integer offset = 0;
            Integer size = 200;
            
            while (true) {
                List<String> thirdOrderNoList = profitSharingTradeMixedOrderService.listThirdOrderNoByTenantId(tenantId, startTime, offset, size);
                if (ObjectUtils.isEmpty(thirdOrderNoList)) {
                    break;
                }
                
                // 根据微信支付订单号处理
                dealWith(thirdOrderNoList);
                
                offset += size;
            }
        });
    }
    
    private void dealWith(List<String> thirdOrderNoList) {
        // 查询出存在解冻的订单
        List<String> unfreezeByThirdOrderNoList = profitSharingOrderService.listUnfreezeByThirdOrderNo(thirdOrderNoList);
    
        thirdOrderNoList.stream().forEach(thirdOrderNo -> {
            // 校验微信支付订单号是否已经存在解冻订单
            if (unfreezeByThirdOrderNoList.contains(thirdOrderNo)) {
                return;
            }
            
            // 不存在解冻待处理的明细
            boolean existsNotUnfreezeByThirdOrderNo = profitSharingOrderDetailService.existsNotUnfreezeByThirdOrderNo(thirdOrderNo);
            if (!existsNotUnfreezeByThirdOrderNo) {
                return;
            }
            
            // 存在未处理完成的明细
            boolean existsNotCompleteByThirdOrderNo = profitSharingOrderDetailService.existsNotCompleteByThirdOrderNo(thirdOrderNo);
            if (existsNotCompleteByThirdOrderNo) {
                return;
            }
            
            // 存在分账明细失败的微信支付订单号
            boolean existsFail = profitSharingOrderDetailService.existsFailByThirdOrderNo(thirdOrderNo);
            if (existsFail) {
                // 默认状态解冻中
                Integer unfreezeStatus = ProfitSharingOrderDetailUnfreezeStatusEnum.IN_PROCESS.getCode();
                // 查询分账交易混合订单
                ProfitSharingTradeMixedOrder profitSharingTradeMixedOrder = profitSharingTradeMixedOrderService.queryByThirdOrderNo(thirdOrderNo);
                if (ObjectUtils.isEmpty(profitSharingTradeMixedOrder)) {
                    log.info("PROFIT SHARING UNFREEZE INFO!, profit sharing trade mixed order is not find, thirdOrderNo = {}",thirdOrderNo);
                    return;
                }
                // 调用解冻接口
                try {
                    profitSharingOrderService.doUnFreeze(profitSharingTradeMixedOrder);
                    // 修改分账明细失败的为解冻中
                } catch (ProfitSharingException e) {
                    log.error("PROFIT SHARING UNFREEZE ERROR!, thirdOrderNo = {}",thirdOrderNo, e);
                    
                    unfreezeStatus = ProfitSharingOrderDetailUnfreezeStatusEnum.LAPSED.getCode();
                }
    
                // 修改分账失败的明细解冻状态
                List<Integer> businessTypeList = new ArrayList<>();
                businessTypeList.add(ProfitSharingBusinessTypeEnum.BATTERY_PACKAGE.getCode());
                businessTypeList.add(ProfitSharingBusinessTypeEnum.INSURANCE.getCode());
                businessTypeList.add(ProfitSharingBusinessTypeEnum.BATTERY_SERVICE_FEE.getCode());
                businessTypeList.add(ProfitSharingBusinessTypeEnum.SYSTEM.getCode());
    
                profitSharingOrderDetailService.updateUnfreezeStatusByThirdOrderNo(thirdOrderNo, ProfitSharingOrderDetailStatusEnum.FAIL.getCode(), unfreezeStatus, businessTypeList, System.currentTimeMillis());
            }
    
            // 修改分账完成的明细的解冻状态为无需解冻
            List<Integer> businessTypeList = new ArrayList<>();
            businessTypeList.add(ProfitSharingBusinessTypeEnum.BATTERY_PACKAGE.getCode());
            businessTypeList.add(ProfitSharingBusinessTypeEnum.INSURANCE.getCode());
            businessTypeList.add(ProfitSharingBusinessTypeEnum.BATTERY_SERVICE_FEE.getCode());
            businessTypeList.add(ProfitSharingBusinessTypeEnum.SYSTEM.getCode());
    
            profitSharingOrderDetailService.updateUnfreezeStatusByThirdOrderNo(thirdOrderNo, ProfitSharingOrderDetailStatusEnum.COMPLETE.getCode(), ProfitSharingOrderDetailUnfreezeStatusEnum.DISPENSE_WITH.getCode(), businessTypeList, System.currentTimeMillis());
            
        });
    }
}
