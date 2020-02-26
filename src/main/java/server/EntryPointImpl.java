package server;

import interfaces.IEntryPoint;
import interfaces.IGalgeLogik;

import io.javalin.Javalin;
import io.javalin.http.Context;

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
public class EntryPointImpl extends UnicastRemoteObject implements IEntryPoint{

    //Setting up the clientpart of this service.
    // Connection to the gameserver on Jacobs machine.
    final String nameSpace = "http://server/";
    final String gameLocalPart = "GalgeLogikImplService";
    private String GAMEURL = "http://130.225.170.204:9898/galgespil?wsdl";
    private IGalgeLogik spil;

    private List<String> inGamers = new ArrayList<String>();

    public EntryPointImpl() throws MalformedURLException, RemoteException {
        super();
        URL gameurl = new URL(GAMEURL);
        QName gameQname = new QName(nameSpace, gameLocalPart);
        Service gameservice = Service.create(gameurl, gameQname);
        spil = gameservice.getPort(IGalgeLogik.class);
        System.out.println("gameURL = " + GAMEURL);


        //Setting up Javalin Endpoints
        Javalin restServer = Javalin.create().start(9875);

        //Til debugging or logging, should probably write to a file instead.
        restServer.before(ctx -> {
            System.out.println("EntryPointServer got request "+ctx.method()+" on url " +ctx.url()+ " with parameters "+ctx.queryParamMap()+ " and shape " +ctx.formParamMap());
        });



        restServer.get("/brugteBogstaver/:token",ctx -> restBrugteBogstaver(ctx));
        restServer.get("/synligtOrd/:token", ctx -> getSynligtOrd(ctx.pathParam("token")));
        restServer.get("/ordet/:token", ctx -> getOrdet(ctx.pathParam("token")));
        restServer.get("/getAntalForkerteBogstaver/:token", ctx -> getAntalForkerteBogstaver(ctx.pathParam("token")));
        restServer.get("/sidsteBogstavKorrekt/:token", ctx -> erSidsteBogstavKorrekt(ctx.pathParam("token")));
        restServer.get("/vundet/:token",ctx -> erSpilletVundet(ctx.pathParam("token")));
        restServer.get("/tabt/:token",ctx -> erSpilletTabt(ctx.pathParam("token")));
        restServer.get("/logoff/:token", ctx -> logOff(ctx.pathParam("token")));
        restServer.get("/logon/:username:password", ctx -> logOn(ctx.pathParam("username"), ctx.pathParam("password")));
        restServer.post("/gaet/:token:letter",ctx -> gætBogstav(ctx.pathParam("token"), ctx.pathParam("letter")));
        }

    //SOAP methods
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
        return -1;
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

    private boolean checkGamerToken(String token) {
        //return  inGamers.contains(token);
        return true;
    }

    // REST methods
    private void restBrugteBogstaver(Context ctx) {
        String token = getTokenFromCtx(ctx);
        List<String> brugteBogstaver;
        brugteBogstaver = getBrugteBogstaver(token);
        if (brugteBogstaver!=null) {
            ctx.json(brugteBogstaver);
        }else{
            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /brugtebogstaver/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restOrdet(Context ctx) {
        String token = getTokenFromCtx(ctx);
        String ordet;
        ordet = getOrdet(token);
        if (ordet!=null){
            ctx.json(ordet);
        } else{
            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /ordet/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restAntalForkerteBogstaver(Context ctx) {
        String token = getTokenFromCtx(ctx);
        int antal = getAntalForkerteBogstaver(token);
        if (antal > -1) {
            ctx.json(antal);
        } else {
            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /AntalForkerteBogstaver/\\033[3mvalidToken\\033[0m");
        }
    }

    private boolean restSidsteBogstavKorrekt(Context ctx) {
        return false;
    }

    private boolean restVundet(Context ctx) {
        return false;
    }

    private boolean restTabt(Context ctx) {
        return false;
    }

    private void restGaetBogstav(Context ctx) {

    }

    private String estLogOn(Context ctx) {
        return null;
    }

    private void restLogOff(Context ctx) {

    }

    private String getTokenFromCtx(Context ctx){
        return ctx.pathParam("token");
    }
}






