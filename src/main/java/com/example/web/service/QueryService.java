package com.example.web.service;

import com.saxonica.xqj.SaxonXQDataSource;
import org.springframework.stereotype.Service;

import javax.xml.xquery.*;
import java.util.Properties;

@Service
public class QueryService {

	public String query(String category) {
		String query = "for $books in doc(\"classpath:wsu.xml\")/root " +
				"return $books";
		/*String query = "for $books in doc('classpath:books.xml')/bookstore " +
				"return $books";*/
		StringBuffer sb = new StringBuffer();

		try {
			XQDataSource ds = new SaxonXQDataSource();
			XQConnection conn = ds.getConnection();
			XQPreparedExpression exp = conn.prepareExpression(query);
			XQResultSequence result = exp.executeQuery();

			int i =1;
			while (result.next()) {
				System.out.println("轮次:"+i);
				i++;
//				String temp = result.getNode().getChildNodes().item(1).getPrefix();
//				String temp = result.getNode().getChildNodes().item(1)
//						.getChildNodes().item(1).getTextContent();
				String temp = result.getItemAsString(null);
				System.out.println(temp);
				sb.append(temp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sb.toString();
	}
}
