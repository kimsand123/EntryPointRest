package server;

import javax.xml.ws.Endpoint;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class EntryPointServer {
    public static void main (String[] args) throws MalformedURLException, RemoteException {
        EntryPointImpl entryPointImpl = new EntryPointImpl();
        System.out.println("Publicerer GalgeServer via SOAP til consol client");
        Endpoint.publish("http://[::]:9876/galgespil", entryPointImpl);
        System.out.println("EntryPointServer server is running..\n\n");

          /*  Javalin app = Javalin.create().start(8989);
            app.get("/getsynligtord", ctx -> ctx.result())*/
    }

}
