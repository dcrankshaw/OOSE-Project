package edu.jhu.cs.oose.biblio.model.tests;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CompareDates {
  public static void main(String[] args) throws ParseException {

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    // Get Date 1
    Date d1 = df.parse("2000-02-01");

    // Get Date 2
    Date d2 = df.parse("2001-03-02");

    String relation;
    if (d1.equals(d2))
      relation = "the same date as";
    else if (d1.before(d2))
      relation = "before";
    else
      relation = "after";
    System.out.println(d1 + " is " + relation + ' ' + d2);
  }
}
