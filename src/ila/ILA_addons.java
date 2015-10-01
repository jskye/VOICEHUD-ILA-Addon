package ila;

import java.util.ArrayList;
import java.util.Hashtable;

/***************************************************************************
 * ILA_addons is the main file managing new abilities/add-ons of ILA.  
 * Add-ons can be anything from the integration of a home automation API to 
 * a new calendar GUI or extended conversations ... whatever comes to your
 * mind! :-)
 * An add-on will be triggered by a keyword like "test_addon" (see example)
 * that can be used when teaching ILA new stuff. During load-up every add-on
 * command will be automatically added to the list of commands in ILA's teach
 * GUI and can then be used to teach ILA the new ability.
 * 
 * @author Florian Quirin for I.L.A. project
 ***************************************************************************/
public class ILA_addons {
	
	//handle all add-ons
	//these lists are created automatically at startup out of all the Add-on jar files in the 'Addons' folder
	static ArrayList<String> list_of_addons = new ArrayList<String>();					//keep track of add-ons active
	static ArrayList<String> list_of_commands = new ArrayList<String>();				//keep track of commands - note: must be all small letters!
	static ArrayList<String> list_of_command_descriptions = new ArrayList<String>();	//keep track of command descriptions
	static Hashtable<String, ILA_addon_interface> classes = new Hashtable<String, ILA_addon_interface>(); 	//used to cache already defined classes  
	
	public static String answer(String usaid) {
		
		//this will be the answer ILA gives you - usually the sub-commands will call ILA_answers.get("sub-command-i") for language specific answers
		String answerIt="";
		
		String memories[]=ILA_decisions.splitMemory(ILA.short_memory);
		//memories hold the info about the user input and command type:
		//memories[0]:	what the user said or what ILA interpreted
		//memories[1]:  command type that lead here, in this case "addon"
		//memories[2]:  sub-command type inside this method, e.g. "test_addon"
		//memories[3]:  parameters to the command if required. Can also be acquired inside the sub-command by using ILA.state and ILA.stage
		//memories[4..] more parameters if required. Can all be asked inside sub-command
		
		//clean up a bit, just in case
		if (memories.length>3){
			for (int i=3; i<memories.length; i++){
				memories[i]=memories[i].trim();
				//one could add more here but usually its done inside the command
				//System.out.println("input parameter "+(i-2)+": "+memories[i]);		//debug
			}
		}
		
		//set context - very general - one can specialize this in the command itself
		ILA_decisions.setContext(usaid);
		
		//--------CHECK ALL ADD-ONs FOR THE COMMAND AND EXECUTE IT---------
		
		//first check for open parameters and custom questions which can extend the command before going into the actual request
		//depending on how you define your open parameter command this can also switch grammar
		answerIt=ILA_decisions.identifyOpenParameter(memories);
		
		//in case there was no open parameter continue with request ...
		if (answerIt.matches("")){
		
			//check add-on list for the given add-on command
			if (list_of_commands.contains(usaid.toLowerCase())){
				answerIt=classes.get(usaid.toLowerCase()).callCommand(usaid.toLowerCase(), memories);
			}
			//ILA expected to find the command here but did not oO - this will probably never happen but just in case notify the user
			else{
				//TODO: check and improve this
				answerIt=ILA_answers.get("customs4");
			}
			
		}
		return answerIt;
	}

}
