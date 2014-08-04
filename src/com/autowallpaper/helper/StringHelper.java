package com.autowallpaper.helper;

import java.util.ArrayList;
import java.util.Collection;

public class StringHelper {

	public static String formatSubredditList(Collection<String> valuesCollection) {
		ArrayList<String> values = new ArrayList<String>();
		values.addAll(valuesCollection);
		
		if (values.size() == 0) return "";
		
    	if (values.size() == 1)
    		for (String s : values)
    			return "r/" + s;
    	
    	if (values.size() == 2)
    		return "r/" + values.get(0) + " and " + "r/" + values.get(1);
    	
    	String line = "";
    	for (int i = 0; i < values.size() - 1; i ++)
    		line += "r/" + values.get(i) + ", ";
    	line += " and " + "r/" + values.get(values.size() - 1);
    	
    	return line;
	}
	
}
