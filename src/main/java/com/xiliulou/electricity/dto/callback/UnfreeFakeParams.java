package com.xiliulou.electricity.dto.callback;


import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * Description: This class is FakeParams!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/9/4
 **/
@Data
@Builder
public class UnfreeFakeParams {
    
    private String orderId;
    
    private String authNO;
    
    private Integer channel;
    
    private Integer tenantId;
}
