import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Set;
import java.util.HashSet;

public class SpamChecker{
	public Set<String> spamKeywords;
	
	public SpamChecker(){
		spamKeywords = new HashSet<>();
		loadSpamKeywords();
	}
	
	public void loadSpamKeywords(){
		try(BufferedReader br = new BufferedReader(new FileReader("spam.txt"))){
			String line;
			while((line=br.readLine())!=null){
				spamKeywords.add(line);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public boolean isSpam(String message){
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
	
	/*public static void main(String[] args){
		SpamChecker s = new SpamChecker();
		System.out.println(s.spamKeywords);
		System.out.println(s.isSpam("Congratulations, you have won a prize!"));
		System.out.println(s.isSpam("This is an urgent message."));
		System.out.println(s.isSpam("Claim your free money now!"));
	}*/
}
