package cn.yiming1234.gitstarcenter.repo;

import cn.yiming1234.gitstarcenter.entity.VisitorRecordDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface VisitorRepo extends JpaRepository<VisitorRecordDO, Long> {
    boolean existsByUserUUIDAndVisitDate(String userUUID, LocalDate visitDate);

    @Query("SELECT COUNT(DISTINCT v.userUUID) FROM VisitorRecordDO v WHERE v.visitDate = :date")
    long countUniqueVisitors(@Param("date") LocalDate date);
}
