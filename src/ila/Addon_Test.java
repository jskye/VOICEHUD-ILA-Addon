package ila;

import java.util.ArrayList;

import ila.ILA_addon_interface;

/***************************************************************************
 * This is an example add-on for ILA.
 * Your add-on can use multiple classes but there must be exactly one that
 * has the name of your add-on's .jar-file and that implements the 
 * AddonConfiguration Class. This file has to implement the 
 * 'ILA_addon_interface' as well!
 * 
 * @author Florian Quirin for I.L.A. project
 ***************************************************************************/
public class Addon_Test implements ILA_addon_interface {

	//---CONFIGURATION---
	//configuration information of the add-on - must be in the class that has the name of
	//the .jar-file of your add-on and that implements 'ILA_addon_interface'
	public AddonConfiguration getConfiguration() {
		
		//a list of all commands inside this add-on - defined by YOU ;-)
		//make this names as unique as possible to avoid conflicts with other add-ons
		//note: use only small case letters as names!!!
		ArrayList<String> commands = new ArrayList<String>();
		commands.add("test_addon");
		commands.add("test_addon_get_info");
		commands.add("test_addon_print_parameters");
		commands.add("test_addon_complexer");
		commands.add("test_addon_switch_super_context");
		commands.add("test_addon_end_super_context");
		commands.add("test_addon_super_context");
		
		//a description of each command for ILA's teachGUI - TODO: different languages support 
		ArrayList<String> commands_description = new ArrayList<String>();
		commands_description.add("TEST: simple first test for add-ons");
		commands_description.add("TEST: simple second test for add-ons");
		commands_description.add("TEST: simple third test for add-ons with parameters");
		commands_description.add("TEST: more complex test for add-ons with interaction");
		commands_description.add("TEST: switch super context to addon_test");
		commands_description.add("TEST: end super context addon_test and go back to default");
		commands_description.add("TEST: test super context");
		
		//context keywords - under development (ignore for now) - TODO: implement!
		ArrayList<String> contexts = new ArrayList<String>();
		contexts.add("addon_test");
		
		//initialize configuration
		String name_of_addon = "Add-On Tests";		//mainly for user info
		String version_of_addon = "1.1";			//...
		AddonConfiguration config = new AddonConfiguration(name_of_addon, version_of_addon, commands, commands_description, contexts);
		return config;
	}
	
	//---Manage Answers---
	//get the language specific file with answers
	//use this if you decide to put all your answers in an external file and want to support different languages
	public String answerFile(){
		String file;
		if (ILA.language.matches("de")){
			file="Addons/Addon_Test/answers_de.txt";
		}else{
			file="Addons/Addon_Test/answers_en.txt";
		}
		return file;
	}

