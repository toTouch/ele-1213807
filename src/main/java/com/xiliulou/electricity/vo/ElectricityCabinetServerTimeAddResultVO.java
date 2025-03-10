package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ElectricityCabinetServerTimeAddResultVO {
    /**
     * 添加成功数量
     */
    private Integer successNum;

    /**
     * 添加失败数量
     */
    private Integer failNum;

    /**
     * 未找到的sn集合
     */
    private List<String> notFoundSnList;

    /**
     * 重复的sn集合
     */
    private List<String> repeatSnList;
}
