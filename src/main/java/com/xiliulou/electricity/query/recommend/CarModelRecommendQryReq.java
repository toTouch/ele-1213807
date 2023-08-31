package com.xiliulou.electricity.query.recommend;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 车辆型号推荐请求数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarModelRecommendQryReq implements Serializable {

    private static final long serialVersionUID = -2246611489401045032L;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 加盟商ID
     */
    @NotNull(message = "[加盟商]不能为空")
    private Integer franchiseeId;

    /**
     * 门店ID
     */
    private Integer storeId;

    /**
     * 经度
     */
    @NotNull(message = "[经度]不能为空")
    private Double longitude;

    /**
     * 纬度
     */
    @NotNull(message = "[纬度]不能为空")
    private Double latitude;

    /**
     * 距离(m)
     */
    @NotNull(message = "[距离]不能为空")
    private Double distance;



}
