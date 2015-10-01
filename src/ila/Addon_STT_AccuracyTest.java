package ila;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

public class Addon_STT_AccuracyTest implements ILA_addon_interface {
	
	//---CONFIGURATION---
	//configuration information of the add-on - must be in the class that has the name of
	//the .jar-file of your add-on and that implements 'ILA_addon_interface'
	public AddonConfiguration getConfiguration() {
		
		//a list of all commands inside this add-on - defined by YOU ;-)
		ArrayList<String> commands = new ArrayList<String>();
		commands.add("batchaccuracytest");
		
		//a description of each command for ILA's teachGUI - TODO: different languages support 
		ArrayList<String> commands_description = new ArrayList<String>();
		if (ILA.language.matches("de"))
			commands_description.add("STT GENAUIGKEIT testen von .wav-files im Ordner ...");
		else
			commands_description.add("STT ACCURACY test of .wav-files in folder");
		
		//context keywords - under development (ignore for now) - TODO: implement!
		ArrayList<String> contexts = new ArrayList<String>();
		contexts.add("batchaccuracytest");
		
		//initialize configuration
		String name_of_addon = "STT Accuracy Testing";		//mainly for user info
		String version_of_addon = "1.0";			//...
		AddonConfiguration config = new AddonConfiguration(name_of_addon, version_of_addon, commands, commands_description, contexts);
		return config;
	}
	
	//---MAIN---
	//main method to call an add-on specific command - will be called from ILA_addons - expects an String-answer in return
	//if you write more classes for your add-on they all have to be accessed from here.
	public String callCommand(String command, String[] memories) {
		
		//define the variable for the answer
		String answerIt="no specific answer found";

		switch (command.toLowerCase()) {
			
			//batch testing of accuracy with given files and transcription - takes first parameter the folder and second parameter how many of the files should be tested
			case "batchaccuracytest":
				
				//check if the recognizer is local (Sphinx not Google)
				if (ILA.defaultSTTengine==1){
					answerIt="the Google web API ist not supported for accuracy tests right now, sorry.";
					break;
					//String ansToQ = ILA_speechControl.askDirectQuestion("are you sure you want to use a cloud recognizer?");
					//if (!ILA_answers.isConfirmation(ansToQ)){
					//	answerIt="ok I better abort the batch accuracy test.";
					//	break;
					//}
				}
				String recFolder = "SpeechData/VoiceTraining";
				String customAnswer = "done";
				int limitOfFiles = Integer.MAX_VALUE;
				//check for input parameters
				if (memories.length>5){
					customAnswer = memories[5];
            	}
				if (memories.length>4){
					limitOfFiles = Integer.parseInt(memories[4]);
            	}
            	if (memories.length>3){
            		recFolder = memories[3];
            	}
				//check if file exists
				File transcription=new File(recFolder + "/ILA_voice_train.transcription");
				if (!transcription.exists()){
					answerIt="I can't find the transcription file";
					break;
				}
            	//notify the user
            	ILA_interface.avatar.new_dialogue_noExtras(ILA_answers.get("testaccuracy0"), true);
            	//and go
            	ILA_speechControl.sphinxSTT.switchGrammar("dialog");		//make sure to use the default input grammar (if active) or no grammar
				List<String> sentences = ILA_teachGUI.readStringFile( recFolder + "/ILA_voice_train.transcription", false);
				
				String tmpSent ="", cleanSent="", wavFile="";
				double[] totalWER = new double[sentences.size()];
				double stats, wer;
				int totalWords = 0, numberOfWords = 0;
				int totalErrors = 0, numberOfErrors = 0;
				long startTime = ILA.tic();
				for (int i=0; i<Math.min(sentences.size(),limitOfFiles); i++){
					//skip on user abort
					if (ILA_interface.abortAndReturn)
						break;
					tmpSent = sentences.get(i);
					cleanSent = tmpSent.replaceAll("\\(.*\\)", "").replaceAll("(<s>|<\\/s>)", "").toLowerCase().trim();
					wavFile = tmpSent.replaceAll(".*\\(", "").replaceAll("\\).*", "").trim()+".wav";
					System.out.println("\nTarget: " + cleanSent + " - Wav: " +wavFile);

					stats = Test_ILA.testAccuracy( recFolder + "/" + wavFile, cleanSent );
					numberOfWords = cleanSent.split("( )+").length;
					wer = stats;
					numberOfErrors = (int) Math.round(((double) numberOfWords)*wer);
					System.out.println("Words: "+numberOfWords+" - Errors: "+numberOfErrors+" - WER: " + wer);
					totalWER[i] = wer;
					totalWords += numberOfWords;
					totalErrors += numberOfErrors;
				}
				UnivariateStatistic stat = new Mean();
				double mean = stat.evaluate(totalWER);
				System.out.println("Total num. words: "+totalWords+" - total errors: "+totalErrors+" - mean WER(1): " + String.format("%.4f",mean) +" - mean WER(2): " + String.format("%.4f",((double)totalErrors)/((double)totalWords)));
				System.out.println("Time needed: "+(ILA.tic()-startTime)+" ms");
				ILA_interface.textArea.append("\nTotal num. words: "+totalWords+" - total errors: "+totalErrors+" - mean WER: " + String.format("%.4f",((double)totalErrors)/((double)totalWords))+"\n");
				ILA_interface.textArea.append("Time needed: "+(ILA.tic()-startTime)+" ms\n");
				answerIt=customAnswer;
				break;
				
			}
		
		//no hit
		return answerIt;
		
	}

}
