package com.mkl.consumeruser.service;

import com.mkl.providerticket.service.TicketService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Reference(version = "1.2.3")
    TicketService ticketService;

    public void hello() {
        System.out.println(ticketService.getTicket());
        System.out.println("already got the ticket!!!");
    }
}