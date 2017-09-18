/*
 * -------------------------------------------------------------------------------------
 * Mi-Me Confidential
 *
 * Copyright (C) 2015 Shanghai Mi-Me Financial Information Service Co., Ltd.
 * All rights reserved.
 *
 * No part of this file may be reproduced or transmitted in any form or by any means,
 * electronic, mechanical, photocopying, recording, or otherwise, without prior
 * written permission of Shanghai Mi-Me Financial Information Service Co., Ltd.
 * -------------------------------------------------------------------------------------
 */
package cc.xiaoquer.tinytools.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.ArrayUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * <<description>>
 * Version     : 1.0
 * Created By  : Nicholas
 * Created Date: 2016/9/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MailForm {
    private String exchange = "smtp.exmail.qq.com";
    private String sender;
    private String sendername;
    private String senderpwd;
    private String receiver;
    private String copy;
    private String title;
    private String body;

    public String toSimpleString() {
        StringBuffer sb = new StringBuffer();

        sb.append("发件人(").append(sender)
                .append(") ,收件人(").append(receiver)
                .append("), 抄送(").append(copy)
                .append("), 标题(").append(title)
                .append(")");

        return sb.toString();
    }

    private String[] splitToArray(String s) {

        s = s.replaceAll("；", ";").replaceAll(",", ";").replaceAll("，", ";");

        String[] arr = StringUtils.split(s, ";");

        Set<String> set = new HashSet<>();

        for (String str :  arr) {
            if (StringUtils.isNotBlank(str)) {
                set.add(str);
            }
        }

        return set.toArray(new String[]{});
    }

    public String[] getReceiverArray() {
        return splitToArray(this.receiver);
    }

    public String[] getCopyArray() {
        return splitToArray(this.copy);
    }
}
