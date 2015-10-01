package ila;

import java.io.File;
import java.util.ArrayList;

/***************************************************************************
 * This is an add-on for ILA to control XBMC/Kodi and serves as an 
 * example of how to control devices via HTTP GET commands.
 * Kodi examples: http://kodi.wiki/view/JSON-RPC_API/Examples
 * 
 * It requires at least ILA Beta v3.8 to run.
 * 
 * @author Florian Quirin for I.L.A. project
 ***************************************************************************/
public class Addon_XBMC_Kodi_Controls implements ILA_addon_interface {
	
	//---CONFIGURATION---
	public AddonConfiguration getConfiguration() {
		
		//a list of all commands inside this add-on
		//make this names as unique as possible to avoid conflicts with other add-ons
		//note: use only small case letters as names!!!
		//for now it is only a proof of principle with the command: stop, play from folder
		ArrayList<String> commands = new ArrayList<String>();
		commands.add("xbmc_kodi_controls_stop");
		commands.add("xbmc_kodi_controls_play");
		
		//a description of each command for ILA's teachGUI 
		ArrayList<String> commands_description = new ArrayList<String>();
		commands_description.add("XBMC: stops the XBMC/Kodi player");
		commands_description.add("XBMC: plays mp3s from a folder [=parameter]");
		
		//context keywords - under development (ignore for now) - TODO: implement!
		ArrayList<String> contexts = new ArrayList<String>();
		contexts.add("xbmc_kodi_control");
		
		//initialize configuration
		String name_of_addon = "Add-On XBMC/Kodi Controls";		//mainly for user info
		String version_of_addon = "0.1";			//...
		AddonConfiguration config = new AddonConfiguration(name_of_addon, version_of_addon, commands, commands_description, contexts);
		return config;
	}
	
	//---Manage Answers---
	//get the language specific file with answers
	public String answerFile(){
		String file;
		if (ILA.language.matches("de")){
			file="Addons/Addon_XBMC_Kodi_Controls/answers_de.txt";
		}else{
			file="Addons/Addon_XBMC_Kodi_Controls/answers_en.txt";
		}
		return file;
	}
	
	//---MAIN---
	
	//XBMC server access
	String kodiPort = "80";
	String kodiURL =  "http://localhost:"+kodiPort+"/jsonrpc?request=";
	
	//helper to handle Http request and Json responses
	Tools_Reader reader = new Tools_Reader();
	
