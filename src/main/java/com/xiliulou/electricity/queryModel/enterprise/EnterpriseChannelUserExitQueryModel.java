package com.xiliulou.electricity.queryModel.enterprise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/1/16 16:15
 * @desc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EnterpriseChannelUserExitQueryModel {
    /**
     * 用户Id集合
     */
    private List<Long> uidList;
    
    /**
     * 类型集合
     */
    private List<Integer> typeList;
}
