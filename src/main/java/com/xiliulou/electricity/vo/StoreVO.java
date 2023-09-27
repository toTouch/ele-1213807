package com.xiliulou.electricity.vo;

import lombok.Data;

import java.util.List;


/**
 * 门店表(TStore)实体类
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@Data
public class StoreVO {
    
    //全天
    public static final String ALL_DAY = "-1";
    
    //自定义时间段
    public static final String CUSTOMIZE_TIME = "1";
    
    //不合法数据
    public static final String ILLEGAL_DATA = "2";
    
    private Long id;
    
    /**
     * 门店名称
     */
    private String name;
    
    /**
     * 门店地址
     */
    private String address;
    
    /**
     * 地址经度
     */
    private Double longitude;
    
    /**
     * 地址纬度
     */
    private Double latitude;
    
    /**
     * 0--正常 1--删除
     */
    private Object delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 可用状态(0--启用，1--禁用)
     */
    private Integer usableStatus;
    
    private Double distance;
    
    /**
     * 联系电话
     */
    private String servicePhone;
    
    /**
     * 营业时间
     */
    private String businessTime;
    
    /**
     * 营业时间类型
     */
    private String businessTimeType;
    
    /**
     * 营业开始时间
     */
    private Long beginTime;
    
    /**
     * 营业结束时间
     */
    private Long endTime;
    
    /**
     * 是否营业 0--营业 1--打烊
     */
    private Integer isBusiness;
    
    private Long uid;
    
    //在线柜机数
    private Integer onlineElectricityCabinet;
    
    //满电电池
    private Integer fullyElectricityBattery;
    
    /**
     * 绑定用户名称
     */
    private String userName;
    
    private Long franchiseeId;
    
    private Integer percent;
    
    private String franchiseeName;
    
    private Integer payType;
    
    /**
     * 门店图片列表
     */
    private List<PictureVO> pictureList;
    
    /**
     * 门店标签 服务类型
     */
    private List<String> serviceType;
    
    /**
     * 门店详情
     */
    private String detail;
    
    
}
