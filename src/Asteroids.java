package src;
/******************************************************************************
  Asteroids, Version 1.3

  Copyright 1998-2001 by Mike Hall.
  Please see http://www.brainjar.com for terms of use.

  Revision History:

  1.01, 12/18/1999: Increased number of active photons allowed.
                    Improved explosions for more realism.
                    Added progress bar for loading of sound clips.
  1.2,  12/23/1999: Increased frame rate for smoother animation.
                    Modified code to calculate game object speeds and timer
                    counters based on the frame rate so they will remain
                    constant.
                    Improved speed limit checking for ship.
                    Removed wrapping of photons around screen and set a fixed
                    firing rate.
                    Added sprites for ship's thrusters.
  1.3,  01/25/2001: Updated to JDK 1.1.8.

  Usage:

  <applet code="Asteroids.class" width=w height=h></applet>

  Keyboard Controls:

  S            - Start Game    P           - Pause Game
  Cursor Left  - Rotate Left   Cursor Up   - Fire Thrusters
  Cursor Right - Rotate Right  Cursor Down - Fire Retro Thrusters
  Spacebar     - Fire Cannon   H           - Hyperspace
  M            - Toggle Sound  D           - Toggle Graphics Detail

******************************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.applet.Applet;
import java.applet.AudioClip;

/******************************************************************************
  The AsteroidsSprite class defines a game object, including it's shape,
  position, movement and rotation. It also can detemine if two objects collide.
******************************************************************************/

public class Asteroids extends Applet implements Runnable, KeyListener {

  // Copyright information.

  String copyName = "Asteroids";
  String copyVers = "Version 1.3";
  String copyInfo = "Copyright 1998-2001 by Mike Hall";
  String copyLink = "http://www.brainjar.com";
  String copyText = copyName + '\n' + copyVers + '\n'
                  + copyInfo + '\n' + copyLink;

  // Thread control variables.

  Thread loadThread;
  Thread loopThread;

  // Background stars.

  int     numStars;
  Point[] stars;

  // Game data.

  int score;
  int highScore;
  int newShipScore;
  int newUfoScore;

  // Flags for game state and options.

 
  boolean loaded = false;
  boolean paused;
  boolean playing;
  boolean sound;
  boolean detail;

  // Key flags.

  boolean left  = false;
  boolean right = false;
  boolean up    = false;
  boolean down  = false;

  // Sprite objects.

  Ship ship;
  //AsteroidsSprite   fwdThruster, revThruster;
  Ufo         ufo;
  Missle      missle;
  Photon[]    photons    = new Photon[Assets.MAX_SHOTS];
  Asteroid[]  asteroids  = new Asteroid[Assets.MAX_ROCKS];
  Explosion[] explosions = new Explosion[Assets.MAX_SCRAP];

  // Ship data.

  int shipsLeft;       // Number of ships left in game, including current one.
  int shipCounter;     // Timer counter for ship explosion.
  int hyperCounter;    // Timer counter for hyperspace.

  // Photon data.

  int   photonIndex;    // Index to next available photon sprite.
  long  photonTime;     // Time value used to keep firing rate constant.

  // Flying saucer data.

  int ufoPassesLeft;    // Counter for number of flying saucer passes.
  int ufoCounter;       // Timer counter used to track each flying saucer pass.

  // Missle data.

  int missleCounter;    // Counter for life of missle.

  // Asteroid data.

  boolean[] asteroidIsSmall = new boolean[Assets.MAX_ROCKS];    // Asteroid size flag.
  int       asteroidsCounter;                            // Break-time counter.
  double    asteroidsSpeed;                              // Asteroid speed.
  int       asteroidsLeft;                               // Number of active asteroids.

  // Explosion data.

  int[] explosionCounter = new int[Assets.MAX_SCRAP];  // Time counters for explosions.
  int   explosionIndex;                         // Next available explosion sprite.


  // Flags for looping sound clips.

  boolean thrustersPlaying;
  boolean saucerPlaying;
  boolean misslePlaying;

  // Counter and total used to track the loading of the sound clips.

  int clipTotal   = 0;
  int clipsLoaded = 0;

  // Off screen image.

