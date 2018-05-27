package src;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URL;

public class Sound extends Applet{
    AudioClip sound;
    boolean playing = false;
    
    public Sound(String file) {
        try {
            sound = getAudioClip(new URL(getCodeBase(), file));
        }
        
        catch (MalformedURLException e) {}
    }

    public void play() { 
        sound.play();
        playing = true;
    }

    public void stop() { 
        sound.stop(); 
        playing = false; 
    }
    public void loop() { 
        sound.loop();
        playing = true;
    }
}
