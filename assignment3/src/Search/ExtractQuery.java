package Search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import PreProcessData.*;
import Classes.Query;

public class ExtractQuery {
    private BufferedReader fr;
    private String line="";
    private String tmpid;
    private String tmpcontent;
    private PriorityQueue<Map.Entry<String,String>> pq;
    private TreeMap<String,String> map;
    private StopWordRemover swr; 
	public ExtractQuery() throws IOException{
		//you should extract the 4 queries from the Path.TopicDir
		//NT: the query content of each topic should be 1) tokenized, 2) to lowercase, 3) remove stop words, 4) stemming
		//NT: you can simply pick up title only for query, or you can also use title + description + narrative for the query content.
		File f=new File("/"+Classes.Path.TopicDir);
		fr = new BufferedReader(new FileReader(f));
		pq = new PriorityQueue<>(new Comparator<Map.Entry<String, String>>(){
			public int compare(Map.Entry<String, String> m1, Map.Entry<String, String> m2) {
				return Integer.valueOf(m1.getKey())-Integer.valueOf(m2.getKey());
			}
		});
		swr = new StopWordRemover();
		map = new TreeMap<>();
		while((line=fr.readLine())!=null) { 
			if(line.indexOf("<num>")!=-1) { //select query id
				tmpid=line.substring(line.indexOf(":")+1).trim();
			}
			if(line.indexOf("<title>")!=-1) { //choose title for query
				tmpcontent=line.substring(line.indexOf("<title>")+7).trim();
				map.put(tmpid, tmpcontent);
			}
		}
		for(Map.Entry<String, String> entry:map.entrySet()) {
			pq.add(entry);
		}
	}
	
	public boolean hasNext()  //if any query exists
	{
		return !pq.isEmpty();
	}
	
	public Query next() throws IOException
	{
		Map.Entry<String, String> entry = pq.poll(); //select query from priority queue
		if(entry==null)
			return null;
		WordTokenizer wt = new WordTokenizer(entry.getValue().toCharArray());
		WordNormalizer wn = new WordNormalizer();
		char[] word = null;
		String result="";
		Query q;
		while((word=wt.nextWord())!=null) {
			word=wn.lowercase(word);
			if(!swr.isStopword(word)) {
				result = result+wn.stem(word)+" ";
			}
		}
		result=result.trim();
		q = new Query(entry.getKey(),result);
		return q;
	}
}
