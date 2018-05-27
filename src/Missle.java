package src;

public class Missle extends AsteroidsSprites{

    
    
    public Missle() {
        super();
        shape.addPoint(0, -4);
        shape.addPoint(1, -3);
        shape.addPoint(1, 3);
        shape.addPoint(2, 4);
        shape.addPoint(-2, 4);
        shape.addPoint(-1, 3);
        shape.addPoint(-1, -3);
    }   
}
