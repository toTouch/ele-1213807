package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @ClassName : MerchantPromotionFeeScanVO
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-21
 */
@Data
public class MerchantPromotionFeeScanCodeVO {
    
    /**
     * 今日扫码人数
     */
    private Integer todayScanCodeNum;
    
    /**
     * 昨日扫码人数
     */
    private Integer yesterdayScanCodeNum;
    
    /**
     * 本月扫码人数
     */
    private Integer currentMonthScanCodeNum;
    
    /**
     * 上月扫码人数
     */
    private Integer lastMonthScanCodeNum;
    
    /**
     * 累计扫码人数
     */
    private Integer totalScanCodeNum;
    
    /**
     * 今日成功人数(首次成功购买指定套餐时间=今日0点～当前时间，邀请状态=邀请成功)
     */
    private Integer todayPurchaseNum;
    
    /**
     * 昨日成功人数(首次成功购买指定套餐时间=昨日0点～今日0点，邀请状态=邀请成功)
     */
    private Integer yesterdayPurchaseNum;
    
    /**
     * 本月成功人数(首次成功购买指定套餐时间=本月1号0点～当前时间，邀请状态=邀请成功)
     */
    private Integer currentMonthPurchaseNum;
    
    /**
     * 上月成功人数(首次成功购买指定套餐时间=上月1号0点～本月1号0点，邀请状态=邀请成功)
     */
    private Integer lastMonthPurchaseNum;
    
    /**
     * 累计成功人数：首次成功购买指定套餐时间<=当前时间，邀请状态=邀请成功
     */
    private Integer totalPurchaseNum;
    
}
