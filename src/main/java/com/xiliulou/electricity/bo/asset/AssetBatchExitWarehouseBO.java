package com.xiliulou.electricity.bo.asset;

import com.xiliulou.electricity.query.asset.AssetExitWarehouseDetailSaveQueryModel;
import com.xiliulou.electricity.query.asset.AssetExitWarehouseSaveQueryModel;
import com.xiliulou.electricity.request.asset.AssetBatchExitWarehouseRequest;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.ElectricityCarVO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 批量退库BO
 * @date 2024/7/2 17:05:05
 */

@Data
@Builder
public class AssetBatchExitWarehouseBO {
    
    private AssetBatchExitWarehouseRequest assetBatchExitWarehouseRequest;
    
    private AssetExitWarehouseSaveQueryModel recordSaveQueryModel;
    
    private List<AssetExitWarehouseDetailSaveQueryModel> detailSaveQueryModelList;
    
    private List<String> snList;
    
    private Integer type;
    
    private Long operator;
    
    private List<ElectricityCabinetVO> exitWarehouseCabinetList;
    
    private List<ElectricityBatteryVO> exitWarehouseBatteryList;
    
    private List<ElectricityCarVO> exitWarehouseCarList;
}
