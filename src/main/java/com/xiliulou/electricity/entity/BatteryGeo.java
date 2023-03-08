package com.xiliulou.electricity.entity;


    



                                    
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                    
                                    
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                    

                                    import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (BatteryGeo)实体类
 *
 * @author Eclair
 * @since 2023-03-03 08:54:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_battery_geo")
public class BatteryGeo {
    
    private Long id;
    /**
    * sn
    */
    private String sn;
    /**
    * 地址经度
    */
    private Double longitude;
    /**
    * 地址纬度
    */
    private Double latitude;
    /**
    * 空间位置信息
    */
    private String gis;
    
    private String geohash;
    
    private Integer tenantId;
    
    private Long createTime;
    
    private Long updateTime;
    
    private Long franchiseeId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
