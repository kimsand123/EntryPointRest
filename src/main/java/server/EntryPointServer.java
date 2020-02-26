package server;

import javax.xml.ws.Endpoint;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class EntryPointServer {
    public static void main (String[] args) throws MalformedURLException, RemoteException {
        EntryPointImpl entryPoint = new EntryPointImpl();
        System.out.println("Publicerer EntryPointserver vis SOAP....\n");
        Endpoint.publish("http://[::]:9876/entrypoint", entryPoint);
        System.out.println("EntryPointServer server is running..\n\n");

          /*  Javalin app = Javalin.create().start(8989);
            app.get("/getsynligtord", ctx -> ctx.result())*/
    }

}
