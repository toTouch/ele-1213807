package com.xiliulou.electricity.entity;





                                    
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                    
                                    
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                    

                                import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * 电池型号(TElectricityBatteryModel)实体类
 *
 * @author makejava
 * @since 2020-11-26 14:44:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_battery_model")
public class TElectricityBatteryModel {
    
    private Integer id;
    
    private String name;
    
    private Integer voltage;
    /**
    * 单位(mah)
    */
    private Integer capacity;
    /**
    * 类型(原材料)
    */
    private String startingMaterial;
    
    private Long createTime;
    
    private Long updateTime;
    
    private Object delFlag;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}