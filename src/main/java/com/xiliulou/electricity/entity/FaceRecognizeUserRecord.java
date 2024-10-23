package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * (FaceRecognizeUserRecord)表实体类
 *
 * @author zzlong
 * @since 2023-02-02 14:27:09
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_face_recognize_user_record")
public class FaceRecognizeUserRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 使用人
     */
    private Long uid;
    /**
     * 审核结果id
     */
    private Long authResultId;
    /**
     * 状态
     */
    private Integer status;
    
    private String certifyId;

    private Integer tenantId;
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //人脸核身结果 0:初始化，1：成功，2：失败
    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = 2;

}
