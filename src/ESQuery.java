
//https://stackoverflow.com/questions/54578222/howto-create-a-bool-query-with-elasticsearch-java-resthighlevelclient-api
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpHost;
import org.apache.lucene.search.Explanation;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvectors.TermVectorsRequest;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ESQuery {
  public static int docs_needed;
  static String index_name;
  long totalHits;
  String src_lines_deleted;

  String path; int es_matches_size;
  RestHighLevelClient client;
  ArrayList<String> tokens = new ArrayList<String>();
  HashMap<String,ArrayList<String>> buggytokens = new HashMap<String,ArrayList<String>>();

  public ESQuery(String index, String p){
      docs_needed=5; 	//K=1 or K=5
      path = p;
      src_lines_deleted="";
      index_name = index;
      client = new RestHighLevelClient(RestClient.builder(
               new HttpHost("localhost", 9200, "http"),
               new HttpHost("localhost", 9201, "http")));
  	  }
  
  public static Map<String, Float> sortByValue(Map<String, Float> map) {
      List<Map.Entry<String, Float>> list = new LinkedList<Map.Entry<String, Float>>(map.entrySet());
      Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
          public int compare(Map.Entry<String, Float> m1, Map.Entry<String, Float> m2) {
              return (m2.getValue()).compareTo(m1.getValue());
          }
      });
      Map<String, Float> result = new LinkedHashMap<String, Float>();
      for (Map.Entry<String, Float> entry : list) {
          result.put(entry.getKey(), entry.getValue());
      }
      return result;
  }

  
  public String readJsonFile(String queryFile) {
      JSONParser jsonParser = new JSONParser();
      String code=null;
      try {
          //Parsing the contents of the JSON file
          JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(queryFile));
          code = (String) jsonObject.get("lines_added");
          src_lines_deleted = (String) jsonObject.get("lines_deleted");
      } catch (FileNotFoundException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      } catch (ParseException e) {
          e.printStackTrace();
      }
      return code;
  }

  public ArrayList<String> listFilesForFolder(final File folder) {
      ArrayList<String> fileList=new ArrayList<String>();
      for (final File fileEntry : folder.listFiles()) {
          if (fileEntry.isDirectory()) {
              listFilesForFolder(fileEntry);
          } else {
              fileList.add(fileEntry.getName());
          }
      } return fileList;
  }

  
  public ArrayList<String> process_explain_output(final String content) throws InterruptedException {
	  final String regex = "weight\\(([lines_added]+):([^\\s]+)";
	  tokens = new ArrayList<String>();

	  final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
      final Matcher matcher = pattern.matcher(content);
      System.out.println("matcher count:"+matcher.groupCount());
      while (matcher.find()) {
    	  if(matcher.groupCount()==2) {
    			  tokens.add(matcher.group(2));				//matcher.groupCount()
    		
    	  } 
      } 
      //sort tokens from largest to smallest assuming the largest token length = 1000 
      tokens.sort((s1, s2) -> Math.abs(s1.length() - 1000) - Math.abs(s2.length() - 1000));
      return tokens;
  }
  
  public HashMap<String,Float> find_matches(String queryFile) throws InterruptedException  {
	  HashMap <String,Float> matching_files = new HashMap <String,Float> ();
      this.totalHits=0; this.es_matches_size=0;
      String jsonbasefile = FilenameUtils.removeExtension(queryFile)+".json";
      String jsonfile = path + jsonbasefile;
      System.out.println("Query json:"+jsonfile);
      File f = new File(jsonfile);
      if(!f.exists()) {
          System.out.println("Missing Json file ");
          return null;
      }
      String sourcecode = readJsonFile(jsonfile);
      String [] texts  = {sourcecode};
      String [] fields = {"lines_added"};
      
     /*  //comment to ignore lines deleted
      	if (src_lines_deleted.length() != 0) {
    	  String [] texts2  = {sourcecode, src_lines_deleted};
    	  String [] fields2 = {"lines_added","lines_deleted"};
    	  texts=texts2;
    	  fields=fields2;
      } 
     */
     
      
      SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
      SearchRequest searchRequest = new SearchRequest(ESQuery.index_name);
      searchSourceBuilder.query(QueryBuilders.moreLikeThisQuery(fields,texts,null).minTermFreq(1));  
      
      searchSourceBuilder.from(0);
      searchSourceBuilder.size(docs_needed);
      searchRequest.source(searchSourceBuilder);
    
      try {
          SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
          SearchHits hits = searchResponse.getHits();
          this.totalHits=hits.getTotalHits().value;
          SearchHit[] searchHits = hits.getHits();
          for (SearchHit hit : searchHits) {
        	  
             Map<String, Object> sourceAsMap = hit.getSourceAsMap();
              
             String filename = (String) sourceAsMap.get("filename");//filename
             String commitHash = (String) sourceAsMap.get("commit_id");
             boolean buggy = Boolean.parseBoolean((String) sourceAsMap.get("buggy"));//buggy
             System.out.println(hit.getScore()+" "+commitHash+" "+filename);
            
              File f1 = new File(filename);
             matching_files.put(commitHash+"_"+f1.getName(),hit.getScore());
             
             System.out.println("Explain API");
             @SuppressWarnings("deprecation")
             //https://www.compose.com/articles/how-scoring-works-in-elasticsearch/
			 ExplainRequest request = new ExplainRequest(ESQuery.index_name, "_doc", hit.getId());
             request.query(QueryBuilders.moreLikeThisQuery(fields,texts,null).minTermFreq(1));
             ExplainResponse response = client.explain(request, RequestOptions.DEFAULT);
             String index = response.getIndex(); 
             String type = response.getType(); 
             String id = response.getId(); 
             System.out.println(id);
             boolean exists = response.isExists(); 
             boolean match = response.isMatch(); 
             boolean hasExplanation = response.hasExplanation(); 
             Explanation explanation = response.getExplanation(); 
             String exp_str = explanation.toString();
             
             ArrayList<String> tokens = process_explain_output(exp_str);
             System.out.println("tokens size:"+tokens.size());
             buggytokens.put(queryFile,tokens);   
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
      
      this.es_matches_size=matching_files.size();
      matching_files = (HashMap<String,Float>) sortByValue(matching_files);
      System.out.println("Es returned size:"+matching_files.size());
      System.out.println(matching_files);
      return matching_files;
  }
}
