package com.xiliulou.electricity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-05-24-15:08
 */
@Data
public class ShareActivityOperateRecordVO {

    /**
     * id
     */
    private Long id;
    /**
     * 活动id
     */
    private Long shareActivityId;
    /**
     * 活动名称
     */
    private String name;
    /**
     * 修改人id
     */
    private Long uid;

    private String username;
    /**
     * 活动套餐
     */
    private String memberCard;
    private List<String> memberCardNames;

    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;


}
