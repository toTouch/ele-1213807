package com.xiliulou.electricity.vo.userinfo.emergencyContact;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 紧急联系人
 * @date 2024/11/11 19:31:09
 */

@Data
public class EmergencyContactVO {
    
    private Long id;
    
    private Long uid;
    
    private String emergencyName;
    
    private String emergencyPhone;
    
    private Integer relation;
}
