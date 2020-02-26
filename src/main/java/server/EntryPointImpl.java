package server;

import interfaces.IEntryPoint;
import interfaces.IGalgeLogik;
import io.javalin.Javalin;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.ws.Service;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;

@WebService(endpointInterface = "interfaces.IEntryPoint")
public class EntryPointImpl extends UnicastRemoteObject implements IEntryPoint {

    //Setting up the clientpart of this service.
    // Connection to the gameserver on Jacobs machine.
    final String nameSpace = "http://server/";
    final String gameLocalPart = "GalgeLogikImplService";
    String GAMEURL = "http://130.225.170.204:9898/galgespil?wsdl";
    IGalgeLogik spil;

    List<String> inGamers = new ArrayList<String>();

    public EntryPointImpl() throws MalformedURLException, RemoteException {
        super();
        URL gameurl = new URL(GAMEURL);
        QName gameQname = new QName(nameSpace, gameLocalPart);
        Service gameservice = Service.create(gameurl, gameQname);
        spil = gameservice.getPort(IGalgeLogik.class);
        System.out.println("gameURL = " + GAMEURL);


        //Setting up Javalin Endpoints
        Javalin app = Javalin.create().start(9875);

        app.get("/getBrugteBogstaver/:token",ctx -> {
            ctx.result(getBrugteBogstaver(ctx.pathParam("token")))
        });

        }





    }

    public ArrayList<String> getBrugteBogstaver(String token) {
        if (checkGamerToken(token)) {
            return spil.getBrugteBogstaver();
        }
        return null;
    }

    public String getSynligtOrd(String token) {
        if (checkGamerToken(token)) {
            return spil.getSynligtOrd();
        }
        return null;
    }

    public String getOrdet(String token) {
        if (checkGamerToken(token)) {
            return spil.getOrdet();
        }
        return null;
    }

    public int getAntalForkerteBogstaver(String token) {
        if (checkGamerToken(token)) {
            return spil.getAntalForkerteBogstaver();
        }
        return 0;
    }

    public boolean erSidsteBogstavKorrekt(String token) {
        if (checkGamerToken(token)) {
            return spil.erSidsteBogstavKorrekt();
        }
        return false;
    }

    public boolean erSpilletVundet(String token) {
        if (checkGamerToken(token)) {
            return spil.erSpilletVundet();
        }
        return false;
    }

    public boolean erSpilletTabt(String token) {
        if (checkGamerToken(token)) {
            return spil.erSpilletTabt();
        }
        return false;
    }

    public void nulstil(String token) {
        if (checkGamerToken(token)) {
            spil.nulstil();
        }
    }

    public void gætBogstav(String token, String bogstav) {
        if (checkGamerToken(token)) {
            spil.gætBogstav(bogstav);
        }
    }

    public void logStatus(String token) {
        if (checkGamerToken(token)) {
            spil.logStatus();
        }
    }

    public void hentOrdFraDR(String token) {
        if (checkGamerToken(token)) {
            spil.hentOrdFraDR();
        }
    }

    public void logOff(String token) {
        inGamers.remove(token);
    }

    public String logOn(String username, String password) {
        String token = null;
        //VALIDATE THE USER AT OUR PYTHON AUTH SERVER.

        // if(validation(username, password) {
        token = String.valueOf(UUID.randomUUID());
        inGamers.add(token);
        return (token);
        //}
        //return "Du har ikke logget ind korrekt.";
    }

    public boolean checkGamerToken(String token) {
        //return  inGamers.contains(token);
        return true;
    }
}






