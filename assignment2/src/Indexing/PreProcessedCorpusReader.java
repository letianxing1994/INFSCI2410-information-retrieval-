package Indexing;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class PreProcessedCorpusReader {
    private BufferedReader bufferreader;
    private BufferedInputStream fileinputstream;
	private String type="";
	private String line;
	private Map<String, String> map;
	private int index=1;
	
	public PreProcessedCorpusReader(String type) throws IOException {
		// This constructor opens the pre-processed corpus file, Path.ResultHM1 + type
		// You can use your own version, or download from http://crystal.exp.sis.pitt.edu:8080/iris/resource.jsp
		// Close the file when you do not use it any more
		File f=new File("/"+Classes.Path.ResultHM1+type);
		fileinputstream=new BufferedInputStream(new FileInputStream(f));
		bufferreader=new BufferedReader(new InputStreamReader(fileinputstream),10*1024*1024);
		line="";
		type=this.type;
	}
	

	public Map<String, String> NextDocument() throws IOException {
		// read a line for docNo, put into the map with <"DOCNO", docNo>
		// read another line for the content , put into the map with <"CONTENT", content>
		
		//record document Number
		map=new HashMap<>();
		if((line=bufferreader.readLine())!=null) {
			map.put("DOCNO",index+"-"+line);
		}else {
			fileinputstream.close();
			bufferreader.close();
			return null;
		}
		
		//record document content
		if((line=bufferreader.readLine())!=null) {
			map.put("CONTENT", line);
		}else {
			fileinputstream.close();
			bufferreader.close();
			return null;
		}
		index++;
		return map;
	}

}