	//main method to call an add-on specific command - will be called from ILA_addons - expects an String-answer in return
	//if you write more classes for your add-on they all have to be accessed from here. Don't forget to access them to the config
	public String callCommand(String command, String[] memories) {
		
		//init
		String answer="";								//answer that will be returned
		ILA_interface.avatar.avatar_mood="neutral";		//optional: ILA can have different moods :-) (under development)
		
		//Kodi variables
		String request="";				//JSON String as request send to Kodi via HTTP GET
		String result="";				//JSON String result received from Kodi
		int player_id;					//Kodi player ID (0: music, 1: video, 2: pictures)
		String music_folder="Data/";	//A folder with music (optimally this should be a user choice not a fixed variable, the user can pass it as a parameter though)	
		
		//now comes the implementation of the commands - feel free to do whatever you want here :-)
		//this is just an example how to select the commands, e.g. you can use switch-cases as well ...
		TheSwitch:			//it is labeled so you can escape for loops etc. ...
		switch (command.toLowerCase()) {
		
			//stop player - note: use only small case letters as names!!!
			case "xbmc_kodi_controls_stop":

				//first get XBMC playerID (-1 if not found or server access problem)
				player_id = getPlayerID();
				if (player_id !=-2){
					
					//found ID
					//check if player is actually running
					if (player_id ==-1){
						answer = ILA_answers.getFromFile(answerFile(), "xbmc_kodi_controls_stop0");		//"it seems Kodi is not playing anything."
						ILA_interface.avatar.avatar_mood="sad";
						break;
					
					//try stop the player
					}else{
						
						//http GET request to stop:
						request = "{\"jsonrpc\":\"2.0\",\"method\":\"Player.Stop\",\"params\":{\"playerid\":"+player_id+"},\"id\":1}";
						
						//send stop request
						result = reader.httpGET(kodiURL, request);
						//System.out.println("result: " + result);		//debug
						
						//double check result if player actually stopped
						String result_val = reader.jsonGetString(result, "result");
						//System.out.println("value: " + result_val);		//debug
						
						if (result_val.trim().matches("OK")){
							if (player_id==0){
								answer = ILA_answers.getFromFile(answerFile(), "xbmc_kodi_controls_stop1");	//"music stopped"
							}else if (player_id==1){
								answer = ILA_answers.getFromFile(answerFile(), "xbmc_kodi_controls_stop2");	//"video stopped";
							}else if (player_id==2){
								answer = ILA_answers.getFromFile(answerFile(), "xbmc_kodi_controls_stop3");	//"pictures stopped";
							}else{
								answer = ILA_answers.getFromFile(answerFile(), "xbmc_kodi_controls_stop4");	//"sorry I'm confused. I can't identify what Kodi is playing.";
								ILA_interface.avatar.avatar_mood="sad";
							}
							break TheSwitch;
						}else{
							answer = ILA_answers.getFromFile(answerFile(), "xbmc_kodi_controls_0");	//"Sorry I couldn't access the Kodi player somehow."
							break TheSwitch;
						}
					}
				
				//no connection to XBMC player found (Kodi server problem?)
				}else{
					answer = ILA_answers.getFromFile(answerFile(), "xbmc_kodi_controls_0");		//"Sorry I couldn't access the Kodi player somehow."
					ILA_interface.avatar.avatar_mood="sad";
					break;
				}
				
			//play something from folder
			case "xbmc_kodi_controls_play":
				
				//update folder with music if user supplied it as parameter 
				if (memories.length>3){
					//check if it is a folder
					File folder = new File(memories[3]);
					if (folder.isDirectory()) {
						music_folder = memories[3];
					
					//parameter is not a folder
					}else{
						answer = ILA_answers.getFromFile(answerFile(), "xbmc_kodi_controls_play0");		//"Sorry but the folder you showed me does not exist."
						ILA_interface.avatar.avatar_mood="sad";
						break;
					}
				}
				
				//check connection to XBMC/Kodi
				player_id = getPlayerID();
				if (player_id !=-2){
					//player found, load all mp3 files, make a new playlist, add music and play
					try{
						//load all files from music_folder
						ArrayList<File> music = new ArrayList<File>();
						music = ILA_decisions.listAllFiles(music_folder, music, false);	//last parameter decides to use sub-folders (true,false)
						
						//prepare playlist number 1
						if (!clearPlaylist("1")){
							answer = ILA_answers.getFromFile(answerFile(), "xbmc_kodi_controls_play0b");		//"Sorry I a folder or connection problem."
							ILA_interface.avatar.avatar_mood="sad";
							break TheSwitch;
						}
						
						//add all mp3s to playlist 1
						String song;
						for (File f : music){
							song = f.getAbsolutePath();
							if (song.matches(".*\\.mp3")){
								if (!addFileToPlaylist("1",song)){
									answer = ILA_answers.getFromFile(answerFile(), "xbmc_kodi_controls_play0b");		//"Sorry I a folder or connection problem."
									ILA_interface.avatar.avatar_mood="sad";
									break TheSwitch;
								}
							}
						}
						
						//play playlist 1
						if (!playPlaylist("1")){
							answer = ILA_answers.getFromFile(answerFile(), "xbmc_kodi_controls_play0b");		//"Sorry I a folder or connection problem."
							ILA_interface.avatar.avatar_mood="sad";
							break TheSwitch;
						}
						
						//mute Kodi until ILA has spoken - it would be nice to call this every time ILA is speaking or recoding
						muteTimer();
						
						//answer
						ILA_decisions.setSuperContext("xbmc_kodi_control");								//set the global context to xbmc - enables the context dependent commands
						answer = ILA_answers.getFromFile(answerFile(), "xbmc_kodi_controls_play1");		//"alrighty lets hear some good music."
						ILA_interface.avatar.avatar_mood="happy";
						break;
						
					//an error occurred - try catch is a bit rough here, better check each response and react, but as this is just a demo ... ;-) 
					}catch (Exception e){
						e.printStackTrace();
						answer = ILA_answers.getFromFile(answerFile(), "xbmc_kodi_controls_0");		//"Sorry I couldn't access the Kodi player somehow."
						ILA_interface.avatar.avatar_mood="sad";
						break;
					}
					
				//no connection to XBMC player found (Kodi server problem?)
					//one could either start Kodi here or ask the user to start Kodi or check the connection to the player
				}else{
					answer = ILA_answers.getFromFile(answerFile(), "xbmc_kodi_controls_0");		//"Sorry I couldn't access the Kodi player somehow."
					ILA_interface.avatar.avatar_mood="sad";
					break;
				}
					
		}
		
		//no hit - this should never happen because this class and method was called because the command list
		//says it will find the correct command here ...
		return answer;
		
	}

	
	//---HELPER methods for XBMC/Kodi---
	
