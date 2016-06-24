package ca.concordia.jtratch.utility;

import java.util.HashMap;
import java.util.Map;

public final class Dic {
	 public static <T> void MergeDic1(HashMap<T, Integer> dic1, HashMap<T, Integer> dic2)
     {
         for (Map.Entry<T, Integer> entry : dic2.entrySet())
         {
             if (entry.getKey() == null) continue;
             if (dic1.containsKey(entry.getKey()))
             {
            	 dic1.put(entry.getKey(), dic1.get(entry.getKey()) + entry.getValue());
             }
             else
             {
            	 dic1.put(entry.getKey(), entry.getValue());
             }
         }
     }

     public static <T1, T2> void MergeDic2( HashMap<T1, T2> dic1, HashMap<T1, T2> dic2)
     {
    	 for (Map.Entry<T1, T2> entry : dic2.entrySet())
         {
             if (entry.getKey() == null) continue;
             if (!dic1.containsKey(entry.getKey()))
             {
                 dic1.put(entry.getKey(), entry.getValue());
             }
         }
     }
}
