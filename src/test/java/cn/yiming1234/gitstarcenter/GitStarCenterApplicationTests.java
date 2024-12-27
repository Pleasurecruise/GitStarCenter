package cn.yiming1234.gitstarcenter;

import cn.yiming1234.gitstarcenter.util.MailUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MailUtilTest {

    @Autowired
    private MailUtil mailUtil;

    @Test
    void testSendMail() {
        mailUtil.sendMail("pleasure@yiming1234.cn", "Pleasurecruise", "Star");
    }
}