package src;

public class Ufo extends AsteroidsSprite{

    public Ufo() {
        // Create shape for the flying saucer.
        super();
        shape.addPoint(-15, 0);
        shape.addPoint(-10, -5);
        shape.addPoint(-5, -5);
        shape.addPoint(-5, -8);
        shape.addPoint(5, -8);
        shape.addPoint(5, -5);
        shape.addPoint(10, -5);
        shape.addPoint(15, 0);
        shape.addPoint(10, 5);
        shape.addPoint(-10, 5);
    }
    
}
