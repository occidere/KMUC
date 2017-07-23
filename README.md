# KMUC
KookMin University Carte (국민대 식단표)

## Dependencies
1. json-simple-1.1.1.jar
    * Project Pages: https://code.google.com/archive/p/json-simple/
    * Download Link: http://www.java2s.com/Code/Jar/j/Downloadjsonsimple11jar.htm
   
## How to Use?
                  
    import kmuc.food;
    
    public class Main {
        public static void main(String[] args) throws Exception {
            Carte carte = new Carte(); //Automatically set date as today.
    //      Carte carte = new Carte("2017-07-23"); //Set date manually.
        
            /* 
             * get Carte String sorted by Time.
             * ex) Breakfast, Lunch, Dinner.
             */
            String carteByTime = carte.getCarteByTime();
        
            System.out.println(carteByTime); //print
        }
    }
               
