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
    boolean erSidsteBogstavKorrekt(String token);

    @WebMethod
    boolean erSpilletVundet(String token);

    @WebMethod
    boolean erSpilletTabt(String token);

    @WebMethod
    void nulstil(String token);

    @WebMethod
    void gætBogstav(String token, String bogstav);

    @WebMethod
    void logStatus(String token);

    @WebMethod
    void hentOrdFraDR(String token);

    @WebMethod
    String validateUser(String username, String password);
}
