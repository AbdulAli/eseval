
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
// bugzilla, postgres
// openstack, qt (change read_csv in GitCommitsChanges)
// might not work for TDD because of return types
// if one patch file is buggy entire commit is buggy
public class MRRCommitsTest extends MRRCommits{
 static float MRR;
 static long elapsedTime;
 static String project="qt";
 static String index="qt_index_camel_case_lowercase"; 
 
 public MRRCommitsTest( String project, String index, String test_commits_csv, String train_commits_csv, String jsonpath, String line_level_csv) {
	 super( project, index, test_commits_csv, train_commits_csv, jsonpath, line_level_csv);
	 
 }

 public void saveResult() throws IOException {
	 System.out.println(this.nc+"--"+this.nb+" ESnull:"+this.ESnull+" Es_no_res:"+this.ES_NoRes);
	 System.out.println("nblc:"+this.nblc+" nclb:"+nclb);
	 ConfusionMatrix cm = new ConfusionMatrix(this.nclean,this.nbuggy,this.nblb,this.nclc,(this.nclean-this.nclc),(this.nbuggy-this.nblb)); 
     
	 float topk1 = (float)this.topK1/this.successIndex;
     float topk5 = (float)this.topK5/this.successIndex;
     float fmclean = cm.FScoreClean();
     float fmbuggy = cm.FScoreBuggy();
     float fscore = cm.FScore();
     float accuracy = cm.calAccuracy();
     float precision = cm.calPrec();
     float prec_clean = cm.calPrecClean();
     float prec_buggy = cm.calPrecBuggy();
    
     float recall=cm.calRecall(); 
     float recall_clean=cm.calRecallClean();
     float recall_buggy=cm.calRecallBuggy();
     
     float timeinsec= elapsedTime/(1000000000);
     System.out.println("Top-K@1 = " +topk1);
     System.out.println("Top-K@5 = " +topk5);
     System.out.println("F-measure clean=" + fmclean);
     System.out.println("F-measure buggy=" + fmbuggy);
 	 System.out.println("F-measure overall="+ fscore);
 	 System.out.println("Accuracy="+accuracy);
 	 
 	 System.out.print("Precision="+precision);
 	 System.out.print("Precision clean="+prec_clean);
 	 System.out.println("Precision buggy="+prec_buggy);

 	 System.out.print(" Recall="+recall);
 	 System.out.print(" Recall clean="+recall_clean);
 	 System.out.println(" Recall buggy="+recall_buggy);

     System.out.println(" MRR = " + MRR);
     System.out.println("Elapsed time in sec " + timeinsec);
      
     FileWriter fw = new FileWriter("/home/hareem/0-Working/ICSME-Revision/openstack-qt-results/results_v3.csv",true);
     
    // FileWriter fw = new FileWriter("/home/hareem/Downloads/NewDataset/ES_results.csv",true);
     StringBuilder sb = new StringBuilder();
     sb.append(project);
     sb.append(',');
     sb.append(topk1);
     sb.append(',');
     sb.append(topk5);
     sb.append(',');
     sb.append(fmclean);
     sb.append(',');
     sb.append(fmbuggy);
     sb.append(',');
     sb.append(MRR);
     sb.append(',');
     sb.append(timeinsec);
     sb.append(',');
     sb.append(accuracy);
     sb.append(',');
     sb.append(fscore);
     sb.append(',');
     sb.append(this.nc);	//total clean in train.csv+test.csv
     sb.append(',');
     sb.append(this.nb);	//total buggy in train.csv+test.csv
     sb.append(',');
     sb.append(this.ESnull);
     sb.append(',');
     sb.append(this.ES_NoRes);
     sb.append(',');
     sb.append(this.nclean);	//test data - files
     sb.append(',');
     sb.append(this.nbuggy);	//test data
     sb.append(',');
     sb.append(this.nclc);
     sb.append(',');
     sb.append(this.nblb);
     sb.append(',');
     sb.append(this.nclean-this.nclc);
     sb.append(',');
     sb.append(this.nbuggy-this.nblb);
     sb.append(',');
     sb.append(precision);
     sb.append(',');
     sb.append(prec_clean);
     sb.append(',');
     sb.append(prec_buggy);
     sb.append(',');
     sb.append(recall);
     sb.append(',');
     sb.append(recall_clean);
     sb.append(',');
     sb.append(recall_buggy);
     sb.append(',');
     sb.append(index);
     sb.append('\n');
     fw.write(sb.toString());
     fw.close();
    
 }
 
