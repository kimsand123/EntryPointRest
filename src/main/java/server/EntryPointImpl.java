package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import interfaces.IEntryPoint;
import interfaces.IGalgeLogik;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.json.JSONObject;

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
    static final String nameSpace = "http://server/";
    static final String gameLocalPart = "GalgeLogikImplService";
    private static String gameIP = "130.225.170.204:9875";
    private static String GAMEURL = "http://130.225.170.204:9898/galgespil?wsdl";
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


        restServer.get("bogstaver/", ctx ->  ctx.contentType("text/html; charset=utf-8")
                .result("<html><body>bogstaver/<br/>\n<br/>\n" +
                        "<a href=\"http://"+gameIP+"/bogstaver/gaet/\">  HUSK \"token\":\"value\":\"letter\":\"value\" i body som formdata. Gætter på et bogstav<br/>\n</br>\n"+
                        "<a href=\"http://"+gameIP+"/bogstaver/brugte/\">   HUSK \"token\":\"value\" i body som formdata. Returnerer de brugte bogstaver<br/>\n</br>\n"+
                        "<a href=\"http://"+gameIP+"/bogstaver/antalforkerte/\">  HUSK \"token\":\"value\" i body som formdata. Returnerer antal forkerte gæt om<br/>\n</br>\n"+
                        "<a href=\"http://"+gameIP+"/bogstaver/ersidstekorrekt/\">   HUSK \"token\":\"value\" i body som formdata. Returnerer om det sidste bogstav var korrekt <br/>\n</br>\n"
                ));
        restServer.post("bogstaver/gaet/", ctx -> restGaetBogstav(ctx));
        restServer.get("bogstaver/brugte/", ctx -> restBrugteBogstaver(ctx));
        restServer.get("bogstaver/antalforkerte/", ctx -> restAntalForkerteBogstaver(ctx));
        restServer.get("bogstaver/ersidstekorrekt/", ctx -> restSidsteBogstavKorrekt(ctx));

        restServer.get("ordet/", ctx ->  ctx.contentType("text/html; charset=utf-8")
                        .result("<html><body>ordet/<br/>\n<br/>\n" +
                                "<a href=\"http://"+gameIP+"/ordet/ord/\">  HUSK \"token\":\"value\" i body som formdata. Returnerer ordet der bliver spillet om<br/>\n</br>\n"+
                                "<a href=\"http://"+gameIP+"/ordet/synligt/\">   HUSK \"token\":\"value\" i body som formdata. Returnerer det der er synligt af ordet indtil videre<br/>\n</br>\n"
                                ));
        restServer.get("ordet/ord/", ctx -> restOrdet(ctx));
        restServer.get("ordet/synligt/", ctx -> restSynligtOrd(ctx));

        restServer.get("spillet/", ctx ->  ctx.contentType("text/html; charset=utf-8")
                .result("<html><body>spillet/<br/>\n<br/>\n" +
                        "<a href=\"http://"+gameIP+"/spillet/vundet/\">  HUSK \"token\":\"value\" i body som formdata. Returnerer om spillet er vundet<br/>\n</br>\n"+
                        "<a href=\"http://"+gameIP+"/spillet/tabt/\">   HUSK \"token\":\"value\" i body som formdata. Returnerer om spillet er tabt<br/>\n</br>\n"
                ));
        restServer.get("spillet/vundet/", ctx -> restVundet(ctx));
        restServer.get("spillet/tabt/", ctx -> restTabt(ctx));


      /*  restServer.get("auth/", ctx -> {
        });*/
        restServer.get("auth/", ctx ->  ctx.contentType("text/html; charset=utf-8")
                .result("<html><body>auth/<br/>\n<br/>\n" +
                        "<a href=\"http://"+gameIP+"/auth/logon/\">  HUSK \"username\":\"value\",\"password\":\"value\" i body som formdata. Logger på spillet<br/>\n</br>\n"+
                        "<a href=\"http://"+gameIP+"/auth/logoff/\">   HUSK \"token\":\"value\" i body som formdata. Logger af spillet<br/>\n</br>\n"
                ));
        restServer.post("auth/logoff/", ctx -> {
            System.out.println("token: " + ctx.formParam("token"));
            restLogOff(ctx);
        });
        restServer.post("auth/logon/", ctx -> {
            System.out.println("username" + ctx.formParam("username") + "  password " + ctx.formParam("password"));
            restLogOn(ctx);
        });


    }

