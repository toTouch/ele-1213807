package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * 实名认证信息(TEleUserAuth)实体类
 *
 * @author makejava
 * @since 2021-02-20 13:37:38
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_user_auth")
public class EleUserAuth {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
    * 用户uid
    */
    private Long uid;
    /**
    * 资料项id
    */
    private Integer entryId;
    /**
    * 资料项内容
    */
    private String value;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 资料项状态 0--未审核，1--审核成功 2--审核失败
    */
    private Object status;
    /**
    * 删除标记
    */
    private Object delFlag;
    
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //等待审核中
    public static Integer STATUS_PENDING_REVIEW = 0;
    //审核被拒绝
    public static Integer STATUS_REVIEW_REJECTED = 1;
    //审核通过
    public static Integer STATUS_REVIEW_PASSED = 2;

}