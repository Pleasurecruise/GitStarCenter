package cn.yiming1234.gitstarcenter.service.impl;

import cn.yiming1234.gitstarcenter.entity.VisitorRecordDO;
import cn.yiming1234.gitstarcenter.repo.VisitorRepo;
import cn.yiming1234.gitstarcenter.service.VisitorService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class VisitorServiceImpl implements VisitorService {
    private final VisitorRepo visitorRepo;

    public VisitorServiceImpl(VisitorRepo visitorRepo) {
        this.visitorRepo = visitorRepo;
    }

    @Override
    public void recordVisit(String userUUID) {
        LocalDate today = LocalDate.now();
        if (!visitorRepo.existsByUserUUIDAndVisitDate(userUUID, today)) {
            visitorRepo.save(new VisitorRecordDO(userUUID, today));
        }
    }

    @Override
    public Long countUniqueVisitors(LocalDate localDate) {
        return visitorRepo.countUniqueVisitors(localDate);
    }
}
