//Code adapted from https://stackoverflow.com/questions/5836148/how-to-use-opennlp-with-java

import java.io.File;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.stemmer.PorterStemmer;

public class PosTag {

	public static void main(String[] args) throws Exception {
		//Instanciate POS tagger
		POSModel model = new POSModelLoader().load(new File("/Users/vinu.venugopal/CloudDrive/NoSQL2023/Assignment-2/JARS/opennlp-en-ud-ewt-pos-1.0-1.9.3.bin")); //Edit path to the pre-trained model file
		POSTaggerME tagger = new POSTaggerME(model);

		//String line = "Can anyone help me dig through OpenNLP's documentation?";
		if (line != null) {
			SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
	    	String tokenizedLine[] = tokenizer.tokenize(line); //Tokenize line
	    	String[] tags = tagger.tag(tokenizedLine); //Instanciate tags

			//POS Tag
	    	POSSample sample = new POSSample(tokenizedLine, tags); //Identify tags
	    	System.out.println("\n\n" + sample.toString()); //Print tagged sentence
			for(String token : sample.getTags()){
				System.out.println(token); //Print tags of words
		  	}
			System.out.println("\n\nSteammed words:");
			//Steammer
			PorterStemmer steammer = new PorterStemmer(); // Instanciate Steammer
			for(String token : tokenizedLine){
				System.out.println(steammer.stem(token).toString());
			}
			
		}
		return;
	}

}
