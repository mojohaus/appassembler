package org.codehaus.mojo.appassembler.example.jsw;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

public class MyServer
{
    public static void main( String[] args )
    {
        Server server = new Server(8080);

        String webapp = "/portal-front-web";
        WebAppContext appContext = new WebAppContext("src/main/webapp", webapp);
        server.addHandler(appContext);

        try {
            server.start();
        } catch (Exception e) {
            System.err.println("Error while starting Jetty:");
            e.printStackTrace();
        }
    }
}
