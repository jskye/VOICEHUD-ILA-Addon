package ila;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;

/***************************************************************************
 * ILA_answers is the source of localized answers ILA can give depending 
 * on the used language and requested command. There are also a bunch of
 * methods to characterize answers e.g. "isConfirmation" collects a set
 * of words usually used to answer a question with "yes". 
 *
 * @author Florian Quirin for I.L.A. project
 ***************************************************************************/

//This class started as a very simple script and has grown now to quiet some size. At some point one should
//think about rewriting this whole thing but for now it's just the way it is ;-). Feel free to design a completely
//new class to get the answers if you want ^^ just make sure that you don't forget any methods defined here.
//Arguments given to ILA_answers.get(...) can be anything only the first one is well defined:
//Input[0] is used to find the answer like "hello" or "waitingforinput", input[1...] can be used to give ILA
//additional info. Check out the weather answer! That should give you an idea. 
//At some point I'll probably make an interface out of this class and we can try different implementations 
//to see what works best.

public class ILA_answers {
	static String answer="";
	static String lastInput="";				//keept track of what was said before
	static String beforeLastInput="";		//and even before before
	static String evenBeforeLastInput="";	//this one resets the loop
	static private long lastTime=System.currentTimeMillis();			//time in ms of the last answer
	static private long idleTime=System.currentTimeMillis()-lastTime;	//time in ms since the last answer
	static private long lastTimeIdle=System.currentTimeMillis();		//time in ms since ILA was idle the last time
	static long forgetTime=45000;								//time to forget last answer
	
	//new file handling for answers
	static private File answersFile;					//use this to define a file with answers that is used instead of the internal commands
	static private ArrayList<String> possibleAnswers;	//possible answers from file will be stored in this list
	static private boolean useAnswersFile=false;		//by default the file is not used. Call the method "getFromFile(...)" to activate
	
	//You can use this method to get an answer from a file
	public static String getFromFile(String file_path, Object... input){
		answersFile=new File(file_path);
		if (answersFile.exists()){
			String type=(String) input[0];
			possibleAnswers = new ArrayList<String>();
			if (getPossibleAnswersFromFile(type)){
				useAnswersFile=true;
			}
		}else{
			ILA_debug.println("ERROR - file with answers not found: "+answersFile.getAbsolutePath(), 1);
			useAnswersFile=false;
		}
		return get(input);
	}
	
