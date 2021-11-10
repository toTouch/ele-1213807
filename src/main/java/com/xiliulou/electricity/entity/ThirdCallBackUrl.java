package com.xiliulou.electricity.entity;


    



                                    
                                            
                                            
                                            
                                            
                                            
                                            
                    
                                    
                                            
                                            
                                            
                                            
                                            
                                            
                    

                            import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (ThirdCallBackUrl)实体类
 *
 * @author Eclair
 * @since 2021-11-10 15:25:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_third_call_back_url")
public class ThirdCallBackUrl {
    
    private Integer id;
    
    private Integer tenantId;
    /**
    * 换电url
    */
    private String exchangeUrl;
    /**
    * 还电池url
    */
    private String returnUrl;
    /**
    * 租电池url
    */
    private String rentUrl;
    
    private Long createTime;
    
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
