package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-07-17-15:05
 */
@Data
public class AlipayUserCertifyInfoQuery {
    
    @NotBlank(message = "姓名不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String userName;
    
    @NotBlank(message = "身份证号不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String idNumber;
    
    @NotBlank(message = "参数不合法", groups = {UpdateGroup.class})
    private String certifyId;
    
    @NotBlank(message = "身份证正面照片", groups = {UpdateGroup.class})
    private String frontPicture;
    
    @NotBlank(message = "身份证反面照片", groups = {UpdateGroup.class})
    private String backPicture;
}
