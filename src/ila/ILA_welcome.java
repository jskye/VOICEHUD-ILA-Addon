package ila;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/***************************************************************************
 * ILA_welcome is the ILA welcome screen, the first screen the user sees 
 * after loading. It's intention is to help the user get started.
 * It also demonstrates the general visual design of ILA 
 *
 * @author Florian Quirin for I.L.A. project
 ***************************************************************************/

//I included this method in the dev. package mainly to give you an idea how
//to create new windows and how to make ILA do things on a button click :-)

public class ILA_welcome {

	//define the Java Swing components and help variables
	ILA_window window;
	JLabel langSelect;
	JButton testSystem, speakerAdapt, settings, langSelect1, langSelect2;
	String initLang=ILA.language;
	JPanel bottom;
	JCheckBox showOnStart;
	double bottomSize;
	
	//let's go!
	public ILA_welcome(){
		
		//creates a default ILA interface with certain proportions (menue button size/title size/mid size/south size).
		//The idea was to give the user a nice starting point to create a component in the ILA look-and-feel (as they say today ^^)
		//and then just add some stuff to "mid" and "south". However Swing is tricky, sometimes formatation gets completely lost :-/
		window = new ILA_window("welcome to I.L.A.", (double) 0.03, (double) 0.1, (double) 0.16, (double) 0.41);
		//we remove "mid" and "south" for now because we want to rebuild them now
		window.remove(window.south);
		window.remove(window.mid);
		
		//prepare the size of a new component
		bottomSize = (1f-window.northSize-window.titleSize-window.midSize-window.southSize);
		
		//label for language select
		langSelect = new JLabel();
		langSelect.setPreferredSize(new Dimension((int)(320*0.85f*ILA.scale),(int)(37*ILA.scale)));
		langSelect.setFont(new java.awt.Font(ILA.textFont, 1, (int)(0.045*window.w*ILA.textScale)));
		langSelect.setText("<html><center>Select your language / wähle deine Sprache</center></html>");
		window.mid.add(langSelect);
	    
		//language Button 1-english
		langSelect1 = new JButton ();
		langSelect1.setPreferredSize(new Dimension((int)(125*ILA.scale),(int)(37*ILA.scale)));
		langSelect1.setHorizontalAlignment(JLabel.CENTER);
		langSelect1.setVerticalAlignment(JLabel.CENTER);
		langSelect1.setBackground(new Color(50,133,255));
		langSelect1.setForeground(new Color(255,255,255));
		langSelect1.setFont(new java.awt.Font(ILA.textFont, 1, (int)(0.045*window.w*ILA.textScale)));
		langSelect1.setText("English");
		langSelect1.setFocusPainted(false);
		window.mid.add(langSelect1);
		langSelect1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	// change language and button look and recreate the lower part in the current language
            	ILA_commands.changeVoiceRecLang("en");
            	langSelect2.setBackground(new Color(50,133,255));
            	langSelect1.setBackground(new Color(0,255,0));
            	createSouth();
            	refresh();
            }
        });
		
		//language Button 2-deutsch
		langSelect2 = new JButton ();
		langSelect2.setPreferredSize(new Dimension((int)(125*ILA.scale),(int)(37*ILA.scale)));
		langSelect2.setHorizontalAlignment(JLabel.CENTER);
		langSelect2.setVerticalAlignment(JLabel.CENTER);
		langSelect2.setBackground(new Color(50,133,255));
		langSelect2.setForeground(new Color(255,255,255));
		langSelect2.setFont(new java.awt.Font(ILA.textFont, 1, (int)(0.045*window.w*ILA.textScale)));
		langSelect2.setText("Deutsch");
		langSelect2.setFocusPainted(false);
		window.mid.add(langSelect2);
		langSelect2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	// change language and button look and recreate the lower part in the current language
            	ILA_commands.changeVoiceRecLang("de");
            	langSelect1.setBackground(new Color(50,133,255));
            	langSelect2.setBackground(new Color(0,255,0));
            	createSouth();
            	refresh();
            }
        });
		
		//update buttons
		if (ILA.language.matches("de")){
			langSelect2.setBackground(new Color(0,255,0));
			langSelect1.setBackground(new Color(50,133,255));
		}else{
			langSelect1.setBackground(new Color(0,255,0));
			langSelect2.setBackground(new Color(50,133,255));
		}
		
		//add the new mid to the window again
		window.add(window.mid);
		
		//create the south part of the window
		createSouth();

		window.setProperLocation();
		window.openWindow();
	}
	
	//create the rest of the window
	void createSouth(){
		
		//because we ant to call this everytime the user clicks a button we have to remove the old part
		window.remove(window.south);
		if (bottom!=null)
			window.remove(bottom);
		
		//prepare text in current language
		String text="";
		String testName="", adaptName="", settingsName="", showName="";
		if (ILA.language.matches("de")){
			text = "Hallo und willkommen :-)\n\n"
					+ "ILA kann auf viele Möglichkeiten eingestellt und angepasst "
					+ "werden, auf der Homepage findest du dazu mehrere Tutorials. "
					+ "Schau dich am besten einfach mal um und experimentiere ein "
					+ "wenig ^^. Bevor du startest empfehle ich den 'System Test' "
					+ "und die 'Anpassung des akustischen Modells' durchzuführen. "
					+ "Das Menü erreichst du jederzeit über den 3. Knopf oben rechts. "
					+ "\n\nViel Spaß!";
			
			testName="System Test"; adaptName="Anpassung des akustischen Modells"; settingsName="Einstellungen"; showName="anzeigen beim Start";
		}else{
			text = "Hello and welcome :-)\n\n"
					+ "ILA can be configured and customized in many ways please check "
					+ "the homepage for tutorials and info. The best way to discover ILA "
					+ "is to play with the settings and experiment a bit ^^. I recommend to "
					+ "start with the 'Test' feature and the 'Acoustic model adaptation' to "
					+ "adjust ILA to your hardware. You can access the menu anytime by "
					+ "clicking the 3rd button on the upper right corner."
					+ "\n\nHave Fun!";
			
			testName="System Test"; adaptName="Speaker Adaptation"; settingsName="Settings"; showName="show on start";
		}
		
		//create a new south panel
		window.south = new JPanel (new GridBagLayout());
		window.south.setBackground(new Color(255,255,255));
		window.south.setPreferredSize(new Dimension(window.w,(int)(window.southSize*window.h)));
		
		//set the text
		JTextArea message = new JTextArea();
		message.setBackground(new Color(255,255,255));
		//message.setForeground(new Color(50,133,255));
		message.setForeground(new Color(0,0,0));
		message.setFont(new java.awt.Font(ILA.textFont, 1, (int)(0.045*window.w*ILA.textScale)));
	    message.setPreferredSize(new Dimension((int)(window.w*0.85),(int)(window.southSize*window.h)));
		message.setMinimumSize(new Dimension((int)(window.w*0.85),(int)(window.southSize*window.h)));
		message.setLineWrap(true);
		message.setWrapStyleWord(true);
		message.setText(text);
	    //avatar.setText(formatText(currentSentence));
		window.south.add(message,new GridBagConstraints());
		
		//south.setBackground(new Color(255,255,255));
	    window.add(window.south);
	    
	    //make the buttons on the bottom of the window
	    bottom = new JPanel (new FlowLayout(FlowLayout.CENTER, (int)(5*ILA.scale),(int)(5*ILA.scale)));
		//GridBagConstraints c = new GridBagConstraints();
	    bottom.setBackground(new Color(255,255,255));
	    bottom.setPreferredSize(new Dimension(window.w,(int)(bottomSize*window.h)));
	    
	 	//test-system button
  		testSystem = new JButton ();
  		testSystem.setPreferredSize(new Dimension((int)(280*ILA.scale),(int)(37*ILA.scale)));
  		testSystem.setHorizontalAlignment(JLabel.CENTER);
  		testSystem.setVerticalAlignment(JLabel.CENTER);
  		testSystem.setBackground(new Color(50,133,255));
  		testSystem.setForeground(new Color(255,255,255));
  		testSystem.setFont(new java.awt.Font(ILA.textFont, 1, (int)(0.045*window.w*ILA.textScale)));
  		testSystem.setText(testName);
  		testSystem.setFocusPainted(false);
  		bottom.add(testSystem);
  		testSystem.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
            	  
            	  //call an ILA command by simply adding the text and activate the input just like a user would
            	  ILA_interface.textField.setText("test");
            	  ILA_interface.sendActionThread();
              }
        });
  		
  		//speaker adaptation button
  		speakerAdapt = new JButton ();
  		speakerAdapt.setPreferredSize(new Dimension((int)(280*ILA.scale),(int)(37*ILA.scale)));
  		speakerAdapt.setHorizontalAlignment(JLabel.CENTER);
  		speakerAdapt.setVerticalAlignment(JLabel.CENTER);
  		speakerAdapt.setBackground(new Color(50,133,255));
  		speakerAdapt.setForeground(new Color(255,255,255));
  		speakerAdapt.setFont(new java.awt.Font(ILA.textFont, 1, (int)(0.045*window.w*ILA.textScale)));
  		speakerAdapt.setText(adaptName);
  		speakerAdapt.setFocusPainted(false);
  		bottom.add(speakerAdapt);
  		speakerAdapt.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent evt) {

            	  //call an ILA command by simply adding the text and activate the input just like a user would
            	  ILA_interface.textField.setText("ussa");
            	  ILA_interface.sendActionThread();
              }
        });
  		
  		//settings button
  		settings = new JButton ();
  		settings.setPreferredSize(new Dimension((int)(280*ILA.scale),(int)(37*ILA.scale)));
  		settings.setHorizontalAlignment(JLabel.CENTER);
  		settings.setVerticalAlignment(JLabel.CENTER);
  		settings.setBackground(new Color(50,133,255));
  		settings.setForeground(new Color(255,255,255));
  		settings.setFont(new java.awt.Font(ILA.textFont, 1, (int)(0.045*window.w*ILA.textScale)));
  		settings.setText(settingsName);
  		settings.setFocusPainted(false);
  		bottom.add(settings);
  		settings.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent evt) {

            	  //open the settings window
            	  ILA_interface.openSettingsGUI();
              }
        });
  		
  		//make the "show on start" checkbox
  		showOnStart = new JCheckBox(showName);
  		showOnStart.setForeground(new Color(50,133,255));
  		showOnStart.setBackground(new Color(255,255,255));
  		showOnStart.setPreferredSize(new Dimension((int)(180*ILA.scale),(int)(37*ILA.scale)));
  		showOnStart.setSelected(true);
  		showOnStart.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
            	  //change the behavior of the welcome window
            	  if (showOnStart.isSelected()){
            		  //System.out.println("always show on start");
            		  ILA.showWelcomeWindow=1;
            	  }else{
            		  //System.out.println("never show on start");
            		  ILA.showWelcomeWindow=0;
            	  }
              }
        });
  		bottom.add(showOnStart);
  		
  		//empty space to adjust button position
        JPanel dummy1 = new JPanel ();
        dummy1.setBackground(new Color(255,255,255));
        dummy1.setPreferredSize(new Dimension((int)(110*ILA.scale),(int)(37*ILA.scale)));
        bottom.add(dummy1);
  		
  		window.add(bottom);
	}
	
	void refresh(){
		window.setVisible(false);
		window.setVisible(true);
	}
	
}
