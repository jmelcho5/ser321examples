import java.io.*;
/**
 * Purpose: demonstrate simple Java Fraction class with command line,
 * jdb debugging, and Ant build file.
 *
 * Ser321 Foundations of Distributed Applications
 * see http://pooh.poly.asu.edu/Ser321
 * @author Tim Lindquist Tim.Lindquist@asu.edu
 *         Software Engineering, CIDSE, IAFSE, ASU Poly
 * @version January 2020
 */
public class Fraction {

   private int numerator, denominator;

   public Fraction(){
      numerator = denominator = 0;
   }

   public void print() {
    System.out.print(numerator + "/" + denominator );
   }

   public void setNumerator (int n){
      numerator = n;
   }

   public void setDenominator (int d) {
      denominator = d;
   }

   public int getDenominator() {
      return denominator;
   }

   public int getNumerator() {
      return numerator;
   }

   public static void main (String args[]) {
      if (args.length == 2) {
         int argN = 0;
         int argD = 0;
         try {
            // create a new instance
            // Fraction *frac = [[Fraction alloc] init];
            Fraction frac = new Fraction();

            argN = Integer.parseInt(args[0]);
            argD = Integer.parseInt(args[1]);

            // set the values
            frac.setNumerator(argN);
            frac.setDenominator(argD);

            // print it
            System.out.print("The fraction is: " + frac.getNumerator() + " / " + frac.getDenominator() + " = ");
            frac.print();
            System.out.println("");

         }catch(Exception e) {
            e.printStackTrace();
         }
      } else {
         System.out.println("Exactly 2 arguments should be provided.\n gradle runFrac -Pdenom=4 -Pnum=3 or gradle runFrac -Pnum=3 -Pdenom=4");
      }
   }
}

