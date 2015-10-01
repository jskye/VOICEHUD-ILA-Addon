package ila;

/***************************************************************************
 * STT_CustomRecognizer is a class one can use to implement a custom method 
 * for speech-to-text recognition. Anything is possible ... ;-)
 * 
 * @author Florian Quirin for I.L.A. project
 ***************************************************************************/

public class STT_CustomRecognizer {

	public int stopCounter=120;				//maximum wait time when stopping the recognizer - x*200ms wait time (120=24s) 
	public boolean usePreRecording=false;	//you can use ILA's default methods to pre-record a wav-file (Data/rec.wav) 
	public int abort=0;						//use this for example to keep recognizer in a loop until aborted (see DEMO below)
	public boolean isActive=false;			//use this for example to keep track of the recognizer/transcriber status
	public boolean isFirstStart=true;		//used to tweak some user feedback - will be set to true after first initialization
	
	Thread worker;		//thread to run the recognition process
	
	//initialize the recognizer - load all stuff needed to run the recognizer on startup (auto-executed)
	public void config() {
		//notify user that we have to load stuff
		ILA_interface.avatar.load_animation();
		if (!isFirstStart)
			ILA_interface.avatar.new_dialogue_noExtras(ILA_answers.get("ilahastoload"), false);
		//add code here:
		//
		//
		//
		//successful initialization should end with:
    	if (!isFirstStart){
    		//notify user that we are done
			ILA_interface.avatar.new_dialogue_noExtras(ILA_answers.get("ilahastoload2"), true);
    	}
        isFirstStart=false;
		ILA_speechControl.is_CustomRecognizerInitialized=1;		//notify speech control
		ILA_debug.println("CUSTOM-RECOGNIZER initialized with sampleRate: "+ILA.audioSampleRate+" Hz",1);	//debug
		ILA_interface.avatar.unblock_controls();
	}

	//returns the current volume of the recorder
	public float getVolume() {
		float volume=0.0f;
		
		//if you are using the default wav-recorder this will be used:
		if (ILA_speechControl.inuse_genericVoiceRec==1){
			volume=ILA_speechControl.genericVoiceIn.left.level()*(float)(5.0);
		
		//else: use your own volume-method
		}else{
			//add code here:
			//
			//
		}
		return volume;
	}

	//the custom method to perform speech-to-text
	public void recognize() {
		//we want this to run in a thread
		worker = new Thread() {
        	public void run() {
        		System.out.println("CUSTOM-RECOGNIZER: recognize() DEMO");					//debug
		
				//if you want to perform recognition on a pre-recorded wav-file use this:
				if (usePreRecording){
					ILA_speechControl.recordVoice("Data/");
					//maximum recording time in ms
					ILA_interface.avatar.timer2=new Tools_Timer(15000);									
					ILA_interface.avatar.timer2.start();
					//timer after activation (wait at least this time before aborting recognition)
					ILA_interface.avatar.timer3=new Tools_Timer(ILA_speechControl.initialWaittime);
					ILA_interface.avatar.timer3.start();
					//wait for pre-recording
					while (ILA_speechControl.inuse_genericVoiceRec==1){
						try { Thread.sleep(200); } catch (InterruptedException e) {	e.printStackTrace(); }
					}
					//check if the user switched to keyboard input
					if (!ILA_interface.inputFromKeyboard){
		        		ILA_interface.avatar.load_animation();		//transcribing should block user input and notify the user that we are loading
		        		//then you can add your code to analyze the recorded wav (path: Data/rec.wav):
						//
		        		//
		        		//end with:
		        		finalize_and_activate();
		        		
		        	//Keyboard input interrupts transcribing
					}else{
						//end with:
						finalize_and_activate();
		        	}
		
				//or write your own code (you can/should use the same timers for max. rec and activation as above):
				}else{
					//add code here:
					//
					//DEMO:
					//
					notifyILA_recording(5000);		//notify ILA/User and set maximum recording time to 5s
					String hypo = "";
					abort=0;	isActive=true;
					while (abort==0){
						ILA.waitfor(2000);			//simulates the transcription process
						hypo = "This is a";
						addInterfaceText(hypo);
						if (abort==1) break;
						ILA.waitfor(2000);			//simulates the transcription process
						hypo = "custom recognizer demo";
						addInterfaceText(hypo);
					}
					isActive=false;
					//
					//END DEMO
					//end with:
					finalize_and_activate();
				}
        	}
        };
        worker.start();
	}

