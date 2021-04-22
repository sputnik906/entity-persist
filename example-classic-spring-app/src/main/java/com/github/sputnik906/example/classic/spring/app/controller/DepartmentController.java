package com.github.sputnik906.example.classic.spring.app.controller;

import com.github.sputnik906.example.classic.spring.app.dao.DepartmentService;
import com.turkraft.springfilter.FilterParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DepartmentController.PATH)
public class DepartmentController {

  public static final String PATH = "api/departments";

  private final DepartmentService service;

  @Autowired
  public DepartmentController(
    DepartmentService service) {
    this.service = service;
  }

  @RequestMapping(value = "/sumNominalLoad",method = RequestMethod.GET)
  public Double sumNominalLoad(
    @RequestParam(value = "search", required = false) String search
  ) {
    return service.sumNominalLoad(
      search!=null?FilterParser.parse(search.trim()):null
    );
  }

  @RequestMapping(value = "/maxNominalLoad",method = RequestMethod.GET)
  public Double maxNominalLoad(
    @RequestParam(value = "search", required = false) String search
  ) {
    return service.maxNominalLoad(
      search!=null?FilterParser.parse(search.trim()):null
    );
  }

  @RequestMapping(value = "/minNominalLoad",method = RequestMethod.GET)
  public Double minNominalLoad(
    @RequestParam(value = "search", required = false) String search
  ) {
    return service.minNominalLoad(
      search!=null?FilterParser.parse(search.trim()):null
    );
  }
}
