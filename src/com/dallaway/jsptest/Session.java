package com.dallaway.jsptest;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import java.net.*;
import java.io.*;

/**
 * HTTP session implementation for managing client state and firing off HTTP requests.
 *
 * <p>
 * This session is your starting point for all JSP testing.  Create
 * a sessions, and from this you can make requests and query cookies.
 * <p>
 * Set HTTP headers in the session using <code>setHeader()</code>.  By default the header
 * "User-Agent" is already set to IE5.0 for NT.  You can change this
 * with a call to: <code>session.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; WinNT4.0; en-US; m14) Gecko/20000419")</code>.
 * <p>
 * To support HTTP/1.1, we send the Host: header (e.g., Host: 127.0.0.1:8080).
 * <p>
 * Other headers you might want to set and suggested values:
 * <br>
 * <table border="1">
 * <tr><th>Header</th><th>Example values</th></tr>
 * <tr><td>Accept</td><td>*&0#047;* or image/gif, image/jpeg</td></tr>
 * <tr><td>Accept-Language</td><td>en-us</td></tr>
 * <tr><td>Authorization</td><td>BASIC d2vjkkjdfKJHGS873H=</td></tr>
 * <tr><td>Referer</td><td>http://foo.baz/index.html</td></tr>
 * </table>
 * <br>
 * ...and any of headers you like, but note that Set-Cookie is handled
 * by the addCookie() method.
 *
 * @author  $Author: richard $
 * @version $Revision: 1.2 $  $Date: 2000/08/18 09:22:50 $
 */
public class Session
{

  private Hashtable cookies; // active cookies
  private Hashtable headers; // HTTP headers

  // Useful constants
  public static final String UA_IE_50_NT = "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)";
  public static final String UA_MOZ_50_NT = "Mozilla/5.0 (Windows; U; WinNT4.0; en-US; m14) Gecko/20000419";

  /** The HTTP version we send with requests */
  private String http_version = "HTTP/1.0";

  /** The string to send to the server to end a HTTP line. */
  private String LINE_END = "\r\n";

  /**
   * Create a new empty session, with default user agent of IE5 (NT).
   */
  public Session()
  {
    reset();
  }

  /**
   * Clear the sessions.  This removes all client-side state information.
   */
  public void reset()
  {
    cookies = new Hashtable();
    headers = new Hashtable();
    setHeader("User-Agent", UA_IE_50_NT);
    setHTTPVersion("HTTP/1.0");
  }

  /**
   * Set the user agent string to send to the server.
   *
   * @param header_name The header to send (do not include trailing colon).
   * @param value The value to associate with the header.
   */
  public void setHeader(String header_name, String header_value)
  {
    headers.put(header_name, header_value);
  }

  /**
   * The list of active cookies.
   *
   * @return cookie[] An array of Cookie objects.
   */
  public synchronized Cookie[] getCookies()
  {

    int n = cookies.size();
    Cookie[] toRet = new Cookie[n];

    Enumeration e = cookies.elements();
    int i = 0;
    while (e.hasMoreElements())
    {
      toRet[i] = (Cookie)e.nextElement();
      i++;
    }

    return toRet;
  }

  /**
   * Add a cookie to this session.
   * <p>
   *
   * @param cookie The cookie to add to the session.
   */
  public synchronized void addCookie(Cookie cookie)
  {
    cookies.put(cookie.getName(), cookie);

  }

  /**
   * Look up a single cookie value by name.
   *
   * @param name The name of the cookie to look up.
   * @return value The value associated with the cookie, or null if the cookie was not found.
   */
  public synchronized String getCookieValue(String cookie_name)
  {

    Cookie c = (Cookie)cookies.get(cookie_name);

    if (c != null)
      return c.getValue();
    else
      return null;
  }


