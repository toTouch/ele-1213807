package com.xiliulou.electricity.vo.activity;

import lombok.Data;

@Data
public class ShareMoneyAndShareActivityVO {
    
    /**
     * 存在邀请返券
     */
    private Boolean existShareActivity;
    
    /**
     * 存在邀请返现
     */
    private Boolean existShareMoneyActivity;
}
