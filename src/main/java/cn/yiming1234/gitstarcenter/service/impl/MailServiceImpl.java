package cn.yiming1234.gitstarcenter.service.impl;

import cn.yiming1234.gitstarcenter.entity.Notification;
import cn.yiming1234.gitstarcenter.mapper.UserMapper;
import cn.yiming1234.gitstarcenter.repo.NotificationRepo;
import cn.yiming1234.gitstarcenter.service.MailService;
import cn.yiming1234.gitstarcenter.vo.MailVO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MailServiceImpl implements MailService {
    private final NotificationRepo notificationRepo;
    private final UserMapper userMapper;
    private final int READ = 1;
    private final int UNREAD = 0;
    public MailServiceImpl(NotificationRepo notificationRepo,UserMapper userMapper) {
        this.notificationRepo = notificationRepo;
        this.userMapper = userMapper;
    }

    @Override
    public List<MailVO> getMails(String username, Integer status) {
        Pageable limit = PageRequest.of(0, 100);
        List<Notification> notifications;
        int unread = 0;

        int targetUserId =  userMapper.selectByUsername(username).getId();
        if (status != null && unread == status) {
            notifications = notificationRepo.findTop100UnreadByTargetUserId(targetUserId, limit);
        } else {
            notifications = notificationRepo.findTop100ByTargetUserId(targetUserId, limit);
        }
        List<MailVO> mails = new ArrayList<>();
        for (Notification notification: notifications) {
            String sourceUsername = userMapper.selectById(notification.getSourceUserId()).getUsername();
            mails.add(MailVO.builder()
                            .id(notification.getId())
                            .title("交互提醒")
                            .content(sourceUsername + " " +  notification.getInteraction() + " 了你的仓库！继续加油！")
                            .createTime(notification.getCreatedAt())
                            .hasRead(notification.getStatus() == READ)
                    .build());
        }
        return mails;
    }

    @Override
    public void read(String username, Long id) {
        int targetUserId = userMapper.selectByUsername(username).getId();
        Notification notification = notificationRepo.findUserNotificationByIdAndTargetUserId(id, targetUserId);
        if (notification != null) {
            notification.setStatus(READ);
            notificationRepo.save(notification);
        }
    }

    @Override
    public void unread(String username, Long id) {
        int targetUserId = userMapper.selectByUsername(username).getId();
        Notification notification = notificationRepo.findUserNotificationByIdAndTargetUserId(id, targetUserId);
        if (notification != null) {
            notification.setStatus(UNREAD);
            notificationRepo.save(notification);
        }
    }

    @Override
    public void delete(String username, Long id) {
        int targetUserId = userMapper.selectByUsername(username).getId();
        Notification notification = notificationRepo.findUserNotificationByIdAndTargetUserId(id, targetUserId);
        if (notification != null) {
            notificationRepo.delete(notification);
        }
    }
}
