package Indexing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import Classes.Path;

public class MyIndexWriter {
	// I suggest you to write very efficient code here, otherwise, your memory cannot hold our corpus...
	private FileWriter dictfw;
	private FileWriter postfw;
	private FileWriter idnofw;
	private BufferedWriter dictbw;
	private BufferedWriter postbw;
	private BufferedWriter idnobw;
	private Map<String,int[]> dicttermfile=new HashMap<String,int[]>();
	private Map<String,TreeMap<Integer,Integer>> postfile=new HashMap<String,TreeMap<Integer,Integer>>();
	private Map<Integer, String> docidno =new HashMap<Integer,String>();
	private String path="";
	private int wordpointer=0;//this pointer will lose its function when we write documents into separated files
	private int docsearched=0;

	
	public MyIndexWriter(String type) throws IOException {
		// This constructor should initiate the FileWriter to output your index files
		// remember to close files if you finish writing the index
		if( type.equals("trecweb") ) {
			path = Classes.Path.IndexWebDir;
		}else {
			path = Classes.Path.IndexTextDir;
		}
		//three files to wirte:dictionary, postings, docno-docid
		dictfw = new FileWriter("/"+path+"dictionarytermfile.txt");
		dictbw = new BufferedWriter(dictfw);
		postfw = new FileWriter("/"+path+"postingfile.txt");
		postbw = new BufferedWriter(postfw);
		idnofw = new FileWriter("/"+path+"docidnofile.txt");
		idnobw = new BufferedWriter(idnofw);
	}
	
	public void IndexADocument(String docno, String content) throws IOException {
		// you are strongly suggested to build the index by installments
		// you need to assign the new non-negative integer docId to each document, which will be used in MyIndexReader
		int docid=Integer.parseInt(docno.substring(0, docno.indexOf("-")));
		docidno.put(docid,docno.substring(docno.indexOf("-")+1));
		String[] words = new String(content).split(" ");
		
		
		for(String word:words) {
			if(!dicttermfile.containsKey(word)) { //term is not in a dictionary
				//generate a new term in dictionary file
				int[] temp= {wordpointer,1};
				dicttermfile.put(word, temp);
				
				//generate a posting list for a term
				TreeMap<Integer,Integer> temptermpost = new TreeMap<>();
				temptermpost.put(docid, 1);
				postfile.put(word,temptermpost);
				wordpointer++;
			}
			else {//term is already in a dictionary
				//add collection frequency in existed dictionary term file
				int[] temp = dicttermfile.get(word);
				temp[1] = temp[1]+1;
				dicttermfile.put(word,temp);
				
				//add document record in existed posting file
				TreeMap<Integer,Integer> temptermpost = new TreeMap<>();
				//if this document is first searched
				if(!postfile.get(word).containsKey(docid)) {
					 temptermpost = postfile.get(word);
					 temptermpost.put(docid, 1);
					 postfile.put(word, temptermpost);
				}
				else {
					 temptermpost = postfile.get(word);
					 int f=temptermpost.get(docid);
					 f=f+1;
					 temptermpost.put(docid, f);
					 postfile.put(word, temptermpost);
				}	
			}
		}
		docsearched++;
		//30000 documents per file
		if(docsearched%30000 ==0 ) {
			tempfilewrite();
		}
	}
	
	private void tempfilewrite() throws IOException{
		BufferedWriter tempdict=new BufferedWriter(new FileWriter("/"+path+"tempdict-"+docsearched+".txt"));
		BufferedWriter temppost=new BufferedWriter(new FileWriter("/"+path+"temppost-"+docsearched+".txt"));
		BufferedWriter tempidno=new BufferedWriter(new FileWriter("/"+path+"tempidno-"+docsearched+".txt"));
		//write temporary dictionary of 30000 documents into separated files
		for(String term:dicttermfile.keySet()) {
			tempdict.write(term + " " + dicttermfile.get(term)[0]+ " " + dicttermfile.get(term)[1]);
			tempdict.write("\n");
		}
		
		//write temporary postings of 30000 documents into separated files
		for(String termid:postfile.keySet()) {
			temppost.write(termid +" ");
			Map<Integer, Integer> docfre = postfile.get(termid);
			for(int docid: docfre.keySet()) {
				temppost.write(docid+"-"+docfre.get(docid)+" ");
			}
			temppost.write("\n");
		}
		
		//write docid docno relation table into separated files
		for(Map.Entry<Integer, String> entry:docidno.entrySet()) {
			tempidno.write(entry.getKey()+" "+entry.getValue()+"\n");
		}
		tempdict.close();
		temppost.close();
		tempidno.close();
		dicttermfile.clear();
		postfile.clear();
		docidno.clear();
	}
	
	public void Close() throws IOException {
		// close the index writer, and you should output all the buffered content (if any).
		// if you write your index into several files, you need to fuse them here.
		tempfilewrite(); //if there is still a block of documents remained
		int termcount = 0;
		File[] dictfilelist = new File("/"+path).listFiles();
				
		//combine dictionary term file
		for(File subdictfile: dictfilelist) {
			if(subdictfile.getName().contains("tempdict")) {
				BufferedReader tempbr = new BufferedReader(new FileReader(subdictfile));
				String line = new String();
				
				while((line = tempbr.readLine())!=null) {
					String[] tempterminfo = line.split(" ");
					if(!dicttermfile.containsKey(tempterminfo[0])) {
						termcount++;
						int[] arr= {termcount, Integer.parseInt(tempterminfo[2])};
						dicttermfile.put(tempterminfo[0],arr );
					}else {
						int[] arr=dicttermfile.get(tempterminfo[0]);
						arr[1]=arr[1]+Integer.parseInt(tempterminfo[2]);
						dicttermfile.put(tempterminfo[0], arr);
					}
				}
				tempbr.close();
			}
		}
		
		//write dictionarytermfile.txt
		for(Map.Entry<String, int[]> entry:dicttermfile.entrySet()) {
			dictbw.write(entry.getKey()+" "+entry.getValue()[0]+" "+entry.getValue()[1]+"\n");
		}
	    dictbw.close();
		dicttermfile.clear();
		
		//combine posting file
		for(File subpostfile: dictfilelist) {
			if(subpostfile.getName().contains("temppost")) {
				BufferedReader tempbr = new BufferedReader(new FileReader(subpostfile));
				String line = new String();
						
				while((line = tempbr.readLine())!=null) {
					postbw.write(line);
					postbw.write("\n");
				}
				tempbr.close();
			}
		}
		postbw.close();
		postfile.clear();
		
		//combine doc id file
		for(File subdocidno:dictfilelist) {
			if(subdocidno.getName().contains("tempidno")) {
				BufferedReader tempbr=new BufferedReader(new FileReader(subdocidno));
				String line=new String();
				while((line = tempbr.readLine())!=null) {
					idnobw.write(line+"\n");
				}
				tempbr.close();
			}
		}
		idnobw.close();
		docidno.clear();
	}	
}
