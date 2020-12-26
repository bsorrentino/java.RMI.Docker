		 Running the RMI ServletHandler

The RMI ServletHandler can be used as a servlet replacement for the
java-rmi.cgi script that comes with the Java Development Kit.  The RMI
ServletHandler enables RMI to tunnel remote method calls over HTTP
more efficiently than the existing java-rmi.cgi script.

RMI tunnels remote method calls over HTTP in two different ways.
The less efficient of these two mechanisms causes the java-rmi.cgi
script to be invoked once every remote call, which usually requires
the creation of a new process.  As a result, RMI method calls incur a
large overhead when they make use of this cgi-script.

The RMI specification describes in detail how RMI makes use of the
HTTP protocol:

http://java.sun.com/products/jdk/1.2/docs/guide/rmi/spec/rmi-arch.doc11.html

This example demonstrates how RMI developers can take advantage of the
Java Servlet API in order to establish more efficient HTTP/CGI
connections, which don't have the process-creation overhead that CGI
requires.

These installation instructions are written under the assumption that
you will be using JavaWebServer1.1 and compiling the example source
code with JDK1.1.6:

To run the ServletHandler perform the following steps:

 1. Build and install the servlet class files:

    * Ensure that the Servlet extension API class files reside in your
      classpath.  These files are distributed with the
      JavaWebServer1.1 and are located in the following file:

      <webServerRoot>/lib/jws.jar

      The Java Servlet Development Kit also contains a copy of the
      relevant class files and can be obtained from the following
      location:
      
      http://java.sun.com:80/products/java-server/servlets/index.html#sdk
      
    * Compile the program with the command: 
          javac -d . *.java 

    * Move the resulting directory, "rmiservlethandler", to the
      servlets directory of your webserver 
      (for example /usr/local/JavaWebServer1.1/servlets).

    * Ensure that the webserver can read the rmiservlet handler
      directory and the class files it contains, so that the webserver
      has permission to load the classes from this URL).

    * Ensure that the classes for the sample RMI server are
      accessible from a URL that a client VM can access.

 2. Configure the servlet using the webserver's administration
    utility:

    * Gain administrator access to a webserver that supports servlets.

      This servlet prints error messages to stderr.  It is quite
      helpful for debugging purposes to run your webserver in a
      console that you can view so that you can read the text of error
      messages when errors occur.

    * Create/register the servlet handler using your webserver's
      Administration utility through the "Add a new servlet" form
      located at:

      <double-click>web-service -> servlets -> servlets:add

    * Ensure that the web service under which you are installing the
      servlet handler is running on port 80.  The SampleRMIClient will
      fail to operate if the servlet is not registered in a web
      service that is not running on port 80.

    * Use the Administrator utility to create optional configuration
      parameters that will instruct the servlet to download,
      instantiate, and install a remote server in the servlet VM. The
      property editor is located at:

      <double-click>web-service -> servlets ->
      servlets:<yourServletName> -> properties -> add

       Sample Property names and example values: 

       rmiservlethandler.initialServerBindName -> /SampleRMI
       rmiservlethandler.initialServerClass    -> SampleRMIServer
       rmiservlethandler.initialServerCodebase -> http://<machine>/<codebase>/
       
       Note: The codebase must end in '/'

    * Associate the servlet alias, "/cgi-bin/java-rmi.cgi", with the
      servlet created in step 3 using:

      <double-click>web-service -> setup -> Servlet Aliases -> Add

    * Load the servlet through the Administrator utility using:

      <double-click>web-service -> servlets ->
      servlets:<yourServletName> -> load

      If the ServletHandler loads properly, it should print out a
      success message:

