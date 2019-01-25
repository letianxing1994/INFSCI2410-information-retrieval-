package PreProcessData;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import Classes.*;

public class StopWordRemover {
    private HashSet<String> h;
    private BufferedReader bufreader;
    private String line="";
    private String test="";

	public StopWordRemover( ) throws IOException {
		File f=new File(Classes.Path.StopwordDir);// Load and store the stop words from the fileinputstream
		FileReader filereader=new FileReader(f);
		bufreader=new BufferedReader(filereader,10*1024*1024);//set 10mb buffer for files reading task
		h=new HashSet<>();//HashSet for storing stop words
		while((line=bufreader.readLine())!=null) { //add all stop words into hashset
			h.add(line.trim());
		}
		bufreader.close();
	}


	public boolean isStopword( char[] word ) {
		test=String.valueOf(word);// Return true if the input word is a stopword, or false if not.
		return h.contains(test.trim());//find if this is a stop word
	}
}
