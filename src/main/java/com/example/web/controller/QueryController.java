package com.example.web.controller;

import com.example.web.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class QueryController {

	@Autowired
	private QueryService queryService;

	@PostMapping("/query")
	public String query(@RequestParam(value = "category")String category, Model model){
		System.out.println(category);
		String result = queryService.query(category);
		model.addAttribute("result",result);
		return "query";
	}
}
