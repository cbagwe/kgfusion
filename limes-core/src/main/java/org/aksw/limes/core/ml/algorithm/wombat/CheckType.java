package org.aksw.limes.core.ml.algorithm.wombat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

/**
 * This class is use to classify the data type. It check whether the value is String, Number
 * point or a date.
 *
 * @author Khalid Bin Huda Siddiqui (khalids@campus.uni-paderborn.de)
 * @author Khalid Khan (kkhan@campus.uni-paderborn.de)
 * 
 */
public class CheckType {

/**
 * Method to check if the value is String
 * @param input String value
 * @return Data type of a value
 */
	public static String check(String input) {
		// Strip Leading and Trailing Spaces
		String trimInput = input.trim();

		// Check if its a date. eg 2015-05-20T08
		if (checkDate(trimInput) == "date") {
			return "date";
		}
		// Check if its a point. eg (24,55)
		if (checkPoint(trimInput) == "point") {
			return "point";
		}
		// Check if its a number. eg 33 or 33.4
		if (checkNumber(trimInput) == "int" || checkNumber(trimInput) == "double") {
			return "number";
		}
		return "string";
	}
/**
 * Method to check if the value is numeric
 * 
 * @param str the value
 * @return true if the value is numeric
 */
	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

/**
 * Method to check if the value is a String
 * @param str the value
 * @return "int" if the value in integer
 */
	private static String checkNumber(String i) {
		 
		if (i.matches("-?\\d+")) {
			return "int";
		}
		if (isNumeric(i)) {
			return "double";
		}
		return i;
	}

/**
 * Method to check if the value is a point
 * @param i the value
 * @return "point" if the value is a point
 */
	static String checkPoint(String i) {

		// remove all the spaces
		String i1 = i.replaceAll("\\s+", "");

		String firstCharater = i1.substring(0, 1);
		String lastCharater = i1.substring(i1.length() - 1);

		// Remove first and last string
		if(i1.length()>1){
		i1 = i1.substring(1, i1.length() - 1);}

		// Break the string with comma
		if (i1.contains(",")) {
			List<String> list = Arrays.asList(i1.split(","));

			// str.matches("^[+-]?\\d+$") for + sign infront of number
			if (list.get(0).matches("-?\\d+") && list.get(1).matches("-?\\d+")) {
				return "point";
			}

		}
		return "NotAPoint";
	}

/**
 * Method to check if the value is a date
 * @param string the value
 * @return "date" if the given value can be parse as a date 
 */
	public static String checkDate(String string) {

		String[] patterns = { "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ssXXX",
				"yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mmXXX", "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd", "yyyy-MM" };

		for (String pattern : patterns) {
			try {
				new SimpleDateFormat(pattern).parse(string);
				return "date";
			} catch (ParseException e) {
				//System.out.println("Exception occurred :" + e);
			}
		}
		return "notADate";
	}
}
