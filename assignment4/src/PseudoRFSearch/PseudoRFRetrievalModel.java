package PseudoRFSearch;
import java.util.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import Classes.Document;
import Classes.Query;
import IndexingLucene.MyIndexReader;
import SearchLucene.QueryRetrievalModel;

public class PseudoRFRetrievalModel {

	MyIndexReader ixreader;
	QueryRetrievalModel qrm;
	int miu;
	double collectionfreq;
	List<Document> list;
	List<Integer> topklist;
	public PseudoRFRetrievalModel(MyIndexReader ixreader) throws IOException
	{
		this.ixreader=ixreader;
		this.miu=2000;
		this.collectionfreq = ixreader.CollectionFreq();//the total number of terms of this collection
	}
	
	/**
	 * Search for the topic with pseudo relevance feedback in 2017 spring assignment 4. 
	 * The returned results (retrieved documents) should be ranked by the score (from the most relevant to the least).
	 * 
	 * @param aQuery The query to be searched for.
	 * @param TopN The maximum number of returned document
	 * @param TopK The count of feedback documents
	 * @param alpha parameter of relevance feedback model
	 * @return TopN most relevant document, in List structure
	 */
	public List<Document> RetrieveQuery( Query aQuery, int TopN, int TopK, double alpha) throws Exception {	
		// this method will return the retrieval result of the given Query, and this result is enhanced with pseudo relevance feedback
		// (1) you should first use the original retrieval model to get TopK documents, which will be regarded as feedback documents
		// (2) implement GetTokenRFScore to get each query token's P(token|feedback model) in feedback documents
		// (3) implement the relevance feedback model for each token: combine the each query token's original retrieval score P(token|document) with its score in feedback documents P(token|feedback model)
		// (4) for each document, use the query likelihood language model to get the whole query's new score, P(Q|document)=P(token_1|document')*P(token_2|document')*...*P(token_n|document')
		
		
		//get P(token|feedback documents)
		HashMap<String,Double> TokenRFScore = GetTokenRFScore(aQuery,TopK);
		HashMap<Integer,HashMap<String,Integer>> docmap = new HashMap<>();
		HashMap<Integer, Double> scoremap = new HashMap<>(); 
		PriorityQueue<Document> pq = new PriorityQueue<>(new Comparator<Document>(){
			public int compare(Document d1, Document d2) {
				if(d2.score()>d1.score())
					return 1;
				else if(d2.score()==d1.score())
					return 0;
				else
					return -1;
			}
		}) ;
		String tokens[] = aQuery.GetQueryContent().split(" ");//word tokenization
		for(String token:tokens) { //record doc information for word frequency
			int[][] postinglist = ixreader.getPostingList(token);
			if(postinglist==null) continue;
			for(int[] list:postinglist) { //build map for each doc
				int key = list[0];
				int value = list[1];
				if(!docmap.containsKey(key)) {
        	     	    HashMap<String, Integer> temp = new HashMap<>();
        	     	    temp.put(token, value);
        	    	        docmap.put(key, temp);
        	        }
        	        else {
        	    	        HashMap<String, Integer> temp = docmap.get(key);
        	    	        temp.put(token, value);
        	    	        docmap.put(key, temp);
        	        }
			}
		}
		
		for(Map.Entry<Integer, HashMap<String,Integer>> entry: docmap.entrySet()) {
			double wordscore = 1;
			double docscore =1;
			double pQueryf = 1;
			for(String token:tokens) {    //set docid and calculate wordscore 
				long tokencolfreq=ixreader.CollectionFreq(token);
				if((int)tokencolfreq==0) continue;  //if word is not in collection,pass
				double probc = tokencolfreq/collectionfreq;
				int termindoc = 0;
				if(entry.getValue().containsKey(token))
			        termindoc = entry.getValue().get(token);
			    int doctotallen = ixreader.docLength(entry.getKey());
			    wordscore=(termindoc+this.miu*probc)/(doctotallen+this.miu);//smoothing
			    if(TokenRFScore.containsKey(token)) {
					pQueryf=TokenRFScore.get(token);
				}
			    docscore =docscore*(alpha*wordscore-(1-alpha)*pQueryf);
			}
			scoremap.put(entry.getKey(), docscore);
		}
        
		for(Map.Entry<Integer, Double> entry:scoremap.entrySet()) {
			double optimalscore=entry.getValue();
			pq.add(new Document(entry.getKey().toString(),ixreader.getDocno(entry.getKey()),optimalscore));
		}
		// sort all retrieved documents from most relevant to least, and return TopN
		List<Document> results = new ArrayList<Document>();
		results = new ArrayList<Document>(pq);
		return results;
	}
	
	public HashMap<String,Double> GetTokenRFScore(Query aQuery,  int TopK) throws Exception
	{
		// for each token in the query, you should calculate token's score in feedback documents: P(token|feedback documents)
		// use Dirichlet smoothing
		// save <token, score> in HashMap TokenRFScore, and return it
		HashMap<String,Double> TokenRFScore=new HashMap<String,Double>();
		HashMap<Integer,HashMap<String,Integer>> docmap = new HashMap<>();
		this.qrm = new QueryRetrievalModel(ixreader);
		this.list = qrm.retrieveQuery(aQuery, TopK);
		this.topklist = new ArrayList<>();
		String[] tokens = aQuery.GetQueryContent().split(" ");
		for(Document d:list) { //get doc id from original model
			topklist.add(Integer.parseInt(d.docid()));
		}
		for(String token:tokens) {
			int[][] postinglist=ixreader.getPostingList(token);
			if(postinglist == null) continue;
            for(int i=0;i<postinglist.length;i++) {
            	    int key = postinglist[i][0];
            	    int value = postinglist[i][1];
            	    if(!docmap.containsKey(key)) {
            	     	HashMap<String, Integer> temp = new HashMap<>();
            	     	temp.put(token, value);
            	    	    docmap.put(key, temp);
            	    }
            	    else {
            	    	    HashMap<String, Integer> temp = docmap.get(key);
            	    	    temp.put(token, value);
            	    	    docmap.put(key, temp);
            	    }
            }
		}
		
		//calculate fb score for each token
		for(String token:tokens) {
			double score = 1.0;
			int termindoc=0;//the total number of term appears in a document
			int doctotalnum=0;//the total number of terms of a document
			long collectfreq = ixreader.CollectionFreq(token);//the total number of term appears in collection
			double probc = collectfreq/collectionfreq;
			for(int docid:topklist) { //get information from topk list-feedback documents
				doctotalnum += ixreader.docLength(docid);
				HashMap<String,Integer> freqmap = docmap.get(docid);
				if(freqmap!=null&&freqmap.get(token)!=null)
				    termindoc += freqmap.get(token);
			}
			score = (termindoc + miu*probc)/(doctotalnum+miu);//build statistical language model for feedback documents
			TokenRFScore.put(token, score);
		}
		return TokenRFScore;
	}
	
	
}