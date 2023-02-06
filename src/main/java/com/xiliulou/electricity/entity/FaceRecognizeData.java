package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * (FaceRecognizeData)表实体类
 *
 * @author zzlong
 * @since 2023-01-31 15:38:29
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_face_recognize_data")
public class FaceRecognizeData {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 人脸核身次数
     */
    private Integer faceRecognizeCapacity;
    /**
     * 充值时间
     */
    private Long rechargeTime;

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
