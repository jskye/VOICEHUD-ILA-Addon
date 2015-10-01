package ila;

public class TTS_CustomSynthesizer implements ILA_TTS_interface {
	
	static String ttsFile="Data/tts.wav";		//the place where the generated TTS gets saved
	//static String ttsFile="Data/tts.mp3";
	String language=ILA.language;

	//main method - generates a sound-file from 'text' and saves it in 'ttsFile' - rest is done in Avatar.class	
	public void read_write(String text) throws Exception {
		
	}

	//not yet implemented, but just in case ... :-)
	public void selectVoice(String voiceName) {
	}

	//not yet implemented
	public void switchToNextVoice() {
	}

}
