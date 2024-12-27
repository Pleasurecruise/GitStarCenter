package cn.yiming1234.gitstarcenter.service;

import java.time.LocalDate;

public interface VisitorService {
    void recordVisit(String userUUID);

    Long countUniqueVisitors(LocalDate localDate);
}
