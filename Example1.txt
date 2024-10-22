package com.dallaway.jsptest;

/**
 * A simple example of using the JSP TEST classes.

 * <p>
 * Run this class with a URL as an argument.

 * <p>
 * E.g., <tt>java com.dallaway.jsptest.Example1 http://127.0.0.1/foo.html</tt>
 *
 * @author  $Author: richard $
 * @version $Revision: 1.2 $  $Date: 2000/08/18 09:22:49 $
 *
 */
public class Example1
{

  /**
   * Test that the request URL exists.
   */
  public static void main(String[] args)
  {

    if (args.length != 1)
    {
      System.err.println("Usage: com.dallaway.jsptest.Example1 http://someurl/");
      System.exit(1);
    }

    String url_to_test = args[0];

    // First create a session for maintaining cookies etc.
    // You can think of this as your "client" for requests.
    Session session = new Session();

    // Next, request the page.
    Response response = null;
    try
    {
      response = session.request(url_to_test);
    }
    catch (Exception ex)
    {
      System.err.println("Failed to get page: "+ex);
      System.exit(1);
    }

    // We have the page, check the response:
    System.out.println("Status code: "+response.getStatusCode());

    // Was there a cookie called "foo"?  Making a request may well have
    // updated the session's list of cookies.
    String value = session.getCookieValue("foo");
    System.out.println("Cookie \"foo\" has a value of: "+value);

    // If you want all the cookies, go for:
    // Cookie[] cookies = s.getCookies();

    // And a bit of content:
    System.out.println("Content starts with: "+ response.getBody().substring(0, 500));

    System.out.println("Test complete");

  }
}