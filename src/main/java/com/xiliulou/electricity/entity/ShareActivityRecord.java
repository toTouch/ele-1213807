package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 发起邀请活动记录(ShareActivityRecord)实体类
 *
 * @author Eclair
 * @since 2021-07-14 09:45:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_share_activity_record")
public class ShareActivityRecord {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
    * 活动id
    */
    private Integer activityId;
    /**
    * 加密code
    */
    private String code;
    /**
     * 分享状态 1--初始化，2--已分享，3--分享失败
     */
    private Integer status;
    /**
    * 用户uid
    */
    private Long uid;
    /**
    * 邀请人数
    */
    private Integer count;
    /**
     * 可用邀请人数
     */
    private Integer availableCount;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 修改时间
    */
    private Long updateTime;
    /**
    * 租户id
    */
    private Integer tenantId;
    
    /**
     * 加盟商Id
     */
    private Integer franchiseeId;

    //初始化
    public static Integer STATUS_INIT = 1;
    //分享成功
    public static Integer STATUS_SUCCESS = 2;
    //分享失败
    public static Integer STATUS_FAIL = 3;

}
