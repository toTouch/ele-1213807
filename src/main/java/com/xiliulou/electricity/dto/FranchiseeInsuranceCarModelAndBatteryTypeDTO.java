package com.xiliulou.electricity.dto;


import lombok.Data;

/**
 * <p>
 * Description: This class is FranchiseeInsuranceCarModelAndBatteryTypeDTO!
 * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#QZIhddTgBoCWAXxcwAjch0MGnIg">14.11 保险购买记录（3条优化项）</a>
 * <a></a>
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/11
 **/
@Data
public class FranchiseeInsuranceCarModelAndBatteryTypeDTO {
    
    /**
     * <p>
     * Description: 保险id
     * </p>
     */
    private Long id;
    
    /**
     * <p>
     * Description: 类型名
     * <pre>
     *        insuranceType = 0 => 电池类型
     *        insuranceType = 1 => 车辆类型
     *    </pre>
     * </p>
     */
    private String label;
    
    /**
     * <p>
     * Description: 保险类型
     * <pre>
     *        0:电池
     *        1:车辆
     *    </pre>
     * </p>
     */
    private Integer insuranceType;
}
