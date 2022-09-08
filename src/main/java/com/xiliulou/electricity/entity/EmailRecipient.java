package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮件收件人
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-10-15:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailRecipient {
    /**
     * 收件人邮箱
     */
    private String email;
    /**
     * 发送人名字
     */
    private String name;
}
