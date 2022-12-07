package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-11-05-15:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    
    private String id;
    
    private String msg;
}
