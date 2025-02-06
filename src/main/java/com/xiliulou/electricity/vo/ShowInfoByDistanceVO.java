package com.xiliulou.electricity.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

/**
 * @author : renhang
 * @description ShowInfoByDistanceVO
 * @date : 2025-01-09 15:06
 **/
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShowInfoByDistanceVO {

    private Set<String> batteryVoltageSet;

    private List<ElectricityCabinetSimpleVO> resultVo;
}
