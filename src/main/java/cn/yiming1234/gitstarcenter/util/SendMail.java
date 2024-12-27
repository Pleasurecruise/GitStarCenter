package cn.yiming1234.gitstarcenter.util;

import cn.yiming1234.gitstarcenter.entity.Notification;
import cn.yiming1234.gitstarcenter.repo.NotificationRepo;
import cn.yiming1234.gitstarcenter.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
@Slf4j
public class SendMail {

    private final NotificationRepo notificationRepo;
    public SendMail(NotificationRepo notificationRepo){
        this.notificationRepo = notificationRepo;
    }

    public void apply(Integer sourceUserId, Integer targetUserId, String Interaction) {
        Notification notification = new Notification();
        notification.setSourceUserId(sourceUserId);
        notification.setTargetUserId(targetUserId);
        notification.setInteraction(Interaction);
        notification.setStatus(0);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepo.save(notification);
        Result.success("success");
    }

}
