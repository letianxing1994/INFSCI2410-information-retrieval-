package PreProcessData;

import java.util.LinkedList;
import java.util.List;

/**
 * This is for INFSCI 2140 in 2019
 * 
 * TextTokenizer can split a sequence of text into individual word tokens.
 */
public class WordTokenizer {
    private String[] words;
    private String temptexts;
    private String word;
    private int index;

	public WordTokenizer( char[] texts ) {
		temptexts=String.valueOf(texts);// Tokenize the input texts.
		index=0;// a dynamic index increases once call nextWord function
		words=temptexts.trim().split("\\s+");// separate texts into words with space, tabs and identifiable characters
	}
	
	public char[] nextWord() {
		while(index<words.length) {
		    word=words[index].trim().replaceAll("[.,\"\\?!:'()<>{};]", "");//delete all punctuations
		    index++;
		    if(word!=null&&!word.equals(""))//if the word after being tokenized is not empty, return this word 
		        return word.toCharArray();
		    else //if the word after being tokenized is empty, keep searching
		    	    continue;
		}// Return the next word in the document.
		return null;// Return null, if it is the end of the document.
	}	
}
