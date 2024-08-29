package com.xuecheng.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Author daydream
 * Description
 * Date 2024/8/16
 */
@Controller
public class FreemarkerController {
    @GetMapping("/testfreemarker")
    public ModelAndView test() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "wjh");
        modelAndView.setViewName("test");
        return modelAndView;
    }
}
