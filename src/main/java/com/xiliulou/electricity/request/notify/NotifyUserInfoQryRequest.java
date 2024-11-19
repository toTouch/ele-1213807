/**
 *  Create date: 2024/6/26
 */

package com.xiliulou.electricity.request.notify;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * description: 公众号通知用户查询请求
 *
 * @author caobotao.cbt
 * @date 2024/6/26 14:57
 */
public class NotifyUserInfoQryRequest {
    
    /**
     * 根据微信二维码查询
     *
     * @author caobotao.cbt
     * @date 2024/6/26 17:43
     */
    @Data
    public static class QueryByCode {
        
        /**
         * 二维码
         */
        @NotBlank(message = "code不能为空")
        private String code;
    }
    
    
    /**
     * 根据分页参数查询
     *
     * @author caobotao.cbt
     * @date 2024/6/26 17:43
     */
    @Data
    public static class QueryByPage {
        
        /**
         * openid
         */
        @NotNull(message = "offset不能为空")
        private Integer offset;
    
        /**
         * openid
         */
        @NotNull(message = "size不能为空")
        private Integer size;
    }
}
