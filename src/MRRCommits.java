
import java.util.Collection;
import static org.junit.Assert.assertEquals;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

//import org.apache.commons.lang3.StringUtils;


//For an empty clone file query, I count the current AP as result 0
//Query file itself isn’t used for similarity.
public abstract class MRRCommits {
    String test_sexpr_dir, train_sexpr_dir, test_data_csv, train_data_csv, index_name, project, path, line_level_csv;
    static int no_match = 0;
    ESQuery es_query;
    ArrayList<Float> topk_acc=new ArrayList<Float>();
    ArrayList <Integer> IFA_list=new ArrayList<Integer>();
    int topK1; int topK5;
    int nc; int nclc; int nclb;
    int nb; int nblc; int nblb;
    int nbuggy; int nclean;
    int ESnull; int ES_NoRes;
    int successIndex;static int nosexpr;
    GitCommitsChanges git_commits;
    static ArrayList<String> correctly_predicted_commit = new ArrayList<String>();
    //ArrayList<AUC> auc_data  = new ArrayList<AUC>();
    HashMap<String, Pair > auc_data  = new HashMap<String, Pair>();
    
    HashMap<String,Integer> total_commits = new HashMap<String,Integer>();
    
    public MRRCommits(String index, String proj, String test_data_csv,  String train_data_csv, String p, String line_level_csv) {
	    this.line_level_csv = line_level_csv;
	    ESnull=0;
	    path=p; nosexpr=0;
	    topK1=0;topK5=0;
	    es_query = new ESQuery(index,path);
	    this.test_data_csv = test_data_csv;
	    this.train_data_csv = train_data_csv;
	    this.project=proj;
	    this.index_name= index;
	    git_commits = new GitCommitsChanges(this.test_data_csv,this.train_data_csv);
	    
	    nclean=0; nbuggy=0;
	  	System.out.println("train szz_commits total:"+git_commits.train_commits.size());
	    System.out.println("test sszz_commits total:"+git_commits.test_commits.size());
   }
    
   
   public void printMap(HashMap map) {
	   if(!map.isEmpty()) {
            Iterator it = map.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry obj = (Map.Entry)it.next();
                Line ckey=(Line)obj.getKey();
                System.out.println(ckey.getLine_text()+"  "+obj.getValue());
            }
        }
    }
   
   
   public static Map<Line,Integer> sortLinesByBuggyTokens(Map<Line,Integer> map) {
       // TreeMap to store values of HashMap
       TreeMap<Line,Integer> sorted = new TreeMap<>(Collections.reverseOrder());

       // Copy all data from hashMap into TreeMap
       sorted.putAll(map);
       
       // Display the TreeMap which is naturally sorted
       LinkedHashMap<Line,Integer> result = new LinkedHashMap<Line,Integer>();
       int count=0;
       for (Entry<Line,Integer> entry : sorted.entrySet()) {
           if (count < 10) {
         	  result.put(entry.getKey(), entry.getValue());
           	  count++;
           }
       }
       System.out.println("map has "+result.size());
       return result;
   }
   

   public ArrayList<Line> get_lines_from_ground_truth(String commit) {
	  ArrayList <Line> commit_lines = new ArrayList<Line>();
       String line = "";
       String splitBy = ",";
      
       try  {
           BufferedReader br = new BufferedReader(new FileReader(this.line_level_csv));
           String header = br.readLine();
           while ((line = br.readLine()) != null)  {
        	   String[] temp = line.split(splitBy);
               if (temp.length==3 && temp[0].equals(commit)) { 
               	
            	   Line oneline = new Line(temp[0],temp[1].strip(),temp[2]);
            	
            	   if (!commit_lines.contains(oneline))
            		   commit_lines.add(oneline);
            	   		//  System.out.println(oneline.getCommit_id()+' '+oneline.getLabel()+' '+oneline.getLine_text());
              }
           } br.close();
       } catch (IOException e)  {
           e.printStackTrace();
       } 
       
       return commit_lines;
   }

    
    public HashMap<String, Float> identity_ranker(String queryFile, HashMap<String,Float> matching_files, int expected) {
        HashMap<String, Float> similarity_score = new HashMap<String, Float>();
        ArrayList<String> comlist=new ArrayList<String>();
        List<String> matching_files_keys = new ArrayList<String>(matching_files.keySet());
        ArrayList<Float> matching_files_values = new ArrayList<Float>(matching_files.values());
        
        for (int i = 0; i < matching_files_keys.size(); i++)	{
            String file2 =  matching_files_keys.get(i);
            String commit2 = file2.substring(0, file2.indexOf("_"));
            comlist.add(commit2);
            if (!file2.equals(queryFile)) {
            	 similarity_score.put(commit2,matching_files_values.get(i));
            }  
        }  return similarity_score;
    }
       

    public int run_query_Precision(String commit, String queryFile, HashMap<String,Float> matching_files, int expected) throws Exception{
        HashMap<String, Float> tk_similarity_score = new HashMap<String, Float>();
        
        tk_similarity_score = identity_ranker(queryFile, matching_files, expected);
        System.out.println("after rerank "+tk_similarity_score.size());
        tk_similarity_score = (HashMap<String,Float>) ESQuery.sortByValue(tk_similarity_score);
        return calReciprocalRank(commit, tk_similarity_score, expected);
    }

    
    public int getCommitsHavingSameStatusAsQuery(HashMap<String,Float> matching_files_from_index, int expected) {
    	int num=0;
    	int commits_having_same_status_as_query=0;
        List<String> matching_files_keys = new ArrayList<String>(matching_files_from_index.keySet());
        while(num < matching_files_from_index.size()) {
        	String fname = matching_files_keys.get(num);
        	String commit = fname.substring(0,fname.indexOf('_'));        	
        	if (expected == (int) git_commits.train_commits.get(commit) ) {
            	commits_having_same_status_as_query++;
            } num++;
        }
        return commits_having_same_status_as_query;
    }
    
  /*  public static Map<String, Integer> sortByValueInt(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String,Integer>>() {
            public int compare(Map.Entry<String, Integer> m1, Map.Entry<String, Integer> m2) {
                return (m2.getValue()).compareTo(m1.getValue());
            }
        });
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        int count=0;
        for (Map.Entry<String, Integer> entry : list) {
        	 if (count < 10) {
        		 result.put(entry.getKey(), entry.getValue());
        		 count++;
        	 }
        }  return result;
    }
    */
    
    public void predict_buggy_lines(String commit) {
    	int total_buggy_in_gt=0;
    	ArrayList<Line> gt_lines = new ArrayList<Line>();
        
    	HashMap<Line, Integer> buggy_lines_vs_tok_count = new HashMap<Line, Integer>();
    	int correctly_predicted_lines=0; int incorrectly_predicted_lines=0;
    	int no_of_tokens_in_line=0;
    	int predicted_buggy_in_topk=0;
    	
    	gt_lines = get_lines_from_ground_truth(commit);
    	System.out.println("gt lines size:"+gt_lines.size());
    	
    	for (Line line:gt_lines) {
    		Character linelabel = line.getLabel().charAt(0);
      		if (linelabel=='1')
      			total_buggy_in_gt++;
      	
    		String strline = line.getLine_text();
    		no_of_tokens_in_line=0;
    		for (String tok:es_query.tokens) {		
	    		
    			if (tok.length()>1 && strline.contains(tok)) 
	    			no_of_tokens_in_line++;				//a buggy token found
	    			//break;	
	    	}
    		/*if (no_of_tokens_in_line>=1) {		   //we labelled line as buggy
				if (linelabel=='1')
					correctly_predicted_lines++;
				else 
					incorrectly_predicted_lines++;
			}*/
    		buggy_lines_vs_tok_count.put(line,no_of_tokens_in_line);
    		
    	}
    	System.out.println("before sorting:" + buggy_lines_vs_tok_count.size());
    	//Find top 10 buggy lines?
		Map<Line, Integer> sorted_buggy_lines = sortLinesByBuggyTokens(buggy_lines_vs_tok_count);
		
		System.out.println("after sorting:"+sorted_buggy_lines.size());
		
		int loc = -1; int IFA=-1;
		if(!sorted_buggy_lines.isEmpty()) {
            Iterator it = sorted_buggy_lines.entrySet().iterator();
            
            while(it.hasNext()) {
            	
                Map.Entry obj = (Map.Entry)it.next();
                Line ckey = (Line)obj.getKey();
                System.out.println(ckey.getLine_text()+" "+ckey.getLabel()+"  "+obj.getValue());
                loc++;
                if (ckey.getLabel().charAt(0)=='1') {
                	predicted_buggy_in_topk++;
                	
                	if (IFA==-1)
                		IFA=loc;
                	
                }
            }
        }
		System.out.println("total_buggy_in_gt:"+total_buggy_in_gt);
		System.out.println("predicted_buggy_in_topk:"+predicted_buggy_in_topk);
		
		if (total_buggy_in_gt<10)
        	topk_acc.add((float)predicted_buggy_in_topk/total_buggy_in_gt);
        else
        	topk_acc.add((float)predicted_buggy_in_topk/10);
		
		if (IFA!=-1)
			IFA_list.add(IFA);
		else {
				IFACalculator.find_first_buggy(buggy_lines_vs_tok_count);
		}
			
    	System.out.println("No. of buggy lines in top-k:" + predicted_buggy_in_topk);
    	
    }
    
    // function to sort hashmap by values
 	public static HashMap<Line, Integer> sortLinesByBuggyTokens(HashMap<Line, Integer> hm) {
 		
 		List<Map.Entry<Line, Integer> > list =
 			new LinkedList<Map.Entry<Line, Integer> >(hm.entrySet());
 		// Sort the list
 		
 		Collections.sort(list, new Comparator<Map.Entry<Line, Integer> >() {
 			public int compare(Map.Entry<Line, Integer> o1,
 							 Map.Entry<Line, Integer> o2)
 			{
 				
 				return ((o2.getValue())).compareTo((o1.getValue()));
 			}
 		});
 		
 		// put data from sorted list to hashmap
 		HashMap<Line, Integer> temp = new LinkedHashMap<Line, Integer>();
 		int k=0;
 		for (Map.Entry<Line, Integer> ent : list) {
 			if ( !temp.containsKey(ent.getKey()) && k<10 ) {
 				
 				temp.put(ent.getKey(), ent.getValue());
 				k++;
 			}
 		}
 		return temp;
 	}



    public int calReciprocalRank (String commit, HashMap <String, Float> tk_sim_score, int expected) throws Exception{
        float ReciprocalRank = 0;
        int ret_status = 0;
        ArrayList <String> keys = new ArrayList<String> (tk_sim_score.keySet());
        if(expected==1) 
    		nbuggy++;
    	else 
    		nclean++;
        System.out.println("Keys (commits) size:"+keys.size()); 	//keys.size = commitsOlderThanQuery
        for (int i = 0; i < keys.size(); i++) {
            String ret_commit = (String) keys.get(i); 	//commit
            System.out.println(" Match: "+ret_commit);
            ret_status = git_commits.train_commits.get(ret_commit);
            System.out.println(" Match: "+ret_commit+" --- ret_status " + ret_status+" expected "+expected);
            
            if (expected==ret_status) {	
            	if(i==0) {
            		this.topK1++; 
            		if (expected==1) {
            			correctly_predicted_commit.add(commit);
            			predict_buggy_lines(commit);
            			nblb++;
            		} if (expected==0)  {
            			nclc++;
            		}
            	} if(i>0 && i<5) {
            		this.topK5++;
            		if (expected==1) 
            			nblb++;
            		if (expected==0) 
            			nclc++;
            	}
                ReciprocalRank = (float)1/(i+1);
              
                return ret_status; //ReciprocalRank
                
            } else if (expected!=ret_status) {
            	if (expected==0)  {
            		nclb++;
            	} else {
            		nblc++;
            	}	
            }
        }
        return ret_status; //ReciprocalRank
    }
    
    public void save_lists_to_file() throws IOException {
    	FileWriter fw = new FileWriter("/home/hareem/Downloads/NewDataset/topk_accuracy_"+project+".csv",true);
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < topk_acc.size(); i++) {
        	sb.append(correctly_predicted_commit.get(i));
        	sb.append(',');
        	sb.append(topk_acc.get(i));
        	sb.append('\n');
        	
        }
        fw.write(sb.toString());
        fw.close();
        
        fw = new FileWriter("/home/hareem/Downloads/NewDataset/IFA_"+project+".csv",true);
        sb = new StringBuilder();
        for (int i=0; i < IFA_list.size(); i++) {
        	sb.append(correctly_predicted_commit.get(i));
        	sb.append(',');
        	sb.append(IFA_list.get(i));
        	sb.append('\n');
        	
        }
        fw.write(sb.toString());
        fw.close();
    }

    public float meanReciprocalRank(String test_dir) throws InterruptedException, IOException {
        float ReciprocalRank = 0;
        this.successIndex=0; 		//count commits that have past commits to compare against
        float sum=0;
        File folder = new File(test_dir);
        File[] test_files = folder.listFiles();
      
        String queryFile;
        for (int i=0; i < test_files.length; i++) {
        	
        	System.out.println("====================================================");
        	queryFile = test_files[i].getName();
        	System.out.println("Query File: "+test_files[i]+" #"+i);
            try {        
            	String commithash = queryFile.substring(0,queryFile.indexOf('_'));
            	
            	
            	//System.out.println(commithash);
                int expectedLabel = git_commits.getTestCommits().get(commithash);
                
                HashMap<String,Float> ES_returned_files = es_query.find_matches(queryFile);
                System.out.println(es_query.tokens);		//buggy tokens from explain api
                if(ES_returned_files==null) {
                    System.out.println("ES returned null because json file not found");
                    ESnull++;
                    continue;
                }
                if(ES_returned_files.size()==0) {
                    // No matches
                	System.out.println("ES did not return any results!");
                	ES_NoRes++;
                    continue;
                }
                int predicted = run_query_Precision(commithash, queryFile, ES_returned_files, expectedLabel);
                if (total_commits.containsKey(commithash)) {
                	int val = total_commits.get(commithash);
                	if (val==0) { //only update if previous entry is 'not buggy' 
                		total_commits.put(commithash, predicted);
                		//AUC obj = new AUC(commithash, expectedLabel, predicted);	
                		// auc_data.add(obj); 
                		Pair p = new Pair(expectedLabel, predicted);
                		auc_data.put(commithash, p);
                	}
                	
                } else { //update if already not in total_commits 
                	 total_commits.put(commithash,predicted);  
                	// AUC obj = new AUC(commithash, expectedLabel, predicted); 	
                    // auc_data.add(obj); 
                 	Pair p = new Pair(expectedLabel, predicted);
            		auc_data.put(commithash, p);
                }
               
                //sum+=ReciprocalRank;
                this.successIndex++;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        float MRR = sum/this.successIndex;     //git_commits.szz_commits.size();
        System.out.println("===========================================");
        System.out.println("index: "+this.index_name);
        System.out.println("project: "+this.project);
        System.out.println("nclc:"+this.nclc+" nblb:"+this.nblb+" nclb:"+this.nclb+" nblc:"+this.nblc);
        
        int  a =  (topk_acc.size())/2;
        int  b =  (topk_acc.size()/2 - 1);
        System.out.println("Size of acc list: "+ topk_acc.size()+" Middle:"+ a +" "+b);
        Collections.sort(topk_acc);
        System.out.println(topk_acc.get(a)+" and "+topk_acc.get(b));
        
        float acc = (topk_acc.get(a) + topk_acc.get(b))/2;
        System.out.println("Median line_level top-K accuracy:"+ acc);
        save_lists_to_file();      
        return MRR;  
    }
}
