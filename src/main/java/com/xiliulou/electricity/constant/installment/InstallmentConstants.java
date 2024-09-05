package com.xiliulou.electricity.constant.installment;

import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/27 15:24
 */
public interface InstallmentConstants {
    
    Integer PACKAGE_TYPE_BATTERY = 0;
    
    Integer PACKAGE_TYPE_CAR = 1;
    
    Integer PACKAGE_TYPE_CAR_BATTERY = 2;
    
    /**
     * 用于设置接口校验注解type，开启登录校验、数据权限设置
     *
     * @see ProcessParameter
     */
    int PROCESS_PARAMETER_DATA_PERMISSION = 1;
    
    /**
     * 用于设置接口校验注解type，开启登录校验、数据权限设置、分页参数校验
     *
     * @see ProcessParameter
     */
    int PROCESS_PARAMETER_LOGIN_AND_DATA_AND_PAGE = 3;
    
    /**
     * 分期记录状态-初始化
     */
    Integer INSTALLMENT_RECORD_STATUS_INIT = 0;
    
    /**
     * 分期记录状态-待签约
     */
    Integer INSTALLMENT_RECORD_STATUS_UN_SIGN = 1;
    
    /**
     * 分期记录状态-签约成功
     */
    Integer INSTALLMENT_RECORD_STATUS_SIGN = 2;
    
    /**
     * 分期记录状态-解约中
     */
    Integer INSTALLMENT_RECORD_STATUS_TERMINATE = 3;
    
    /**
     * 分期记录状态-已完成
     */
    Integer INSTALLMENT_RECORD_STATUS_COMPLETED = 4;
    
    /**
     * 代扣计划状态-未支付
     */
    Integer DEDUCTION_PLAN_STATUS_INIT = 0;
    
    /**
     * 代扣计划状态-已支付
     */
    Integer DEDUCTION_PLAN_STATUS_PAID = 1;
    
    /**
     * 代扣计划状态-代扣失败
     */
    Integer DEDUCTION_PLAN_STATUS_FAIL = 2;
    
    /**
     * 代扣计划状态-取消支付
     */
    Integer DEDUCTION_PLAN_STATUS_CANCEL = 3;
    
    /**
     * 解约记录-审核中
     */
    Integer TERMINATING_RECORD_STATUS_INIT = 0;
    
    /**
     * 解约记录-拒绝
     */
    Integer TERMINATING_RECORD_STATUS_REFUSE = 1;
    
    /**
     * 解约记录-通过
     */
    Integer TERMINATING_RECORD_STATUS_RELEASE = 2;
    
    /**
     * 蜂云成功响应码
     */
    String FY_SUCCESS_CODE = "WZF00000";
    
    /**
     * 蜂云回调业务参数名-bizContent
     */
    String OUTER_PARAM_BIZ_CONTENT = "bizContent";
    
    /**
     * 回调类型，签约成功
     */
    Integer NOTIFY_STATUS_SIGN = 1;
    
    /**
     * 回调类型，解约成功
     */
    Integer NOTIFY_STATUS_UN_SIGN = 2;
    
    /**
     * 客户端来源(h5)，h5模式返回url，可以直接访问
     */
    String CHANNEL_FROM_H5 = "h5";
    
    String NOTIFY_URL = "https://ele.xiliulou.com/electricityCabinet/outer/installment/sign/notify/%d";
    
    String FY_AESKEY = "RyiQwkaIB2AMvmpJk5RG1g==";
    
    String FY_PUBLICKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkuqUE1JxqGh3bJdQGUMdJZVFKq3kRAP2sHl4Npsi4tV+s9ngWlFGAICExzKvwseRnruRew7Y2EgQl3/u9Gj/ovtYU42eQl5UcZ6Yi5Q0fo4UBfNvGClx1Y1WzF3GpTjAxa/3oh25cwYGpbUP27y2fVwkg11TOiI7QJvFmf41nuOh77HG3+PQCgSxM3HgCbtTXB/UFrdCd8MhSP9EFL6xXS/50Qxqjv4sVVBdx+gqG2n6cRRBgMMlFDk/F8rCZrSNoWVsfFW0umACq2Pg3u48SBdebjKa+xqrb+QhAcRBreRweUAeBBky0TESzACZF6axHiFbC9TJKSXfHW7NWjTnYQIDAQAB";
    
