package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-08-11:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MQMailMessageNotify {

//    private String from;

    private List<EmailRecipient> to;

    private String[] cc;

    private String[] bcc;

    private String subject;

    private String text;

}
