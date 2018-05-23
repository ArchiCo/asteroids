package src;

public class Photon extends AsteroidsSprite {

    public Photon() {
        // Create shape for the photon sprite.
        super();
        shape.addPoint(1, 1);
        shape.addPoint(1, -1);
        shape.addPoint(-1, 1);
        shape.addPoint(-1, -1);
    }
}
