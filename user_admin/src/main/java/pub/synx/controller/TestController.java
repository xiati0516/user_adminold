package pub.synx.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author SynX TA
 * @version 2024
 **/
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    @PostMapping("/echo")
    public String echo(@RequestBody String str) {
        return str;
    }
}
