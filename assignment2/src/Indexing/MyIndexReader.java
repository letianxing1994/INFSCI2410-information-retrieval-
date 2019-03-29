package Indexing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import Classes.Path;


public class MyIndexReader {
	//you are suggested to write very efficient code here, otherwise, your memory cannot hold our corpus...
    private String path = "";
	private FileReader frdict;
	private FileReader frpost;
	private FileReader fridno;
	private BufferedReader brdict;
	private BufferedReader brpost;
	private BufferedReader bridno;
	private Map<String, int[]> dictfile;
	private Map<String, TreeMap<Integer,Integer>> postfile;
	private Map<Integer, String> docidnofile;
	private String line = "";
	
	public MyIndexReader( String type ) throws IOException {
		//read the index files you generated in task 1
		//remember to close them when you finish using them
		//use appropriate structure to store your index
		if(type.equals("trecweb")) {
			path = Path.IndexWebDir;
		}else {
			path = Path.IndexTextDir;
		}
		frdict = new FileReader("/"+path+"dictionarytermfile.txt");
		brdict = new BufferedReader(frdict);
		frpost = new FileReader("/"+path+"postingfile.txt");
		brpost = new BufferedReader(frpost);
		fridno = new FileReader("/"+path+"docidnofile.txt");
		bridno = new BufferedReader(fridno);
		dictfile = new HashMap<String, int[]>();
		postfile = new HashMap<String, TreeMap<Integer,Integer>>();
		docidnofile = new HashMap<Integer,String>();
		
		//read dictionary file into dictfile
		while((line = brdict.readLine())!=null) {
			String temp[]=line.split(" ");
			dictfile.put(temp[0], new int[] {Integer.parseInt(temp[1]),Integer.parseInt(temp[2])});
		}
		
		//read docno-docid file to docidnofile
		while((line = bridno.readLine())!=null) {
			String temp[]=line.split(" ");
			docidnofile.put(Integer.parseInt(temp[0]),temp[1]);
		}
	}
	
	//get the non-negative integer dociId for the requested docNo
	//If the requested docno does not exist in the index, return -1
	public int GetDocid( String docno ) {
		for(Map.Entry<Integer, String> entry:docidnofile.entrySet()) {
			if(entry.getValue().equals(docno))
				return (int)entry.getKey();
			else
				continue;
		}
		return -1;
	}

	// Retrieve the docno for the integer docid
	public String GetDocno( int docid ) {
		if(!docidnofile.containsKey(docid))
		    return null;
		return docidnofile.get(docid);
	}
	
	/**
	 * Get the posting list for the requested token.
	 * 
	 * The posting list records the documents' docids the token appears and corresponding frequencies of the term, such as:
	 *  
	 *  [docid]		[freq]
	 *  1			3
	 *  5			7
	 *  9			1
	 *  13			9
	 * 
	 * ...
	 * 
	 * In the returned 2-dimension array, the first dimension is for each document, and the second dimension records the docid and frequency.
	 * 
	 * For example:
	 * array[0][0] records the docid of the first document the token appears.
	 * array[0][1] records the frequency of the token in the documents with docid = array[0][0]
	 * ...
	 * 
	 * NOTE that the returned posting list array should be ranked by docid from the smallest to the largest. 
	 * 
	 * @param token
	 * @return
	 */
	//get posting list of postfile with certain token
	public int[][] GetPostingList( String token ) throws IOException {
		if(!postfile.containsKey(token)) return null;
		int rowindex=0;
		int row=postfile.get(token).size();
		int[][] result=new int[row][2];
		for(Map.Entry<Integer, Integer> entry:postfile.get(token).entrySet()) {
			result[rowindex][0]=entry.getKey();
			result[rowindex][1]=entry.getValue();
			rowindex++;
		}
		return result;
	}

	// Return the number of documents that contains the token.
	public int GetDocFreq( String token ) throws IOException {
		TreeMap<Integer, Integer> test = new TreeMap<Integer, Integer>();
		line="";
		while((line = brpost.readLine())!=null) {
			String temp[]=line.split(" ");
			if(temp[0].equals(token)) {
				for(int i=1;i<temp.length;i++) {
					int idx=temp[i].indexOf("-");
					int key=Integer.parseInt(temp[i].substring(0,idx));
					int value=Integer.parseInt(temp[i].substring(idx+1));
					test.put(key, value);
				}if(!postfile.containsKey(token)) {
					postfile.put(temp[0],test);
				}else {
				    TreeMap<Integer,Integer> tmp=postfile.get(token);
				    tmp.putAll(test);
				    postfile.put(token,tmp);
				}
			}
		}
        if(!postfile.containsKey(token)) return 0;
		return postfile.get(token).size();
	}
	
	// Return the total number of times the token appears in the collection.
	public long GetCollectionFreq( String token ) throws IOException {
		if(!dictfile.containsKey(token)) return 0;
		return dictfile.get(token)[1];
	}
	
	//close the file
	public void Close() throws IOException {
		frdict.close();
		frpost.close();
		brdict.close();
		brpost.close();
		fridno.close();
		bridno.close();
	}
	
}