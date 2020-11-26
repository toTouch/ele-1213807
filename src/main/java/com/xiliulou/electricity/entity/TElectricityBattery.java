package com.xiliulou.electricity.entity;





                                    
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                    
                                    
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                    

                                                import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * 换电柜电池表(TElectricityBattery)实体类
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_battery")
public class TElectricityBattery {
    
    private Long id;
    /**
    * 所属店铺
    */
    private Long shop;
    /**
    * 代理商id
    */
    private Integer agent;
    /**
    * sn码
    */
    private String serialNumber;
    /**
    * 型号id
    */
    private Integer modelId;
    /**
    * 电池电量
    */
    private Integer capacity;
    /**
    * 0：在仓，1：在库，2：租借
    */
    private Object status;
    
    private Long createTime;
    
    private Long updateTime;
    
    private Object delFlag;
    /**
    * 用户id
    */
    private Long uid;
    
    private Integer cabinetId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}