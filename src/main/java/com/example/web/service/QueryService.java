package com.example.web.service;

import com.saxonica.xqj.SaxonXQDataSource;
import org.springframework.stereotype.Service;

import javax.xml.xquery.*;

@Service
public class QueryService {

	public String query(String category) {
		String query = "for $books in doc(\"classpath:books.xml\")/bookstore " +
				"return $books";
		StringBuffer sb = new StringBuffer();

		try {
			XQDataSource ds = new SaxonXQDataSource();
			XQConnection conn = ds.getConnection();
			XQPreparedExpression exp = conn.prepareExpression(query);
			XQResultSequence result = exp.executeQuery();

			while (result.next()) {
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