	//stop the recognizer - this will automatically start a timer (see variable stopCounter)
	public void stopRecognizer() {
		System.out.println("CUSTOM-RECOGNIZER: stopRecognizer() DEMO");					//debug
		
		//the default pre-recorder can be stopped like this:
		if (usePreRecording){
			ILA_speechControl.stopRecordingVoice();
			
		//or you write your own code:
		}else{
			//add code here:
			//
			//DEMO:
			//
			abort=1;
			//close microphone
			//
			//END DEMO
			//end with:
			ILA_interface.avatar.confirmationSound(true);
			ILA_interface.avatar.load_animation();
		}
	}
	
	//this will be called when the maximum wait time to stop the recognizer is exceeded - it should be used to force quit the recognition
	//note: force quit will lead to a reloading of the config() on next call
	public void interruptRecognizer() {
		System.out.println("CUSTOM-RECOGNIZER: interruptRecognizer() DEMO");			//debug
		//add your code here:
		//
		//
	}
	
	//this is called when a grammar switch request is sent - type would typically be a reference to the grammar-filename
	public void switchGrammar(String type) {
		//grammarTypeInUse=0: dialog, 1: general, 2: custom-file, -1: hey ILA
		System.out.println("CUSTOM-RECOGNIZER: switchGrammar DEMO, input: "+type);		//debug
		//just an example to start with:
    		//String customGrammar=type+"_input_"+ILA.language;
    		//File customGrammarFile = new File(ILA.grammarPath+customGrammar+".gram");
    		//boolean customFileExists = customGrammarFile.exists();
		//add your code here:
		//
		//
    	//ILA_speechControl.sphinxSTT.grammarTypeInUse=
    	//ILA_speechControl.sphinxSTT.grammarNameInUse=type;
		ILA_debug.println("grammar set to: "+type, 3);		//debug
	}
	
	//-------------useful stuff--------------
	
	//notify ILA that we are recording now - switches animations, unblock controls, play blib-sound
	public void notifyILA_recording(int max_rec_time){
		ILA_interface.avatar.confirmationSound(true);
        ILA_interface.avatar.record_animation();	//tell ILA that we record now
		ILA_interface.avatar.unblock_controls();
		//maximum recording time in ms
		ILA_interface.avatar.timer2=new Tools_Timer(max_rec_time);									
		ILA_interface.avatar.timer2.start();
		//timer after activation (wait at least this time before aborting recognition)
		ILA_interface.avatar.timer3=new Tools_Timer(ILA_speechControl.initialWaittime);
		ILA_interface.avatar.timer3.start();
	}
	
	//add recognized text to interface - use this to add text while transcription is still in process 
	public void addInterfaceText(String hypothesis){
		if (!ILA_interface.inputFromKeyboard){
			hypothesis = hypothesis.replaceAll("<unk>", "<???>");
			ILA_interface.textField.setText((ILA_interface.textField.getText().trim() + " " + hypothesis.trim()).trim());
	    	ILA_interface.textField.setCaretPosition(ILA_interface.textField.getDocument().getLength());
	    	//ILA_debug.println("RECOGNIZER(G="+ILA_speechControl.sphinxSTT.grammarTypeInUse+"): "+hypothesis, 4);	
		}
	}
	//set interface text - use this e.g. for a final hypothesis
	public void setInterfaceText(String hypothesis){
		if (!ILA_interface.inputFromKeyboard){
			hypothesis = hypothesis.replaceAll("<unk>", "<???>");
			ILA_interface.textField.setText(hypothesis.trim());
	    	ILA_interface.textField.setCaretPosition(ILA_interface.textField.getDocument().getLength());
	    	//ILA_debug.println("RECOGNIZER(G="+ILA_speechControl.sphinxSTT.grammarTypeInUse+"): "+hypothesis, 4);
		}
	}
	//get interface text
	public String getInterfaceText(){
		return ILA_interface.textField.getText();
	}
	
	//finalize - notify ILA speech control that we are done and activate action
	public void finalize_and_activate(){
		//make sure that you added all utterances or the final hypothesis via addInterfaceText(...) or setInterfaceText(...)
		String hypo=ILA_interface.textField.getText();
        //check if there was (well understood) input
		hypo=hypo.replaceAll("(<unk>|<\\?\\?\\?>)","").trim();
        if (hypo.matches("")){
        	ILA_interface.avatar.result = "???";
			ILA_interface.textField.setText(ILA_interface.avatar.result);
        }
        ILA_debug.println("RECOGNIZER(G="+ILA_speechControl.sphinxSTT.grammarTypeInUse+"): "+hypo, 4);
        //notify and activate
		ILA_speechControl.inuse_CustomRecognizer=0;		//notify speech control that the recognizer is deactivated
		ILA_interface.sendActionThread();				//activate action - will activate the text that has been added to the interface
	}
}
