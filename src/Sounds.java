package src;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URL;

public class Sounds extends Applet{
    
    // Sound clips.
    
    AudioClip crashSound;
    AudioClip explosionSound;
    AudioClip fireSound;
    AudioClip missleSound;
    AudioClip saucerSound;
    AudioClip thrustersSound;
    AudioClip warpSound;
    
    // Counter and total used to track the loading of the sound clips.

    int clipTotal   = 0;
    int clipsLoaded = 0;
    
    public Sounds() {
        super();
    }
    
    public void playCrash()     { crashSound.play();     }
    public void stopCrash()     { crashSound.stop();     }
    public void playExplosion() { explosionSound.play(); }
    public void stopExplosion() { explosionSound.stop(); }
    public void playFire()      { fireSound.play();      }
    public void stopFire()      { fireSound.stop();      }
    public void loopMissle()    { missleSound.loop();    }
    public void playMissle()    { missleSound.play();    }
    public void stopMissle()    { missleSound.stop();    }
    public void loopSaucer()    { saucerSound.loop();    }
    public void playSaucer()    { saucerSound.play();    }
    public void stopSaucer()    { saucerSound.stop();    }
    public void loopThrusters() { thrustersSound.loop(); }
    public void playThrusters() { thrustersSound.play(); }
    public void stopThrusters() { thrustersSound.stop(); }
    public void playWarp()      { warpSound.play();      }
    public void stopWarp()      { warpSound.stop();      }

    
    
    public void loadSounds() {

        // Load all sound clips by playing and immediately stopping them. Update
        // counter and total for display.

        try {
          crashSound     = getAudioClip(new URL(getCodeBase(), "crash.au"));
          clipTotal++;
          explosionSound = getAudioClip(new URL(getCodeBase(), "explosion.au"));
          clipTotal++;
          fireSound      = getAudioClip(new URL(getCodeBase(), "fire.au"));
          clipTotal++;
          missleSound    = getAudioClip(new URL(getCodeBase(), "missle.au"));
          clipTotal++;
          saucerSound    = getAudioClip(new URL(getCodeBase(), "saucer.au"));
          clipTotal++;
          thrustersSound = getAudioClip(new URL(getCodeBase(), "thrusters.au"));
          clipTotal++;
          warpSound      = getAudioClip(new URL(getCodeBase(), "warp.au"));
          clipTotal++;
        }
        catch (MalformedURLException e) {}

        try {
          crashSound.play();     crashSound.stop();     clipsLoaded++;
          repaint(); Thread.currentThread().sleep(Assets.DELAY);
          explosionSound.play(); explosionSound.stop(); clipsLoaded++;
          repaint(); Thread.currentThread().sleep(Assets.DELAY);
          fireSound.play();      fireSound.stop();      clipsLoaded++;
          repaint(); Thread.currentThread().sleep(Assets.DELAY);
          missleSound.play();    missleSound.stop();    clipsLoaded++;
          repaint(); Thread.currentThread().sleep(Assets.DELAY);
          saucerSound.play();    saucerSound.stop();    clipsLoaded++;
          repaint(); Thread.currentThread().sleep(Assets.DELAY);
          thrustersSound.play(); thrustersSound.stop(); clipsLoaded++;
          repaint(); Thread.currentThread().sleep(Assets.DELAY);
          warpSound.play();      warpSound.stop();      clipsLoaded++;
          repaint(); Thread.currentThread().sleep(Assets.DELAY);
        }
        catch (InterruptedException e) {}
      }
}