  Dimension offDimension;
  Image     offImage;
  Graphics  offGraphics;
  
  Sounds sounds;

  // Data for the screen font.

  Font font      = new Font("Helvetica", Font.BOLD, 12);
  FontMetrics fm = getFontMetrics(font);
  int fontWidth  = fm.getMaxAdvance();
  int fontHeight = fm.getHeight();

  public String getAppletInfo() {

    // Return copyright information.

    return(copyText);
  }

  public void init() {

    Dimension d = getSize();
    int i;

    System.out.println(copyText);  // Display copyright information.
    addKeyListener(this);          // Set up key event handling and set focus to applet window.
    requestFocus();
    AsteroidsSprites.width  = d.width;  // Save the screen size.
    AsteroidsSprites.height = d.height;

    // Generate the starry background.
    numStars = AsteroidsSprites.width * AsteroidsSprites.height / 5000;
    stars = new Point[numStars];
    for (i = 0; i < numStars; i++) stars[i] = new Point((int) (Math.random() * AsteroidsSprites.width ), 
                                                        (int) (Math.random() * AsteroidsSprites.height));

    sounds = new Sounds();
    ship   = new Ship();    // Create shape for the ship sprite.
    ufo    = new Ufo();     // Create UFO object
    missle = new Missle();  // Create shape for the guided missle.
      
    for (i = 0; i < Assets.MAX_SHOTS; i++) photons[i]    = new Photon();    // Create shape for each photon sprites.
    for (i = 0; i < Assets.MAX_ROCKS; i++) asteroids[i]  = new Asteroid();  // Create asteroid sprites.
    for (i = 0; i < Assets.MAX_SCRAP; i++) explosions[i] = new Explosion(); // Create explosion sprites.

    // Initialize game data and put us in 'game over' mode.

    highScore = 0;
    sound = true;
    detail = true;
    initGame();
    endGame();
  }

  public void initGame() {

    // Initialize game data and sprites.

    score = 0;
    shipsLeft = Assets.MAX_SHIPS;
    asteroidsSpeed = Assets.MIN_ROCK_SPEED;
    newShipScore = Assets.NEW_SHIP_POINTS;
    newUfoScore = Assets.NEW_UFO_POINTS;
    initShip();
    initPhotons();
    stopUfo();
    stopMissle();
    initAsteroids();
    initExplosions();
    playing = true;
    paused = false;
    photonTime = System.currentTimeMillis();
  }

  public void endGame() {

    // Stop ship, flying saucer, guided missle and associated sounds.

    playing = false;
    stopShip();
    stopUfo();
    stopMissle();
  }

  public void start() {

    if (loopThread == null) {
      loopThread = new Thread(this);
      loopThread.start();
    }
    if (!loaded && loadThread == null) {
      loadThread = new Thread(this);
      loadThread.start();
    }
  }

  public void stop() {

    if (loopThread != null) {
      loopThread.stop();
      loopThread = null;
    }
    if (loadThread != null) {
      loadThread.stop();
      loadThread = null;
    }
  }

  public void run() {

    int i, j;
    long startTime;

    // Lower this thread's priority and get the current time.

    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    startTime = System.currentTimeMillis();

    // Run thread for loading sounds.

    if (!loaded && Thread.currentThread() == loadThread) {
      loadSounds();
      loaded = true;
      loadThread.stop();
    }

    // This is the main loop.

    while (Thread.currentThread() == loopThread) {

      if (!paused) {

        // Move and process all sprites.

        updateShip();
        updatePhotons();
        updateUfo();
        updateMissle();
        updateAsteroids();
        updateExplosions();

        // Check the score and advance high score, add a new ship or start the
        // flying saucer as necessary.

        if (score > highScore)
          highScore = score;
        if (score > newShipScore) {
          newShipScore += Assets.NEW_SHIP_POINTS;
          shipsLeft++;
        }
        if (playing && score > newUfoScore && !ufo.active) {
          newUfoScore += Assets.NEW_UFO_POINTS;
          ufoPassesLeft = Assets.UFO_PASSES;
          initUfo();
        }

        // If all asteroids have been destroyed create a new batch.

        if (asteroidsLeft <= 0)
            if (--asteroidsCounter <= 0)
              initAsteroids();
      }

      // Update the screen and set the timer for the next loop.

      repaint();
      try {
        startTime += Assets.DELAY;
        Thread.sleep(Math.max(0, startTime - System.currentTimeMillis()));
      }
      catch (InterruptedException e) {
        break;
      }
    }
  }

