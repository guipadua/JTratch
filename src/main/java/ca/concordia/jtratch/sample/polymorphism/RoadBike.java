package ca.concordia.jtratch.sample.polymorphism;

import java.nio.BufferOverflowException;

public class RoadBike extends Bicycle{
    // In millimeters (mm)
    private int tireWidth;

    public RoadBike(int startCadence,
                    int startSpeed,
                    int startGear,
                    int newTireWidth){
        super(startCadence,
              startSpeed,
              startGear);
        this.setTireWidth(newTireWidth);
    }

    public int getTireWidth(){
      return this.tireWidth;
    }

    public void setTireWidth(int newTireWidth){
        this.tireWidth = newTireWidth;
    }

    public void printDescription(){
//    	try {
//        	super.printDescription();
//            
//        } catch (BufferOverflowException ex) {
//        	
//        }
    	
    	System.out.println("The RoadBike" + " has " + getTireWidth() +
            " MM tires.");
        throw new ArithmeticException();
    }
}
