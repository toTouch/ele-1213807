package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * (FaceAuthResultData)表实体类
 *
 * @author zzlong
 * @since 2023-02-03 11:03:24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_face_auth_result_data")
public class FaceAuthResultData {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审核结果
     */
    private String authResult;

    private Integer tenantId;
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
