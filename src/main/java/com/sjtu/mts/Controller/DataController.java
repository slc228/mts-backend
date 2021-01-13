package com.sjtu.mts.Controller;

import com.sjtu.mts.Entity.Data;
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

    @GetMapping("/search")
    @ResponseBody
    public List<Data> findByCflagAndPublishedDayAndResourse(
            @RequestParam("cflag") String cflag,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay,
            @RequestParam("fromType") String fromType
    ) {
        List<Data> result = searchService.Search(cflag, startPublishedDay, endPublishedDay, fromType);
        return result;
    }
}
