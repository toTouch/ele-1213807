package com.xiliulou.electricity.request.userinfo.emergencyContact;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @author HeYafeng
 * @date 2024/11/8 17:15:03
 */
@Data
public class EmergencyContactRequest {
    
    private Long id;
    
    @Size(min = 2, max = 20, message = "紧急联系人姓名长度须在2-20之间")
    private String emergencyName;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "无效的手机号")
    private String emergencyPhone;
    
    @Range(min = 0, max = 3, message = "请输入正确的联系人关系")
    private Integer relation;
}
