package interfaces;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.ArrayList;

@WebService
public interface IGalgeLogik {
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

}
