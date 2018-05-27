package src;

import java.awt.Polygon;

public class Asteroid extends AsteroidsSprites{

    public Asteroid() {
        super();
    }
    
    
    public void createAsteroid() {
        // Create a jagged shape for the asteroid and give it a random rotation.
        int i, j;
        int s;
        double theta, r;
        int xm, ym;
        
        shape = new Polygon();
        s = Assets.MIN_ROCK_SIDES + (int) (Math.random() * (Assets.MAX_ROCK_SIDES - Assets.MIN_ROCK_SIDES));
        for (j = 0; j < s; j ++) {
          theta = 2 * Math.PI / s * j;
          r = Assets.MIN_ROCK_SIZE + (int) (Math.random() * (Assets.MAX_ROCK_SIZE - Assets.MIN_ROCK_SIZE));
          xm = (int) -Math.round(r * Math.sin(theta));
          ym = (int)  Math.round(r * Math.cos(theta));
          shape.addPoint(xm, ym);
        }
        active = true;
        angle = 0.0;
        deltaAngle = Math.random() * 2 * Assets.MAX_ROCK_SPIN - Assets.MAX_ROCK_SPIN;

        // Place the asteroid at one edge of the screen.

        if (Math.random() < 0.5) {
          x = -AsteroidsSprites.width / 2;
          if (Math.random() < 0.5)
            x = AsteroidsSprites.width / 2;
            y = Math.random() * AsteroidsSprites.height;
        }
        else {
          x = Math.random() * AsteroidsSprites.width;
          y = -AsteroidsSprites.height / 2;
          if (Math.random() < 0.5)
            y = AsteroidsSprites.height / 2;
        }

        // Set a random motion for the asteroid.

        deltaX = Math.random() * Assets.MIN_ROCK_SPEED;
        if (Math.random() < 0.5)
          deltaX = -deltaX;
        deltaY = Math.random() * Assets.MIN_ROCK_SPEED;
        if (Math.random() < 0.5)
          deltaY = -deltaY;
    }
    
    public void createSmallAsteroid(double tempX, double tempY) {

        int i;
        int s;
        double theta, r;
        int xm, ym;
        
        shape = new Polygon();
        s = Assets.MIN_ROCK_SIDES + (int) (Math.random() * (Assets.MAX_ROCK_SIDES - Assets.MIN_ROCK_SIDES));
        for (i = 0; i < s; i ++) {
          theta = 2 * Math.PI / s * i;
          r = (Assets.MIN_ROCK_SIZE + (int) (Math.random() * (Assets.MAX_ROCK_SIZE - Assets.MIN_ROCK_SIZE))) / 2;
          xm = (int) -Math.round(r * Math.sin(theta));
          ym = (int)  Math.round(r * Math.cos(theta));
          shape.addPoint(xm, ym);
        }
        active = true;
        angle = 0.0;
        deltaAngle = Math.random() * 2 * Assets.MAX_ROCK_SPIN - Assets.MAX_ROCK_SPIN;
        x = tempX;
        y = tempY;
        deltaX = Math.random() * 2 * Assets.MAX_ROCK_SPEED - Assets.MAX_ROCK_SPEED;
        deltaY = Math.random() * 2 * Assets.MAX_ROCK_SPEED - Assets.MAX_ROCK_SPEED;
    }
}
