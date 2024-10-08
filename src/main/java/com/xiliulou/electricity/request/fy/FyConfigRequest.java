package com.xiliulou.electricity.request.fy;


import lombok.Data;

/**
 * <p>
 * Description: This class is FyConfigRequest!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/28
 **/
@Data
public class FyConfigRequest {
    
    private String merchantCode;
    
    private String storeCode;
    
    private String channelCode;
}
