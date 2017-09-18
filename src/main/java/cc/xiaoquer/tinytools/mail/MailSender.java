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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.ImageHtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * <<description>>
 * Version     : 1.0
 * Created By  : Nicholas
 * Created Date: 2016/9/20
 */
@Slf4j
public class MailSender {
    private static final Logger LOGGER_SUCC = LoggerFactory.getLogger("mail.succ");
    private static final Logger LOGGER_FAIL = LoggerFactory.getLogger("mail.fail");

    private static final ExecutorService executor = new ThreadPoolExecutor(5, 10,
            10, TimeUnit.MINUTES,
            new ArrayBlockingQueue(500), new ThreadPoolExecutor.DiscardPolicy());

    public static boolean send(final MailForm mailForm) {
        Future<Boolean> f = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    ImageHtmlEmail email = new ImageHtmlEmail();
                    email.setCharset("UTF-8");
                    email.setHostName(mailForm.getExchange());
                    email.setAuthenticator(new DefaultAuthenticator(mailForm.getSender(), mailForm.getSenderpwd()));

                    email.setSubject(mailForm.getTitle());

                    email.setFrom(mailForm.getSender(), mailForm.getSendername());

                    String[] receiverArray = mailForm.getReceiverArray();
                    if (receiverArray != null && receiverArray.length > 0) {
                        email.addTo(receiverArray);
                    }

                    String[] copyArr = mailForm.getCopyArray();
                    if (copyArr != null && copyArr.length > 0) {
                        email.addCc(copyArr);
                    }

                    email.setHtmlMsg(mailForm.getBody());

                    email.send();

                    LOGGER_SUCC.info("邮件发送成功," + mailForm.toSimpleString());

                    return true;
                } catch (Exception e) {
                    log.error("邮件发送失败," + mailForm.toSimpleString(), e); //tinytools.log
                    LOGGER_FAIL.info("邮件发送失败," + mailForm.toSimpleString());//mailsending-fail.log
                    return false;
                }
            }
        });

        try {
            //10秒必须返回，否则报错。
            return f.get(10, TimeUnit.SECONDS);
            //return f.get();
        } catch (Exception e) {
        }
        return false;
    }

}