  public void loadSounds() {

    sounds.loadSounds();
  }

  public void initShip() {

    // Reset the ship sprite at the center of the screen and
    // initialize thruster sprites.
    ship.initShip();

    if (loaded)
    
    sounds.stopThrusters();
    thrustersPlaying = false;
    hyperCounter = 0;
  }

  public void updateShip() {

    double dx, dy, speed;

    if (!playing)
      return;

    // Rotate the ship if left or right cursor key is down.

    if (left) {
      ship.angle += Assets.SHIP_ANGLE_STEP;
      if (ship.angle > 2 * Math.PI)
        ship.angle -= 2 * Math.PI;
    }
    if (right) {
      ship.angle -= Assets.SHIP_ANGLE_STEP;
      if (ship.angle < 0)
        ship.angle += 2 * Math.PI;
    }

    // Fire thrusters if up or down cursor key is down.

    dx = Assets.SHIP_SPEED_STEP * -Math.sin(ship.angle);
    dy = Assets.SHIP_SPEED_STEP *  Math.cos(ship.angle);
    if (up) {
      ship.deltaX += dx;
      ship.deltaY += dy;
    }
    if (down) {
        ship.deltaX -= dx;
        ship.deltaY -= dy;
    }

    // Don't let ship go past the speed limit.

    if (up || down) {
      speed = Math.sqrt(ship.deltaX * ship.deltaX + ship.deltaY * ship.deltaY);
      if (speed > Assets.MAX_SHIP_SPEED) {
        dx = Assets.MAX_SHIP_SPEED * -Math.sin(ship.angle);
        dy = Assets.MAX_SHIP_SPEED *  Math.cos(ship.angle);
        if (up)
          ship.deltaX = dx;
        else
          ship.deltaX = -dx;
        if (up)
          ship.deltaY = dy;
        else
          ship.deltaY = -dy;
      }
    }

    // Move the ship. If it is currently in hyperspace, advance the countdown.

    if (ship.active) {
      ship.advance();
      ship.render();
      if (hyperCounter > 0)
        hyperCounter--;

      // Update the thruster sprites to match the ship sprite.

      ship.updateThrusters();
      
    }

    // Ship is exploding, advance the countdown or create a new ship if it is
    // done exploding. The new ship is added as though it were in hyperspace.
    // (This gives the player time to move the ship if it is in imminent
    // danger.) If that was the last ship, end the game.

    else
      if (--shipCounter <= 0)
        if (shipsLeft > 0) {
          initShip();
          hyperCounter = Assets.HYPER_COUNT;
        }
        else
          endGame();
  }

  public void stopShip() {

    ship.active = false;
    shipCounter = Assets.SCRAP_COUNT;
    if (shipsLeft > 0)
      shipsLeft--;
    if (loaded)
      sounds.stopThrusters();
    thrustersPlaying = false;
  }

  public void initPhotons() {

    int i;

    for (i = 0; i < Assets.MAX_SHOTS; i++)
      photons[i].active = false;
    photonIndex = 0;
  }

  public void updatePhotons() {

    int i;

    // Move any active photons. Stop it when its counter has expired.

    for (i = 0; i < Assets.MAX_SHOTS; i++)
      if (photons[i].active) {
        if (!photons[i].advance())
          photons[i].render();
        else
          photons[i].active = false;
      }
  }