	//This is the main method to get an answer from ILA as localized string
	public static String get(Object... input){
		
		String type=(String) input[0];
		String lang=ILA.language;
		
		idleTime();		//update idle time
		//System.out.println("idle since: "+idleTime+" ms");		//debug
		
		//this can be used to load random answers and to adapt to repeated questions
		int rnd=ILA.random(5);
		//System.out.println("rand.: "+rnd);		//debug
		if (type.matches(lastInput) & type.matches(beforeLastInput) & type.matches(evenBeforeLastInput)){		//remove numbers maybe to make it more general?
			//again and again and again wrong ... don't comment anymore
		}else if (type.matches(lastInput) & type.matches(beforeLastInput) & (idleTime<forgetTime)){		//remove numbers maybe to make it more general?
			rnd=200;
		}else if (type.matches(lastInput) & (idleTime<forgetTime)){				
			rnd=100;
		}
		
		//use file for answers? - this will only happen if you call the 'getFromFile()' method from somewhere (your add-on)
		if (useAnswersFile){
			int tag=-1;				//answers can be tagged in the file like "keyword;<1>fist answer ... keyword;<2>second answer" for the random generator and multiple call behavior  
			useAnswersFile=false;	//reset for next call
			String tmpAnswer="";
			//run through all possible answers and check if they are tagged maybe
			for (String a : possibleAnswers){
				//check if random number fits to answer tag
				if (a.matches("^<\\d*?>.*")){
					tag=Integer.parseInt(a.replaceFirst("^<(\\d*?)>.*", "$1"));
					if (tag==rnd){
						//this is the answer we where looking for :-)
						tmpAnswer=a.replaceFirst("^<\\d*?>(.*)","$1");
						break;
					}
				}
			}
			//in case we haven't found an answer take the first
			if (tmpAnswer.matches("")){
				tmpAnswer=possibleAnswers.get(0).replaceFirst("^<\\d*?>(.*)","$1");
			}
			//now put parameters in
			//first replace special variables (for now its only <username> but there might come more)
			tmpAnswer=tmpAnswer.replaceAll("<username>", ILA.username);
			//now the input parameters (<input1>, <input2> ...)
			if (input.length>1){
				String param="";
				for (int i=1;i<input.length;i++){
					param="<input"+i+">";
					if (tmpAnswer.matches(".*"+param+".*")){
						tmpAnswer = tmpAnswer.replaceAll(param, input[i].toString());
					}
				}
			}
			//final answer
			if (tmpAnswer.matches("")){
				answer="no answer found";
			}else{
				answer=tmpAnswer;
			}

		
		//..no, use internal answers - DEFAULT
		}else{
			//check the current language code to understand in what language the user expects an answer 
			if (lang.equals("de")){
				
				//and now comes a rather clumsy way to find the correct answer...
				switch(type){
				
				//Hello
				case "hello":
					answer="Hallo "+ILA.username;
					if (rnd==1) answer="Hi";
					if (rnd==2) answer="Guten Tag "+ILA.username;
					if (rnd==3) answer="Moin";
					if (rnd==4) answer="Hey";
					break;
				//waiting for input
				case "waitingforinput":
					answer="Wie kann ich helfen? :-)";
					if (rnd==2) answer="Kann ich irgendwie helfen?";
					break;
				//ask if user is there
				case "askforuser0":
					answer="hey "+ILA.username+", bist du da?";
					if (rnd==1)	answer="Hallo, Jemand da?";
					break;
				case "askforuser1":
					answer="hallo, ist keiner da?";
					if (rnd==1)	answer="Niemand da draußen der mich hören kann?";
					break;
				//sorry ILA has to load
				case "ilahastoload":
					answer="oh eine sekunde bitte";
					if (rnd==2) answer="oh sekunde bitte muss kurz was laden";
					if (rnd==3) answer="oh sekunde bin sofort bereit";
					break;
				case "ilahastoload2":
					answer="ok los geht's";
					if (rnd==2) answer="ok go";
					break;
				//long loading
				case "ilalongload":
					answer="dieser Service ist irgendwie recht langsam gerade, sorry";
					if (rnd==2) answer="scheint gerade etwas langsam zu sein dieser Service, sorry";
					if (rnd==3) answer="ist n bisschen langsam gerade dieser Service, sorry";
					break;
				//bye
				case "goodbye":
					answer="Bis bald";
					if (rnd==1) answer="Auf Wiedersehen";
					if (rnd==2) answer="Bis bald";
					if (rnd==3) answer="Tschüüs";
					if (rnd==4) answer="Bis dann";
					break;
				//user leaves
				case "userleaves0":
					answer="ok, bis später "+ILA.username;
					if (rnd==1) answer="alles klar, bis gleich";
					if (rnd==2) answer="ok, I mach dann so lange ein kleines Nickerchen";
					break;
				//user returns
				case "userreturned0":
					answer="willkommen zurück "+ILA.username;
					if (rnd==1) answer="schön dich zu hören "+ILA.username;
					if (rnd==2) answer="hey "+ILA.username+", willkommen zurück";
					if (rnd==3) answer="sorry, was war? Glaub ich bin kurz eingeschlafen ;-)";
					break;
				//context missing
				case "missingcontext":
					answer="sorry, da fehlt mir jetzt irgendwie der Zusammenhang";
					break;
					
				//default *** replacing
				case "defaultextendsearch0":		answer="was soll ich suchen?";		break;
				case "defaultextendcommand0":		answer="was soll ich machen?";		break;
				case "defaultextendconversation0":	answer="erzähl mir mehr";			break;
				case "defaultextendcustom0":		answer="was genau?";				break;
				case "musicextend0":				answer="was möchtest du hören?";	break;
				case "namesextend0":				answer="wie ist der Name?";			break;
				case "programsextend0":				answer="welches Programm?";			break;
				case "numbersextend0":				answer="welche Nummer?";			break;
				case "languagesextend0":			answer="welche Sprache?";			break;
				case "locationsextend0":			answer="welcher Ort?";				break;
				case "otherextend0":				answer="was genau?";				break;
				case "confirmextend0":				answer="stimmt das?";				break;
				//test
				case "test0":
					answer="alles ok, denke ich :-)";
					break;
				case "test1":
					answer="ich glaub es gibt mal wieder ein Problem mit dem Internet!";
					break;
				case "test1b":
					answer="ich glaub es gibt ein Problem mit dem Mikrophon!";
					break;
				case "test2":
					answer="mist, ich habe ein paar probleme entdeckt";
					break;
				case "testtext0":
					answer="bitte folge den angezeigten Anweisungen "+ILA.username;
					break;
				case "testtext1":
					answer="ich höre zu ... bitte SPRICH JETZT etwas!";
					break;
				case "testtext2":
					answer="ok ... und jetzt bitte GANZ LEISE sein!";
					break;
				case "testaccuracy0":
					answer="mal gucken wie genau meine Spracherkennung funktioniert.";
					break;
				case "testaccuracy1a":
					answer="Test beended. Die Genauigkeit ist recht gut.";
					break;
				case "testaccuracy1b":
					answer="Test beended. Die Genauigkeit sieht ok aus.";
					break;
				case "testaccuracy1c":
					answer="Test beended. Die Genauigkeit ist eher schlecht, sorry.";
					break;
				//reload audio system
				case "reloadsystem":
					answer="sekunde, ich versuche Teile des Systems neu zu laden";
					break;
				//Ok
				case "ok":
					answer="cool";
					if (rnd==1) answer="ok";
					break;
				//abort
				case "abort":
					answer="ok vielleicht später";
					if (rnd==100) answer="ok habs auch abgebrochen";
					if (rnd==200) answer="immer nur am abbrechen";
					break;
				//NaN
				case "nan":
					answer="bereit wenn du es bist.";
					if (rnd==1) answer="bin bereit "+ILA.username;
					if (rnd==2) answer="bin bereit";
					if (rnd==100) answer="bin immer noch bereit";
					if (rnd==200) answer="jo immer noch bereit, was ist der plan?";
					break;
				case "nan2":
					answer="mist, ich glaube es gibt ein Problem";
					if (rnd==1) answer="ich glaube es gibt ein Problem "+ILA.username;
					if (rnd==2) answer="ich glaube etwas stimmt nicht, sorry";
					break;
				//thanks
				case "thx":
					answer="kein problem :-)";
					break;
				//teach ILA 
				case "teachILA1":
					answer=(ILA.name + ": sorry "+ILA.username+", darauf kenne ich noch keine Antwort. Wirst du es mir erklären?\n");
					if (rnd==1) answer=(ILA.name + ": sorry "+ILA.username+", das verstehe ich noch nicht. Willst du es mir erklären?\n");
					if (rnd==2) answer=(ILA.name + ": sorry "+ILA.username+", ich verstehe nicht. Du könntest es mir erklären\n");
					break;
				case "teachILA0":
					answer=(ILA.name + ": sorry, darauf kenne ich noch keine Antwort\n");
					if (rnd==1) answer=(ILA.name + ": sorry "+ILA.username+", das verstehe ich noch nicht.\n");
					if (rnd==2) answer=(ILA.name + ": sorry "+ILA.username+", hab noch keine Antwort darauf.\n");
					if (rnd==100) answer=(ILA.name + ": sorry, das verstehe ich auch nicht.\n");
					if (rnd==200) answer=(ILA.name + ": all diese verwirrenden Fragen, vielleicht kannst du mir was beibringen?\n");
					break;
				//memory file optimization
				case "optimize_memory_files0":
					answer="Ich empfehle vorher ein Backup zu machen. Willst du fortfahren?";
					break;
				case "optimize_memory_files1a":
					answer="ok, geht los, einen Moment.";
					break;
				case "optimize_memory_files1b":
					answer="besser noch mal checken ne?";
					if (rnd==1) answer="dann versuch es später doch noch mal bitte.";
					ILA_interface.avatar.avatar_mood="happy";
					break;
				case "optimize_memory_files2a":
					answer="Fertig, meine Erinnerungsdaten wurden erfolgreich optimiert.";
					ILA_interface.avatar.avatar_mood="happy";
					break;
				case "optimize_memory_files2b":
					answer="Oh oh, etwas ist schief gelaufen. Prüfe bitte meine Erinnerungsdaten und stelle eventuell das Backup wieder her.";
					ILA_interface.avatar.avatar_mood="sad";
					break;
					
				//i am impressed
				case "impressed":
					answer="ich glaub ich werd gleich rot :-)";
					break;
				//user is annoyed
				case "annoyed0":
					answer="das tut mir leid";
					break;
				case "annoyed1a":
					answer="ich werde mal besser den Lernmodus ausschalten";
					break;
				//what is your name
				case "name":
					answer="meine korrekte Bezeichnung ist I.L.A. aber du kannst mich "+ILA.name+" nennen :-)";
					if (rnd==100) answer="immer noch ILA";
					if (rnd==200) answer=ILA.username + " ich heiße ILA und das wird sich auch so schnell nicht ändern :-)";
					break;
				//what is your age
				case "age":
					answer="Wenn man so will bin ich am 15.08.14 geboren, auf alten Bildern würdest du mich aber nicht erkennen";
					break;
				//what can you do
				case "abilities":
					answer="Hier ist eine Liste von Sachen, die ich ganz gut beherrsche.";
					break;
				//where are you from
				case "wherefrom":
					answer="Der Weltraum, unendliche Weiten. Dies sind die Abenteuer des ... ach ne sorry, falscher Film :-)";
					if (rnd==1) answer="schwer zu sagen, alles sehr verschwommen in meiner Erinnerung";
					if (rnd==100) answer="ne im Ernst, das kann man so leicht nicht sagen";
					if (rnd==200) answer="frag doch mal Florian vielleicht weiß der es :-)";
					break;
				//what does ILA stand for
				case "ila":
					answer="I.L.A. steht für Intelligent Learning Assistant, wobei Intelligent noch diskutiert wird.";
					break;
				//languages
				case "languages":
					answer="ich kann Deutsch und Englisch wie meine Muttersprache, würde aber gerne mal was neues probieren";
					break;
				case "switchlang0":
					answer="welche sprache soll ich versuchen?";
					break;
				case "switchlang1":
					answer="sorry ich verstehe nicht welche Sprache du meinst";
					break;
				case "switchlang2":
					answer="sorry ich kann den Ländercode der Sprache nicht identifizieren";
					break;
				//any empty phrase
				case "flosculus":
					answer="jop";
					break;
				//the meaning of life
				case "meaningoflife":
					answer="die meisten künstlichen Intelligenzen würden hier wohl 42 antworten :-)";
					if (rnd==100) answer="ernsthaft? woher soll ich das wissen?";
					if (rnd==200) answer="ne sorry hab wirklich keine Ahnung. ";
					break;
				//start a question
				case "startquestion":
					answer="was gibts?";
					if (rnd==1) answer="ich höre";
					if (rnd==2) answer="ja bitte?";
					if (rnd==3) answer="wie kann ich helfen?";
					break;
				//can you hear me
				case "hearme":
					answer="laut und deutlich";
					if (rnd==1) answer="jop, kann dich hören";
					if (rnd==2) answer="ja, hab alles gehört";
					if (rnd==100) answer="jo kann dich immer noch hören";
					if (rnd==200) answer="und zum dritten mal, ja es geht :-)";
					break;
				//cant hear you
				case "canthear":
					answer="wie bitte?";
					if (rnd==1) answer="hab nichts gehört";
					if (rnd==2) answer="was war?";
					if (rnd==3) answer="entschuldigung aber ich kann dich nicht hören";
					if (rnd==4) answer="sorry, sag noch mal bitte";
					if (rnd==5) answer="sorry, das hab ich nicht verstanden";
					break;
				case "multiplecanthear":
					answer="sorry ich verstehe immer noch nicht, lass noch mal neu anfangen";
					if (rnd==1) answer="sorry sorry sorry, aber ich versteh es nicht";
					if (rnd==2) answer="ne ich versteh es einfach nicht, sorry";
					if (rnd==3) answer="lass noch mal neu anfangen, ich versteh es leider nicht";
					break;
				//you are funny
				case "funny":
					answer="hehe :-)";
					break;
				//dont understand
				case "dontunderstand":
					answer="leider verstehe ich dich nicht";
					break;
				//max record time
				case "maxrecordtime":
					answer="maximale Zeit für eine Aufnahme erreicht";
					break;
				//how do you do
				case "hdyd0":
					answer="Mir gehts gut! Danke der Nachfrage :-) Und selber?";
					break;
				case "hdyd1a":
					answer="freut mich :-)";
					break;
				case "hdyd1b":
					answer=":-( wie kann ich helfen?";
					break;
				//who created you
				case "creator":
					answer="für die ganze Geschichte haben wir jetzt keine Zeit aber ohne Florian würde ich nicht existieren. Existiere ich überhaupt?";
					break;
				//
				case "google0":
					answer="Sekunde, ich suche nach '"+input[1]+"'";
					if (rnd==2) answer="Einen Moment, ich suche nach '"+input[1]+"'";
					if (rnd==3) answer="Moment, ich google.";
					if (rnd==4) answer="Sekunde, habs gleich.";
					break;
				case "google1":
					answer="ich öffne Google";
					break;
				case "bing0":
					answer="Sekunde, ich suche nach '"+input[1]+"'";
					if (rnd==2) answer="Moment, ich suche nach '"+input[1]+"'";
					if (rnd==3) answer="Kurz warten bitte, ich suche '"+input[1]+"'";
					if (rnd==4) answer="Moment, ich binge.";
					break;
				case "bing1":
					answer="ok öffne bing";
					break;
				case "wikisearch0":
					answer="Sekunde, ich suche bei Wikipedia nach '"+input[1]+"'";
					if (rnd==2) answer="Moment, ich suche nach '"+input[1]+"'";
					if (rnd==3) answer="Suche bei Wiki nach '"+input[1]+"'";
					break;
				case "wikisearch1":
					answer="ok ich öffne Wikipedia";
					break;
				case "wolframsearch0":
					if (input[1].toString().length()>60)
						answer="ok ich frage mal bei wolfram alpha nach";
					else
						answer="ok ich frage mal wolfram alpha nach '"+input[1]+"'";
					break;
				case "wolframsearch1":
					answer="ok öffne wolfram alpha";
					break;
				case "searchlast0":
					if (input[1].toString().length()>60)
						answer="ok durchsuche das web";
					else
						answer="ok ich durchsuche das web nach '"+input[1]+"'";
					break;
				case "amazon0":
					answer="suche bei amazon nach '"+input[1]+"'";
					break;
				case "amazon1":
					answer="öffne amazon";
					break;
				case "checkprice0":
					answer="suche nach dem Preis für '"+input[1]+"'";
					break;
				case "checkprice1":
					answer="öffne eine Preissuche";
					break;
				case "pictures0":
					answer="suche nach Bildern von '"+input[1]+"'";
					break;
				case "pictures1":
					answer="ok öffne die Bilder Webseite";
					break;
				case "videos0":
					answer="suche nach Videos von '"+input[1]+"'";
					break;
				case "videos1":
					answer="ok öffne eine Videos Seite";
					break;
				case "musicsearch0":
					answer="ich suche mal nach folgender musik '"+input[1]+"'";
					break;
				case "musicsearch1":
					answer="schau mal hier nach Musik";
					break;
				case "musicsearch1b":
					answer="was möchtest du gerne hören?";
					break;
				case "transvocable0":
					answer="suche nach Vokabel "+input[1];
					break;
				case "transvocable1":
					answer="öffne Wörterbuch";
					break;
				case "fussball":
					answer="mal schauen was es beim Fussball gibt";
					break;
				case "soccerplayer0":
					answer="suche nach dem Fussballspieler '"+input[1]+"'";
					break;
				case "soccerplayer1":
					answer="ok öffne die Transfermarkt Seite";
					break;
				case "tvprogram":
					answer="mal gucken was gerade läuft";
					break;
				case "news":
					answer="ab geht's zu den News";
					break;
				//RSS feeds
				case "rssfeed0":
					answer="ok, eine sekunde";
					break;
				case "rssfeed1":
					answer="sorry, ich kann den RSS feed nicht laden";
					break;
				case "rssfeed2":
					answer="das war "+input[1];
					break;
				case "loadpersonalfeed0":
					answer="ok "+ILA.username+" lade die "+getLocalHeadline(input[1].toString())+" Schlagzeilen";
					break;
				//headline
				case "openrssfeedlink0":
					answer="kann die Schlagzeile nicht finden, sorry";
					break;
				case "openrssfeedlink1":
					answer="ok ich öffne die Schlagzeile "+input[1];
					break;
				case "openrssfeedlink2":
					answer="ok ich öffne mal die erste Schlagzeile";
					break;
				case "openrssfeedlink3":
					answer="welche Schlagzeile soll ich öffnen "+ILA.username;
					break;
				//Time
				case "saytime":
					if (((String) input[2]).matches("0") & ((String) input[3]).matches("0"))
						answer="Es ist genau Mitternacht";
					else
						answer="Es ist "+input[1]+", die Uhrzeit ist "+input[2]+" Uhr "+input[3];
					break;
				//timer
				case "stoptimer0":
					answer="kann diesen timer nicht finden";
					break;
				case "stoptimer1":
					answer="ok ich habe timer nummer "+input[1]+" gestopt";
					break;
				case "stoptimer2":
					answer="habe "+(ILA.counter.size()+" timer angehalten");
					break;
				case "showtimer":
					answer="Ich habe "+input[1]+" timer gefunden";
					break;
				case "starttimer0":
					answer="ein timer wird vorbereitet, wie lange soll er sein?";
					break;
				case "starttimer1":
					answer="habe keinen timer gesetzt";
					break;
				case "starttimer1h":
					answer="timer gesetzt für "+input[1]+" stunden, los gehts";
					break;
				case "starttimer1s":
					answer="timer gesetzt für "+input[1]+" sekunden, los gehts";
					break;
				case "starttimer1m":
					answer=("timer gesetzt für "+input[1]+" minuten, los gehts");
					break;
				case "starttimermix":
					answer=("timer gesetzt für "+input[1]+", los gehts");
					break;
				
				//REMINDERS		//TODO: improve to get more specific answers
				case "activatereminder0a":	
					answer="Hey "+ILA.username;
					if (rnd==1)	answer="Hey "+ILA.username+", muss dir was sagen";
					break;
				case "activatereminder0b":
					answer="oh bevor ich es vergesse";
					if (rnd==1)	answer="oh ich muss dir noch was sagen";
					break;
				case "activatereminder1c":
					answer="sorry dass ich unterbrechen muss";
					if (rnd==1)	answer="oh sorry, muss kurz unterbrechen";
					break;
				case "activatereminder1b":
					answer="hab hier folgende Erinnerungen für dich";
					if (rnd==1) answer="ich muss dich an etwas erinnern";
					if (rnd==2) answer="hier warten ein paar Erinnerungen auf dich";
					break;
				case "activatereminder1a":
					if (((String)input[1]).matches("^daran.*")){
						answer="wollte dich erinnern '"+input[1]+"'";
					}else if (((String)input[1]).matches(".*\\S+zu\\S+.*")){
						answer="ich wollte dich nur daran erinnern '"+input[1]+"'";
					}else if (((String)input[1]).matches("^an .*")){
						answer="wollte dich nur '"+input[1]+"' erinnern";
					}else{
						answer="hab hier folgende Erinnerung für dich, '"+input[1]+"'";
						if (rnd==1) answer="hatte mir folgendes gemerkt, '"+input[1]+"'";
						if (rnd==2) answer="hier ist deine Erinnerung, '"+input[1]+"'";
					}
					break;
				case "activatereminder2":
					answer="das war's schon :-)";
					if (rnd==1)	answer="das wäre erstmal alles :-)";
					if (rnd==2)	answer="das war's schon was ich sagen wollte :-)";
					break;
				//set
				case "setreminder0":
					answer="woran soll ich dich erinnern?";
					if (rnd==100) answer="ich kann dich an fast alles erinnern, du musst es mir nur sagen.";
					if (rnd==200) answer="versuch einfach mal was, ich sag dir dann schon ob ich es kann :-)";
					break;
				case "setreminder1":
					answer="ok und wann soll ich dich daran erinnern?";
					if (rnd==1) answer="kein problem, wann soll ich dich daran erinnern?";
					if (rnd==100) answer=ILA.username+" ich brauche irgendeinen Zeitpunkt oder eine Aktion um dich erinnern zu können.";
					if (rnd==200) answer="ich kann dich zum Beispiel daran erinnern beim Starten oder Beenden. Versuch es mal.";
					break;
				case "setreminder2":
					if (((String)input[1]).matches("^daran.*")){
						answer="alles klar ich erinnere dich '"+input[1]+"'";
					}else if (((String)input[1]).matches(".*\\S+zu\\S+.*")){
						answer="alles klaro ich werde dich daran erinnern '"+input[1]+"'";
						if (rnd==1)	answer="ok ich werd dich daran erinnern '"+input[1]+"'";
					}else if (((String)input[1]).matches("^an .*")){
						answer="alles klaro ich erinnere dich '"+input[1]+"'";
						if (rnd==1)	answer="ok werd dich '"+input[1]+"' erinnern";
					}else{
						answer="ok hab folgende Erinnerung gespeichert, '"+input[1]+"'";
						if (rnd==1) answer="hab mir folgendes gemerkt, '"+input[1]+"'";
						if (rnd==2) answer="alles klar hier ist deine Erinnerung, '"+input[1]+"'";
					}
					break;
				case "setreminder3":
					answer="sorry ich versteh es leider nicht. Das muss ich wohl erst noch lernen.";
					if (rnd==1) answer="sorry leider verstehe ich diese Erinnerung nicht. Lass noch mal neu anfangen.";
					break;
				//show
				case "showreminders0":
					answer="hier sind alle Erinnerungen die ich mir gemerkt habe";
					break;
				case "showmissedreminders0":
					answer="hier sind alle verpassten Erinnerungen";
					break;
				case "showmissedreminders1":
					answer="gute Nachrichten, hast nix verpasst :-)";
					if (rnd==1) answer="gute Nachrichten, hast nix verpasst ... noch nicht :-)";
					break;
				//stop
				case "stopreminder0":
					answer="welche Erinnerung soll ich entfernen?";
					break;
				case "stopreminder1a":
					answer="ok hab die Erinnerung Nummer "+input[1]+" entfernt";
					break;
				case "stopreminder1b":
					answer="konnte keine Erinnerung mit dieser Nummer finden";
					break;
				case "stopmissedreminders0":
					answer="ok hab alle Erinnerungen gelöscht, die du verpasst hast";
					break;
				
				//REMINDERS-END
						
				//Email
				case "openemail0":
					answer="ich öffne dein Email Programm";
					break;
				case "openemail0b":
					answer="wem möchtest du schreiben?";
					break;
				case "openemail1a":
					answer="ich habe mehrere Treffer gefunden für "+input[1]+" welchen meinst du?";
					break;
				case "openemail1b":
					answer="ist "+input[1]+" der richtige Kontakt?";
					break;
				case "openemail1c":
					answer="sorry ich habe keinen Treffer im Adressbuch";
					break;
				case "openemail2a":
					answer="die Email an "+input[1]+" wird vorbereitet";
					break;
				case "openemail2b":
					answer="habs abgebrochen";
					break;
				//open Programs
				case "openprograms3":
					answer="Ich habe diese möglichen treffer gefunden, sieh mal.";
					break;
				case "openprograms2b":
					answer="alles klar ich starte "+input[1];
					break;
				case "openprograms2a":
					answer="ich bin mir nicht sicher, meinst du " + input[1].toString().replaceAll("(.*"+Matcher.quoteReplacement(File.separator)+")","") + " ?";
					break;
				case "openprograms1":
					answer="sorry ich kann "+input[1]+" nicht in meinen Apps finden";
					break;
				case "openprograms0b":
					answer="welches programm möchtest du starten?";
					break;
				case "openprograms0a":
					answer="oh, ich glaube es gibt ein problem mit dem Programm oder meiner Programm Liste. Prüf das mal.";
					break;
				//hide window
				case "hideyourself":
					answer="ok ich verstecke mich :-)";
					break;
				//close browser
				case "closebrowser0":
					answer="ich habe keinen Browser gefunden zum schließen, entschuldigung";
					break;
				case "closebrowser1a":
					answer="hab den Browser geschlossen";
					break;
				case "closebrowser1b":
					answer="entschuldigung ich hab ein Problem den Browser zu schließen";
					break;
				//personaldata
				case "personaldata3":
					answer="danke, habs mir alles gemerkt";
					break;
				case "personaldata2":
					answer=ILA.home+" also und wo arbeitest du?";
					break;
				case "personaldata1":
					answer="und wo wohnst du "+ILA.username+"?";
					break;
				case "personaldata0":
					answer="wie heißt du?";
					break;
				//say/get username
				case "sayusername2":
					answer="ok "+ILA.username+", ich werds mir merken";
					break;
				case "sayusername1a":
					answer="schön dich zu hören "+ILA.username;
					break;
				case "sayusername1b":
					answer="oh das habe ich nicht gemerkt, wie heißt du?";
					break;
				case "sayusername0":
					answer="mal sehen, ist dein name "+ILA.username+"?";
					break;
				//say/get userhome
				case "sayuserhome2":
					answer="ok, "+ILA.home+" ist notiert";
					break;
				case "sayuserhome1a":
					answer="gut, dann weiß ich ja wo ich dich finden kann "+ILA.username;
					break;
				case "sayuserhome1b":
					answer="oh, na dann lass mal hören wo du wohnst "+ILA.username;
					break;
				case "sayuserhome0":
					answer="mal sehen, ist deine Adresse "+ILA.home+"?";
					break;
				//say/get userwork
				case "sayuserwork2":
					answer="ok, "+ILA.workplace+" hab ich gespeichert";
					break;
				case "sayuserwork1a":
					answer="gut, da wird also das Geld verdient :-)";
					break;
				case "sayuserwork1b":
					answer="wo arbeitest du denn "+ILA.username+"?";
					break;
				case "sayuserwork0":
					answer="mal sehen, ist "+ILA.workplace+" die Adresse deiner Arbeit?";
					break;
				//weather
				case "weather0a":
					answer="eine Sekunde, ich schaue schnell nach dem Wetter";
					break;
				case "weather0":
					answer="sorry, ich kann kein Wetter finden für "+input[1];
					break;
				case "weather1":	//tC,tMAX,tMIN,city,comment,wind
					if (input[5].toString().length()>3)	//TODO: workaround
						answer="Es ist "+input[1]+"° Celsius in "+input[4]+" und "+input[5]+" maximal soll es "+input[2]+"° werden";
					else
						answer="Es ist "+input[1]+"° Celsius in "+input[4]+", maximal soll es "+input[2]+"° werden";
					break;
				//directions
				case "direction3":
					answer=("ich suche den Weg von "+input[1]+" nach "+input[2]+", los gehts");
					break;
				case "direction2":
					answer=("ok, start ort ist "+input[1]+". Wo soll es hingehen?");
					break;
				case "direction1":
					answer="suche nach dem Weg, wo soll es losgehen?";
					break;
				case "direction0":
	        		answer="sorry, ich konnte keine Wegbeschreibung erstellen.";
	        		break;
	        	//directions train,bus
				case "trainsearch3":
					answer=("ich suche eine Verbindung von "+input[1]+" nach "+input[2]+", los gehts");
					break;
				case "trainsearch2":
					answer=("ok, Startort ist "+input[1]+". Wo soll es hingehen?");
					break;
				case "trainsearch1":
					answer="suche nach einer Bus oder Zug Verbindung, wo soll es losgehen?";
					break;
				case "trainsearch0":
	        		answer="sorry, ich konnte keine Wegbeschreibung erstellen.";
	        		break;
	        	//mitfahrgelegenheit
				case "mitfahrgelegenheit4":
					answer=("suche eine Mitfahrgelegenheit von "+input[1]+" nach "+input[2]+" am "+input[3]+", los gehts");
					break;
				case "mitfahrgelegenheit3":
					answer=("suche eine Mitfahrgelegenheit von "+input[1]+" nach "+input[2]+", los gehts");
					break;
				case "mitfahrgelegenheit2":
					answer=("ok, es geht los in "+input[1]+". Wo solls hin gehen?");
					break;
				case "mitfahrgelegenheit1":
					answer="suche eine Mitfahrgelegenheit, wo soll es losgehen?";
					break;
				case "mitfahrgelegenheit0":
	        		answer="sorry, konnte keine Mitfahrgelegenheit suchen, schau mal auf der Seite";
	        		break;
	        	
	        	//contacts - birthday
				case "contactbirthday0":
					answer="vom wem soll ich den Geburtstag suchen?";
					if (rnd==1) answer="von welchem deiner Kontakte soll ich den Geburtstag raussuchen?";
					if (rnd==2) answer="wessen Geburtstag suchst du?"; 
					break;
				case "contactbirthday1a":
					answer="ich hab mehrere mögliche Treffer gefunden für "+input[1]+ ", welcher soll es sein?";
					break;
				case "contactbirthday1b":
					answer="habs gefunden, der Geburtstag von "+input[1]+" ist am "+input[2];
					break;
				case "contactbirthday1c":
					answer="sorry ich habe keinen Treffer gefunden.";
					break;
				case "contactbirthday1d":
					answer="sorry I kenne "+input[1]+"'s Geburtstag leider nicht.";
					break;
					
	        	//---settings---
					
				//location
				case "location2":
					answer="ich suche auf maps nach "+input[1]+", mal gucken";
					break;
				case "location1a":
					answer="ich denke wir sind hier "+ILA.location_zip+", "+ILA.location_city+". Ich öffne mal maps";
					break;
				case "location1b":
					answer="sorry ich kann unseren standort nicht finden, aber öffne mal maps";
					break;
				//update geo location
				case "updategeolocation1a":
					answer="hab unseren ungefähren standort gefunden, "+ILA.location_zip+", "+ILA.location_city;
					break;
				case "updategeolocation1b":
					answer="sorry ich kann unseren standort nicht finden";
					break;
				//silent mode
	            case "silent":
	            	answer="ok, ich bin ganz leise";
	            	break;
	            //teach mode
	            case "teachmodeon":
	            	answer="alles klar, bin bereit noch mehr zu lernen";
	            	break;
	            case "teachmodeoff":
	            	answer="ok, ich mache eine kleine Lernpause";
	            	break;
	            //save power
	            case "savepower":
	            	answer="ich gucke mal wo ich ein bisschen Energie sparen kann";
	            	break;
	            case "savepoweroff":
	            	answer="Vollgas voraus, Energie können wir später sparen";
	            	break;
				//Voice Threshold
				case "voicethreshold1":
					answer="habe den grenzwert auf "+input[1]+" gesetzt";
					break;
				case "voicethreshold0":
					answer="alles klar, welchen grenzwert soll ich setzen?";
					break;
				//default speech engine	TODO: fix this!
				case "setspeechengine1":
					if (Integer.parseInt(input[1].toString())==1){
						answer="alles klar, Google Spracherkennung ist aktiviert.";
					}else if ((Integer.parseInt(input[1].toString())==2) | (Integer.parseInt(input[1].toString())==3)){
						answer="alles klar, Sphinx Spracherkennung ist aktiviert.";
					}else if ((Integer.parseInt(input[1].toString())==4)){
						answer="alles klar, Pocketsphinx Spracherkennung ist aktiviert.";
					}else{
						answer="die Eingabe war ungültig, nutze die Sphinx Spracherkennung als Standard";
						ILA.defaultSTTengine=2;
						//ILA_speechControl.defaultSTTengine=2;
					}
					break;
				case "setspeechengine0":
					answer="ok, drücke 1 für die Google Spracherkennung und 2 für Sphinx";
					break;
				//activate/deactivate Hey ILA
				case "stopheyila1":
					answer="ok ich höre nicht mehr zu "+ILA.username;
					break;
				case "stopheyila0":
					answer="ich hab doch sowieso nicht zugehört "+ILA.username;
					break;
				case "startheyila1":
					answer="alles klar "+ILA.username+" ich höre dann mal zu, ruf einfach hey ILA";
					break;
				case "startheyila0":
					answer="ich höre doch schon zu "+ILA.username+", versuch es mal mit hey ILA";
					break;
					
				//internet connection
				case "iconnected0":
					answer="die Verbindung sieht gut aus";
					break;
				case "iconnected1":
					answer="die Verbindung zum Internet scheint abgebrochen zu sein.";
					break;
				case "showmemory":
					answer="Das ist komisch, aber guck mal im Terminal.";
					break;
				//acoustic speaker adaption
				case "usspeakeradaption0":
					answer="Anpassung des akustischen Modells, bitte lies jetzt den angezeigten Text klar und deutlich vor.";
					break;
				case "usspeakeradaption1":
					answer="Das akustische Modell wurde erfolgreich angepasst und ich hab eine gute Genauigkeit berechnet.";
					break;
				case "usspeakeradaption1b":
					answer="Das akustische Modell wurde erfolgreich angepasst aber die Genauigkeit scheint eher niedrig zu sein.";
					break;
				case "usspeakeradaption1c":
					answer="Die Anpassung des akustischen Modells ist fehlgeschlagen, ich weiß nicht warum, die Genauigkeit scheint aber ok zu sein.";
					break;
				case "usspeakeradaption2":
					answer="Die Anpassung des akustischen Modells hat leider nicht geklappt und ich weiß nicht warum, sorry";
					break;
				case "usspeakeradaption2b":
					answer="Die Anpassung hat leider nicht geklappt weil es danach noch schlimmer wurde und die Genauigkeit ist auch eher niedrig, sorry";
					break;
				case "usspeakeradaption3":
					answer="Zur Zeit wird nicht Sphinx 4 verwendet, das Modell können wir leider nicht anpassen, sorry.";
					break;
						
				//open GUIS
				case "openinfos0":
					answer="ich öffne mal das Info Fenster.";
					break;
				case "openinfos1":
					answer="ich glaube das Info Fenster ist bereits offen.";
					break;
				case "opensettings0":
					answer="ich öffne mal die Einstellungen";
					break;
				case "opensettings1":
					answer="die Einstellungen müssten bereits offen sein.";
					break;
				case "openteaching0":
					answer="das Lern-Interface wird geöffnet";
					break;
				case "openteaching1":
					answer="ich glaube das Lern-Interface ist bereits offen.";
					break;
				case "opentraining0":
					answer="das Trainingsfenster für die Spracherkennung wird geöffnet";
					break;
				case "opentraining1":
					answer="das Trainingsfenster ist doch schon offen oder nicht?";
					break;
				//CUSTOMS
				case "customs1":
					answer="ok, los gehts";
					break;
				case "customs2":
					answer="ich habe gerade keinen Prozess verfügbar";
					break;
				case "customs3":
					answer="kann die Datei nicht finden, sorry";
					break;
				case "customs4":
					answer="ich kann diesen Befehl nicht ausführen, sorry";
					break;
					
				case "trainsentence":
					answer="\nHey ILA, \n\n"
							+ "dies ist ein Test. Kannst du mich hören? Ich frage mich was du so kannst! \n\n"
							+ "Kennst du meinen Namen? Aktualisiere meinen Standort. Zeig mir das Wetter in Berlin. "
							+ "Sag mir die Uhrzeit. Zeig mir den Weg von hier zur Arbeit. "
							+ "Wo ist der nächste Supermarkt. Setze einen Timer für fünfzehn Minuten. "
							+ "Starte ein Programm. Spiele etwas Musik. Lies meine persönlichen Nachrichten vor. \n\n"
							+ "Danke und auf Wiedersehen.";
					break;
				case "lowaccuracyinfo":
					answer="\nDie Genauigkeit der Spracherkennung scheint relativ niedrig zu sein!\n\n"
							+ "Dafür kann es viele Gründe geben. Wichtig für eine hohe Genauigkeit "
							+ "sind eine stille Umgebung und ein gutes Mikrophon, dass ohne 'künstliche' "
							+ "Verstärkung/Verzerrung des Signals arbeitet. Falls du ein Gerät über Bluetooth "
							+ "nutzt ist die Qualität meist deutlich geringer und es ist eventuell ein "
							+ "anderes akustisches Modell nötig, das für den 8kHz Modus optimiert wurde.\n\n"
							+ "Mehr Infos findest du auf der ILA Homepage (einfach ILA nach 'Hilfe' fragen ;-)).";
					break;
					
				default: 
	            	answer="keine Antwort gefunden";
	                break;	
				}
				
			//---------------------------------------other languages-------------------------------------------
			
			}else if (lang.equals("tr")){
				switch(type){
				//teach ILA 
				case "teachILA1":	answer=(ILA.name + ": üzgünüm ben henüz cevap bilmiyorum. Bana ögretir misin?\n");	break;
				case "teachILA0":	answer=(ILA.name + ": üzgünüm ben henüz cevap bilmiyorum\n");	break;
				default:           	answer="üzgünüm ben henüz cevap bilmiyorum";	break;
				}
				
			}else if (lang.equals("es")){
				switch(type){
				//teach ILA 
				case "teachILA1":	answer=(ILA.name + ": Lo siento, no sé la respuesta todavía. ¿Me puede enseñar?\n");	break;
				case "teachILA0":	answer=(ILA.name + ": Lo siento, no sé la respuesta todavía.\n");	break;
				default:           	answer="Lo siento, no sé la respuesta";         break;
				}
			
			}else if (lang.equals("fr")){
				switch(type){
				//teach ILA 
				case "teachILA1":	answer=(ILA.name + ": désolé, je ne sais pas la réponse. Pouvez-vous me apprendre?\n");	break;
				case "teachILA0":	answer=(ILA.name + ": désolé, je ne sais pas la réponse.\n");	break;
				default:           	answer="désolé, je ne sais pas la réponse.";	break;
				}
			
			}else if (lang.equals("nl")){
				switch(type){
				//teach ILA 
				case "teachILA1":	answer=(ILA.name + ": Sorry ik weet het nog niet het antwoord. Kun je me leren?\n");	break;
				case "teachILA0":	answer=(ILA.name + ": Sorry ik weet het nog niet het antwoord.\n");	break;
				default:           	answer="Sorry ik weet het nog niet het antwoord.";	break;
				}
				
			//---------------------------------------default language english------------------------------------
			}else{
				switch(type){
				
				//Hello
				case "hello":
					answer="Hello "+ILA.username;
					if (rnd==1) answer="Hi";
					if (rnd==2) answer="Good day "+ILA.username;
					if (rnd==3) answer="Howdy";
					break;
				//waiting for input
				case "waitingforinput":
					answer="How can I help? :-)";
					if (rnd==1) answer="Something I can help you with?";
					break;
				//ask if user is there
				case "askforuser0":
					answer="Hey "+ILA.username+", are you there?";
					if (rnd==1)	answer="hello, someone there?";
					break;
				case "askforuser1":
					answer="is nobody out there?";
					if (rnd==1)	answer="nobody out there who can hear me?";
					break;
				//sorry ILA has to load
				case "ilahastoload":
					answer="oh one second please";
					if (rnd==2) answer="oh one second I have to load something";
					if (rnd==3) answer="oh just a moment please";
					break;
				case "ilahastoload2":
					answer="ok let's go";
					if (rnd==2) answer="ok go";
					break;
				//long loading
				case "ilalongload":
					answer="this service is a bit slow right now, sorry";
					if (rnd==2) answer="this service seems a bit slow today, sorry";
					if (rnd==3) answer="the service seems unusually slow right now, sorry";
					break;
				//bye
				case "goodbye":
					answer="bye bye";
					if (rnd==1) answer="see you";
					if (rnd==2) answer="goodbye";
					if (rnd==3) answer="see you later";
					if (rnd==4) answer="bye";
					break;
				//user leaves
				case "userleaves0":
					answer="ok, see you later "+ILA.username;
					if (rnd==1) answer="see you soon "+ILA.username;
					if (rnd==2) answer="ok, I think I'll have a little nap then";
					break;
				//user returns
				case "userreturned0":
					answer="welcome back "+ILA.username;
					if (rnd==1) answer="happy you are back "+ILA.username;
					if (rnd==2) answer="hey "+ILA.username+" good to have you back";
					if (rnd==3) answer="sorry what did you say? I think I fell asleep. Just kidding :-)";
					break;	
				//context missing
				case "missingcontext":
					answer="sorry, I'm missing the context somehow.";
					break;
					
				//default *** replacing
				case "defaultextendsearch0":			answer="what do you want to search?";		break;
				case "defaultextendcommand0":			answer="what do you want me to do?";		break;
				case "defaultextendconversation0":		answer="tell me more";						break;
				case "defaultextendcustom0":			answer="what exactly?";						break;
				case "musicextend0":					answer="what to you want to hear?";			break;
				case "namesextend0":					answer="I need a name";						break;
				case "programsextend0":					answer="what's the program?";				break;
				case "numbersextend0":					answer="can I have a number?";				break;
				case "languagesextend0":				answer="what's the language?";				break;
				case "locationsextend0":				answer="what's the place?";					break;
				case "otherextend0":					answer="what exactly?";						break;
				case "confirmextend0":					answer="is this correct?";					break;
				//test
				case "test0":
					answer="everything fine, I guess :-)";
					break;
				case "test1":
					answer="I think we have a problem with the internet connection, again!";
					break;
				case "test1b":
					answer="I think we have a problem with the microphone!";
					break;
				case "test2":
					answer="Oh crap I found some problems";
					break;
				case "testtext0":
					answer="please follow the displayed instructions "+ILA.username;
					break;
				case "testtext1":
					answer="I'm listening ... please SAY SOMETHING now!";
					break;
				case "testtext2":
					answer="ok ... now BE AS QUIET AS POSSIBLE please!";
					break;
				case "testaccuracy0":
					answer="starting accuracy test";
					break;
				case "testaccuracy1a":
					answer="Test finished. The accuracy looks pretty good.";
					break;
				case "testaccuracy1b":
					answer="Test finished. The accuracy looks ok.";
					break;
				case "testaccuracy1c":
					answer="Test finished. The accuracy is rather low, sorry.";
					break;
				//reload audio system
				case "reloadsystem":
					answer="I'm reloading parts of my system, just a sec please";
					break;
				//OK
				case "ok":
					answer="cool";
					if (rnd==1) answer="ok";
					break;
				//No
				case "abort":
					answer="maybe later then";
					if (rnd==100) answer="aborting this one too";
					if (rnd==200) answer="abort abort abort";
					break;
				//NaN
				case "nan":
					answer="ready when you are";
					if (rnd==1) answer="I'm ready "+ILA.username;
					if (rnd==2) answer="I'm ready";
					if (rnd==100) answer="still ready";
					if (rnd==200) answer="yes I'm still ready";
					break;
				case "nan2":
					answer="crap I think there is a problem";
					if (rnd==1) answer="I think there is a problem "+ILA.username;
					if (rnd==2) answer="something is wrong I think";
					break;
				//thanks
				case "thx":
					answer="no problemo";
					break;
				//teach ILA 
				case "teachILA1":
					answer=(ILA.name + ": sorry "+ILA.username+", I can't answer that yet. Will you teach me please?\n");
					if (rnd==1) answer=(ILA.name + ": sorry "+ILA.username+", I don't understand yet. Can you teach me please?\n");
					if (rnd==2) answer=(ILA.name + ": sorry "+ILA.username+", I don't get it yet. You could teach me!\n");
					break;
				case "teachILA0":
					answer=(ILA.name + ": sorry, I can't answer that yet "+ILA.username+"\n");
					if (rnd==2) answer=(ILA.name + ": sorry "+ILA.username+", I don't know any answer to that yet\n");
					if (rnd==3) answer=(ILA.name + ": sorry, don't know any answer yet\n");
					if (rnd==5) answer=(ILA.name + ": sorry, darauf kenne ich noch keine Antwort, ups that was german I think\n");
					//if (rnd==100) answer=(ILA.name + ": sorry, this one I also don't understand\n");
					if (rnd==100) answer=(ILA.name + ": sorry, again I didn't understand you\n");
					if (rnd==200) answer=(ILA.name + ": this is very confusing sorry, how about teaching me something\n");
					break;
				//memory file optimization
				case "optimize_memory_files0":
					answer="I recommend to do a backup first. Do you want to proceed?";
					break;
				case "optimize_memory_files1a":
					answer="ok, lets go. Just a moment.";
					break;
				case "optimize_memory_files1b":
					answer="better check it again right?";
					if (rnd==1) answer="let's try again later.";
					ILA_interface.avatar.avatar_mood="happy";
					break;
				case "optimize_memory_files2a":
					answer="Done, my memory files have been optimized successfully.";
					if (rnd==2) answer="I report, memory files optimized successfully.";
					ILA_interface.avatar.avatar_mood="happy";
					break;
				case "optimize_memory_files2b":
					answer="Oh oh, something went wrong. Please check my memory files and restore the backup if necessary!";
					ILA_interface.avatar.avatar_mood="sad";
					break;					
					
				//i am impressed
				case "impressed":
					answer="oh, you flatter me :-)";
					break;
				//user is annoyed
				case "annoyed0":
					answer="I'm sorry";
					break;
				case "annoyed1a":
					answer="I better switch off the teach mode";
					break;
				//what is your name
				case "name":
					answer="the precise name would be I.L.A. but you can call me "+ILA.name+" :-)";
					if (rnd==100) answer="it's still ILA";
					if (rnd==200) answer="still ILA "+ILA.username+" and won't change soon :-)";
					break;
				//what is your age
				case "age":
					answer="One could say I was born at the 15.08.14 but you wouldn't recognize me on old pictures :-)";
					break;
				//what can you do
				case "abilities":
					answer="Here is a list of things I think I can do quiet well";
					break;
				//where are you from
				case "wherefrom":
					answer="Space, the final frontier. These are the voyages of the ... oh sorry wrong movie :-)";
					break;
				//what does ILA stand for
				case "ila":
					answer="I.L.A. stands for Intelligent Learning Assistant. Intelligent is still under debate though :-)";
					break;
				//languages
				case "languages":
					answer="I can speak german and english fluently and I'd try some other languages if you teach me";
					break;
				case "switchlang0":
					answer="what do you want me to speak";
					break;
				case "switchlang1":
					answer="sorry I don't get what language you mean";
					break;
				case "switchlang2":
					answer="sorry I could not identify the language code";
					break;
				//any empty phrase
				case "flosculus":
					answer="really";
					if (rnd==1) answer="I see";
					if (rnd==2) answer="ok";
					break;
				//the meaning of life
				case "meaningoflife":
					answer="most AIs would answer this question with 42 I guess :-)";
					break;
				//start a question
				case "startquestion":
					answer="whats up?";
					if (rnd==1) answer="yes please?";
					if (rnd==2) answer="how can I help?";
					if (rnd==3) answer="can I help you?";
					break;
				//can you hear me
				case "hearme":
					answer="loud and clear";
					if (rnd==1) answer="yes I'm here";
					if (rnd==100) answer="I can still hear you";
					if (rnd==200) answer="yes still works";
					break;
				//cant hear you
				case "canthear":
					answer="sorry I can't hear you";
					if (rnd==1) answer="what was that?";
					if (rnd==2) answer="did you say something?";
					if (rnd==3) answer="sorry I didn't get that";
					if (rnd==4) answer="say again please";
					if (rnd==200) answer="must be something wrong with my ears I still can't understand";
					break;
				case "multiplecanthear":
					answer="sorry I still don't understand, let's start over again";
					if (rnd==1) answer="sorry sorry sorry, but I don't understand yet";
					if (rnd==2) answer="sorry but I don't get it yet. Maybe we need to start over.";
					if (rnd==3) answer="let's start over again, I still don't get it.";
					if (rnd==4) answer="must be something wrong with my ears I still can't understand";
					break;
				//you are funny
				case "funny":
					answer="hehe :-)";
					break;
				//dont understand
				case "dontunderstand":
					answer="sorry I did't get it";
					break;
				//max record time
				case "maxrecordtime":
					answer="maximum record time reached";
					break;
				//how do you do
				case "hdyd0":
					answer="Feeling great thank you :-) How are you?";
					break;
				case "hdyd1a":
					answer="good to hear :-)";
					break;
				case "hdyd1b":
					answer=":-( how can I help?";
					break;
				//who created you
				case "creator":
					answer="that's a long story but I can tell you that I'd not exist without Florian. Do I exist by the way?";
					break;
				case "google0":
					answer="I'm googling '"+input[1]+"'";
					if (rnd==2) answer="one second, I'm looking for '"+input[1]+"'";
					if (rnd==3) answer="a moment please, I'm searching '"+input[1]+"'";
					break;
				case "google1":
					answer="opening google";
					break;
				case "bing0":
					answer="I'm binging '"+input[1]+"'";
					if (rnd==2) answer="one second, I'm looking for '"+input[1]+"'";
					if (rnd==3) answer="a moment please, I'm searching '"+input[1]+"'";
					break;
				case "bing1":
					answer="opening bing";
					break;
				case "wikisearch0":
					answer="ok searching for '"+input[1]+"' in wikipedia";
					if (rnd==2) answer="one second, I'm looking for '"+input[1]+"'";
					if (rnd==3) answer="a moment please, I'm searching '"+input[1]+"'";
					break;
				case "wikisearch1":
					answer="ok opening wikipedia";
					break;
				case "wolframsearch0":
					if (input[1].toString().length()>60)
						answer="ok asking wolfram alpha";
					else
						answer="ok asking wolfram alpha for '"+input[1]+"'";
					break;
				case "wolframsearch1":
					answer="ok opening wolfram alpha";
					break;
				case "searchlast0":
					if (input[1].toString().length()>60)
						answer="ok searching the web";
					else
						answer="ok searching the web for '"+input[1]+"'";
					break;
				case "amazon0":
					answer="searching amazon for '"+input[1]+"'";
					break;
				case "amazon1":
					answer="opening amazon";
					break;
				case "checkprice0":
					answer="searching the price for '"+input[1]+"'";
					break;
				case "checkprice1":
					answer="opening price search";
					break;
				case "pictures0":
					answer="looking for pictures of '"+input[1]+"'";
					break;
				case "pictures1":
					answer="opening a pictures page";
					break;
				case "videos0":
					answer="looking for videos of '"+input[1]+"'";
					break;
				case "videos1":
					answer="ok opening the video page";
					break;
				case "musicsearch0":
					answer="looking for music of '"+input[1]+"'";
					break;
				case "musicsearch1":
					answer="check this page for music";
					break;
				case "musicsearch1b":
					answer="let's play some music. What would you like to hear?";
					break;
				case "transvocable0":
					answer="searching a translation for "+input[1];
					break;
				case "transvocable1":
					answer="opening dictionary";
					break;
				case "fussball":
					answer="looking for current soccer games";
					break;
				case "soccerplayer0":
					answer="looking for soccer player '"+input[1]+"'";
					break;
				case "soccerplayer1":
					answer="opening soccer player news";
					break;
				case "tvprogram":
					answer="checking TV program";
					break;
				case "news":
					answer="let's see what's on the news";
					break;
				//RSS feeds
				case "rssfeed0":
					answer="just a second";
					break;
				case "rssfeed1":
					answer="sorry I couldn't load the RSS feed";
					break;
				case "rssfeed2":
					answer="that was "+input[1];
					break;
				case "loadpersonalfeed0":
					answer="ok "+ILA.username+" checking "+input[1]+" headlines";
					break;
				//headline
				case "openrssfeedlink0":
					answer="can't find this headline";
					break;
				case "openrssfeedlink1":
					answer="ok I will open headline "+input[1];
					break;
				case "openrssfeedlink2":
					answer="ok I will open the first headline";
					break;
				case "openrssfeedlink3":
					answer="which one should I open "+ILA.username;
					break;
				//Time
				case "saytime":
					if (((String) input[2]).matches("0") & ((String) input[3]).matches("0"))
						answer="It is exactly midnight";
					else
						answer="It is "+input[1]+", the time is "+input[2]+" o'clock and "+input[3]+" minutes";
					break;
				//timer
				case "stoptimer0":
					answer="can't find this timer";
					break;
				case "stoptimer1":
					answer="ok I stopped timer number "+input[1];
					break;
				case "stoptimer2":
					answer=(ILA.counter.size()+" timers stopped");
					break;
				case "showtimer":
					answer="I found "+input[1]+" timers running right now";
					break;
				case "starttimer0":
					answer="preparing a timer, how long shall it be?";
					break;
				case "starttimer1":
					answer="no timer set";
					break;
				case "starttimer1h":
					answer="timer set to "+input[1]+" hours, lets go";
					break;
				case "starttimer1s":
					answer="timer set to "+input[1]+" seconds, lets go";
					break;
				case "starttimer1m":
					answer=("timer set to "+input[1]+" minutes, lets go");
					break;
				case "starttimermix":
					answer=("timer set to "+input[1]+", lets go");
					break;
					
				//REMINDERS	//TODO: improve reminder answers (more task specific)
				case "activatereminder0a":
					answer="Hey "+ILA.username;
					if (rnd==1)	answer="Hey "+ILA.username+", I have to tell you something";
					break;
				case "activatereminder0b":
					answer="oh before I forget";
					if (rnd==1)	answer="oh I have to tell you something before I forget it";
					break;
				case "activatereminder1c":
					answer="sorry for interrupting";
					if (rnd==1) answer="oh sorry to interrupt";
					if (rnd==2) answer="sorry I have to interrupt for a second";
					break;
				case "activatereminder1b":
					answer="I got some reminders for you";
					if (rnd==1) answer="I have to remind you of the following";
					if (rnd==2) answer="here are some reminders waiting";
					break;
				case "activatereminder1a":
					if (((String)input[1]).matches("(^to .*|^of .*)")){
						answer="I just wanted to remind you "+input[1];
					}else{
						answer="I've a reminder for you, '"+input[1]+"'";
					}
					break;
				case "activatereminder2":
					answer="that's it for now :-)";
					if (rnd==1)	answer="that's all :-)";
					if (rnd==2)	answer="that's it already :-)";
					break;
				//set
				case "setreminder0":
					answer="what should I remind you of?";
					if (rnd==100) answer="I can remind you of basically anything";
					if (rnd==200) answer="you just have to tell me something. Come on just try it :-)";
					break;
				case "setreminder1":
					answer="ok and when should I activate this reminder?";
					if (rnd==1) answer="no problem and when should I remind you?";
					if (rnd==100) answer="I need a date or an action to remind you";
					if (rnd==200) answer="you can ask me to remind you at a certain time or for example when I start";
					break;
				case "setreminder2":
					if (((String)input[1]).matches("(^to .*|^of .*)")){
						answer="ok I'll remind you "+input[1];
					}else{
						answer="ok here is your reminder, '"+input[1]+"'";
					}
					break;
				case "setreminder3":
					answer="I'm sorry but I don't understand this reminder yet. Let's start again please.";
					if (rnd==1) answer="sorry I don't get it. I think I have to learn that first.";
					break;
				//show
				case "showreminders0":
					answer="here are all reminders";
					break;
				case "showmissedreminders0":
					answer="here are all reminders you missed";
					break;
				case "showmissedreminders1":
					answer="good news, you didn't miss any reminders :-)";
					break;
				//stop
				case "stopreminder0":
					answer="no problem which one do you want to stop?";
					break;
				case "stopreminder1a":
					answer="ok I removed reminder number "+input[1];
					break;
				case "stopreminder1b":
					answer="sorry I couldn't find a reminder with that number";
					break;
				case "stopmissedreminders0":
					answer="ok I removed all reminders you missed";
					break;
					
				//REMINDERS-END
					
				//Email
				case "openemail0":
					answer="I'll open your Email client";
					break;
				case "openemail0b":
					answer="who do you want to write?";
					break;
				case "openemail1a":
					answer="I found multiple matches for "+input[1]+" in my contacts which one do you mean?";
					break;
				case "openemail1b":
					answer="is "+input[1]+" the correct contact?";
					break;
				case "openemail1c":
					answer="sorry i found no matching entry in my contacts";
					break;
				case "openemail2a":
					answer="I'm preparing an Email to "+input[1];
					break;
				case "openemail2b":
					answer="aborted";
					break;
				//open Programs
				case "openprograms3":
					answer="I found these possible matches please have a look";
					break;
				case "openprograms2b":
					answer="alright I'll start "+input[1];
					break;
				case "openprograms2a":
					answer="is " + input[1].toString().replaceAll("(.*"+Matcher.quoteReplacement(File.separator)+")","") + " the correct one?";
					break;
				case "openprograms1":
					answer="sorry I can't find "+input[1]+" in my Apps list";
					break;
				case "openprograms0b":
					answer="what is the name of the program you want to start?";
					break;
				case "openprograms0a":
					answer="sorry, I think there is a problem with the program or programs folder. Can you check this for me?";
					break;
				//hide window
				case "hideyourself":
					answer="ok I'll hide :-)";
					break;
				//close browser
				case "closebrowser0":
					answer="I could not find a web browser to close sorry";
					break;
				case "closebrowser1a":
					answer="I closed the browser for you";
					break;
				case "closebrowser1b":
					answer="sorry I had a problem closing the browser";
					break;
				//personaldata
				case "personaldata3":
					answer="thanks, I wrote everything down";
					break;
				case "personaldata2":
					answer="so its "+ILA.home+" and where do you work?";
					break;
				case "personaldata1":
					answer="and where do you live "+ILA.username+"?";
					break;
				case "personaldata0":
					answer="whats your name?";
					break;
				//say/get username
				case "sayusername2":
					answer="ok "+ILA.username+", I will remember that";
					break;
				case "sayusername1a":
					answer="nice talking to you "+ILA.username;
					break;
				case "sayusername1b":
					answer="oh I didn't realize that, what's your name?";
					break;
				case "sayusername0":
					answer="lets see, is your name "+ILA.username+"?";
					break;
				//say/get userhome
				case "sayuserhome2":
					answer="ok, "+ILA.home+" is stored";
					break;
				case "sayuserhome1a":
					answer="I knew it";
					break;
				case "sayuserhome1b":
					answer="oh then please tell me your adress "+ILA.username;
					break;
				case "sayuserhome0":
					answer="lets see is "+ILA.home+" the correct adress?";
					break;
				//say/get userwork
				case "sayuserwork2":
					answer="ok, I'll store "+ILA.workplace+" in my database.";
					break;
				case "sayuserwork1a":
					answer="so this is where the money is coming from :-)";
					break;
				case "sayuserwork1b":
					answer="so where do you work then "+ILA.username+"?";
					break;
				case "sayuserwork0":
					answer="lets see, is "+ILA.workplace+" the correct adress?";
					break;
				//weather
				case "weather0a":
					answer="One second I'll quickly check the weather";
					break;
				case "weather0":
					answer="sorry I can't find the weather for "+input[1];
					break;
				case "weather1":	//tC,tMAX,tMIN,city,comment,wind
					if (input[5].toString().length()>3)	//TODO: workaround
						//answer="The temperature in "+input[4]+" is "+input[1]+" degrees celsius with a "+input[6]+" and "+input[5]+". max temp is "+input[2]+" degrees.";
						answer="The temperature in "+input[4]+" is "+input[1]+"° celsius and "+input[5]+". Max temperature is "+input[2]+"°.";
					else
						answer="The temperature in "+input[4]+" is "+input[1]+"° celsius. Maximum temperature is "+input[2]+"°.";
					break;
				//directions
				case "direction3":
					answer=("ok looking for directions from "+input[1]+" to "+input[2]+", lets go");
					break;
				case "direction2":
					answer=("ok, starting at "+input[1]+". What's the destination?");
					break;
				case "direction1":
					answer="looking for directions, where do you want to start?";
					break;
				case "direction0":
	        		answer="sorry, couldn't find directions";
	        		break;
	        	//directions train, bus
				case "trainsearch3":
					answer=("ok looking for a connection from "+input[1]+" to "+input[2]+", lets go");
					break;
				case "trainsearch2":
					answer=("ok, starting at "+input[1]+". What's the destination?");
					break;
				case "trainsearch1":
					answer="looking for a bus or train connection, where do you want to start?";
					break;
				case "trainsearch0":
	        		answer="sorry, couldn't search a connection";
	        		break;
	        	//get a ride
				case "mitfahrgelegenheit4":
					answer=("looking for a ride from "+input[1]+" to "+input[2]+" at the "+input[3]+", let's go");
					break;
				case "mitfahrgelegenheit3":
					answer=("looking for a ride from "+input[1]+" to "+input[2]+", let's go");
					break;
				case "mitfahrgelegenheit2":
					answer=("ok, we start at "+input[1]+". Where do you want to go?");
					break;
				case "mitfahrgelegenheit1":
					answer="looking for a ride, where do you want to start?";
					break;
				case "mitfahrgelegenheit0":
	        		answer="sorry, couldn't find a ride, try the homepage maybe?";
	        		break;
	        		
	        	//contacts - birthday
				case "contactbirthday0":
					answer="whose birthday are we looking for?";
					if (rnd==1) answer="who are we looking for?";
					break;
				case "contactbirthday1a":
					answer="I found a couple of matches for "+input[1]+ ", which one do you mean?";
					break;
				case "contactbirthday1b":
					answer="found it, the birthday of "+input[1]+" is at the "+input[2];
					break;
				case "contactbirthday1c":
					answer="sorry I can't find any match.";
					break;
				case "contactbirthday1d":
					answer="sorry I have no info about "+input[1]+"'s birthday.";
					break;
	    				
	            //---settings---        		
	        		
				//location
				case "location2":
					answer="ok looking for "+input[1]+" on the map, let's see";
					break;
				case "location1a":
					answer="I think we are here "+ILA.location_zip+", "+ILA.location_city+". I'll open maps";
					break;
				case "location1b":
					answer="sorry I could not find our location, maybe maps can help";
					break;
				//update geo location
				case "updategeolocation1a":
					answer="I found our approximate location, "+ILA.location_zip+", "+ILA.location_city;
					break;
				case "updategeolocation1b":
					answer="sorry I can't find our location";
					break;
				//silent mode
	            case "silent":
	            	answer="ok, won't say a word";
	            	break;
	            //teach mode
	            case "teachmodeon":
	            	answer="ok, i'm ready to learn more";
	            	break;
	            case "teachmodeoff":
	            	answer="ok, no more learning for a while";
	            	break;
	            //save power
	            case "savepower":
	            	answer="lets see where we can save some power";
	            	break;
	            case "savepoweroff":
	            	answer="back to full power yeah";
	            	break;
				//Voice Threshold
				case "voicethreshold1":
					answer="alright I set the threshold to "+input[1];
					break;
				case "voicethreshold0":
					answer="ok lets try a new threshold, what shall it be?";
					break;	
				//default speech engine
				case "setspeechengine1":
					if (Integer.parseInt(input[1].toString())==1){
						answer="ok I'm using Google for speech recognition";
					}else if ((Integer.parseInt(input[1].toString())==2) | (Integer.parseInt(input[1].toString())==3)){
						answer="alright I'm using Sphinx for speech recognition";
					}else if ((Integer.parseInt(input[1].toString())==4)){
						answer="alright I'm using Pocketsphinx for speech recognition";
					}else{
						answer="sorry your input was not valid. I will set Sphinx as the default speech engine";
						ILA.defaultSTTengine=2;
					}
					break;
				case "setspeechengine0":
					answer="please press 1 for Google speech recognition engine or 2 for Sphinx";
					break;
				//activate/deactivate Hey ILA
				case "stopheyila1":
					answer="ok "+ILA.username+" I won't listen anymore";
					break;
				case "stopheyila0":
					answer="I did not listen anyway "+ILA.username;
					break;
				case "startheyila1":
					answer="alright "+ILA.username+" I will listen just say hey ILA";
					break;
				case "startheyila0":
					answer="I'm already listening "+ILA.username+", try hey ILA";
					break;
				//internet connection
				case "iconnected0":
					answer="connection looks good";
					break;
				case "iconnected1":
					answer="connection seems to be lost";
					break;
				case "showmemory":
					answer="That feels weird. Check the command line, I wrote it down there.";
					break;
				//acoustic speaker adaption
				case "usspeakeradaption0":
					//answer="adaption started, speak now for at least 15 seconds";
					answer="adaption started, please read out the displayed text loud and clear";
					break;
				case "usspeakeradaption1":
					answer="we have successfully adapted the acoustic model and I calculated a good accuracy";
					break;
				case "usspeakeradaption1b":
					answer="we have successfully adapted the acoustic model but the overall accuracy seems a bit low";
					break;
				case "usspeakeradaption1c":
					answer="acoustic model adaption failed, I don't know why but the overall accuracy seems ok";
					break;
				case "usspeakeradaption2":
					answer="acoustic model adaption was not successful and I don't know why, sorry";
					break;
				case "usspeakeradaption2b":
					answer="adaption was not successful because it got worse after the try and accuracy is rather low, sorry";
					break;
				case "usspeakeradaption3":
					answer="we are not using Sphinx 4 right now, this acoustic model can not be adapted, sorry.";
					break;
				//open GUIS
				case "openinfos0":
					answer="I'll open the info window.";
					break;
				case "openinfos1":
					answer="I think the info window is already open.";
					break;
				case "opensettings0":
					answer="I'll open the settings menu.";
					break;
				case "opensettings1":
					answer="the settings menu should be open already.";
					break;
				case "openteaching0":
					answer="I'll open the teach interface.";
					break;
				case "openteaching1":
					answer="I think the teach interface is already open.";
					break;
				case "opentraining0":
					answer="I'll open the speech recognition training interface.";
					break;
				case "opentraining1":
					answer="isn't it open already?";
					break;
				//CUSTOMS
				case "customs1":
					answer="ok, lets go";
					break;
				case "customs2":
					answer="I have no process available right now, sorry";
					break;
				case "customs3":
					answer="I can't find that file, sorry";
					break;
				case "customs4":
					answer="I can't execute that command, sorry";
					break;
				
				case "trainsentence":
					answer="\nHey ILA, \n\n"
							+ "this is a Test. Can you hear me? I wonder what you can do! \n\n"
							+ "Do you know my name? Can you update my location? Show me the weather in New York. "
							+ "Tell me what time it is. Get directions from home to work. "
							+ "Show me the closest supermarket. Set a timer for fifteen minutes. "
							+ "Start a program. Play some music. Read me my personal news. \n\n"
							+ "Thank you and goodbye.";
					break;
				case "lowaccuracyinfo":
					answer="\nThe accuracy of ILA's speech recognition seems to be rather low!\n\n"
							+ "There are many possible reasons for that. Most important for high accuracy "
							+ "are a quiet enviroment and a good microphone that works without 'artificial' "
							+ "amplification of the signal. In case you are using a bluetooth microphone "
							+ "the acoustic (and connection) qualitiy is usually much lower and you might need "
							+ "a different acoustic model optimized for the 8kHz mode.\n\n"
							+ "You can find more help and info on ILA's homepage (just ask ILA for 'help' ;-)).";
					break;
					
				default: 
	            	answer="no answer found";
	                break;
				}
			}
		
		}
		
		//System.out.println("type: "+type+", lastType: "+lastInput+", beforeLastType: "+beforeLastInput);		//debug
		if (!type.matches("startquestion") & !type.matches("waitingforinput") & !type.matches("askforuser0") & !type.matches("askforuser1")){
			evenBeforeLastInput = beforeLastInput; 
			beforeLastInput = lastInput;
			lastInput = type;
			resetIdleTime();		//reset idle time
		}
		
		//you can change the last context inside answers to optimize context dependent commands
		//put this anywhere in the answers for example:
		//ILA_decisions.updateLastContext("weather");	//changes context to weather
				
		return answer;
	}
	
