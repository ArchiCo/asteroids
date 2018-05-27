package src;

import src.Thruster.ThrusterType;

public class Ship extends AsteroidsSprites{

    Thruster fwdThruster = new Thruster(ThrusterType.FORWARD);
    Thruster revThruster = new Thruster(ThrusterType.REVERSE); 
    
    public Ship() {
        super();
        createShip();
    }
    
    public void createShip() {
        shape.addPoint(0, -10);
        shape.addPoint(7, 10);
        shape.addPoint(-7, 10);
        initShip();
    }
    
    public void initShip() {

        // Reset the ship sprite at the center of the screen.

        active = true;
        angle = 0.0;
        deltaAngle = 0.0;
        x = 0.0;
        y = 0.0;
        deltaX = 0.0;
        deltaY = 0.0;
        render();

        // Initialize thruster sprites.
        updateThrusters();
     }
    
    public void updateThrusters() {
        fwdThruster.x = this.x;
        fwdThruster.y = this.y;
        fwdThruster.angle = this.angle;
        fwdThruster.render();
        revThruster.x = this.x;
        revThruster.y = this.y;
        revThruster.angle = this.angle;
        revThruster.render();

    }
    
    public Thruster getForwardThruster() {
        return fwdThruster;
    }
    
    public Thruster getReverseThruster() {
        return revThruster;
    }
   
}
