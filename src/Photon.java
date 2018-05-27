package src;

public class Photon extends AsteroidsSprites {

    
    public Photon() {
        super();
        // Create shape for the photon sprite.
        shape.addPoint(1, 1);
        shape.addPoint(1, -1);
        shape.addPoint(-1, 1);
        shape.addPoint(-1, -1);
    }

}
