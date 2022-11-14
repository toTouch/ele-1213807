package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * (UserDataScope)表实体类
 *
 * @author zzlong
 * @since 2022-09-19 14:22:34
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_data_scope")
public class UserDataScope {

    @TableId(value = "uid", type = IdType.AUTO)
    private Long id;

    private Long uid;

    private Long dataId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
