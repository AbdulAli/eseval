
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class GitCommitsChanges {
	int nb;int nc;
    HashMap<String, Integer> test_commits = new HashMap<String, Integer>();
    HashMap<String, Integer> train_commits = new HashMap<String, Integer>();
    
    GitCommitsChanges(String groundtruth, String traindata) {
    	System.out.println("testdata/ground truth:"+groundtruth);
    	System.out.println("train data:"+traindata);
    	
    	nb=0;nc=0;
        test_commits  = read_csv(groundtruth);
        System.out.println("nc:"+nc+" nb:"+nb+" total:"+(nc+nb));
        train_commits = read_csv(traindata);
        System.out.println("nc:"+nc+" nb:"+nb);
      
     }

    public void printMap(HashMap map) {
        if(!map.isEmpty()) {
            Iterator it = map.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry obj = (Map.Entry)it.next();
                String ckey=(String)obj.getKey(); 
                System.out.println(ckey+"  "+obj.getValue());
            }
        }
    }
    
    public HashMap<String,Integer> read_csv(String groundtruth) {
    	//openstack and qt
    	System.out.print("READING!!");
        HashMap<String, Integer> git_commits = new HashMap<String, Integer>();
        String line = "";
        String splitBy = ",";
        try  {
            BufferedReader br = new BufferedReader(new FileReader(groundtruth));
            String header = br.readLine();
            while ((line = br.readLine()) != null)  {
                String[] temp = line.split(splitBy);
                git_commits.put(temp[0], Integer.parseInt(temp[1]));
                
                if(Integer.parseInt(temp[1])==1) {
                	nb++;            	
                } else {
                	nc++;	
                }	
            } br.close();
        } catch (IOException e)  {
            e.printStackTrace();
        } return git_commits;
    }

 
    /*
    public HashMap<String,Integer> read_csv(String groundtruth) {
    	System.out.print("READING!!");
        HashMap<String, Integer> git_commits = new HashMap<String, Integer>();
        String line = "";
        String splitBy = ",";
        try  {
            BufferedReader br = new BufferedReader(new FileReader(groundtruth));
            String header = br.readLine();
            while ((line = br.readLine()) != null)  {
                String[] temp = line.split(splitBy);
                // commit and label
                git_commits.put(temp[2], Integer.parseInt(temp[20])); 
                if(Integer.parseInt(temp[20])==1) {	        
                	nb++;            	
                } else {
                	nc++;	
                }	
            } br.close();
        } catch (IOException e)  {
            e.printStackTrace();
        } return git_commits;
    }*/

    public HashMap<String,Integer> getTestCommits() {
        return test_commits;
    }
    
    public HashMap<String,Integer> getTrainCommits() {
        return train_commits;
    }

    public int getTotalClean() {return nc;}
    public int getTotalBuggy() {return nb;}

   
}
