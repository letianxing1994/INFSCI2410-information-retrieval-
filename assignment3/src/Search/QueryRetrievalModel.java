package Search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import Classes.Query;
import Classes.Document;
import IndexingLucene.MyIndexReader;

public class QueryRetrievalModel {
	
	protected MyIndexReader indexReader;
	private int miu;
	private double CollectionLen;
	private HashMap<Integer,HashMap<String, Integer>> docmap;
	private HashMap<Integer,Double> scoremap;
	private PriorityQueue<Map.Entry<Integer,Double>> pq;
	private ArrayList<Document> result;
	
	public QueryRetrievalModel(MyIndexReader ixreader) throws IOException {
		indexReader = ixreader;
		CollectionLen = indexReader.CollectionFreq();
		miu = 1000; //set miu values
	}
	
	/**
	 * Search for the topic information. 
	 * The returned results (retrieved documents) should be ranked by the score (from the most relevant to the least).
	 * TopN specifies the maximum number of results to be returned.
	 * 
	 * @param aQuery The query to be searched for.
	 * @param TopN The maximum number of returned document
	 * @return
	 */
	
	public List<Document> retrieveQuery( Query aQuery, int TopN ) throws IOException {
		// NT: you will find our IndexingLucene.Myindexreader provides method: docLength()
		// implement your retrieval model here, and for each input query, return the topN retrieved documents
		// sort the documents based on their relevance score, from high to low
		docmap = new HashMap<>();
        scoremap = new HashMap<>();
        pq = new PriorityQueue<Map.Entry<Integer,Double>>(new Comparator<Map.Entry<Integer, Double>>(){
        	    public int compare(Map.Entry<Integer, Double> m1, Map.Entry<Integer,Double> m2) {
        	    	    if((m2.getValue()-m1.getValue())==0) return 0;
        	    	    return (m1.getValue()-m2.getValue())>0?-1:1;
        	    }
        });
        result =new ArrayList<>();
		String[] tokens = aQuery.GetQueryContent().split(" ");
	    int[][] postinglist;
	    HashMap<String,Integer> temp;
		for(String token:tokens) {         //set each word of each query into a map
			postinglist = indexReader.getPostingList(token);
			if(postinglist == null) continue;
			for(int[] posting:postinglist) {
				temp =new HashMap<>();
				temp.put(token, posting[1]);
				docmap.put(posting[0], temp);
			}
		}		
		for(Map.Entry<Integer, HashMap<String,Integer>> entry: docmap.entrySet()) {
			double wordscore=1;
			for(String token:tokens) {    //set docid and calculate wordscore 
				long tokencolfreq=indexReader.CollectionFreq(token);
				if((int)tokencolfreq==0) continue;  //if word is not in collection,pass
				double probc = tokencolfreq/CollectionLen;
				int termindoc = 0;
				if(entry.getValue().containsKey(token))
			        termindoc = entry.getValue().get(token);
			    int doctotallen = indexReader.docLength(entry.getKey());
			    wordscore*=(termindoc+this.miu*probc)/(doctotallen+this.miu);//smoothing
			}
			scoremap.put(entry.getKey(), wordscore);
		}
		for(Map.Entry<Integer, Double> entry: scoremap.entrySet()) {
			pq.add(entry);  //priority queue to sort documents
		}
		for(int i=0;i<TopN;i++) { //return topN documents from priority queue
			Map.Entry<Integer, Double> entry = pq.poll();
			String docno = indexReader.getDocno(entry.getKey());
			String docid = entry.getKey()+"";
			Document e = new Document(docid,docno,entry.getValue());
			result.add(e);
		}
		return result;
	}
	
}