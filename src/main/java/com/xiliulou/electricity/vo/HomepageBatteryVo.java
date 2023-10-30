package com.xiliulou.electricity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author hrp
 * @date 2022/08/03 14:22
 * @mood 首页电池分析
 */
@Data
public class HomepageBatteryVo {

    private Integer count;

    private List<HomepageBatteryFrequencyVo> homepageBatteryFrequencyVos;
}
