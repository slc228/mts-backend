package com.sjtu.mts.Controller;

import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.Response.AmountTrendResponse;
import com.sjtu.mts.Response.CflagCountResponse;
import com.sjtu.mts.Response.DataResponse;
import com.sjtu.mts.Repository.DataRepository;
import com.sjtu.mts.Response.ResourceCountResponse;
import com.sjtu.mts.Service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/data")
@CrossOrigin
public class DataController {

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private SearchService searchService;

    @GetMapping("/findByCflag/{cflag}")
    @ResponseBody
    public List<Data> findById(@PathVariable("cflag") int cflag) {
        return dataRepository.findByCflag(String.valueOf(cflag));
    }

    @GetMapping("/globalSearch/dataSearch")
    @ResponseBody
    public DataResponse findByKeywordAndCflagAndPublishedDayAndFromType(
            @RequestParam("keyword") String keyword,
            @RequestParam("cflag") String cflag,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay,
            @RequestParam("fromType") String fromType,
            @RequestParam("page") int page,
            @RequestParam("pageSize") int pageSize,
            @RequestParam("timeOrder") int timeOrder
    ) {
        return searchService.Search(keyword, cflag, startPublishedDay, endPublishedDay, fromType,
                page, pageSize, timeOrder);
    }

    @GetMapping("/globalSearch/resourceCount")
    @ResponseBody
    public ResourceCountResponse countByKeywordAndPublishedDayAndFromType(
            @RequestParam("keyword") String keyword,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay
    ) {
        return searchService.globalSearchResourceCount(keyword, startPublishedDay,
                endPublishedDay);
    }

    @GetMapping("/globalSearch/cflagCount")
    @ResponseBody
    public CflagCountResponse countByKeywordAndPublishedDayAndCflag(
            @RequestParam("keyword") String keyword,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay
    ) {
        return searchService.globalSearchCflagCount(keyword, startPublishedDay, endPublishedDay);
    }

    @GetMapping("/globalSearch/amountTrendCount")
    @ResponseBody
    public AmountTrendResponse countAmountTrendByKeywordAndPublishedDay(
            @RequestParam("keyword") String keyword,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay
    ) {
        return searchService.globalSearchTrendCount(keyword, startPublishedDay, endPublishedDay);
    }
}
