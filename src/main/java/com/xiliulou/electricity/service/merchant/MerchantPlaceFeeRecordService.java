package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeRecord;
import com.xiliulou.electricity.request.asset.ElectricityCabinetBatchOutWarehouseRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceFeeRecordPageRequest;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceFeeRecordVO;
import com.xiliulou.security.bean.TokenUser;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/17 22:34
 * @desc
 */
public interface MerchantPlaceFeeRecordService {
    
    Integer countTotal(MerchantPlaceFeeRecordPageRequest merchantPlacePageRequest);
    
    List<MerchantPlaceFeeRecordVO> listByPage(MerchantPlaceFeeRecordPageRequest merchantPlacePageRequest);
    
    void asyncInsertOne(MerchantPlaceFeeRecord merchantPlaceFeeRecord);
    
    void asyncRecords(List<ElectricityCabinet> electricityCabinetList, ElectricityCabinetBatchOutWarehouseRequest batchOutWarehouseRequest, TokenUser userInfo);
    
    Integer existPlaceFeeByCabinetId(List<Long> cabinetIdList);
}