 public void saveAUCData() throws IOException {
	 FileWriter fw = new FileWriter("/home/hareem/0-Working/ICSME-Revision/openstack-qt-results/AUC_qt_K="+ESQuery.docs_needed+".csv");
     StringBuilder sb = new StringBuilder();
     System.out.println("AUC list size:"+auc_data.size());
     sb.append("project");
     sb.append(',');
     sb.append("commit");
     sb.append(',');
     sb.append("expected");
     sb.append(',');
     sb.append("predicted");
     sb.append(',');
     sb.append('\n');
     int count=0;
     for (Entry<String, Pair> obj:auc_data.entrySet()) {
    	 count++;
    	 sb.append(project);
         sb.append(',');
         sb.append(obj.getKey());
         sb.append(',');
         sb.append(obj.getValue().expected);
         sb.append(',');
         sb.append(obj.getValue().predicted);
         sb.append(',');
         sb.append('\n');
     }
     System.out.println(count);
     fw.write(sb.toString());
     fw.close();
 }

 
 public static void main(String[] args) {
    try {
    	 
         /*String test_data_csv = "/home/hareem/Downloads/NewDataset/model_execution/JITLine_replication_package/JITLine/data/bugzilla_test.csv";
         String train_data_csv  = "/home/hareem/Downloads/NewDataset/model_execution/JITLine_replication_package/JITLine/data/bugzilla_train.csv";
         String testjsonfiles   = "/home/hareem/Downloads/NewDataset/jit/bugzilla_test/jsonfiles_of_diffs/";
         */
        
    	 // change read_csv in GitCommitsChanges
    	 String test_data_csv = "/home/hareem/0-Working/ICSME-Revision/openstack-qt-data/qt_test.csv";
     	 String train_data_csv = "/home/hareem/0-Working/ICSME-Revision/openstack-qt-data/qt_train.csv";
         String testjsonfiles   = "/home/hareem/0-Working/ICSME-Revision/qt_test/alljsonfiles_of_diffs/";
		 String line_level_csv  = "/home/hareem/0-Working/ICSME-Revision/openstack-qt-data/qt_complete_buggy_line_level_modified.csv";
         
         MRRCommitsTest test = new MRRCommitsTest(index, project, test_data_csv, train_data_csv, testjsonfiles, line_level_csv);
         long startTime = System.nanoTime();
         
         MRR = test.meanReciprocalRank(testjsonfiles);
         System.out.println("MRR "+MRR);
         elapsedTime = System.nanoTime() - startTime;
         test.saveResult();
         test.saveAUCData();
         test.es_query.client.close();
         System.out.println("ES docs:"+test.es_query.docs_needed);
         System.out.println("correctly_predicted_commit:"+correctly_predicted_commit.size());
         PrintWriter writer = new PrintWriter("/home/hareem/Downloads/NewDataset/"+project+"_commits_correctly_predicted_concatenated_newmodified.txt", "UTF-8");
         for (String str:correctly_predicted_commit) {
        	 writer.println(str);
         }
         writer.close();

         FileWriter fw = new FileWriter("/home/hareem/0-Working/ICSME-Revision/openstack-qt-results/buggy_tokens_"+project+".csv");
 		 StringBuilder sb = new StringBuilder();
     	 for (Map.Entry mapElement : test.es_query.buggytokens.entrySet()) {
     		   String key_queryfile = (String)mapElement.getKey();
     		   
     		   ArrayList<String> tokens_values = (ArrayList<String>) mapElement.getValue();
     		   String [] parts = key_queryfile.split("_");
     		   String commitId = parts[0];
     		   //if (correctly_predicted_commit.contains(commitId)) 
     		   { 	
     			   sb.append(key_queryfile );
     			   sb.append(',');	         
     			   sb.append(tokens_values);
     			   sb.append('\n');
     		   	 }
     	       }
     		   fw.write(sb.toString());
     		   fw.close();
     	   
     } catch (Exception e) {
         e.printStackTrace();
     }
 }
 // prec, rec, f1, auc, FAR, dist_heaven, recall_20_percent_effort, effort_at_20_percent_LOC_recall,p_opt
 }
