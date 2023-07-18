package com.xiliulou.electricity.entity;


    



                                    
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                    
                                    
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                    

                                                    import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (ElePower)实体类
 *
 * @author Eclair
 * @since 2023-07-18 10:20:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_power")
public class ElePower {
    
    private Long id;
    
    private String sn;
    
    private String eName;
    
    private Long eid;
    
    private Integer storeId;
    
    private Integer franchiseeId;
    
    private Integer tenantId;
    /**
    * 上报时间
    */
    private Long reportTime;
    
    private Long createTime;
    /**
    * 耗电类别 0--平用电 1--峰用电 3--谷用电
    */
    private Integer type;
    /**
    * 总共使用电量
    */
    private Double sumPower;
    /**
    * 每小时耗电量
    */
    private Double hourPower;
    /**
    * 每小时电费
    */
    private Double electricCharge;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
