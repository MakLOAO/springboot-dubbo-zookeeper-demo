package com.mkl.providerticket.service;

import org.apache.dubbo.config.annotation.Service;
import org.springframework.stereotype.Component;

// 注意是dubbo包的service，它将服务发布出去
@Component
@Service(version = "1.2.3")
public class TicketServiceImpl implements TicketService {

    @Override
    public String getTicket() {
        return "ready to get a ticket...";
    }
}