package com.xiliulou.electricity.vo.enterprise;
import lombok.Data;

/**
 * @author maxiaodong
 * @description: 骑手扫码检测vo
 * @date 2023/9/19 4:20
 */

@Data
public class EnterpriseChannelUserScanCheckVO {
    /**
     * 原站点Id
     */
    private Long oldEnterpriseId;

    /**
     * 新站点id
     */
    private Long enterpriseId;
    
    /**
     * 状态: 1: 滞纳金，2：未归还电池，3：冻结，4：冻结申请
     */
    private String status;
    
    /**
     * 站点名称
     */
    private String enterpriseName;
}
