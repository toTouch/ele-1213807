/**
 * Create date: 2024/6/26
 */

package com.xiliulou.electricity.request.notify;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * description: 公众号通知用户操作请求
 *
 * @author caobotao.cbt
 * @date 2024/6/26 14:57
 */
@Data
public class NotifyUserInfoOptRequest {
    
    /**
     * 主键id
     */
    @NotNull(groups = UpdateGroup.class, message = "id不能为空")
    private Long id;
    
    /**
     * 用户名
     */
    @NotBlank(groups = {UpdateGroup.class, CreateGroup.class}, message = "userName不能为空")
    private String userName;
    
    /**
     * 电话
     */
    @NotBlank(groups = {UpdateGroup.class, CreateGroup.class}, message = "phone不能为空")
    private String phone;
    
    /**
     * 微信授权用户唯一标识
     */
    @NotBlank(groups = {CreateGroup.class}, message = "openId不能为空")
    private String openId;
    
    /**
     * 昵称
     */
    private String nickName;
    
    
}
