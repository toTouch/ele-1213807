package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeRecord;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceFeeRecordMapper;
import com.xiliulou.electricity.query.merchant.MerchantPlaceFeeRecordQueryModel;
import com.xiliulou.electricity.request.asset.ElectricityCabinetBatchOutWarehouseRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceFeeRecordPageRequest;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeRecordService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceFeeRecordVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2024/2/17 22:35
 * @desc
 */
@Service("merchantPlaceMapService")
@Slf4j
public class MerchantPlaceFeeRecordServiceImpl implements MerchantPlaceFeeRecordService {
    protected XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("CABINET_PLACE_RECORD_HANDLE_THREAD_POOL", 2,
            "cabinet_place_record_handle_thread_pool");
    
    @Resource
    private MerchantPlaceFeeRecordMapper merchantPlaceFeeRecordMapper;
    
   @Resource
   private UserService userService;
    
    @Slave
    @Override
    public Integer countTotal(MerchantPlaceFeeRecordPageRequest merchantPlaceFeePageRequest) {
        MerchantPlaceFeeRecordQueryModel merchantPlaceFeeQueryModel = new MerchantPlaceFeeRecordQueryModel();
        BeanUtils.copyProperties(merchantPlaceFeePageRequest, merchantPlaceFeeQueryModel);
        
        return merchantPlaceFeeRecordMapper.countTotal(merchantPlaceFeeQueryModel);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceFeeRecordVO> listByPage(MerchantPlaceFeeRecordPageRequest merchantPlaceFeePageRequest) {
        MerchantPlaceFeeRecordQueryModel merchantPlaceFeeQueryModel = new MerchantPlaceFeeRecordQueryModel();
        BeanUtils.copyProperties(merchantPlaceFeePageRequest, merchantPlaceFeeQueryModel);
    
        List<MerchantPlaceFeeRecord> merchantPlaceFeeRecordList = this.merchantPlaceFeeRecordMapper.selectListByPage(merchantPlaceFeeQueryModel);
        if (ObjectUtils.isEmpty(merchantPlaceFeeRecordList)) {
            return Collections.EMPTY_LIST;
        }
    
        List<MerchantPlaceFeeRecordVO> voList = new ArrayList<>();
        merchantPlaceFeeRecordList.forEach(item -> {
            MerchantPlaceFeeRecordVO vo = new MerchantPlaceFeeRecordVO();
            
            BeanUtils.copyProperties(item, vo);
            
            // 查询修改人名称
            User user = userService.queryByUidFromCache(item.getModifyUserId());
            if (Objects.nonNull(user)) {
                vo.setModifyUserName(user.getName());
            }
            voList.add(vo);
        });
        
        return voList;
    }
    
    @Override
    public Integer save(MerchantPlaceFeeRecord merchantPlaceFeeRecord) {
        return merchantPlaceFeeRecordMapper.insert(merchantPlaceFeeRecord);
    }
    
    @Override
    public void asyncRecords(List<ElectricityCabinet> electricityCabinetList, ElectricityCabinetBatchOutWarehouseRequest outWarehouseRequest, TokenUser user) {
        if (ObjectUtils.isEmpty(electricityCabinetList)) {
            return ;
        }
        
        List<MerchantPlaceFeeRecord> placeFeeRecords = new ArrayList<>();
        for (ElectricityCabinet electricityCabinet: electricityCabinetList) {
            BigDecimal oldFee = BigDecimal.ZERO;
            BigDecimal newFee = BigDecimal.ZERO;
            // 判断新的场地费用和就的场地费用是否存在变化如果存在变化则将变换存入到历史表
            if (Objects.nonNull(electricityCabinet.getPlaceFee())) {
                oldFee = electricityCabinet.getPlaceFee();
            } else {
                oldFee = new BigDecimal(NumberConstant.MINUS_ONE);
            }
    
            if (Objects.nonNull(outWarehouseRequest.getPlaceFee())) {
                newFee = outWarehouseRequest.getPlaceFee();
            } else {
                newFee = new BigDecimal(NumberConstant.MINUS_ONE);
            }
            
            MerchantPlaceFeeRecord merchantPlaceFeeRecord = null;
            // 场地费有变化则进行记录
            if (!Objects.equals(newFee.compareTo(oldFee), NumberConstant.ZERO)) {
                merchantPlaceFeeRecord = new MerchantPlaceFeeRecord();
                merchantPlaceFeeRecord.setCabinetId(electricityCabinet.getId());
                if (!Objects.equals(newFee.compareTo(BigDecimal.ZERO), NumberConstant.MINUS_ONE)) {
                    merchantPlaceFeeRecord.setNewPlaceFee(newFee);
                }
                if (!Objects.equals(oldFee.compareTo(BigDecimal.ZERO), NumberConstant.MINUS_ONE)) {
                    merchantPlaceFeeRecord.setOldPlaceFee(oldFee);
                }
                if (Objects.nonNull(user)) {
                    merchantPlaceFeeRecord.setModifyUserId(user.getUid());
                    merchantPlaceFeeRecord.setTenantId(electricityCabinet.getTenantId());
                    long currentTimeMillis = System.currentTimeMillis();
                    merchantPlaceFeeRecord.setCreateTime(currentTimeMillis);
                    merchantPlaceFeeRecord.setUpdateTime(currentTimeMillis);
                }
                placeFeeRecords.add(merchantPlaceFeeRecord);
            }
        }
        
        if (ObjectUtils.isNotEmpty(placeFeeRecords)) {
            // 持久化
            executorService.execute(() -> {
                merchantPlaceFeeRecordMapper.batchInsert(placeFeeRecords);
            });
        }
    }
}
