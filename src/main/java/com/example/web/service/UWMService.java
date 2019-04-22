package com.example.web.service;

import com.saxonica.xqj.SaxonXQDataSource;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

import javax.validation.constraints.Null;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import java.util.ArrayList;
import java.util.List;

@Service
public class UWMService {

	public String query(String courseId, String title, String instructor, String days,
						String original, String sort) {
		int validConditionNumbers = 0;
		StringBuffer query = new StringBuffer("for $course in doc(\"classpath:uwm.xml\")/root/* " +
				"where ");
		StringBuffer count = new StringBuffer("for $courses in doc(\"classpath:uwm.xml\")/root " +
				"let $count := count($courses/course_listing[");

		if (!"".equals(courseId.trim())) {
			query = addCondition(validConditionNumbers, "course", courseId, query);
			count = addCountCondition(validConditionNumbers, "course", courseId, count);
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
		if (!"".equals(days)) {
			query = addCondition(validConditionNumbers, "days", days, query);
			count = addCountCondition(validConditionNumbers, "days", days, count);
			validConditionNumbers++;
		}

		query = addTail(query, sort);
//		query.append("order by $course/course return $course ");
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
					String tempCourseId = "";
					String tempTitle = "";
					String tempCredit = "";
					String tempLevel = "";
					List<String> dayList = new ArrayList<>();
					List<String> hourList = new ArrayList<>();
					List<String> placeList = new ArrayList<>();
					List<String> instructorList = new ArrayList<>();

					for (int i = 0; i < nodeList.getLength(); i++) {
						if (nodeList.item(i).getNodeName().equals("course"))
							tempCourseId = nodeList.item(i).getTextContent();
						else if (nodeList.item(i).getNodeName().equals("title"))
							tempTitle = nodeList.item(i).getTextContent();
						else if (nodeList.item(i).getNodeName().equals("credits"))
							tempCredit = nodeList.item(i).getTextContent();
						else if (nodeList.item(i).getNodeName().equals("level"))
							tempLevel = nodeList.item(i).getTextContent();
						else if (nodeList.item(i).getNodeName().equals("section_listing")) {
							NodeList section = nodeList.item(i).getChildNodes();
							dayList.add(pickDays(section));
							hourList.add(pickHours(section));
							placeList.add(pickPlace(section));
							instructorList.add(pickInstructor(section));
						}
					}

					result.append("***************Course ID: " + tempCourseId + "***************\n");
					result.append("Title: " + tempTitle + "\n");
					result.append("Credit: " + tempCredit + "\n");
					result.append("Level: " + tempLevel + "\n");
					for (int i = 0; i < dayList.size(); i++) {
						int sectionNumber = i + 1;
						result.append("Section " + sectionNumber + ":\n");
						result.append("Days: " + dayList.get(i) + "\n");
						result.append("Hours: " + hourList.get(i) + "\n");
						result.append("Place: " + placeList.get(i) + "\n");
						result.append("Instructor: " + instructorList.get(i) + "\n");
					}
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
			query.append("order by $course/course return $course ");
		else if (sort.equals("title"))
			query.append("order by $course/title return $course ");
		else if (sort.equals("credit"))
			query.append("order by $course/credits return $course ");
		return query;
	}

	private StringBuffer addCondition(int number, String condition, String value, StringBuffer query) {
		if (condition.equals("days")) {
			if (number == 0)
				query.append("$course//" + condition + "[contains(.,'" + value + "')] ");
			else
				query.append("and $course//" + condition + "[contains(.,'" + value + "')] ");
		} else if (condition.equals("instructor")) {
			if (number == 0)
				query.append("$course//" + condition + "[contains(lower-case(.),'" + value + "')] ");
			else
				query.append("and $course//" + condition + "[contains(lower-case(.),'" + value + "')] ");
		} else if (condition.equals("title")) {
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
		if (condition.equals("days")) {
			if (number == 0)
				count.append(".//" + condition + "[contains(.,'" + value + "')] ");
			else
				count.append("and .//" + condition + "[contains(.,'" + value + "')] ");
		} else if (condition.equals("instructor")) {
			if (number == 0)
				count.append(".//" + condition + "[contains(lower-case(.),'" + value + "')] ");
			else
				count.append("and .//" + condition + "[contains(lower-case(.),'" + value + "')] ");
		} else if (condition.equals("title")) {
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

	private String pickDays(NodeList list) {
		String days = "";
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals("days")) {
				days = list.item(i).getTextContent();
				break;
			}
		}
		return days;
	}

	private String pickInstructor(NodeList list) {
		String instructor = "";
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals("instructor")) {
				instructor = list.item(i).getTextContent();
				break;
			}
		}
		return instructor;
	}

	private String pickHours(NodeList list) {
		String start = "";
		String end = "";
		NodeList hours = null;
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals("hours")) {
				hours = list.item(i).getChildNodes();
				break;
			}
		}
		for (int i = 0; i < hours.getLength(); i++) {
			if (hours.item(i).getNodeName().equals("start"))
				start = hours.item(i).getTextContent();
			else if (hours.item(i).getNodeName().equals("end"))
				end = hours.item(i).getTextContent();
		}
		return start + "--" + end;
	}

	private String pickPlace(NodeList list) {
		String building = "";
		String room = "";
		NodeList place = null;
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals("bldg_and_rm")) {
				place = list.item(i).getChildNodes();
				break;
			}
		}
		for (int i = 0; i < place.getLength(); i++) {
			if (place.item(i).getNodeName().equals("bldg"))
				building = place.item(i).getTextContent();
			else if (place.item(i).getNodeName().equals("rm"))
				room = place.item(i).getTextContent();
		}
		return building + "--" + room;
	}
}