javawebserver: rmiservlethandler.initialServerClass  valid: SampleRMIServer
javawebserver: rmiservlethandler.initialServerBindName  valid: SampleRMI
javawebserver: rmiservlethandler.initialServerCodebase valid: http://<machine>/<codebase>/
javawebserver: RMI Servlet Handler loaded sucessfully.
javawebserver: Attempting to load remote class...
javawebserver: Server class loaded successfully...
javawebserver: Remote object created successfully.

 3. Run the sample RMI client and server:

    * If you specified the optional server init parameters in step 3,
      you may now run an RMI client to test the servlet, otherwise you
      need will start an RMI server manually.  The sample RMI server
      included in this example will be started by the ServletHandler
      itself, if the configuration properties are specified.  You can
      also start the server from the command line (using the following
      command), if you do not set these properties:

      java SampleRMIServer

    * The ServletHandler and java-rmi.cgi will most often be invoked
      by an RMI client that resides inside a firewall.

      To successfully test the servlet you need to ensure that HTTP is
      the only protocol that your client can use to contact servers
      running on the webserver host.  The SampleRMIClient has been
      written so that it will only open "HTTP to CGI" remote call
      connections.  If the RMI client is able to make a direct
      connection to the RMI server, then the ServletHandler doPost
      method will not be invoked and the example will only appear to
      run correctly (the client and server will not use HTTP).  More
      information on this topic is given in the file,
      SampleRMIClient.java.

    * To run the client, use the command:

      java SampleRMIClient <servletHostname>

      When a firewall separates the client and server, you will need
      to set the client VM's proxy host properties as follows:

java -Dhttp.proxyHost=<proxyHost> -Dhttp.proxyPort=<proxyPort>
    SampleRMIClient cycler.east.sun.com

      If the servlet loaded correctly (with the optional remote
      server), you should see the following output from the servlet when
      the client invokes a remote method:

javawebserver: command: forward
javawebserver: param: 1099
javawebserver: command: forward
javawebserver: param: 59690
javawebserver: command: forward
javawebserver: param: 59690      <- This port number will vary.

      The client should print:

String passed to remote server: This is a test of the RMI servlet handler
Servlet installed correctly.

      If you have configured your webserver incorrectly, the
      SampleRMIClient is likely to fail with a
      java.rmi.UnmarshalException.  Please note that there are many
      reasons why the sample client may throw this exception: for
      example, you may have neglected to associate the servlet handler
      with the alias, "/cgi-bin/java-rmi.cgi"

Miscellaneous Tips:

   * In JDK1.1.x, it is not possible to unexport remote objects.
     Consequently, when the RMI ServletHandler servlet is "unloaded"
     using the servlet Administration utility, remote objects exported
     by the servlet will remain exported.  In 1.1.x, to ensure that no
     remote objects are running in the servlet VM, you must restart
     the webserver that is running the RMI servlet handler extension.

     Two new API calls in JDK1.2 allow remote objects to be
     unexported.  If you use this servlet in JDK1.2, it is recommended
     that you write a destroy method for the ServletHandler so that
     when the servlet is unloaded it will clean up or "unexport" any
     remote objects that it exported.  The file ServletHandler.java
     provides more details related to this point.
     
   * It is often the case that this RMI servlet will not have
     permission to access the java.rmi.server.codebase property.
     Furthermore, even if this servlet can set this property, if
     another servlet has already set the codebase property and this
     servlet tried to set it as well, the codebase for one of the two
     servlets would likely be incorrect.

     If you do export remote objects in a servlet, we recommend that
     you do not attempt to set the "java.rmi.server.codebase property"
     or rely upon the webserver being able to load classes from its
     classpath.  Instead, if you force this servlet to load classes
     for remote objects from a URL that is accessible from client VMs,
     RMI will forward the value of this URL to all clients that need
     to download RMI classes. Hence, your servlet will have no need to
     set the java.rmi.server.codebase.  The example servlet
     demonstrates how to load and install a sample remote server class
     (see ServletHandler.init() ). Arbitrary remote classes may be
     installed into a servlet VM in this fashion.
     
   * Details regarding how RMI tunnels call information over http can
     be found in the RMI spec at the following location:

     http://java.sun.com/products/jdk/1.2/docs/guide/rmi/spec/rmi-arch.doc11.html
     http://java.sun.com/products/jdk/1.2/docs/guide/rmi/spec/rmi-protocol.doc4.html

     If you have problems getting this example to work, the documents at the 
     following locations may help you:

     http://java.sun.com/products/jdk/1.2/docs/guide/rmi/faq.html
     http://java.sun.com/products/jdk/1.2/docs/guide/rmi/getstart.doc.html
     http://archives.java.sun.com/archives/rmi-users.html
