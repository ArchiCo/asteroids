package src;

public class Thruster extends AsteroidsSprites{
    
    enum ThrusterType {
        FORWARD, REVERSE;
    }
    
    public Thruster(ThrusterType type) {
        super();
        
        // Create shapes for the ship thrusters.
        switch (type) {
            case FORWARD:
                shape.addPoint(0, 12);
                shape.addPoint(-3, 16);
                shape.addPoint(0, 26);
                shape.addPoint(3, 16);
                break;
    
            case REVERSE:
                shape.addPoint(-2, 12);
                shape.addPoint(-4, 14);
                shape.addPoint(-2, 20);
                shape.addPoint(0, 14);
                shape.addPoint(2, 12);
                shape.addPoint(4, 14);
                shape.addPoint(2, 20);
                shape.addPoint(0, 14);
                break;
        }
    }
}
