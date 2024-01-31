package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 10:29
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_channel_employee")
public class ChannelEmployee {
    
    private Long id;
    
    private Long uid;
    
}
