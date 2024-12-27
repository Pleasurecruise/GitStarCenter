package cn.yiming1234.gitstarcenter.repo;

import cn.yiming1234.gitstarcenter.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepo extends JpaRepository<Notification, Long> {
    /**
     * 统计用户未读通知数量
     */
    Long countByTargetUserIdAndStatus(Integer targetUserId, Integer status);

    /**
     * 查询用户未读通知
     */
    @Query("SELECT u from Notification u where u.targetUserId = :targetUserId and u.status = 0 ORDER BY u.id DESC")
    List<Notification> findTop100UnreadByTargetUserId(@Param("targetUserId") Integer targetUserId, Pageable pageable);

    /**
     * 查询用户通知
     */
    @Query("SELECT u from Notification u where u.targetUserId = :targetUserId ORDER BY u.id DESC")
    List<Notification> findTop100ByTargetUserId(@Param("targetUserId") Integer targetUserId, Pageable pageable);

    /**
     * 查询用户通知
     */
    Notification findUserNotificationByIdAndTargetUserId(Long id, Integer targetUserId);
}
