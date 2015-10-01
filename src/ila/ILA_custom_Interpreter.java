package ila;

/***************************************************************************
 * ILA_custom_Interpreter can be used to identify commands by analyzing the
 * user input. Here anyone can implement his own ideas :)
 * The previous step is the 'memory'-check. If there is no known command
 * identical to the user input it will be redirected here.
 * If there is no result here the next step will be the hard-coded
 * interpreter. 'No result' should be returned as "". 
 *
 * @author Florian Quirin for I.L.A. project
 ***************************************************************************/

public class ILA_custom_Interpreter {
	
	static boolean inUse=false;		//use this custom interpreter's interpretInput(...) method in ILA queue
	
	//input should be anything from a clear command to a naturally spoken sentence (easy right? ^_^)
	//output needs to be string compatible to ILA's 'memory' structure: label;command-type;keyword;parameter1;parameter2;... 
	//e.g.:
	//who am I?;conversation;sayusername
	//search pictures;search;pictures;Bonobo apes
	//start program;command;openprogram;Calculator
	//get directions;search;direction;home;work
	
	public static String interpretInput(String user_said){
		String newMemory="";			//will be the resulting memory-command
		String orgIn=user_said;			//save original input just in case
		String param1,param2;			//temporary strings
		
		//normally one would start with tweaking the input by removing special characters and simplify as much as possible
		user_said=ILA_decisions.tweakString(user_said);		//this will automatically call language_tweakString(...) first (see below)
		
		//examples - feel free to implement this in a completely different manner, whatever works ...
		if (ILA.language.matches("en|oz|ca|in|uk")){
			
			//don't forget to set "inUse=true" to activate this branch (see top of this class)
			
			//a) simple command - "hey ILA I wonder if you know who I am"
			if (stringContains(user_said, new String[]{"who am I", "who I am", "what's my name", "do you know me"})){
				newMemory=("who am I;conversation;sayusername");
			}
			//b) more complex command - "please search pictures of Bonobo apes"
			else if (stringContains(user_said, "search pictures")){
				param1=whatsAfter("pictures", user_said);
				param1=param1.replaceAll("\\b(^of the|^of|^the)\\b", "").trim();
				newMemory=("search pictures;search;pictures;"+param1);
			}
			//c) even more complex command - "can you please get directions from here to work
			else if (stringContains(user_said, new String[]{"get directions", "show me the way"})){
				user_said=user_said.replaceAll("\\b(get directions|show me the way)\\b", "");
				param1=whatsAfter("from",user_said).replaceAll("\\b(to).*", "").trim();
				param2=whatsAfter("to",user_said).trim();
				newMemory=("get directions;search;direction;"+param1+";"+param2);
			}
		}
		
		//System.out.println("ILA - original input: "+orgIn);						//debug
		//System.out.println("ILA - custom interpretation result: "+newMemory);		//debug
		return newMemory;
	}
	
	//-------------------------------------Tools-----------------------------------------
	//these methods will be automatically called by ILA depending on your language setting
	//together with ILA_answers they make up the most important parts of localizing ILA
	
	//translate some keywords for tweaking - you can add other tweaking stuff too (special characters if needed etc.) 
	public static String language_tweakString(String userIn){
		if (ILA.language.matches("fr|es")){
			userIn=userIn.toLowerCase().trim();			//everything should be lower case
			userIn=userIn.trim().replaceAll("\\b(s'il vous plaît|por favor)\\b", "");	//remove please, it just makes everything more complicated ^^
			userIn=userIn.replaceAll("\\b(^bonjour|^hola)\\s\\b", "").trim();			//remove hello if user begins with it
		}
		//add whatever you think makes sense here ... :-)
		return userIn;
	}
	//this one is used before saving a string to a grammar file - depending on the dictionary you'll use you might have to handle special characters etc.  
	public static String language_tweakString_grammar(String userIn){
		//examples in French and Spanish:
		if (ILA.language.matches("fr|es")){
			userIn=userIn.toLowerCase().trim();			//everything should be lower case
			userIn=userIn.trim().replaceAll("\\b(s'il vous plaît|por favor)\\b", "[$1]");					//I tend to make a 'please' optional in grammar files ;-)
			userIn=userIn.replaceAll("\\b(^bonjour ila|^bonjour|^hola ila|^hola)\\s\\b", "[$1] ").trim();	//same for hello at start
		}
		//add whatever you think makes sense here ... :-)
		return userIn;
	}
	
