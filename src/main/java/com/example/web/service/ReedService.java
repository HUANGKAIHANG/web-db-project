package com.example.web.service;

import com.saxonica.xqj.SaxonXQDataSource;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

@Service
public class ReedService {

	public String query(String courseId, String title, String instructor, String subject, String days,
						String original, String sort) {
		int validConditionNumbers = 0;
		StringBuffer query = new StringBuffer("for $course in doc(\"classpath:reed.xml\")/root/* " +
				"where ");
		StringBuffer count = new StringBuffer("for $courses in doc(\"classpath:reed.xml\")/root " +
				"let $count := count($courses/course[");

		if (!"".equals(courseId.trim())) {
			query = addCondition(validConditionNumbers, "reg_num", courseId, query);
			count = addCountCondition(validConditionNumbers, "reg_num", courseId, count);
			validConditionNumbers++;
		}
		if (!"".equals(title.trim())) {
			query = addCondition(validConditionNumbers, "title", title, query);
			count = addCountCondition(validConditionNumbers, "title", title, count);
			validConditionNumbers++;
		}
		if (!"".equals(instructor.trim())) {
			query = addCondition(validConditionNumbers, "instructor", instructor, query);
			count = addCountCondition(validConditionNumbers, "instructor", instructor, count);
			validConditionNumbers++;
		}
		if (!"".equals(subject)) {
			query = addCondition(validConditionNumbers, "subj", subject, query);
			count = addCountCondition(validConditionNumbers, "subj", subject, count);
			validConditionNumbers++;
		}
		if (!"".equals(days)) {
			if (days.equals("R"))
				days = "Th";
			query = addCondition(validConditionNumbers, "days", days, query);
			count = addCountCondition(validConditionNumbers, "days", days, count);
			validConditionNumbers++;
		}

		query = addTail(query, sort);
//		query.append("order by $course/reg_num return $course ");
		count.append("]) return $count");

		System.out.println("XQuery:");
		System.out.println(query.toString());
		System.out.println("XCount:");
		System.out.println(count.toString());

		StringBuffer result = new StringBuffer();
		StringBuffer countResult = new StringBuffer();
		try {
			XQDataSource dataSource = new SaxonXQDataSource();
			XQConnection connection = dataSource.getConnection();

			XQPreparedExpression expression = connection.prepareExpression(query.toString());
			XQResultSequence resultSequence = expression.executeQuery();
			if (original.equals("1")) {
				while (resultSequence.next()) {
					String temp = resultSequence.getItemAsString(null);
					result.append(temp);
				}
			} else if (original.equals("0")) {
				while (resultSequence.next()) {
					NodeList nodeList = resultSequence.getNode().getChildNodes();
					String tempCourseId = nodeList.item(0).getTextContent();
					String tempSubject = nodeList.item(1).getTextContent();
					String tempTitle = nodeList.item(4).getTextContent();
					String tempCredit = nodeList.item(5).getTextContent();
					String tempDays = nodeList.item(7).getTextContent();
					String tempInstructor = nodeList.item(6).getTextContent();
					NodeList timesList = nodeList.item(8).getChildNodes();
					String tempStart = timesList.item(0).getTextContent();
					String tempEnd = timesList.item(1).getTextContent();
					NodeList placeList = nodeList.item(9).getChildNodes();
					String tempBuilding = placeList.item(0).getTextContent();
					String tempRoom = placeList.item(1).getTextContent();

					result.append("***************Course ID: " + tempCourseId + "***************\n");
					result.append("Subject: " + tempSubject + "\n");
					result.append("Title: " + tempTitle + "\n");
					result.append("Credit: " + tempCredit + "\n");
					result.append("Days: " + tempDays + "\n");
					result.append("Instructor: " + tempInstructor + "\n");
					result.append("Time: " + tempStart + "--" + tempEnd + "\n");
					result.append("Place: " + tempBuilding + "--" + tempRoom + "\n");
				}
			}

			XQPreparedExpression countExpression = connection.prepareExpression(count.toString());
			XQResultSequence countResultSequence = countExpression.executeQuery();
			while (countResultSequence.next()) {
				String temp = countResultSequence.getItemAsString(null);
				countResult.append("Find about ");
				countResult.append(temp);
				countResult.append(" result(s) \n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return countResult.toString() + result.toString();
	}

	private StringBuffer addTail(StringBuffer query, String sort) {
		if (sort.equals("courseId"))
			query.append("order by $course/reg_num return $course ");
		else if (sort.equals("title"))
			query.append("order by $course/title return $course ");
		else if (sort.equals("credit"))
			query.append("order by $course/units return $course ");
		return query;
	}

	private StringBuffer addCondition(int number, String condition, String value, StringBuffer query) {
		if (condition.equals("title") || condition.equals("instructor")) {
			if (number == 0)
				query.append("contains(lower-case($course/" + condition + ") ,'" + value + "') ");
			else
				query.append("and contains(lower-case($course/" + condition + ") ,'" + value + "') ");
		} else {
			if (number == 0)
				query.append("contains($course/" + condition + " ,'" + value + "') ");
			else
				query.append("and contains($course/" + condition + " ,'" + value + "') ");
		}
		return query;
	}

	private StringBuffer addCountCondition(int number, String condition, String value, StringBuffer count) {
		if (condition.equals("title") || condition.equals("instructor")) {
			if (number == 0)
				count.append("contains(lower-case(./" + condition + "),'" + value + "') ");
			else
				count.append("and contains(lower-case(./" + condition + "),'" + value + "') ");
		} else {
			if (number == 0)
				count.append("contains(./" + condition + ",'" + value + "') ");
			else
				count.append("and contains(./" + condition + ",'" + value + "') ");
		}
		return count;
	}
}
