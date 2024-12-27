package cn.yiming1234.gitstarcenter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "monitor_visitor")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class VisitorRecordDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userUUID;

    @Column(nullable = false)
    private LocalDate visitDate;

    public VisitorRecordDO(String userUUID, LocalDate today) {
        this.userUUID = userUUID;
        this.visitDate = today;
    }
}
