package com.xiliulou.electricity.request.payparams;


import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * <p>
 * Description: This class is WechatPublicKeyRequest!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/6
 **/
@Data
public class WechatPublicKeyRequest implements Serializable {
    
    private static final long serialVersionUID = 6226143645002787380L;
    
    @NotNull(groups = UpdateGroup.class, message = "id不可为空")
    private Long id;
    
    @NotNull(groups = {CreateGroup.class}, message = "支付参数id不能为空")
    private Long payParamsId;
    
    private Long franchiseeId;
    
    @NotEmpty(groups = {CreateGroup.class, UpdateGroup.class}, message = "公钥不能为空")
    @NotNull(groups = {CreateGroup.class, UpdateGroup.class}, message = "公钥不能为空")
    private String pubKey;
    
    @NotEmpty(groups = {CreateGroup.class, UpdateGroup.class}, message = "证书序列号不能为空")
    @NotNull(groups = {CreateGroup.class, UpdateGroup.class}, message = "公钥id不能为空")
    private String pubKeyId;
}
