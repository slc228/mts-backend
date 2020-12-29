package com.sjtu.mts.Controller;

import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.Repository.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    private DataRepository dataRepository;

    @GetMapping("/findByCflag/{cflag}")
    @ResponseBody
    public List<Data> findById(@PathVariable("cflag") int cflag) {
        List<Data> result = dataRepository.findByCflag(String.valueOf(cflag));
        return result;
    }
}
