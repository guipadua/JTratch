package ca.concordia.jtratch.sample.polymorphism;

public class TestBikes {
	  public static void main(String[] args){
	    Bicycle bike01, bike02, bike03;

	    bike01 = new Bicycle(20, 10, 1);
	    bike02 = new MountainBike(20, 10, 5, "Dual");
	    bike03 = new RoadBike(40, 20, 8, 23);

	    try{
	    	bike01.printDescription();
	 	    //bike02.printDescription();
	 	    //bike03.printDescription();
	    } catch(Exception ex){
	    	//empty block
	    }
	    
	    try{
	    	bike02.printDescription();
	 	    
	    } catch(Exception ex){
	    	//empty block
	    }
	    
	    try{
	    	bike03.printDescription();
	    } catch(Exception ex){
	    	//empty block
	    }
	    
	   
	  }
	}
