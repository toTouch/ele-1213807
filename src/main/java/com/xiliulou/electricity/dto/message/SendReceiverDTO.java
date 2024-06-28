package com.xiliulou.electricity.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @ClassName: SendReceiverRequest
 * @description:
 * @author: renhang
 * @create: 2024-06-04 21:38
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendReceiverDTO {
    
    /**
     * 发送渠道
     */
    private Integer sendChannel;
    
    /**
     * 接受者
     */
    private Set<String> receiver;
}