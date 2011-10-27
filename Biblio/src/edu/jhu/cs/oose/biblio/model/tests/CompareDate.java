package edu.jhu.cs.oose.biblio.model.tests;
import java.util.*;
import java.text.*;

public class CompareDate{
  public static void main(String args[]){
  Calendar cal = Calendar.getInstance();
  Calendar currentcal = Calendar.getInstance();
  cal.set(2000, Calendar.JUNE, 29);
  currentcal.set(currentcal.get(Calendar.YEAR),
currentcal.get(Calendar.MONTH), currentcal.get(Calendar.DAY_OF_MONTH));
  if(cal.before(currentcal))
  System.out.print("Current date(" + new SimpleDateFormat("dd/MM/yyyy").
format(currentcal.getTime()) + ") is greater than the given date " + new
SimpleDateFormat("dd/MM/yyyy").format(cal.getTime()));
  else if(cal.after(currentcal))
  System.out.print("Current date(" + new SimpleDateFormat("dd/MM/yyyy").
format(currentcal.getTime()) + ") is less than the given date " + new 
SimpleDateFormat("dd/MM/yyyy").format(cal.getTime()));
  else
  System.out.print("Both date are equal.");
  }
}
