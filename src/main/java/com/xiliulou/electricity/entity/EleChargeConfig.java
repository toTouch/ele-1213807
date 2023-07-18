package com.xiliulou.electricity.entity;


    



                                    
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                    
                                    
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                    

                                        import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (EleChargeConfig)实体类
 *
 * @author Eclair
 * @since 2023-07-18 10:21:40
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_charge_config")
public class EleChargeConfig {
    
    private Long id;
    /**
    * 电价名称
    */
    private String name;
    /**
    * 所属加盟商id
    */
    private Long franchiseeId;
    /**
    * 所属门店id
    */
    private Long storeId;
    /**
    * 所属换电柜Id
    */
    private Integer eid;
    
    private Integer tenantId;
    /**
    * 数据类型 0--加盟商全部（运营商） 1--门店全部（加盟商）2--柜机
    */
    private Integer type;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;
    /**
    * 规则
    */
    private String jsonRule;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