  public void initUfo() {

    double angle, speed;

    // Randomly set flying saucer at left or right edge of the screen.

    ufo.active = true;
    ufo.x = -AsteroidsSprites.width / 2;
    ufo.y = Math.random() * 2 * AsteroidsSprites.height - AsteroidsSprites.height;
    angle = Math.random() * Math.PI / 4 - Math.PI / 2;
    speed = Assets.MAX_ROCK_SPEED / 2 + Math.random() * (Assets.MAX_ROCK_SPEED / 2);
    ufo.deltaX = speed * -Math.sin(angle);
    ufo.deltaY = speed *  Math.cos(angle);
    if (Math.random() < 0.5) {
      ufo.x = AsteroidsSprites.width / 2;
      ufo.deltaX = -ufo.deltaX;
    }
    if (ufo.y > 0)
      ufo.deltaY = ufo.deltaY;
    ufo.render();
    saucerPlaying = true;
    if (sound)
      sounds.loopSaucer();
    ufoCounter = (int) Math.abs(AsteroidsSprites.width / ufo.deltaX);
  }

  public void updateUfo() {

    int i, d;
    boolean wrapped;

    // Move the flying saucer and check for collision with a photon. Stop it
    // when its counter has expired.

    if (ufo.active) {
      if (--ufoCounter <= 0) {
        if (--ufoPassesLeft > 0)
          initUfo();
        else
          stopUfo();
      }
      if (ufo.active) {
        ufo.advance();
        ufo.render();
        for (i = 0; i < Assets.MAX_SHOTS; i++)
          if (photons[i].active && ufo.isColliding(photons[i])) {
            if (sound)
              sounds.playCrash();
            explode(ufo);
            stopUfo();
            score += Assets.UFO_POINTS;
          }

          // On occassion, fire a missle at the ship if the saucer is not too
          // close to it.

          d = (int) Math.max(Math.abs(ufo.x - ship.x), Math.abs(ufo.y - ship.y));
          if (ship.active && hyperCounter <= 0 &&
              ufo.active && !missle.active &&
              d > Assets.MAX_ROCK_SPEED * Assets.FPS / 2 &&
              Math.random() < Assets.MISSLE_PROBABILITY)
            initMissle();
       }
    }
  }

  public void stopUfo() {

    ufo.active = false;
    ufoCounter = 0;
    ufoPassesLeft = 0;
    if (loaded)
      sounds.stopSaucer();
    saucerPlaying = false;
  }

  public void initMissle() {

    missle.active = true;
    missle.angle = 0.0;
    missle.deltaAngle = 0.0;
    missle.x = ufo.x;
    missle.y = ufo.y;
    missle.deltaX = 0.0;
    missle.deltaY = 0.0;
    missle.render();
    missleCounter = Assets.MISSLE_COUNT;
    if (sound)
      sounds.loopMissle();
    misslePlaying = true;
  }

  public void updateMissle() {

    int i;

    // Move the guided missle and check for collision with ship or photon. Stop
    // it when its counter has expired.

    if (missle.active) {
      if (--missleCounter <= 0)
        stopMissle();
      else {
        guideMissle();
        missle.advance();
        missle.render();
        for (i = 0; i < Assets.MAX_SHOTS; i++)
          if (photons[i].active && missle.isColliding(photons[i])) {
            if (sound)
              sounds.playCrash();
            explode(missle);
            stopMissle();
            score += Assets.MISSLE_POINTS;
          }
        if (missle.active && ship.active &&
            hyperCounter <= 0 && ship.isColliding(missle)) {
          if (sound)
            sounds.playCrash();
          explode(ship);
          stopShip();
          stopUfo();
          stopMissle();
        }
      }
    }
  }

  public void guideMissle() {

    double dx, dy, angle;

    if (!ship.active || hyperCounter > 0)
      return;

    // Find the angle needed to hit the ship.

    dx = ship.x - missle.x;
    dy = ship.y - missle.y;
    if (dx == 0 && dy == 0)
      angle = 0;
    if (dx == 0) {
      if (dy < 0)
        angle = -Math.PI / 2;
      else
        angle = Math.PI / 2;
    }
    else {
      angle = Math.atan(Math.abs(dy / dx));
      if (dy > 0)
        angle = -angle;
      if (dx < 0)
        angle = Math.PI - angle;
    }

    // Adjust angle for screen coordinates.

    missle.angle = angle - Math.PI / 2;

    // Change the missle's angle so that it points toward the ship.

    missle.deltaX = 0.75 * Assets.MAX_ROCK_SPEED * -Math.sin(missle.angle);
    missle.deltaY = 0.75 * Assets.MAX_ROCK_SPEED *  Math.cos(missle.angle);
  }

