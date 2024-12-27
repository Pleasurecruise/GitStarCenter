package cn.yiming1234.gitstarcenter.util;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailUtil {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${yiming1234.mail.from}")
    private String mailFrom;

    @Value("${yiming1234.mail.subject}")
    private String mailSubject;

    /**
     * 发送文本邮件
     */
    public void sendMail(String to, String username, String Interaction) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(mailFrom);
        message.setSubject(mailSubject);
        message.setText("Github用户 " + username + " " + Interaction + " 了你的仓库，继续加油哦！");
        mailSender.send(message);
    }

    /**
     * 发送HTML邮件
     */
    public void sendHtmlMail(String to, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            messageHelper.setTo(InternetAddress.parse(to));
            messageHelper.setText(content, true);
            mailSender.send(message);
            log.info("发送HTML邮件成功");
        } catch (Exception e) {
            log.error("发送HTML邮件失败", e);
        }
    }

}
