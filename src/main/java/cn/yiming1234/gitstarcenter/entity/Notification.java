package cn.yiming1234.gitstarcenter.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer sourceUserId;
    private  Integer targetUserId;
    private  String Interaction;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