  public void stopMissle() {

    missle.active = false;
    missleCounter = 0;
    if (loaded)
      sounds.stopMissle();
    misslePlaying = false;
  }

  public void initAsteroids() {

    // Create random shapes, positions and movements for each asteroid.

    for (int i = 0; i < Assets.MAX_ROCKS; i++) {

      // Create a jagged shape for the asteroid and give it a random rotation.
      asteroids[i].createAsteroid();
      asteroids[i].render();
      asteroidIsSmall[i] = false;
    }

    asteroidsCounter = Assets.STORM_PAUSE;
    asteroidsLeft = Assets.MAX_ROCKS;
    if (asteroidsSpeed < Assets.MAX_ROCK_SPEED)
      asteroidsSpeed += 0.5;
  }

  public void initSmallAsteroids(int n) {

    int count;
    int i;
    double tempX, tempY;

    // Create one or two smaller asteroids from a larger one using inactive
    // asteroids. The new asteroids will be placed in the same position as the
    // old one but will have a new, smaller shape and new, randomly generated
    // movements.

    count = 0;
    i = 0;
    tempX = asteroids[n].x;
    tempY = asteroids[n].y;
    do {
      if (!asteroids[i].active) {
        asteroids[i].createSmallAsteroid(tempX, tempY);
        asteroidIsSmall[i] = true;
        count++;
        asteroidsLeft++;
      }
      i++;
    } while (i < Assets.MAX_ROCKS && count < 2);
  }

  public void updateAsteroids() {

    int i, j;

    // Move any active asteroids and check for collisions.

    for (i = 0; i < Assets.MAX_ROCKS; i++)
      if (asteroids[i].active) {
        asteroids[i].advance();
        asteroids[i].render();

        // If hit by photon, kill asteroid and advance score. If asteroid is
        // large, make some smaller ones to replace it.

        for (j = 0; j < Assets.MAX_SHOTS; j++)
          if (photons[j].active && asteroids[i].active && asteroids[i].isColliding(photons[j])) {
            asteroidsLeft--;
            asteroids[i].active = false;
            photons[j].active = false;
            if (sound)
              sounds.playExplosion();
            explode(asteroids[i]);
            if (!asteroidIsSmall[i]) {
              score += Assets.BIG_POINTS;
              initSmallAsteroids(i);
            }
            else
              score += Assets.SMALL_POINTS;
          }

        // If the ship is not in hyperspace, see if it is hit.

        if (ship.active && hyperCounter <= 0 &&
            asteroids[i].active && asteroids[i].isColliding(ship)) {
          if (sound)
            sounds.playCrash();
          explode(ship);
          stopShip();
          stopUfo();
          stopMissle();
        }
    }
  }

  public void initExplosions() {

    int i;

    for (i = 0; i < Assets.MAX_SCRAP; i++) {
      explosions[i].shape = new Polygon();
      explosions[i].active = false;
      explosionCounter[i] = 0;
    }
    explosionIndex = 0;
  }

