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

import static io.javalin.apibuilder.ApiBuilder.path;

@WebService(endpointInterface = "interfaces.IEntryPoint")
public class EntryPointImpl extends UnicastRemoteObject implements IEntryPoint {

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
            System.out.println("EntryPointServer got request " + ctx.method() + " on url " + ctx.url() + " with parameters " + ctx.queryParamMap() + " and shape " + ctx.formParamMap());
        });

        restServer.get("/", ctx -> ctx.contentType("text/html; charset=utf-8")
                .result("<html><body>Velkommen til Online-Galgeleg<br/>\n<br/>\n" +
                        "Du skulle tage at logge ind og spille med."));
        // Enten den her organisation
        restServer.routes(() -> {
            path("bogstaver", () -> {
                restServer.get("/brugte/:token", ctx -> restBrugteBogstaver(ctx));
                restServer.get("/antalforkerte/:token", ctx -> restAntalForkerteBogstaver(ctx));
                restServer.get("/sidstekorrekt/:token", ctx -> restSidsteBogstavKorrekt(ctx));
            });
            path("ordet", () -> {
                restServer.get("/:token",ctx -> restOrdet(ctx));
                restServer.get("/synligt/:token", ctx -> restSynligtOrd(ctx));

            });
            path("spillet", () ->{
               restServer.get("/vundet/:token",ctx -> restVundet(ctx));
               restServer.get("/tabt/:token", ctx -> restTabt(ctx));
            });
            //Ved ikke om dem her skal stå uden for routes..
            restServer.get("/logoff/:token", ctx -> restLogOff(ctx));
            restServer.post("logon/:username:password", ctx-> restLogOn(ctx));
            restServer.post("/gaet/:token:letter", ctx -> restGaetBogstav(ctx));
        });


        //Eller den her organisation
        restServer.get("/:token/brugtebogstaver", ctx -> restBrugteBogstaver(ctx));
        restServer.get("/:token/synligtOrd", ctx -> getSynligtOrd(ctx.pathParam("token")));
        restServer.get("/:token/ordet", ctx -> getOrdet(ctx.pathParam("token")));
        restServer.get("/:token/AntalForkerteBogstaver", ctx -> getAntalForkerteBogstaver(ctx.pathParam("token")));
        restServer.get("/:token/sidsteBogstavKorrekt", ctx -> erSidsteBogstavKorrekt(ctx.pathParam("token")));
        restServer.get("/:token/vundet", ctx -> erSpilletVundet(ctx.pathParam("token")));
        restServer.get("/:token/tabt", ctx -> erSpilletTabt(ctx.pathParam("token")));
        restServer.get("/:token/logoff", ctx -> logOff(ctx.pathParam("token")));
        restServer.get("/logon/:username:password", ctx -> logOn(ctx.pathParam("username"), ctx.pathParam("password")));
        restServer.post(":token/gaet/:letter", ctx -> gætBogstav(ctx.pathParam("token"), ctx.pathParam("letter")));
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

    public int erSidsteBogstavKorrekt(String token) {
        if (checkGamerToken(token)) {
            boolean janej;
            janej = spil.erSidsteBogstavKorrekt();
            if (janej) {
                return 1;
            } else {
                return 0;
            }
        } else
            return -1;
    }

    public int erSpilletVundet(String token) {
        if (checkGamerToken(token)) {
            boolean janej;
            janej = spil.erSpilletVundet();
            if (janej) {
                return 1;
            } else {
                return 0;
            }
        }
        return -1;
    }

    public int erSpilletTabt(String token) {
        if (checkGamerToken(token)) {
            boolean janej;
            janej = spil.erSpilletTabt();
            if (janej) {
                return 1;
            } else {
                return 0;
            }
        }
        return -1;
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
        if (brugteBogstaver != null) {
            ctx.json(brugteBogstaver);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted

            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /brugtebogstaver/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restOrdet(Context ctx) {
        String token = getTokenFromCtx(ctx);
        String ordet;
        ordet = getOrdet(token);
        if (ordet != null) {
            ctx.json(ordet);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted

            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /ordet/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restSynligtOrd(Context ctx) {

    }

    private void restAntalForkerteBogstaver(Context ctx) {
        String token = getTokenFromCtx(ctx);
        int antal = getAntalForkerteBogstaver(token);
        if (antal > -1) {
            ctx.json(antal);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted

            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /AntalForkerteBogstaver/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restSidsteBogstavKorrekt(Context ctx) {
        String token = getTokenFromCtx(ctx);
        int korrekt = erSidsteBogstavKorrekt(token);
        //TODO clients skal håndtere en int -1=ikke valideret, 0=falsk, 1=true
        if (korrekt > -1) {
            ctx.json(korrekt);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted

            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /SidsteBogstavKorrekt/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restVundet(Context ctx) {
        String token = getTokenFromCtx(ctx);
        int vundet = erSpilletVundet(token);
        if (vundet > -1) {
            ctx.json(vundet);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted
            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /SidsteBogstavKorrekt/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restTabt(Context ctx) {
        String token = getTokenFromCtx(ctx);
        int tabt = erSpilletTabt(token);
        if (tabt > -1) {
            ctx.json(tabt);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted
            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /SidsteBogstavKorrekt/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restGaetBogstav(Context ctx) {
        String token = getTokenFromCtx(ctx);
        String letter = ctx.pathParam("letter");
        //TODO interface til gætBogstav skal returnere om det gik godt eller dårligt med token check.
        if (checkGamerToken(token)){
            gætBogstav(token, letter);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted
            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /SidsteBogstavKorrekt/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restLogOn(Context ctx) {
        String username = ctx.pathParam("username");
        String password = ctx.pathParam("password");
        //CHECK USERNAME
        String token = "asældkfjaæsdlkfj"; //TODO validate user
        if(!token.equals(null)){
            ctx.json(token);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted
            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /SidsteBogstavKorrekt/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restLogOff(Context ctx) {
        String token = getTokenFromCtx(ctx);
        logOff(token);
        ctx.status(200).result("Du er logget af spillet");
    }

    private String getTokenFromCtx(Context ctx) {
        return ctx.pathParam("token");
    }
}