  /**
   * Request a HTTP URL.  The result of this request may update the session state
   * (e.g., if cookie are sent).
   *
   * @param request The HTTP request to make, such as "http://127.0.0.1/hello.jsp" or
   * "http://127.0.0.1/hello.jsp?foo=baz".
   * @return response The response from the server.
   *
   * @throws MalformedURLException Thrown if the supplied url is badly formed.
   * @throws java.io.IOException Thrown if there was any erors reading across the network.
   */
  public Response request(String url) throws MalformedURLException, java.io.IOException
  {

      // Look at http://193.2.190.78/~anton/solaris.inorg.chem.msu.ru/cs-books/java/Hacking-Java/ch6.htm#SupportingtheCookieProtocol

      URL u = new URL(url);
      int port = u.getPort();
      if (port < 0) port = 80;

      Socket socket = new Socket(u.getHost(), port);
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());
      out.writeBytes("GET ");
      out.writeBytes(u.getFile());
      out.writeBytes(" ");
      out.writeBytes(http_version);
      out.writeBytes(LINE_END);

      // Add headers
      Enumeration h = headers.keys();
      while (h.hasMoreElements())
      {
        String header = (String)h.nextElement();
        String value = (String)headers.get(header);
        out.writeBytes(header);
        out.writeBytes(": ");
        out.writeBytes(value);
        out.writeBytes(LINE_END);
      }

      // Add the cookies.
      Enumeration e = cookies.elements();
      StringBuffer cookie_string = new StringBuffer();
      while (e.hasMoreElements())
      {
        Cookie c = (Cookie)e.nextElement();
        if (cookie_string.length() > 0) cookie_string.append("; ");
        cookie_string.append(c.toCookieString());
      }

      if (cookie_string.length() > 0)
      {
        out.writeBytes("Cookie: "+cookie_string.toString());
        out.writeBytes(LINE_END);
      }

      // Send the Host: header.
      out.writeBytes("Host: ");
      out.writeBytes(u.getHost());
      out.writeBytes(":");
      out.writeBytes(Integer.toString(u.getPort()));
      out.writeBytes(LINE_END);

      // End of request
      out.writeBytes(LINE_END);
      out.flush();


      // Start of the response.


      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      // First line is status:
      String status_line = in.readLine();

      // Read the headers...
      Hashtable headers = readHeaders(in);

      // Eat the body
      String body=readBody(in);

      Response res = new Response(body, headers, status_line);

      in.close();
      out.close();
      socket.close();

      return res;
  }


  /**
   * Read the content from a URL stream and convert to a string.
   *
   * @param in The Input stream to read from.
   * @return body The string version of the content.
   * @throws IOException Thrown if there was any problem reading the response.
   */
  private String readBody (BufferedReader in) throws IOException
  {

    StringBuffer toRet = new StringBuffer();

    String line;
    while (true)
    {
      line = in.readLine();
      if (line == null) break;
      toRet.append(line);
    }

    return toRet.toString();
  }


  /**
   * Read the HTTP headers from an input stream.
   *
   * @param in The input stream to read from.
   * @param headers Hash of all the headers read from the response.
   * @throws IOException Thrown if there was an error while reading the response.
   */
  private Hashtable readHeaders(BufferedReader in) throws IOException
  {

      String line;
      Hashtable headers = new Hashtable();

      while (true)
      {
        line = in.readLine();
        if (line == null || line.length() == 0) break;

        if (line.toLowerCase().startsWith("set-cookie: "))
        {
            addCookie(new Cookie(line.substring(12)));
            continue;
        }

        // Regular header
        int colon = line.indexOf(":");

        if (colon == -1) continue;

        String name = line.substring(0, colon);
        String value = line.substring(colon+1);

        headers.put(name, value.trim()) ;

      }

    return headers;

    }


    /**
     * Set the HTTP version to send.
     *
     * @param version The HTTP version string, e.g., "HTTP/1.0" (the default).
     */
    public void setHTTPVersion(String version)
    {
      this.http_version = version;
    }


    /**
     * The HTTP version that is being sent with requests.
     *
     * @return  version The HTTP version sent with requests, such as "HTTP/1.0".
     */
     public String getHTTPVersion()
     {
      return this.http_version;
     }
}