  public void explode(AsteroidsSprites s) {

    int c, i, j;
    int cx, cy;

    // Create sprites for explosion animation. The each individual line segment
    // of the given sprite is used to create a new sprite that will move
    // outward  from the sprite's original position with a random rotation.

    s.render();
    c = 2;
    if (detail || s.sprite.npoints < 6)
      c = 1;
    for (i = 0; i < s.sprite.npoints; i += c) {
      explosionIndex++;
      if (explosionIndex >= Assets.MAX_SCRAP)
        explosionIndex = 0;
      explosions[explosionIndex].active = true;
      explosions[explosionIndex].shape = new Polygon();
      j = i + 1;
      if (j >= s.sprite.npoints)
        j -= s.sprite.npoints;
      cx = (int) ((s.shape.xpoints[i] + s.shape.xpoints[j]) / 2);
      cy = (int) ((s.shape.ypoints[i] + s.shape.ypoints[j]) / 2);
      explosions[explosionIndex].shape.addPoint(
        s.shape.xpoints[i] - cx,
        s.shape.ypoints[i] - cy);
      explosions[explosionIndex].shape.addPoint(
        s.shape.xpoints[j] - cx,
        s.shape.ypoints[j] - cy);
      explosions[explosionIndex].x = s.x + cx;
      explosions[explosionIndex].y = s.y + cy;
      explosions[explosionIndex].angle = s.angle;
      explosions[explosionIndex].deltaAngle = 4 * (Math.random() * 2 * Assets.MAX_ROCK_SPIN - Assets.MAX_ROCK_SPIN);
      explosions[explosionIndex].deltaX = (Math.random() * 2 * Assets.MAX_ROCK_SPEED - Assets.MAX_ROCK_SPEED + s.deltaX) / 2;
      explosions[explosionIndex].deltaY = (Math.random() * 2 * Assets.MAX_ROCK_SPEED - Assets.MAX_ROCK_SPEED + s.deltaY) / 2;
      explosionCounter[explosionIndex] = Assets.SCRAP_COUNT;
    }
  }

  public void updateExplosions() {

    int i;

    // Move any active explosion debris. Stop explosion when its counter has
    // expired.

    for (i = 0; i < Assets.MAX_SCRAP; i++)
      if (explosions[i].active) {
        explosions[i].advance();
        explosions[i].render();
        if (--explosionCounter[i] < 0)
          explosions[i].active = false;
      }
  }

  public void keyPressed(KeyEvent e) {

    char c;

    // Check if any cursor keys have been pressed and set flags.

    if (e.getKeyCode() == KeyEvent.VK_LEFT)
      left = true;
    if (e.getKeyCode() == KeyEvent.VK_RIGHT)
      right = true;
    if (e.getKeyCode() == KeyEvent.VK_UP)
      up = true;
    if (e.getKeyCode() == KeyEvent.VK_DOWN)
      down = true;

    if ((up || down) && ship.active && !thrustersPlaying) {
      if (sound && !paused)
        sounds.loopThrusters();
      thrustersPlaying = true;
    }

    // Spacebar: fire a photon and start its counter.

    if (e.getKeyChar() == ' ' && ship.active) {
      if (sound & !paused)
        sounds.playFire();
      photonTime = System.currentTimeMillis();
      photonIndex++;
      if (photonIndex >= Assets.MAX_SHOTS)
        photonIndex = 0;
      photons[photonIndex].active = true;
      photons[photonIndex].x = ship.x;
      photons[photonIndex].y = ship.y;
      photons[photonIndex].deltaX = 2 * Assets.MAX_ROCK_SPEED * -Math.sin(ship.angle);
      photons[photonIndex].deltaY = 2 * Assets.MAX_ROCK_SPEED *  Math.cos(ship.angle);
    }

    // Allow upper or lower case characters for remaining keys.

    c = Character.toLowerCase(e.getKeyChar());

    // 'H' key: warp ship into hyperspace by moving to a random location and
    // starting counter.

    if (c == 'h' && ship.active && hyperCounter <= 0) {
      ship.x = Math.random() * AsteroidsSprites.width;
      ship.y = Math.random() * AsteroidsSprites.height;
      hyperCounter = Assets.HYPER_COUNT;
      if (sound & !paused)
        sounds.playWarp();
    }

    // 'P' key: toggle pause mode and start or stop any active looping sound
    // clips.

    if (c == 'p') {
      if (paused) {
        if (sound && misslePlaying)
          sounds.loopMissle();
        if (sound && saucerPlaying)
          sounds.loopSaucer();
        if (sound && thrustersPlaying)
          sounds.loopThrusters();
      }
      else {
        if (misslePlaying)
          sounds.stopMissle();
        if (saucerPlaying)
          sounds.stopSaucer();
        if (thrustersPlaying)
          sounds.stopThrusters();
      }
      paused = !paused;
    }

    // 'M' key: toggle sound on or off and stop any looping sound clips.

    if (c == 'm' && loaded) {
      if (sound) {
        sounds.stopCrash();
        sounds.stopExplosion();
        sounds.stopFire();
        sounds.stopMissle();
        sounds.stopSaucer();
        sounds.stopThrusters();
        sounds.stopWarp();
      }
      else {
        if (misslePlaying && !paused)
          sounds.loopMissle();
        if (saucerPlaying && !paused)
          sounds.loopSaucer();
        if (thrustersPlaying && !paused)
          sounds.loopThrusters();
      }
      sound = !sound;
    }

    // 'D' key: toggle graphics detail on or off.

    if (c == 'd')
      detail = !detail;

    // 'S' key: start the game, if not already in progress.

    if (c == 's' && loaded && !playing)
      initGame();

    // 'HOME' key: jump to web site (undocumented).

    if (e.getKeyCode() == KeyEvent.VK_HOME)
      try {
        getAppletContext().showDocument(new URL(copyLink));
      }
      catch (Exception excp) {}
  }

