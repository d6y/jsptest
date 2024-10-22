package com.dallaway.jsptest;

import java.net.*;
import java.io.*;

/**
 * Starts up a server which simply echos to STDOUT anything it receives.
 *
 * <p>
 * This is useful for debugging communications to servers where you want
 * to be sure you're sending the right things.  Designed to work
 * with HTTP, SMTP and similar text protocols.
 *
 * @author  $Author: richard $
 * @version $Revision: 1.2 $  $Date: 2000/08/18 09:22:49 $
 */
public class EchoServer
{

  private static final int BUFSIZ = 1024;
  int port = 80;

  /**
   * Construct a new echoing server on the given port.
   *
   * @param port Port to listen on.
   */
  public EchoServer(int port)
  {
    this.port = port;
  }

  /**
   * Start listening forever.
   *
   * @throws IOException Thrown if there were any network errors.
   */
  public void go() throws IOException
  {

    notice("Starting on: "+port);

    // Start listening for requests
    ServerSocket ss = new ServerSocket(port);

    while (true)
    {

      try
      {
        // Accept an incoming request...
        Socket request = ss.accept();
        // Note that we don't bother firing off a new thread, as this is just
        // intended for debugging single requests
        handle(request);
        // Start listening again...
      }
      catch (IOException iox)
      {
        error("Error accepting request", iox);
      }

    }


  }


  /**
   * Hande a single request by echoing to stdout.
   *
   * @param request The socket request to read and echo.
   * @throws IOException Thrown if there was any errors reading the request.
   */
  private void handle (Socket request) throws IOException
  {
    InputStream in = request.getInputStream();
    byte[] buffer = new byte[BUFSIZ];

      while (true)
      {
        int num_read = in.read(buffer);

        if (num_read == -1) break;  // end of data

        output(new String(buffer, 0 , num_read));

      }
  }


  /**
   * Output the request received to stdout.
   *
   * @param msg The message to output
   */
  public void output(String msg)
  {
    System.out.println(msg);
  }

  private void error(String msg, Exception x)
  {
    System.err.println(msg+": "+x.getMessage());
  }

  private void notice(String msg)
  {
    System.out.println(msg);
  }

  /**
   * Start an echo service.
   */
  public static void main(String[] args) throws IOException
  {

    if (args.length != 1)
    {
      System.err.println("Usage: ServerEcho port-num");
      System.exit(0);
    }

    EchoServer echo = new EchoServer(Integer.parseInt(args[0]));

    echo.go();
  }


}

