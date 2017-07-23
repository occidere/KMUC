# KMUC
KookMin University Carte (국민대 식단표)
<p>
## Dependencies
 1. json-simple-1.1.1.jar
   1.1 Project Pages: https://code.google.com/archive/p/json-simple/
   2.2 Download Link: http://www.java2s.com/Code/Jar/j/Downloadjsonsimple11jar.htm
<p>
## How to Use?
`
import kmuc.food;

public class Main{
    public static void main(String[] args) throws Exception {
	Carte carte = new Carte(); //Automatically set date as today.
//	Carte carte = new Carte("2017-07-23"); //Set date manually.
	
	/* 
	 * get Carte String sorted by Time.
	 * ex) Breakfast, Lunch, Dinner.
	 */
	String carteByTime = carte.getCarteByTime();
	
	System.out.println(carteByTime); //print
    }
}
`
