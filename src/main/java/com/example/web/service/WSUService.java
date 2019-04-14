package com.example.web.service;

import com.saxonica.xqj.SaxonXQDataSource;
import org.springframework.stereotype.Service;

import javax.xml.xquery.*;

@Service
public class WSUService {

	public String query(String courseId, String title, String instructor, String subject, String days) {
		int validConditionNumbers = 0;
		StringBuffer query = new StringBuffer("for $course in doc(\"classpath:wsu.xml\")/root/* where ");
		if (!"".equals(courseId.trim())) {
			query = addCondition(validConditionNumbers, "sln", courseId, query);
			validConditionNumbers++;
		}
		if (!"".equals(title.trim())) {
			query = addCondition(validConditionNumbers, "title", title, query);
			validConditionNumbers++;
		}
		if (!"".equals(instructor.trim())) {
			query = addCondition(validConditionNumbers, "title", instructor, query);
			validConditionNumbers++;
		}
		if (!"".equals(subject)) {
			query = addCondition(validConditionNumbers, "prefix", subject, query);
			validConditionNumbers++;
		}
		if (!"".equals(days)) {
			if (days.equals("T"))
				days = "TU";
			else if (days.equals("R"))
				days = "TH";
			query = addCondition(validConditionNumbers, "days", days, query);
			validConditionNumbers++;
		}
		query.append("order by $course/sln return $course ");
		System.out.println("XQuery:");
		System.out.println(query.toString());
		StringBuffer result = new StringBuffer();
		try {
			XQDataSource dataSource = new SaxonXQDataSource();
			XQConnection connection = dataSource.getConnection();
			XQPreparedExpression expression = connection.prepareExpression(query.toString());
			XQResultSequence resultSequence = expression.executeQuery();
			while (resultSequence.next()) {
				String temp = resultSequence.getItemAsString(null);
				result.append(temp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	private StringBuffer addCondition(int number, String condition, String value, StringBuffer query) {
		if (number == 0)
			query.append("contains($course/" + condition + " ,'" + value + "') ");
		else
			query.append("and contains($course/" + condition + " ,'" + value + "') ");
		return query;
	}
}
