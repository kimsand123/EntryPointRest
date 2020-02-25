package com.company;
import io.javalin.Javalin;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.ws.Service;

import static io.javalin.apibuilder.ApiBuilder.*;

public class EntryPointRest {
    //Two webservice frameworks
    //Javalin for REST endpoints (FLUTTER)
    //Soap for the console client.
    //All the method calls should be present.
    /*
     @WebMethod
    ArrayList<String> getBrugteBogstaver();
    @WebMethod
    String getSynligtOrd();
    @WebMethod
    String getOrdet();
    @WebMethod
    int getAntalForkerteBogstaver();
    @WebMethod
    boolean erSidsteBogstavKorrekt();
    @WebMethod
    boolean erSpilletVundet();
    @WebMethod
    boolean erSpilletTabt();
    @WebMethod
    void nulstil();
    @WebMethod
    void gætBogstav(String bogstav);
    @WebMethod
    void logStatus();
    @WebMethod
    void hentOrdFraDR();

     */

    public static void main(String[] args) throws MalformedURLException {
        final String PROD_ENV = "130.225.170.204";
        final String gameLocalPart = "GalgeLogikImplService";
        final int GAMEPORT = 9898;

        String GAMEURL = "http://" + PROD_ENV + ":"+GAMEPORT+"/galgespil?wsdl";
        URL gameurl = new URL(GAMEURL);
        QName gameQname = new QName("http://server/", "GalgeLogikImplService)");
        Service gameservice = Service.create(gameurl, gameQname);
        IGalgeLogik spil = gameservice.getPort(IGalgeLogik.class);




        Javalin app = Javalin.create().start(8989);
        app.get("/getsynligtord", ctx -> ctx.result())

    }
}





