package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 电柜批量导入时图片批量保存
 */
@Data
public class ElectricityCabinetPictureBatchSaveRequest {
    
    /**
     * 图片名称
     */
    private List<String> fileNameList;
    
    /**
     * 柜机ID
     */
    private List<Long> otherIdList;
    
    /**
     * 文件类型 电柜图片：1
     */
    @NotNull(message = "fileType不能为空")
    private Integer fileType;
}