  public void keyReleased(KeyEvent e) {

    // Check if any cursor keys where released and set flags.

    if (e.getKeyCode() == KeyEvent.VK_LEFT)
      left = false;
    if (e.getKeyCode() == KeyEvent.VK_RIGHT)
      right = false;
    if (e.getKeyCode() == KeyEvent.VK_UP)
      up = false;
    if (e.getKeyCode() == KeyEvent.VK_DOWN)
      down = false;

    if (!up && !down && thrustersPlaying) {
      sounds.stopThrusters();
      thrustersPlaying = false;
    }
  }

  public void keyTyped(KeyEvent e) {}

  public void update(Graphics g) {

    paint(g);
  }

  public void paint(Graphics g) {

    Dimension d = getSize();
    int i;
    int c;
    String s;
    int w, h;
    int x, y;

    // Create the off screen graphics context, if no good one exists.

    if (offGraphics == null || d.width != offDimension.width || d.height != offDimension.height) {
      offDimension = d;
      offImage = createImage(d.width, d.height);
      offGraphics = offImage.getGraphics();
    }

    // Fill in background and stars.

    offGraphics.setColor(Color.black);
    offGraphics.fillRect(0, 0, d.width, d.height);
    if (detail) {
      offGraphics.setColor(Color.white);
      for (i = 0; i < numStars; i++)
        offGraphics.drawLine(stars[i].x, stars[i].y, stars[i].x, stars[i].y);
    }

    // Draw photon bullets.

    offGraphics.setColor(Color.white);
    for (i = 0; i < Assets.MAX_SHOTS; i++)
      if (photons[i].active)
        offGraphics.drawPolygon(photons[i].sprite);

    // Draw the guided missle, counter is used to quickly fade color to black
    // when near expiration.

    c = Math.min(missleCounter * 24, 255);
    offGraphics.setColor(new Color(c, c, c));
    if (missle.active) {
      offGraphics.drawPolygon(missle.sprite);
      offGraphics.drawLine(missle.sprite.xpoints[missle.sprite.npoints - 1], missle.sprite.ypoints[missle.sprite.npoints - 1],
                           missle.sprite.xpoints[0], missle.sprite.ypoints[0]);
    }

    // Draw the asteroids.

    for (i = 0; i < Assets.MAX_ROCKS; i++)
      if (asteroids[i].active) {
        if (detail) {
          offGraphics.setColor(Color.black);
          offGraphics.fillPolygon(asteroids[i].sprite);
        }
        offGraphics.setColor(Color.white);
        offGraphics.drawPolygon(asteroids[i].sprite);
        offGraphics.drawLine(asteroids[i].sprite.xpoints[asteroids[i].sprite.npoints - 1], asteroids[i].sprite.ypoints[asteroids[i].sprite.npoints - 1],
                             asteroids[i].sprite.xpoints[0], asteroids[i].sprite.ypoints[0]);
      }

    // Draw the flying saucer.

    if (ufo.active) {
      if (detail) {
        offGraphics.setColor(Color.black);
        offGraphics.fillPolygon(ufo.sprite);
      }
      offGraphics.setColor(Color.white);
      offGraphics.drawPolygon(ufo.sprite);
      offGraphics.drawLine(ufo.sprite.xpoints[ufo.sprite.npoints - 1], ufo.sprite.ypoints[ufo.sprite.npoints - 1],
                           ufo.sprite.xpoints[0], ufo.sprite.ypoints[0]);
    }

    // Draw the ship, counter is used to fade color to white on hyperspace.

    c = 255 - (255 / Assets.HYPER_COUNT) * hyperCounter;
    if (ship.active) {
      if (detail && hyperCounter == 0) {
        offGraphics.setColor(Color.black);
        offGraphics.fillPolygon(ship.sprite);
      }
      offGraphics.setColor(new Color(c, c, c));
      offGraphics.drawPolygon(ship.sprite);
      offGraphics.drawLine(ship.sprite.xpoints[ship.sprite.npoints - 1], ship.sprite.ypoints[ship.sprite.npoints - 1],
                           ship.sprite.xpoints[0], ship.sprite.ypoints[0]);

      // Draw thruster exhaust if thrusters are on. Do it randomly to get a
      // flicker effect.

      if (!paused && detail && Math.random() < 0.5) {
        if (up) {
          
          offGraphics.drawPolygon(ship.getForwardThruster().sprite);
          offGraphics.drawLine(ship.getForwardThruster().sprite.xpoints[ship.getForwardThruster().sprite.npoints - 1], ship.getForwardThruster().sprite.ypoints[ship.getForwardThruster().sprite.npoints - 1],
                  ship.getForwardThruster().sprite.xpoints[0], ship.getForwardThruster().sprite.ypoints[0]);
        }
        if (down) {
          offGraphics.drawPolygon(ship.getReverseThruster().sprite);
          offGraphics.drawLine(ship.getReverseThruster().sprite.xpoints[ship.getReverseThruster().sprite.npoints - 1], ship.getReverseThruster().sprite.ypoints[ship.getReverseThruster().sprite.npoints - 1],
                  ship.getReverseThruster().sprite.xpoints[0], ship.getReverseThruster().sprite.ypoints[0]);
        }
      }
    }

    // Draw any explosion debris, counters are used to fade color to black.

    for (i = 0; i < Assets.MAX_SCRAP; i++)
      if (explosions[i].active) {
        c = (255 / Assets.SCRAP_COUNT) * explosionCounter [i];
        offGraphics.setColor(new Color(c, c, c));
        offGraphics.drawPolygon(explosions[i].sprite);
      }

    // Display status and messages.

    offGraphics.setFont(font);
    offGraphics.setColor(Color.white);

    offGraphics.drawString("Score: " + score, fontWidth, fontHeight);
    offGraphics.drawString("Ships: " + shipsLeft, fontWidth, d.height - fontHeight);
    s = "High: " + highScore;
    offGraphics.drawString(s, d.width - (fontWidth + fm.stringWidth(s)), fontHeight);
    if (!sound) {
      s = "Mute";
      offGraphics.drawString(s, d.width - (fontWidth + fm.stringWidth(s)), d.height - fontHeight);
    }

    if (!playing) {
      s = copyName;
      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 - 2 * fontHeight);
      s = copyVers;
      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 - fontHeight);
      s = copyInfo;
      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight);
      s = copyLink;
      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + 2 * fontHeight);
      if (!loaded) {
        s = "Loading sounds...";
        w = 4 * fontWidth + fm.stringWidth(s);
        h = fontHeight;
        x = (d.width - w) / 2;
        y = 3 * d.height / 4 - fm.getMaxAscent();
        offGraphics.setColor(Color.black);
          offGraphics.fillRect(x, y, w, h);
        offGraphics.setColor(Color.gray);
        if (clipTotal > 0)
          offGraphics.fillRect(x, y, (int) (w * clipsLoaded / clipTotal), h);
        offGraphics.setColor(Color.white);
        offGraphics.drawRect(x, y, w, h);
        offGraphics.drawString(s, x + 2 * fontWidth, y + fm.getMaxAscent());
      }
      else {
        s = "Game Over";
        offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
        s = "'S' to Start";
        offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4 + fontHeight);
      }
    }
    else if (paused) {
      s = "Game Paused";
      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
    }

    // Copy the off screen buffer to the screen.

    g.drawImage(offImage, 0, 0, this);
  }
}
