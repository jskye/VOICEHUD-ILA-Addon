This is the folder for acoustic models used by Sphinx-4. Together with

your microphone this is the most important part for good quality high

precision speech recognition.

The developer version of ILA includes only the 8kHz english model, plz

see the compiled verison of ILA or e.g. CMU Sphinx homepage for more

acoustic models.


If you are planning to switch between different models please consider 

defining a 'samplerate.properties' file in the AM folder to auto-load

the samplerate (see example in the included folders).


If you want to mix Pocketsphinx specific AMs and Sphinx-4 (for let's

say 'hey ILA') please include the string "pocket" in the foldername of

the Pocketsphinx model (if it is specifically made for Pocketsphinx)

and set the "defaultAcousticModel" parameter in settings 

('Data/config.properties') so Sphinx-4 can fall back to another

model (Sphinx-4 and Pocketsphinx models are not 100% compatible I think)