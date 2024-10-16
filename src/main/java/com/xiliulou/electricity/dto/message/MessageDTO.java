package com.xiliulou.electricity.dto.message;


import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 * Description: This class is MessageDTO!
 * </p>
 * <p>Project: xiliulou-other-notes</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/7/12
 **/
@Data
public class MessageDTO implements Serializable {
    
    private static final long serialVersionUID = 5890019006837613071L;
    
    private String code;
    
    private Long tenantId;
    
    private Long notifyTime;
    
    private Map<String,Object> context;
}
