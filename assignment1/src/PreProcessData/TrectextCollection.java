package PreProcessData;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import Classes.Path;

/**
 * This is for INFSCI 2140 in 2019
 *
 */
public class TrectextCollection implements DocumentCollection {
	private BufferedReader bufreader;
	private Map<String, Object> map;
	private String line;
	private StringBuilder no;
	private StringBuilder content;

	public TrectextCollection() throws IOException {
		// Open file reader and buffer reader.
		File f=new File(Classes.Path.DataTextDir);
		BufferedInputStream filereader=new BufferedInputStream(new FileInputStream(f));
		line="";
		bufreader=new BufferedReader(new InputStreamReader(filereader,"utf-8"),10*1024*1024);		
	}
	
	public Map<String, Object> nextDocument() throws IOException {
		no=new StringBuilder();
		content=new StringBuilder();
		map=new HashMap<>();// map used for storing doc number and doc content
		while((line=bufreader.readLine())!=null) { 
			//if the line contains <DOCNO>, get the substring of doc number
			if(line.indexOf("</DOCNO>")!=-1) {
			    no.append(line.substring(line.indexOf("<DOCNO>")+7,line.indexOf("</DOCNO>")).trim());//find doc number
			} 
			//stop reading in advance when program is going to read text
			else if(line.indexOf("<TEXT>")!=-1) { 
				break;
			}
			//skip and abandon information unnecessary
			else {
				continue;
			}
		}
		// When no document left, return null, and close the file.
		if(line==null) {  
			bufreader.close();
			return null;
		}
		//record content of the text before text has been throughly read
		while((line=bufreader.readLine()).indexOf("</TEXT>")==-1) { //record content of the text before text has been throughly read
			content.append(line+'\n');
		}
		//record doc number and doc content
		map.put(no.toString(), content.toString().toCharArray());
		return map;
	}	
}
