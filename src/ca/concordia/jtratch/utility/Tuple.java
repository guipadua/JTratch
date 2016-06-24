package ca.concordia.jtratch.utility;

public class Tuple<T, U> {
		  public final T Item1;
		  public final U Item2;
		
		  public Tuple(T arg1, U arg2) {
		    super();
		    this.Item1 = arg1;
		    this.Item2 = arg2;
		  }
		  @Override
		  public String toString() {
		    return String.format("(%s, %s)", Item1, Item2);
		  }
}
