package interfaces;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.ArrayList;

@WebService
public interface IEntryPoint {

    @WebMethod
    ArrayList<String> getBrugteBogstaver(String token);

    @WebMethod
    String getSynligtOrd(String token);

    @WebMethod
    String getOrdet(String token);

    @WebMethod
    int getAntalForkerteBogstaver(String token);

    @WebMethod
    int erSidsteBogstavKorrekt(String token);

    @WebMethod
    int erSpilletVundet(String token);

    @WebMethod
    int erSpilletTabt(String token);

    @WebMethod
    void nulstil(String token);

    @WebMethod
    void gætBogstav(String token, String bogstav);

    @WebMethod
    void logStatus(String token);

    @WebMethod
    void hentOrdFraDR(String token);

    @WebMethod
    String logOn(String username, String password);

    @WebMethod
    void logOff (String token);
}
