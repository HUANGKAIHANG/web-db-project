package com.example.web.service;

import com.saxonica.xqj.SaxonXQDataSource;
import org.springframework.stereotype.Service;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

@Service
public class UWMService {

	public String query(String courseId, String title, String instructor, String days) {
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

		query.append("order by $course/course return $course ");
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
			while (resultSequence.next()) {
				String temp = resultSequence.getItemAsString(null);
				result.append(temp);
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
				count.append(".//" + condition + "[contains(.,'" + value + "')]");
			else
				count.append("and .//" + condition + "[contains(.,'" + value + "')]");
		} else if (condition.equals("instructor")) {
			if (number == 0)
				count.append(".//" + condition + "[contains(lower-case(.),'" + value + "')]");
			else
				count.append("and .//" + condition + "[contains(lower-case(.),'" + value + "')]");
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
}