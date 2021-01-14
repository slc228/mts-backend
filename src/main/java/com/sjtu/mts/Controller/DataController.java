package com.sjtu.mts.Controller;

import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.Response.DataResponse;
import com.sjtu.mts.Repository.DataRepository;
import com.sjtu.mts.Service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private SearchService searchService;

    @GetMapping("/findByCflag/{cflag}")
    @ResponseBody
    public List<Data> findById(@PathVariable("cflag") int cflag) {
        List<Data> result = dataRepository.findByCflag(String.valueOf(cflag));
        return result;
    }

    @GetMapping("/globalSearch")
    @ResponseBody
    public DataResponse findByKeywordAndCflagAndPublishedDayAndResourse(
            @RequestParam("keyword") String keyword,
            @RequestParam("cflag") String cflag,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay,
            @RequestParam("fromType") String fromType,
            @RequestParam("page") int page,
            @RequestParam("pageSize") int pageSize,
            @RequestParam("timeOrder") int timeOrder
    ) {
        DataResponse result = searchService.Search(keyword, cflag, startPublishedDay, endPublishedDay, fromType,
                page, pageSize, timeOrder);
        return result;
    }
}
