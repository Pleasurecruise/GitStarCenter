package cn.yiming1234.gitstarcenter.service;

import cn.yiming1234.gitstarcenter.vo.MailVO;

import java.util.List;

public interface MailService {
    List<MailVO> getMails(String username, Integer status);
    void read(String name, Long id);
    void unread(String username, Long id);
    void delete(String name, Long id);

}
