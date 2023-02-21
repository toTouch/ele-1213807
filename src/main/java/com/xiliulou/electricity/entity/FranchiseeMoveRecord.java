package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * 加盟商迁移记录(FranchiseeMoveRecord)表实体类
 *
 * @author zzlong
 * @since 2023-02-07 17:12:51
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_franchisee_move_record")
public class FranchiseeMoveRecord {
    /**
     * 操作Id
     */
    private Integer id;
    /**
     * 用户uid
     */
    private Long uid;
    /**
     * 操作人姓名
     */
    private String name;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 旧加盟商
     */
    private Long oldFranchiseeId;
    /**
     * 新加盟商
     */
    private Long newFranchiseeId;
    /**
     * 电池型号
     */
    private String batteryType;
    /**
     * 迁移前换电套餐
     */
    private Long batteryCardId;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