	//translate time keywords to ILA-code - this is used when interpreting phrases that are supposed to include a date or time
	//like: tomorrow, today, in 10 minutes ...
	//example input: "in 2 days at 10 o'clock" -> expected ILA-code output: "in 2-DAYS at 10-TIME"
	//note: this can be arbitrarily complex and highly non trivial oO ... good luck ^^
	public static String time_tweaking(String userIn){
		//examples in Spanish:
		if (ILA.language.matches("es")){
			userIn=userIn.toLowerCase().trim();			//everything should be lower case
			userIn=userIn.replaceAll("(hacia las )(\\d+)", "$2-TIME");					//at x o'clock
			userIn=userIn.replaceAll("(a las )(\\d+)( de la mañana)\\b", "$2-TIME");	//at x o'clock in the morning
			userIn=userIn.replaceAll("(a las )\\s(\\d+)( de la noche)\\b", "$2-PM");	//at x o'clock in the evening
			userIn=userIn.replaceAll("(\\d)\\s(segundos)", "$1-SEC");		//in x seconds
			userIn=userIn.replaceAll("(\\d)\\s(minutos)", "$1-MIN");		//in x minutes
			userIn=userIn.replaceAll("(\\d)\\s(horas)", "$1-HRS");			//in x hours
			userIn=userIn.replaceAll("(\\d)\\s(días)", "$1-DAYS");			//in x days
			userIn=userIn.replaceAll("(\\d)\\s(meses)", "$1-MNTHS");		//in x months
			userIn=userIn.replaceAll("(\\d)\\s(años)", "$1-YRS");			//in x years
			userIn=userIn.replaceAll("\\b(pasado mañana)\\b", "the day after tomorrow");	//the day after tomorrow
			userIn=userIn.replaceAll("\\b(mañana)\\b", "tomorrow");							//tomorrow
			userIn=userIn.replaceAll("\\b(hoy)\\b", "today");								//today
		}
		return userIn;
	}
//removed - replaced by time_tweaking()
//	//translate time keywords to ILA-code - this is used when converting time keywords for timers (in theory this could be replaced with the one for reminders,...sometime)
//	//like: 10 minutes, 30 seconds, 2 hours ...
//	//example input: "set a timer for 5 minutes" -> expected ILA-code output: "set a timer for 5 mm"
//	public static String time2_tweaking(String number){
//		//examples in Spanish:
//		if (ILA.language.matches("es")){
//			number=number.toLowerCase().trim();			//everything should be lower case
//			number=number.toLowerCase().replaceAll("\\b(día|días)\\b", "dd");			//day, days
//			number=number.toLowerCase().replaceAll("\\b(hora|horas)\\b", "hh");			//hour, hours
//			number=number.toLowerCase().replaceAll("\\b(minuto|minutos)\\b", "mm");		//minute, minutes
//			number=number.toLowerCase().replaceAll("\\b(segundo|segundos)\\b", "ss");	//second, seconds
//		}
//		return number;
//	}
	
	//translate trigger keywords to ILA-code - this is used when asking ILA to do stuff "on start" or "when you close"
	//example input: "can you remind me to grab the keys when I leave" -> trigger keyword: ON_LEAVE
	public static String trigger_tweaking(String userIn){
		//examples in Spanish (sorry my Spanish is horrible ^^):
		if (ILA.language.matches("es")){
			userIn = userIn.replaceAll("\\b(en el arranque)\\b",Tools_Reminder.ON_START);					//when ILA starts
			userIn = userIn.replaceAll("\\b(en la salida)\\b",Tools_Reminder.ON_EXIT);						//when ILA exits
			userIn = userIn.replaceAll("\\b(cuando quieras|en algun momento)\\b",Tools_Reminder.ON_IDLE);	//when ILA has nothing to do
			userIn = userIn.replaceAll("\\b(cuando vaya|cuando me dejo)\\b",Tools_Reminder.ON_LEAVE);		//when user says "I'm leaving"
			userIn = userIn.replaceAll("\\b(cuando vuelva)\\b",Tools_Reminder.ON_RETURN);					//when user says "I'm back"
		}
		return userIn;
	}
	
	//translate location keywords to ILA-code - this is used when asking ILA to find locations like "home", "work" etc. 
	//example input: "get directions from home to work" -> location keywords: "home", "work"
	public static String location_tweaking(String userIn){
		//examples in Spanish:
		if (ILA.language.matches("es")){
			userIn=userIn.replaceAll("\\b(casa|mi casa)\\b", ILA.home);								//home, my home, my place, where I live ...
			userIn=userIn.replaceAll("\\b(trabajo|mi trabajo)\\b", ILA.workplace);					//work, my work, where I work ...
			userIn=userIn.replaceAll("\\b(ubicación|mi sitio|este lugar)\\b", ILA.myLocation());	//here, my location, where I am ...
			userIn=userIn.trim();
		}
		return userIn;
	}
	
	//check strings for matching words
	private static boolean stringContains(String userIn, String matchStr) {
		return ILA_decisions.stringContains(userIn, matchStr);
	}
	private static boolean stringContains(String userIn, String[] containsOneOf) {
		return ILA_decisions.stringContains(userIn, containsOneOf);
	}
	//check strings for words before "key", after "key" and between "key-A" and "key-B"
	private static String whatsBefore(String after, String full){
		return ILA_decisions.whatsBefore(after, full);
	}
	private static String whatsAfter(String before, String full){
		return ILA_decisions.whatsAfter(before, full);
	}
	private static String whatsBetween(String before, String after, String full){
		return ILA_decisions.whatsBetween(before, after, full);
	}
}