	//get player ID - returns 1,2,3 for music, video, pictures, -1 for unknown and -2 for connection error
	public int getPlayerID(){
		String request = "";		//http GET request
		String result = "";			//result from request
		int player_id=-1;			//save player_id, start with -1 as unknown
		request = "{\"jsonrpc\": \"2.0\", \"method\": \"Player.GetActivePlayers\", \"id\": 1}";
		result = reader.httpGET(kodiURL, request);
		//System.out.println("result: " + result);						//debug
		//connection error
		if (result.trim().matches("<connection error>")){
			return -2;
		}
		String result_val = reader.jsonGetArray(result, "result");
		//System.out.println("result_val: " + result_val);				//debug
		if (!result_val.trim().matches("")){
			player_id = reader.jsonGetInteger(result_val, "playerid");
			return player_id;		//return ID if found
		}
		return -1;					//return -1 if not found
	}
	
	//clear playlist
	public boolean clearPlaylist(String ID){
		String request = "{\"jsonrpc\": \"2.0\", \"method\": \"Playlist.Clear\", \"params\":{\"playlistid\":" + ID + "}, \"id\": 1}";
		String result = reader.httpGET(kodiURL, request);
		//check result
		//System.out.println("playlist clear result: " + result);			//debug
		String result_val = reader.jsonGetString(result, "result");
		if (result_val.trim().matches("OK")){
			return true;
		}
		return false;
	}
	
	//add file ID to playlist
	public boolean addFileToPlaylist(String playlistid, String ID){
		//stupid file systems, we need to convert windows path to unix for Kodi
		if (ILA.isWindows){
			ID = ID.replace("\\","/");
		}
		String request = "{\"jsonrpc\": \"2.0\", \"method\": \"Playlist.Add\", \"params\":{\"playlistid\":" + playlistid + ", \"item\" :{ \"file\" : \"" + ID + "\"}}, \"id\" : 1}";
		String result = reader.httpGET(kodiURL, request);
		//check result
		//System.out.println("playlist add result: " + result);			//debug
		String result_val = reader.jsonGetString(result, "result");
		if (result_val.trim().matches("OK")){
			return true;
		}
		return false;
	}
	
	//play playlist with ID
	public boolean playPlaylist(String ID){
		String request = "{\"jsonrpc\": \"2.0\", \"method\": \"Player.Open\", \"params\":{\"item\":{\"playlistid\":" + ID + ", \"position\" : 0}}, \"id\": 1}";
		String result = reader.httpGET(kodiURL, request);
		//check result
		//System.out.println("playlist play result: " + result);			//debug
		String result_val = reader.jsonGetString(result, "result");
		if (result_val.trim().matches("OK")){
			return true;
		}
		return false;
	}
	
	//volume control: 
	public int reduceVolume(){
		String request = "{\"jsonrpc\": \"2.0\", \"method\": \"Application.SetVolume\", \"params\": { \"volume\": \"decrement\" }, \"id\": 1 }";
		String result = reader.httpGET(kodiURL, request);
		//check result
		//System.out.println("volume result: " + result);			//debug
		int result_val = reader.jsonGetInteger(result, "result");
		if (result_val>=0 & result_val<=100){
			return result_val;
		}
		return -1;
	}
	public int increaseVolume(){
		String request = "{\"jsonrpc\": \"2.0\", \"method\": \"Application.SetVolume\", \"params\": { \"volume\": \"increment\" }, \"id\": 1 }";
		String result = reader.httpGET(kodiURL, request);
		//check result
		//System.out.println("volume result: " + result);			//debug
		int result_val = reader.jsonGetInteger(result, "result");
		if (result_val>=0 & result_val<=100){
			return result_val;
		}
		return -1;
	}
	public boolean mute(){
		String request = "{\"jsonrpc\": \"2.0\", \"method\": \"Application.SetMute\", \"params\": { \"mute\": true }, \"id\": 1 }";
		@SuppressWarnings("unused")
		String result = reader.httpGET(kodiURL, request);
		//check result
		//System.out.println("volume mute result: " + result);			//debug
		return true;
	}
	public boolean unmute(){
		String request = "{\"jsonrpc\": \"2.0\", \"method\": \"Application.SetMute\", \"params\": { \"mute\": false }, \"id\": 1 }";
		@SuppressWarnings("unused")
		String result = reader.httpGET(kodiURL, request);
		//check result
		//System.out.println("volume unmute result: " + result);			//debug
		return true;
	}
	//trigger a timed mute that ends when ILA stops speaking or recording
	public void muteTimer(){
		Thread worker;
		//do it in its own thread
		worker = new Thread() {
          	public void run() {
          		//mute and wait a bit (2s)
          		mute();
          		try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
          		//then start checking for ILA actions
          		int counter=100;		//max wait 20s
          		while( (ILA_interface.avatar.isILAspeaking() || ILA_interface.avatar.is_recording==1) & counter>1 ){
        			counter--;
        			ILA_debug.println("ADDON-KODI - mute timer is waiting...",2);			//debug
        			try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }
        		}
          		//timers are over: unmute
          		unmute();
          	}
        };
        worker.start();
	}
}
