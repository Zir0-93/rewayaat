package com.rewayaat.controllers;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * Handles requests for the home page.
 */
@Controller
public class HomeController {

    private static Logger log = Logger.getLogger(HomeController.class.getName());

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public final String home(@RequestParam(value = "q", required = false) String query,
                             @RequestParam(value = "page", defaultValue = "1") int page,
                             HttpServletRequest request, final Model model) {
        log.info("Entered home controller");
        if (query != null && !query.isEmpty()) {
            // returning the query as a model attribute allows the front end to
            // consume the rewayaat REST API using that query and display the
            // results.
            log.info("Query param is " + query + ", returning query value in model.");
            model.addAttribute("query", query);
            log.info("Page param is " + page + ", returning page value in model.");
            model.addAttribute("page", page);
        }
        return "index";
    }
}
