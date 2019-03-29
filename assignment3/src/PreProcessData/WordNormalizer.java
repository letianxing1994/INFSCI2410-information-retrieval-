package PreProcessData;

import Classes.*;

/**
 * This is for INFSCI 2140 in 2018
 * 
 */
public class WordNormalizer {
    private String temp;
    private Classes.Stemmer tempstem;

	public char[] lowercase(char[] chars) {
		temp=String.valueOf(chars);
		chars=temp.toLowerCase().toCharArray();// Transform the word uppercase characters into lowercase.
		return chars;
	}

	public String stem(char[] chars) {
		String str = "";
		tempstem=new Classes.Stemmer(); //stem the words passed through this function 
		tempstem.add(chars, chars.length);
		tempstem.stem();
		str=tempstem.toString();//return string type of this word
		return str;
	}

}
