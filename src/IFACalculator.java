
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class IFACalculator {
	static HashMap<Line, Integer> sorted_buggy_lines = new HashMap<Line, Integer>();
	
	public static HashMap<Line, Integer> sortLinesByBuggyTokens(HashMap<Line, Integer> hm) {
 		//returns all entries not just top 10
 		List<Map.Entry<Line, Integer> > list =
 			new LinkedList<Map.Entry<Line, Integer> >(hm.entrySet());
 	
 		Collections.sort(list, new Comparator<Map.Entry<Line, Integer> >() {
 			public int compare(Map.Entry<Line, Integer> o1,
 							 Map.Entry<Line, Integer> o2)
 			{
 				
 				return ((o2.getValue())).compareTo((o1.getValue()));
 			}
 		});
 		
 		// put data from sorted list to hashmap
 		HashMap<Line, Integer> temp = new LinkedHashMap<Line, Integer>();
 		
 		for (Map.Entry<Line, Integer> ent : list) {
 			if ( !temp.containsKey(ent.getKey()) ) {
 				
 				temp.put(ent.getKey(), ent.getValue());
 				
 			}
 		}
 		return temp;
 	}

	
	public void printMap(HashMap map) {
		   if(!map.isEmpty()) {
	         Iterator it = map.entrySet().iterator();
	         while(it.hasNext()) {
	             Map.Entry obj = (Map.Entry)it.next();
	             Line ckey=(Line)obj.getKey();
	             System.out.println(ckey.getLine_text()+"  "+obj.getValue() +" "+ckey.getLabel());
	         }
	     }
	 }

	public static int find_first_buggy(HashMap<Line, Integer> map) {
		sorted_buggy_lines = sortLinesByBuggyTokens(map); 

		//printMap(sorted_buggy_lines);
		//System.out.println("after sorting:"+sorted_buggy_lines.size());
		
		int loc = -1; int IFA=-1;
		if(!sorted_buggy_lines.isEmpty()) {
            Iterator it = sorted_buggy_lines.entrySet().iterator();
            
            while(it.hasNext()) {
            	
                Map.Entry obj = (Map.Entry)it.next();
                Line ckey = (Line)obj.getKey();
                System.out.println(ckey.getLine_text()+" "+ckey.getLabel()+"  "+obj.getValue());
                loc++;
                if (ckey.getLabel().charAt(0)=='1') {

                	if (IFA==-1) {
                		IFA=loc;
                		return IFA;
                	}
                	
                }
            }
        }

		return -1;
	}
	

}