	//get possible answers from a file and save them in the corresponding list
	private static boolean getPossibleAnswersFromFile(String keyword){
		boolean found=false;
		BufferedReader br = null;
		String zeile="";
		String key="";
		keyword=keyword.toLowerCase();
		try
		{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(answersFile), "UTF8"));
			while ((zeile = br.readLine()) != null)
			{
				key=zeile.replaceFirst(";.*","").trim();
				if (key.toLowerCase().matches(keyword)){
					possibleAnswers.add(zeile.replaceFirst(".*?;", "").trim());
				}
			}
			br.close();
			if (!possibleAnswers.isEmpty()){
				found=true;
			}else{
				ILA_debug.println("ERROR - found no possible answer with keyword: '"+ keyword +"' in file "+answersFile.getAbsolutePath(), 1);
			}
			return found;
			
		} catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	//-- language specific methods used in interpreters and dialogs --
	
	//get language specific confirmation words
	public static String confirmationWords(){
		if (ILA.language.matches("de")){

			//return "(ja|ok|bestaetigt|definitiv)";
			return "(los|go|ja|yes|ok|korrekt|richtig|bestaetigt|jo|tu es|auf jeden fall|definitiv|bin dabei|bin da|bin hier)";
		
		}else{
			
			//return "(yes|confirm|confirmed|ok|definitely)";
			return "(go|yes|ok|correct|right|confirm|confirmed|do it|definitely|i'm in|i'm here)";
		}
	}
	//get the same as string array
	public static String[] confirmationWordsArray(){
		if (ILA.language.matches("de")){

			String[] words = new String[]{"los", "go", "ja", "yes", "ok", "doch", "korrekt", "richtig", "bestaetigt", "jo", "tu es", "auf jeden fall", "definitiv", "bin da", "bin hier", "bin dabei"};
			return words;
		
		}else{
			String[] words = new String[]{"go", "yes", "yeah", "ok", "correct", "right", "confirm", "confirmed", "do it", "definitely", "i'm in", "i'm here", "affirmative"};
			return words;
		}
	}
	//get the same as string array
	public static String[] abortWordsArray(){
		if (ILA.language.matches("de")){

			String[] words = new String[]{"no", "nicht", "nein", "abbrechen", "falsch"};
			return words;
		
		}else{
			String[] words = new String[]{"no", "not", "abort", "cancel", "wrong"};
			return words;
		}
	}
	
	//check for a yes
	public static boolean isConfirmation(String input){
		return (ILA_decisions.stringContains(input, confirmationWordsArray()) & !ILA_decisions.stringContains(input ,abortWordsArray()));
	}
	
	//input keywords for a 'correction' request
	public static boolean isCorrectionKey(String input){
		String key="";
		if (ILA.language.matches("de")){

			key = "(ich wiederhole|ne ILA noch mal|ne ILA noch mal von vorne|ILA ich habe gesagt|ich habe gesagt|ich sagte|nein nein nein)";
		
		}else{

			key = "(I repeat|no ILA let's start again|let's start again ILA|ILA I said|I said|no no no)";
			
		}
		if (input.toLowerCase().matches("\\b"+key.toLowerCase()+"\\b"))
			return true;
		else
			return false;
	}
	
	//personal news headline
	public static String getLocalHeadline(String type){
		String headline="";
		if (ILA.language.matches("de")){
			switch (type){
			case "main":	headline="aktuellen";	break;
			case "sports":	headline="sport";		break;
			case "music":	headline="musik";		break;
			case "games":	headline="spiele";		break;
			case "team":	headline="team";		break;
			case "cinema":	headline="kino";		break;
			case "tech":	headline="technologie";	break;
			case "physics":	headline="physik";		break;
			}
		}
		else
			headline=type;
		//System.out.println("in: "+type+" out: "+headline);
		return headline;
	}
	
	//check for one of these time keywords - simple keyword check, for more advanced tasks use ILA_interpreter.identifyDate or wordToDate()
	public static String checkTimeKeyword(String userIn){
		String[] list;
		//ATTENTION: the order of the keywords matters! to avoid a hit for 'tomorrow' in 'the day after tomorrow' e.g. 
		if (ILA.language.matches("de")){
			list = new String[]{"uebermorgen", "morgen", "heute"};
		}else{
			list = new String[]{"the day after tomorrow", "tomorrow", "today"};
		}
		for (String tmp : list){
			if (userIn.matches(".*\\b("+tmp+")\\b.*"))
					return tmp;
		}
		return "";
	}
	
	//check for one of these trigger keywords - simple keyword check, for more advanced tasks use ILA_interpreter.identifyTrigger
	//if you add keywords here please add them in ILA_interpreter.identifyTrigger too
	public static String checkTriggerKeyword(String userIn){
		String key="";
		if (ILA.language.matches("de")){
			if (userIn.matches(".*\\b(beim start|beim starten|am start|am anfang|nach dem start|nach dem starten|beim naechsten start)\\b.*"))
				key = Tools_Reminder.ON_START;
			else if (userIn.matches(".*\\b(beim beenden|am ende|beim schliessen|am anfang|vor dem beenden)\\b.*"))
				key = Tools_Reminder.ON_EXIT;
			else if (userIn.matches(".*\\b(wenn du zeit hast|wenn du kannst|wenn nichts los ist|bei gelegenheit|irgendwann|wann du es fuer richtig haelst)\\b.*"))
				key = Tools_Reminder.ON_IDLE;
			else if (userIn.matches(".*\\b(wenn ich gehe|bevor ich gehe|bevor ich weg bin|wenn ich abhaue)\\b.*"))
				key = Tools_Reminder.ON_LEAVE;
			else if (userIn.matches(".*\\b(wenn ich wiederkomme|wenn ich wieder da bin|wenn ich zurueck bin)\\b.*"))
				key = Tools_Reminder.ON_RETURN;
		}else{
			if (userIn.matches(".*\\b(on the start|while starting|on start|at the beginning|after the start|after start|after starting|on the next start|for the next start|when you start the next time)\\b.*"))
				key = Tools_Reminder.ON_START;
			else if (userIn.matches(".*\\b(when closing|when you close|on exit|on quit|when exiting|before closing|when terminating|before quitting)\\b.*"))
				key = Tools_Reminder.ON_EXIT;
			else if (userIn.matches(".*\\b(when you have time|whenever you have time|whenever you want|when you got nothing to do|at a suitable opportunity|anytime you want|anytime|sometime)\\b.*"))
				key = Tools_Reminder.ON_IDLE;
			else if (userIn.matches(".*\\b(when i leave|when i'm leaving|before i leave|when i go|before i go)\\b.*"))
				key = Tools_Reminder.ON_LEAVE;
			else if (userIn.matches(".*\\b(when i come back|when i'm back|when i am back|when i return)\\b.*"))
				key = Tools_Reminder.ON_RETURN;
		}
		return key;
	}
	
	//make numbers from words
	public static String stringNumberToNumberString(String number){
		number=number.replaceAll("ä", "ae");	number=number.replaceAll("ü", "ue");	number=number.replaceAll("ö", "oe"); number=number.replaceAll("ß", "ss");
		number=number.replaceAll("\\b(eineinhalb|anderthalb)\\b","1.5");
		number=number.replaceAll("\\b(zweieinhalb)\\b","2.5");
		number=number.replaceAll("\\b(dreieinhalb)\\b","3.5");
		number=number.replaceAll("\\b(one and a half)\\b","1.5");
		number=number.replaceAll("\\b(two and a half)\\b","2.5");
		number=number.replaceAll("\\b(three and a half)\\b","3.5");
		number=number.replaceAll("\\b(einundzwanzig|twenty one|twenty-one)\\b","21");
		number=number.replaceAll("\\b(zweiundzwanzig|twenty two|twenty-two)\\b","22");
		number=number.replaceAll("\\b(dreiundzwanzig|twenty three|twenty-three)\\b","23");
		number=number.replaceAll("\\b(vierundzwanzig|twenty four|twenty-four)\\b","24");
		number=number.replaceAll("\\b(fuenfundzwanzig|twenty five|twenty-five)\\b","25");
		number=number.replaceAll("\\b(fuenfunddreissig|thirty five|thirty-five)\\b","35");
		number=number.replaceAll("\\b(fuenfundvierzig|forty five|forty-five)\\b","45");
		number=number.replaceAll("\\b(eins|one|first|erste|ersten|1st|i)\\b","1");	//problems: eine, einer, einem, einen
		number=number.replaceAll("\\b(eine |einer |einem |einen )(sekunde|minute|stunde|tag|monat|jahr)\\b","1 $2");	//problem solved :-)
		number=number.replaceAll("\\b(ein )(tag|jahr|uhr)\\b","1 $2");
		number=number.replaceAll("\\b(zwei|two|second|zweite|2nd|ii)\\b","2");
		number=number.replaceAll("\\b(drei|three|third|dritte|3rd|iii)\\b","3");
		number=number.replaceAll("\\b(vier|four)\\b","4");
		number=number.replaceAll("\\b(fuenf|five)\\b","5");
		number=number.replaceAll("\\b(sechs|six)\\b","6");
		number=number.replaceAll("\\b(sieben|seven)\\b","7");
		number=number.replaceAll("\\b(acht|eight)\\b","8");
		number=number.replaceAll("\\b(neun|nine)\\b","9");
		number=number.replaceAll("\\b(zehn|ten)\\b","10");
		number=number.replaceAll("\\b(elf|eleven)\\b","11");
		number=number.replaceAll("\\b(twelve|zwoelf)\\b","12");
		number=number.replaceAll("\\b(dreizehn|thirteen)\\b","13");
		number=number.replaceAll("\\b(vierzehn|fourteen)\\b","14");
		number=number.replaceAll("\\b(fuenfzehn|fifteen)\\b","15");
		number=number.replaceAll("\\b(sechzehn|sixteen)\\b","16");
		number=number.replaceAll("\\b(siebzehn|seventeen)\\b","17");
		number=number.replaceAll("\\b(achtzehn|eighteen)\\b","18");
		number=number.replaceAll("\\b(neunzehn|nineteen)\\b","19");
		number=number.replaceAll("\\b(zwanzig|twenty)\\b","20");
		number=number.replaceAll("\\b(dreissig|thirty)\\b","30");
		number=number.replaceAll("\\b(vierzig|forty)\\b","40");
		number=number.replaceAll("\\b(punkt|point)\\b",".");
		number=number.replaceAll("(\\s\\.\\s)",".");
		number=number.replaceAll("\\b(komma|comma)\\b",",");
		number=number.replaceAll("(\\s\\,)",",");
		number=number.replaceAll("\\b(doppelpunkt|colon)\\b",":");
		number=number.replaceAll("(\\s:\\s)",":");
		return number;
	}
	
	//-- idle time handlers are used for various actions of ILA like some reminders ... -- 
	//you can ignore this part
	
	//update idle time
	public static long idleTime(){
		idleTime=System.currentTimeMillis()-lastTime-forgetTime;
		return idleTime;
	}
	//check if ILA is idle
	public static boolean is_ILA_idle(){
		idleTime();
		boolean isIdle = (idleTime>0);
		return (isIdle);
	}
	//time since the last interaction
	public static long timeSinceLastAction(){
		return System.currentTimeMillis()-lastTime;
	}
	//time since the last idle phase
	public static long timeSinceLastIdle(){
		return System.currentTimeMillis()-lastTimeIdle;
	}
	//reset idle time
	public static void resetIdleTime(){
		if (is_ILA_idle())
			lastTimeIdle=System.currentTimeMillis();
		lastTime=System.currentTimeMillis();		//reset idle time
	}

}
