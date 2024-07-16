package com.smail;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Set;
import java.util.HashSet;

public class SpamChecker{
	public static Set<String> spamKeywords = new HashSet<>();
	
	static {
		loadSpamKeywords();
	}
	
	public static void loadSpamKeywords(){
		try(BufferedReader br = new BufferedReader(new FileReader("D:\\Sakthi\\Github\\Smail-Application\\Smail\\src\\main\\java\\com\\smail\\spam.txt"))){
			String line;
			while((line=br.readLine())!=null){
				spamKeywords.add(line);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static boolean isSpam(String message){
		message = message.replaceAll("[^A-Za-z\\s]","");
		String words[] = message.split("\\s+");
		int count=0;
		for(String word:words){
			if(spamKeywords.contains(word.toLowerCase())){
				if(++count>=5){
					return true;
				}
			}
		}
		return false;
	}
}