	//---MAIN---
	//main method to call an add-on specific command - will be called from ILA_addons - expects an String-answer in return
	//if you write more classes for your add-on they all have to be accessed from here.
	public String callCommand(String command, String[] memories) {
		
		//define the variable for the answer
		String answer="no specific answer found";

		//ILA can have different moods :-) (under development - right now it just changes the color of ILA)
		ILA_interface.avatar.avatar_mood="neutral";
		
		//now this is the implementation of the commands - feel free to do whatever you want here :-)
		//this is just an example how to select the commands, e.g. you can use switch-cases as well ...
		
		TheSwitch:			//it is labeled so you can escape for loops etc. ...
		switch (command.toLowerCase()) {
			
			//first command - note: use only small case letters as name!!!
			case "test_addon":
				System.out.println("ADD-ON TEST called -> TEST succesful! :-)");
				//(old) direct answer:
				//answer="I tested the add-on and it worked";
				//(new) load answer from file:
				answer=ILA_answers.getFromFile(answerFile(), "test_addon");		//"I tested the add-on and it worked"
				break;
			
			//second command	
			case "test_addon_get_info":
				System.out.println("ADD-ON TEST info: this is a test for the new add-on implementation :-)");
				System.out.println("ADD-ON Name: "+getConfiguration().name);
				System.out.println("ADD-ON Version: "+getConfiguration().version);
				answer="I wrote the add-on info to cmd";
				break;
		
			//third command
			case "test_addon_print_parameters":
				System.out.println("ADD-ON TEST input parameters:");
				String parameter;
				
				//first 3 (0,1,2) parameters are the user input, command-type (addon) and command itself
				//so parameters start at 3!
				if (memories.length>3){
					for (int i=3; i<memories.length; i++){
						parameter = memories[i];
						System.out.println("parameter "+(i-2)+": "+parameter);
					}
					answer="I wrote the add-on test parameters to cmd";
					break;
				
				//no parameters given
				}else{
					answer="The add-on test worked but there were no parameters given";
					break;
				}
				
			//fourth command
			case "test_addon_complexer":
			//this is a test-command to demonstrate how ILA can interact during commands

				//if we have parameters already then use them
				if (memories.length>3){
					
					//check the first parameter in stage one (ok technically its stage zero :-p)
					//if (ILA.stage==0 & memories[3].matches( ILA_answers.confirmationWords() )){
					if (ILA.stage==0 & ILA_answers.isConfirmation(memories[3])){
						
						answer="ok what comes next? Feel free to ask anything!";
						//answerIt = ILA_answers.get("testaddon1a");
						ILA.state=3.0;	//tell ILA to ask for another parameter
						ILA.stage++;	//avoid getting stuck in this 'case' again
						ILA_speechControl.sphinxSTT.switchGrammar("dialog");		//dialog-grammar is either completely free (if grammar is off) or holds all the possible sentences teached by the user
						break TheSwitch;	//break to escape the 'case - normal break is enough here, it's just to demonstrate how it would work :-)
					}
					else if (ILA.stage==0){
						
						answer="ok then maybe next time.";
						//answerIt = ILA_answers.get("testaddon1b");
						ILA.state=0.0;	//tell ILA to go back to idle state
						ILA.stage=0;	//reset stage too
						//grammar is automatically set to default
						break;
					}
					
					//check the second parameter in stage two - at this point there should be either a second parameter or ILA aborted the whole thing because it didn't understand the input 
					if (ILA.stage==1 & memories.length>4){
						
						answer="ok, if this wasn't a test we would proceed with: "+memories[4];
						//answerIt = ILA_answers.get("testaddon2a");
						ILA.state=0.0;	//we are done
						ILA.stage=0;	//reset and go
						//grammar is automatically set to default
						break;
					}
					//reset because we failed to get the next parameter
					else{
						//usually you should not end up here because ILA will ask automatically 3 times for the next parameter before it quits the whole command
						answer="I wonder what went wrong. Try again later please.";
						//answerIt = ILA_answers.get("testaddon2b");
						ILA.state=0.0;	//we are done
						ILA.stage=0;	//reset and go
						//grammar is automatically set to default
						break;
					}
				}
				
				//if we don't have parameters ask for them
				else if (ILA.state==0.0){
					
					answer="testing addons. Shall we proceed?";
					//correct way for language specific answer: 	answerIt = ILA_answers.get("testaddon0") - where "testaddon0" would be an entry you made inside ILA_answers.get() method (name is arbitrary)
					ILA.state=3.0;	//tell ILA to ask for another parameter
					ILA_speechControl.sphinxSTT.switchGrammar("confirm");		//switch grammar 'cause we want to ask a yes/no answer (even if grammar is off, we can still go back if "settings->grammar on ILA question" is active)
					break;															//note that 'confirm' refers to the grammar file 'confirm_input_"languageCode".gramm'. You can simply create new files like 'myOwnGrammar_input_xy.gramm' and use them with "myOwnGrammar"
				}
				else{
					//usually you should not end up here because ILA will ask automatically 3 times for the next parameter before it quits the whole command
					answer="I wonder what went wrong. Try again later please.";
					//answerIt = ILA_answers.get("testaddon2b");
					ILA.state=0.0;	//we are done
					ILA.stage=0;	//reset and go
					//grammar is automatically set to default
					break;
				}
				
			//fifth command
			case "test_addon_switch_super_context":
			//set the super context to addon_test
				ILA_decisions.setSuperContext("addon_test");
				answer=ILA_answers.getFromFile(answerFile(), "test_addon_switch_super_context");		//"Alright I switched the super context to addon test."
				break;
			
			//sixth command
			case "test_addon_end_super_context":
			//return super context to default
				ILA_decisions.setSuperContext("default");
				answer=ILA_answers.getFromFile(answerFile(), "test_addon_end_super_context");			//"Alright I quit the addon super context test."
				break;
				
			//seventh command
			case "test_addon_super_context":
			//this is a test-command to demonstrate how ILA can work in a specific super context

				//if we have parameters already then use them
				if (memories.length>3){
					
					//check the first parameter in stage one (ok technically its stage zero :-p)
					//if (ILA.stage==0 & memories[3].matches( ILA_answers.confirmationWords() )){
					if (ILA.stage==0 & ILA_answers.isConfirmation(memories[3])){
						answer=ILA_answers.getFromFile(answerFile(), "test_addon_super_context1a");		//"sorry but there is nothing to do yet."
						ILA.state=0.0;	//back to idle
						ILA.stage=0;	//reset
						break TheSwitch;	//break to escape the 'case - normal break is enough here, it's just to demonstrate how it would work :-)
					}
					else if (ILA.stage==0){
						answer=ILA_answers.getFromFile(answerFile(), "test_addon_super_context1b");		//"ok maybe later."
						ILA.state=0.0;	//tell ILA to go back to idle state
						ILA.stage=0;	//reset stage too
						//grammar is automatically set to default
						break;
					}
				}	
				
				//if we don't have parameters ask for them
				else if (ILA.state==0.0){
					answer=ILA_answers.getFromFile(answerFile(), "test_addon_super_context0");		//"congratulation you reached the super context addon test. Proceed?"
					//correct way for language specific answer: 	answerIt = ILA_answers.get("testaddon0") - where "testaddon0" would be an entry you made inside ILA_answers.get() method (name is arbitrary)
					ILA.state=3.0;	//tell ILA to ask for another parameter
					ILA_speechControl.sphinxSTT.switchGrammar("confirm");		//switch grammar 'cause we want to ask a yes/no answer (even if grammar is off, we can still go back if "settings->grammar on ILA question" is active)
					break;														//note that 'confirm' refers to the grammar file 'confirm_input_"languageCode".gramm'. You can simply create new files like 'myOwnGrammar_input_xy.gramm' and use them with "myOwnGrammar"
				}
				else{
					//usually you should not end up here because ILA will ask automatically 3 times for the next parameter before it quits the whole command
					answer="I wonder what went wrong. Try again later please.";
					//answerIt = ILA_answers.get("testaddon2b");
					ILA.state=0.0;	//we are done
					ILA.stage=0;	//reset and go
					//grammar is automatically set to default
					break;
				}
	
		}
		
		//no hit - this should never happen because this class and method was called because the command list
		//says it will find the correct command here ...
		return answer;
		
	}

}
