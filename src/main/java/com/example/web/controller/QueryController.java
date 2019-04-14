package com.example.web.controller;

import com.example.web.service.QueryService;
import com.example.web.service.ReedService;
import com.example.web.service.UWMService;
import com.example.web.service.WSUService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class QueryController {

	@Autowired
	private QueryService queryService;

	@Autowired
	private WSUService wsuService;

	@Autowired
	private UWMService uwmService;

	@Autowired
	private ReedService reedService;

	@PostMapping("/query")
	public String query(@RequestParam(value = "courseId") String courseId,
						@RequestParam(value = "title") String title,
						@RequestParam(value = "instructor") String instructor,
						@RequestParam(value = "subject") String subject,
						@RequestParam(value = "days") String days,
						Model model) {
		System.out.println("====================================");
		System.out.println("courseId=" + courseId);
		System.out.println("title=" + title);
		System.out.println("instructor=" + instructor);
		System.out.println("subject=" + subject);
		System.out.println("days=" + days);

		if ("".equals(courseId.trim()) &&
				"".equals(title.trim()) &&
				"".equals(instructor.trim()) &&
				"".equals(subject) &&
				"".equals(days)) {
			System.out.println("NO Conditions!");
			model.addAttribute("resultWSU","please use at least one condition to search.");
			model.addAttribute("resultUWM","please use at least one condition to search.");
			model.addAttribute("resultReed","please use at least one condition to search.");
		}else{
			String resultWSU = wsuService.query(courseId,title,instructor,subject,days);
//			String resultWSU = queryService.query("1");
			String resultUWM = "";
			String resultReed = "";
			model.addAttribute("resultWSU", resultWSU);
			model.addAttribute("resultUWM", resultUWM);
			model.addAttribute("resultReed", resultReed);
			model.addAttribute("courseId", courseId);
			model.addAttribute("title", title);
			model.addAttribute("instructor", instructor);
			model.addAttribute("subject", subject);
			model.addAttribute("days", days);
		}
		return "query";
	}
}
