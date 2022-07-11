package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import lombok.Data;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-07-08-9:57
 */
@Data
public class EleOrderOperHistoryDetailVO {
    /**
     * 操作记录
     */
    private List<ElectricityCabinetOrderOperHistory> historyList;
    /**
     * 上报数据类型 0：旧版本，1：新版本
     */
    private int type;



}
