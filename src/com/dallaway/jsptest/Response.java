package com.dallaway.jsptest;

import java.util.Vector;
import java.util.Hashtable;
import java.net.HttpURLConnection;

/**
 * Container for the results from processing a HTTP request.
 *
 * @author  $Author: richard $
 * @version $Revision: 1.2 $  $Date: 2000/08/18 09:22:50 $
 */
public class Response
{

  private Hashtable headers;
  private String body;
  private String status_line = null;

  private int status = -1; // HTTP status code

  /**
   * Construct a new response from a HTTP request.
   *
   * @param body    The object containg the body of the response.
   * @param headers Http headers.
   * @param status  The status line.
   */
  public Response(String body, Hashtable headers, String status_line)
  {
    this.headers = headers;
    this.body = body;
    this.status_line = status_line;

  }


  /**
   * Get the text body of the response.
   *
   * @return body The string body (e.g., HTML) for the response.
   */
  public String getBody()
  {
    return body;
  }

  /**
   * Get the HTTP status code for this request.
   *
   * @return code The HTTP status code, or -1 if the code is unavailable (e.g., cannot
   * be parsed from the server response).
   */
  public int getStatusCode()
  {

    // If we've parsed it already, return it.
    if (status != -1) return status;

    // parse the status string from "HTTP/1.1 200 OK" to get the "200" bit.
    if (status_line == null) return -1;

    int l = status_line.length();

    // Find the start of the status code.
    int s = status_line.indexOf(' ');
    if (s == -1) return -1;
    while (s < l && status_line.charAt(s) == ' ') s++;  // Skip extra spaces.
    if (s == l) return -1; // off the end of the string.

    // S is now positioned at the start of the status code.

    // Now we look for the space after the status code.
    int e = status_line.indexOf(' ', s);
    if (e == -1) return -1;

    String code = status_line.substring(s, e);

    try
    {
      status = Integer.parseInt(code);
    }
    catch (NumberFormatException nfx)
    {
      return -1;
    }

    return status;

  }



  /**
   * Get a HTTP header.  Examples include: status, Date, Server, Last-Modified,
   * Content-Length, Content-Type, Set-Cookie.
   *
   * @param name    The name of the header to look up.
   * @return values A vector of values associated with the header, or null of the header was not found.
   */
  public Vector getHeader(String name)
  {
    return (Vector)headers.get(name);
  }

  /**
   * Lookup the i-th header value for a given header.
   *
   * @param name    The name of the header.
   * @param index   The value to recover, e.g., zero for the first value.
   *
   * @return value  The index-th value of the header, or null if none was found.
   */
  public String getHeader(String name, int index)
  {
    Vector values = (Vector)headers.get(name);
    if (values == null) return null;
    if (values.size() < index) return null;
    return (String)values.elementAt(index);
  }


  /**
   * Add a new header to our list of headers.  Note that headers can have the same
   * name, so we are storing the header values in a vector which in turn
   * is stored in a hashtable keyed on the name of the value.
   *
   * @param name  The name of the header (before the colon).
   * @param value The value for the header.
   */
  private void addHeader(String name, String value)
  {
    Vector values = (Vector)headers.get("name");
    if (values == null)
    {
      values = new Vector();
      headers.put(name, values);
    }

    values.addElement(value);
  }

  /**
   * From the body, get the text from between a start and end string.
   *
   * <p>
   * This is a convieniece method to help you find particular
   * text on a page.
   *
   * <p>
   * E.g., if you page contains the text:
   * "Reason for error: something bad happened."
   * You can extract the reason with:
   *   String reason = request.extractBetween("Reason for error: ", ".");
   *
   *
   * <p>
   * @param   start_text The start text to search for,
   * @param   end_text The text that ends the item you're looking for.
   *
   * @return  between The text between the end of the start_text and the start
   * of the end text.  Got that?  Null if no match was found. If more than one
   * match is found, only the first is returned.
   *
   * @see #getBetweens
   */
   public String getBetween(String start_text, String end_text)
   {

    String[] betweens = getBetweens(start_text, end_text);

    if (betweens == null) return null;
    if (betweens.length == 0) return null;
    return betweens[0];

   }

   /**
    * From the body, extract all occurances of the text between a start
    * and end string.
    * <p>
    *
    * @see #getBetween for details and if you're only looking for one match.
    *
    * @param  start_text The start text to search for,
    * @param  end_text The text that ends the item you're looking for.
    *
    * @return betweens Array containing the text between the end of the start_text
    * and the start of the end text for all occurances of the start text. Null
    * if start_text or end_text is null or if there is no body text to search.
    * Can be a zero length array.
    *
    */
   public String[] getBetweens(String start_text, String end_text)
   {

      // Sanity checks first:
      if (start_text == null || end_text == null || "".equals(end_text) || "".equals(start_text))
        return null;

      if (body == null) return null;

      // Temporary storage for anything we find.
      Vector matches = new Vector();

      // Surely the compiler would do this for me anyway...
      int start_text_length = start_text.length();
      int end_text_length = end_text.length();


      int start_point = 0;      // Where to start searching from inside body
      int start_match_point;    // Where the start text matches
      int end_match_point;      // where the end text matches inside body

      while (true)
      {

        // Find the start_text
        start_match_point = body.indexOf(start_text, start_point);
        if (start_match_point == -1) break;

        start_match_point += start_text_length; // Skip past the start_text itself

        // Find the end_text
        end_match_point = body.indexOf(end_text, start_match_point);
        if (end_match_point == -1) break;

        // If we get here, we've got a match inside body

        matches.addElement( body.substring(start_match_point, end_match_point) );

        // Move on for the next search

        start_point = end_match_point + end_text_length;

      } // endwhile


      //  Convert results to a string array
      int n = matches.size();
      String[] toRet = new String[n];
      for(int i=0; i<n; i++)
      {
        toRet[i] = (String)matches.elementAt(i) ;
      }


      return toRet;
   }


}