/*
        //Eller den her organisation
        restServer.get("/:token/brugtebogstaver", ctx ->{
                    System.out.println("beforebrugtebogstaver" + ctx.pathParam("token"));
                    restBrugteBogstaver(ctx);
        });
        restServer.get("/:token/synligtord", ctx -> {
            System.out.println("before synligtord "+ ctx.pathParam("token"));
            restSynligtOrd(ctx);

        });
        restServer.get("/:token/ordet", ctx -> {
            System.out.println("Before ordet " + ctx.pathParam("token"));
            restOrdet(ctx);
        });
        restServer.get("/:token/antalforkertebogstaver", ctx -> {
            System.out.println("before antalforkertebogstaver "+ ctx.pathParam("token"));
            restAntalForkerteBogstaver(ctx);
        });
        restServer.get("/:token/sidstebogstavkorrekt", ctx -> {
            System.out.println("before sidsteBogstavKorrekt "+ ctx.pathParam("token"));
            restSidsteBogstavKorrekt(ctx);
        } );
        restServer.get("/:token/vundet", ctx -> {
            System.out.println("before vundet "+ ctx.pathParam("token"));
            restVundet(ctx);
        });
        restServer.get("/:token/tabt", ctx -> {
            System.out.println("before tabt "+ ctx.pathParam("token"));

            restTabt(ctx);
        });
        restServer.get("/:token/logoff", ctx -> {
            System.out.println("before logoff "+ ctx.pathParam("token"));

            restLogOff(ctx);
        });
        restServer.get("/logon/:username/:password", ctx -> {
            System.out.println("username " + ctx.pathParam("username") + " password: " + ctx.pathParam("password"));
            restLogOn(ctx);
        });
        restServer.post("/:token/gaet/:letter", ctx -> {
            System.out.println("token "+ ctx.pathParam("token") + "  letter"+ ctx.pathParam("letter"));
            restGaetBogstav(ctx);
        });
        */


    //SOAP methods
    public ArrayList<String> epGetBrugteBogstaver(String token) {
        if (checkGamerToken(token)) {
            return spil.getBrugteBogstaver();
        }
        return null;
    }

    public String epGetSynligtOrd(String token) {
        if (checkGamerToken(token)) {
            return spil.getSynligtOrd();
        }
        return null;
    }

    public String epGetOrdet(String token) {
        if (checkGamerToken(token)) {
            return spil.getOrdet();
        }
        return null;
    }

    public int epGetAntalForkerteBogstaver(String token) {
        if (checkGamerToken(token)) {
            return spil.getAntalForkerteBogstaver();
        }
        return -1;
    }

    public int epErSidsteBogstavKorrekt(String token) {
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

    public int epErSpilletVundet(String token) {
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

    public int epErSpilletTabt(String token) {
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

    public void epNulstil(String token) {
        if (checkGamerToken(token)) {
            spil.nulstil();
        }
    }

    public void epGætBogstav(String token, String bogstav) {
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

    public void epLogOff(String token) {
        inGamers.remove(token);
    }

    public String epLogOn(String username, String password) throws UnirestException {

        System.out.println("restLogon brugernavn: " + username + " password " + password);
        String url = "http://130.225.170.204:8970/auth/";

        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        System.out.println(body);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .body(body)
                .asJson();
        JSONObject json = response.getBody().getObject();
        System.out.println(json.toString());
        String token = json.getString("token");

        if (token != null) {
            inGamers.add(token);
        }
        return (token);
    }

    private boolean checkGamerToken(String token) {
        return inGamers.contains(token);
    }

    // REST methods
    private void restBrugteBogstaver(Context ctx) {
        String token = ctx.queryParam("token");
        System.out.println("restBrugteBogstaver token: " + token);
        List<String> brugteBogstaver;
        brugteBogstaver = epGetBrugteBogstaver(token);
        if (brugteBogstaver != null) {
            ctx.result("Her er de brugte bogstaver ").json(brugteBogstaver);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted

            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /brugtebogstaver/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restOrdet(Context ctx) {
        String token = ctx.queryParam("token");
        System.out.println("restOrdet token: " + token);
        String ordet;
        ordet = epGetOrdet(token);
        if (ordet != null) {
            ctx.json(ordet);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted

            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /ordet/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restSynligtOrd(Context ctx) {
        String token = ctx.queryParam("token");
        System.out.println("synligt ord token: " + token);
        String synligtOrd;
        synligtOrd=epGetSynligtOrd(token);
        if(synligtOrd != null){
            ctx.json(synligtOrd);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted

            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /ordet/\\033[3mvalidToken\\033[0m");
        }

    }

    private void restAntalForkerteBogstaver(Context ctx) {
        String token = ctx.queryParam("token");
        System.out.println("restAntalForkerteBogstaver token: " + token);
        int antal = epGetAntalForkerteBogstaver(token);
        if (antal > -1) {
            ctx.json(antal);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted

            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /AntalForkerteBogstaver/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restSidsteBogstavKorrekt(Context ctx) {
        String token = ctx.queryParam("token");
        System.out.println("restSidsteBogstavKorrekt token: " + token);
        int korrekt = epErSidsteBogstavKorrekt(token);
        //TODO clients skal håndtere en int -1=ikke valideret, 0=falsk, 1=true
        if (korrekt > -1) {
            ctx.json(korrekt);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted

            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /SidsteBogstavKorrekt/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restVundet(Context ctx) {
        String token = ctx.queryParam("token");
        System.out.println("restVundet token: " + token);
        int vundet = epErSpilletVundet(token);
        if (vundet > -1) {
            ctx.json(vundet);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted
            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /SidsteBogstavKorrekt/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restTabt(Context ctx) {
        String token = ctx.queryParam("token");
        System.out.println("resttabt token: " + token);
        int tabt = epErSpilletTabt(token);
        if (tabt > -1) {
            ctx.json(tabt);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted
            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /SidsteBogstavKorrekt/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restGaetBogstav(Context ctx) {
        String token = ctx.formParam("token");
        String letter = ctx.formParam("letter");
        System.out.println("restLogoff token: " + token + " letter: " + letter);
        //TODO interface til gætBogstav skal returnere om det gik godt eller dårligt med token check.
        if (checkGamerToken(token)) {
            epGætBogstav(token, letter);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted
            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /SidsteBogstavKorrekt/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restLogOn(Context ctx) throws UnirestException {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
        String token = epLogOn(username, password);
        if (!token.equals(null)) {
            ctx.json(token);
        } else {
            //TODO depending on which REST organisation the feedback should be adapted
            ctx.status(401).result("Ikke Autoriseret. Du skal bruge en valideret token samt \n syntaksen /SidsteBogstavKorrekt/\\033[3mvalidToken\\033[0m");
        }
    }

    private void restLogOff(Context ctx) {
        String token = ctx.formParam("token");
        inGamers.remove(token);
        System.out.println("restLogoff token: " + token);
        epLogOff(token);
        ctx.status(200).result("Du er logget af spillet");
    }
}






