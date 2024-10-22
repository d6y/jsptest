package com.dallaway.jsptest;

import junit.framework.*;

/**
 * Test suite for unit testing the JSP Test classes.
 *
 * <p>
 * This is a test of the testing classes.  You probably don't
 * want to even look at this unless you plan to change the code.
 *
 * <p>
 * If you do want to run this, you will need to be running a local
 * webserver on port 8787 with the /test/ mapped to the wwwtest/test folder of this project
 * (or copy the files into your server root...whatever).  You can change this port and
 * server set up by modifying the TEST_SERVER string.
 *
 * <p>
 * I set port 8787 with Resin (1.2b1) by changing the following lines
 * in resin.conf and starting httpd:
 *
 * <pre>
 * &lt;app-dir&gt;d:\projects\jsptest\wwwtest&lt;/app-dir&gt;
 * &lt;httpd-port&gt;8787&lt;/httpd-port&gt;
 * </pre>
 *
 * These settings may or may not be appropriate to your Resin set up.
 * You'll know if you have the correct set up by requesting:
 * http://127.0.0.1:8787/test/simple.jsp and getting something back.
 * <p>
 *
 * <p>
 * You will also need the junit.jar file in you classpath.  You can get
 * JUnit from http://xprogramming.com/software.htm
 *
 * <p>
 * The three methods that use the JSP TEST classes are:
 * testSimpleRequest, testSimpleJSPRequest and testJSPWithParam.
 *
 * <p>
 * Call this class via one of the JUnit test runners or call the main()
 * to use the default text UI.
 *
 * @author  $Author: richard $
 * @version $Revision: 1.2 $  $Date: 2000/08/18 09:22:50 $
 */
public class JSPTestTest extends TestCase
{

  // The server and port to hit to request the test files used in
  // this test class.  Map /test at this address to the
  // wwwtest folder of this project.
  private static final String TEST_SERVER = "http://127.0.0.1:8787";


  /**
   * Constructor.  Looks like you have to have this here.
   */
  public JSPTestTest(String name)
  {
    super(name);
  }

  /**
   * Defines the collection of all tests to run.  In this case
   * we go for all the tests in this class (all the methods that
   * begin with the four characters "test").
   */
  public static Test suite()
  {
    return new TestSuite(JSPTestTest.class);
  }


  /**
   * Run all the tests using text output.
   */
  public static void main(String args[])
  {
    junit.textui.TestRunner.run(suite());
  }


  // ---------- START OF TESTS ----------


  /**
   * Test the parsing of the Set-Cookie string.
   */
  public void testCookieConstructionBySetLine()
  {
    Cookie c = new Cookie("testarg=test value; path=/");
    assertEquals("Name mismatch", "testarg", c.getName());
    assertEquals("Value mismatch", "test value", c.getValue());
  }


  /**
   * Test creation of a cookie via a (name,value) pair.
   */
  public void testCookieConstruction()
  {
    Cookie c = new Cookie("testarg", "test value");
    assertEquals("Name mismatch", "testarg", c.getName());
    assertEquals("Value mismatch", "test value", c.getValue());
  }


  /**
   * Test adding a cookie to the session.
   */
  public void testCookieAdd()
  {

    Session s = new Session();

    // Before we add a cookie we should have zero cookies.
    Cookie[] zero = s.getCookies();
    assertEquals("Expected no cookies", 0, zero.length);

    // Add a cookie to the session
    Cookie c = new Cookie("test1", "hello world");
    s.addCookie(c);

    // Check that the new cookie is in the session.
    Cookie[] cookies = s.getCookies();
    assertEquals("Cookie len", 1, cookies.length);
    assertEquals("Cookie name", "test1", cookies[0].getName());
    assertEquals("Cookie value", "hello world", cookies[0].getValue());

    // Check the other way of looking up a value
    assertEquals("Mismatch", "hello world", s.getCookieValue("test1"));
  }

  /**
   * Test (1) adding two cookies to a session, and (2) adding a cookie with
   * the same name as an existing cookie.
   */
  public void testAdd2Cookies()
  {

    Session s = new Session();

    // Add 1...
    Cookie c = new Cookie("test1", "hello world");
    s.addCookie(c);

    // Add another to make sure we have two cookies
    Cookie c2 = new Cookie("test2", "bye");
    s.addCookie(c2);
    assertEquals("Length", 2, s.getCookies().length);

    // If you add a cookie with the same name as another, you overwrite the previous cookie
    Cookie c3 = new Cookie("test2", "something else");
    s.addCookie(c3);
    Cookie[] cookies = s.getCookies();
    assertEquals("Length", 2, cookies.length );
    assertEquals("Name", "test2", cookies[0].getName());
    assertEquals("Value", "something else", cookies[0].getValue());
  }


