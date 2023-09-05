package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-05-15:13
 */
@Data
public class UserAuthMessageVO {

    private Long uid;

    private Integer authStatus;

    private String msg;

}
