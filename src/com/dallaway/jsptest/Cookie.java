package com.dallaway.jsptest;

/**
 * Container for a HTTP cookie.
 *
 * <p>
 * Note that this is an incomplete implementation for use in JSP Test.  In
 * particular we do not yet implement domain, path or expiry.
 *
 * <p>
 * We will need to add expiry if your application replies on removing
 * cookies by setting an expiry date that has passed.
 * <p>
 *
 * <p>
 * Don't use this in a real client application because we don't even check domain
 * when sending cookies.
 *
 * @author  $Author: richard $
 * @version $Revision: 1.2 $  $Date: 2000/08/18 09:22:49 $
 *
 */
public class Cookie
{

  private String name;
  private String value;

  /**
   * Construct a new cookie from a HTTP response "set-cookie:" header.
   * <p>
   * Note that expiry and path attributes are ignored.
   *
   * @param set_line  The HTTP response, excluding the "set-cookie" part.
   */
  public Cookie(String set_line)
  {
    // The set line from the server will look something like this:
    // Set-Cookie: mycookie=some_value; path=/
    // But we expect just this bit: mycookie=some_value; path=/

    int e = set_line.indexOf(";");
    if (e == -1)
    {
      // No semi-colon, so assume the value runs to the end of the string.
      e = set_line.length();
    }

    // We now have a start and end index for the assignment part of the string: s to e
    String assignment = set_line.substring(0,e);

    int q = assignment.indexOf("=");
    if (q == -1) throw new IllegalArgumentException("Expected an = in cookie assignment");

    name = assignment.substring(0, q);
    value = assignment.substring(q+1);
  }

  /**
   * Construct a new cookie with a given name and value.
   *
   * @param name  The name of the cookie.
   * @param value The value for the cookie.
   */
  public Cookie(String name, String value)
  {
    this.name = name;
    this.value = value;
  }


  /**
   * Get the name of this cookie.
   *
   * @return name   The name of the cookie.
   */
  public String getName()
  {
    return name;
  }


  /**
   * Get the value associated with this cookie.
   *
   * @return value  The value associated with this cookie.
   */
  public String getValue()
  {
    return value;
  }


  /**
   * Convert the (name,value) pair into a string suitable for
   * sending to the server.
   *
   * @return cookie_string  The string to send to the server, after the Cookie: line.
   */
  public String toCookieString()
  {
      StringBuffer b = new StringBuffer();
      b.append(name);
      b.append("=");
      b.append(value);
      return b.toString();
  }

}