    String FY_privateKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCRwT3mfZSKJbHEEHeYjwSnAJuXC8/04zf75C/iXjdsFHWJNHcNyL1WKmaB+xlNM2ACqN7bnLN6X6xD6ums4E9fPxCKEVsTuCT1U1tUgTzV1oLYM797yvVMjZY8lDjMmtYxtfdenMirQDxBPuqi+QIp/i2+Om1zZ5dMqGtroVsmQ1QmCkWWZbnyJOYd56BgGSWmkS3tGPc54ZBh5TyUc+plGmCPSqJntA9IaR/6bENyeRTP7adH7bNtMMHItHgtqMAlsKs8HG88vscdroORuCGYWrAQh+hd/3Za3wrwH8VwFJveHsE8L10S4JJuq4WMkf3D5x7DJXN9/MfkaZBZKQVhAgMBAAECggEBAIIevO8nD1OhQSji9pHYo/OfREe9QOLDcnhKh0EUkW/IeAGMN/izS+w/oBHMJBNamQzmy0XNiCDohnZ/ois2eUVznIVV5zQcgcoNp4wycCgK//aZhAm07/ZutD/Spz5pwGzSoUrytubDSseIrpndqUPp6dZYfNu5EEZcWMHWY/QscB1vrPhCOlL3WlW1Dw/2Mwpckbpe8fVakBxKAh+DGbdwYxzpFr90lu47cO5shdYXrcoVLZG7cBWL+Innt/gZVP0JbiltvgQCeZDJRTYpVc6VgitQzwyRLIFXqNVJ24POOODAnFhcc9cRRlQqRxefAWiBTxqGN5FqFXm4PEoquBECgYEAyZ+TStQmJRKP428lIk/BiDymxEoNeuo31gVhfitvmUIbUatTEvEdr+Ysi+RIA4nTFaUWed6FdXbnmVx3UcgHQxZE+YZGBWxbwFxEbqUKfBM0VkfCPxeH6o7iSSgOjFvcgbW+mjbuB1FTDonkgMUbbwwSZ2x0y/jyLbu9CSlSF5UCgYEAuRBouyFA8tL/2ek7mGwKFaj10b14X7uygfYVJU2iO9WNl++Fn2oBl/gUdVgxXcuhxl10/MBvzuOQBPxeQYNW5fHe7FxYTuWREnV+ub66cxVkg+HUuYHDbSysS1cR97kSxXt3Dbij62Sevo8dXnF5d5P2ISlzLFYq8Bzp6ruuk50CgYA5M3WVOCTuRZre2mx9lIUCgrqJW70BTyuyI2qqYeZSY6fefq2d5RwYCERBF+26W0KwlFqRDqlsTuSb5NjRMd+lj87Xv9ljtn89Tq/3eAlGdChYBhm8zo8343udZ6jg9zrS7d36YHUdTFw09rgGNGV5FsKt5t4n9CO5B9c0ny10FQKBgQCZO+4MLF+z8GQrEgTnk2XSQ4GvdnwKgMcw9wrRRXP0qUHBsXFp6wOAyshd1XWi3W6Jam7orYqmuyRQDIsbId49LcXQ3jG8ujciGqmgsT1+9xKwZLiYFcs0Rc4v39ufy92wKSgN83m++iS7UpMV1U2nZpthIAoehwaspLRHSbomvQKBgQCT8drsPfwWpzGb1gXkK7JRDW1EtwnTPb7dzGIw60QmwkzTv0YSv8513HlXSbVLVTqj70iB+ZbteH1LEGPVbni0Lo71FHXKYW+yhb7Mo4YnEolk4RxiuH3a7ETM0HvtEoUrKX5rBaCiGMFIqwq/bsi36gPG2GKf2rGlVsA7gTOI7g==";
}
