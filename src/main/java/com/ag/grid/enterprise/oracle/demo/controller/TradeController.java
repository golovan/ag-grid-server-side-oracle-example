package com.ag.grid.enterprise.oracle.demo.controller;

import com.ag.grid.enterprise.oracle.demo.request.EnterpriseGetRowsRequest;
import com.ag.grid.enterprise.oracle.demo.response.EnterpriseGetRowsResponse;
import com.ag.grid.enterprise.oracle.demo.dao.TradeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class TradeController {

    private TradeDao tradeDao;

    @Autowired
    public TradeController(@Qualifier("tradeDao") TradeDao tradeDao) {
        this.tradeDao = tradeDao;
    }

    @RequestMapping(method = POST, value = "/getRows")
    @ResponseBody
    public EnterpriseGetRowsResponse getRows(@RequestBody EnterpriseGetRowsRequest request) {
        return tradeDao.getData(request);
    }
}