  /**
   * A simple http request.
   */
  public void testSimpleRequest()
  {

    Session s = new Session();
    Response r = null;

    try
    {
      r = s.request(TEST_SERVER+"/test/simple.html");
    }
    catch (Exception x)
    {
      fail("Exception requesting HTTP: "+x.getMessage());
    }

    int status = r.getStatusCode();
    assertEquals("Status", 200, status );
    assert("Wrong filename", r.getBody().indexOf("filename=simple.html") != -1);

  }


  /**
   * A simple http request for a JSP that will send back a cookie.
   */
  public void testSimpleJSPRequest()
  {

    Session s = new Session();
    Response r = null;

    try
    {
      r = s.request(TEST_SERVER+"/test/simple.jsp");
    }
    catch (Exception x)
    {
      fail("Exception requesting HTTP: "+x.getMessage());
    }

    int status = r.getStatusCode();
    assertEquals("Status", 200, status);
    assert(r.getBody().indexOf("filename=simple.jsp") != -1);
    // The cookie sent back from the JSP:
    assertEquals("Cookie value", "test_value", s.getCookieValue("test_name"));

  }

  /**
   * Test by requesting a JSP but passing a parameter as a GET.
   */
  public void testJSPWithParam()
  {

    Session s = new Session();
    Response r = null;

    try
    {
      r = s.request(TEST_SERVER+"/test/echo.jsp?name=You");
    }
    catch (Exception x)
    {
      assert("Exception requesting HTTP: "+x.getMessage(), false);
    }

    int status = r.getStatusCode();
    assertEquals("Status", 200, status );
    assert(r.getBody().indexOf("filename=echo.jsp") != -1);

    // The page should contain the string "Hello You" somewhere
    assert(r.getBody().indexOf("Hello You") != -1);

    // Also, the comment at the start of the page should contain "NAME=You"
    assert(r.getBody().indexOf("NAME=You") != -1);

  }


  /**
   * Test the getBetween methods of request.
   */
  public void testGetBetween()
  {

    // Test string.  We'll extract all the animal= values
    String test_body =
      "This is a test string.  animal=[dog] animal=[cat] some stuff" +
      "more stuff animal= and animal=[] and animal=[fish] and " +
      "animal=[ and animal=[dog] again. bestanimal=[dog].";

    // This is what we expect
    String[] correct = {"dog", "cat", "", "fish", " and animal=[dog", "dog"};

    // Run the method
    Response r = new Response(test_body, null, null);
    String[] a1 = r.getBetweens("animal=[", "]");

    assertEquals("Array length", correct.length, a1.length);

    // Check individual cell elements
    for(int i=0; i<a1.length; i++)
    {
      assertEquals("Cell failed "+i,  correct[i], a1[i]);
    }

    String first = r.getBetween("animal=[", "]");
    assertEquals("First match failed", correct[0], first);


    // Now try something we don't expect to match

    Response r2 = new Response("yada yada yada yada", null, null);
    String[] a2 = r2.getBetweens("animal=[", "]");

    assertEquals("Expected zero elements for a non-match", 0, a2.length);

    assert("First element of non match was not null", r2.getBetween("animal=[", "]") == null);

  }



  /**
   * test of sending a cookie from Session to a JSP.
   */
  public void testCookieSend()
  {

    Session s = new Session();
    Response r = null;

    s.addCookie(new Cookie("test", "hello"));

    try
    {
      r = s.request(TEST_SERVER+"/test/cookietest.jsp");
    }
    catch (Exception x)
    {
      fail("Exception requesting HTTP: "+x.getMessage());
    }

    int status = r.getStatusCode();
    assertEquals("Status", 200, status );
    assert("Expected filename cookietest.jps in HTML", r.getBody().indexOf("filename=cookietest.jsp") != -1);

    // The cookie sent back from the JSP should echo the value of the cookie
    // "test" that we sent
    String echo_value = s.getCookieValue("echo");


    assertEquals("Cookie not echoed", "hello", echo_value);



  }





}