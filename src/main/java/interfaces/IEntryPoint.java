package interfaces;

import com.mashape.unirest.http.exceptions.UnirestException;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.ArrayList;

@WebService
public interface IEntryPoint {

    @WebMethod
    ArrayList<String> epGetBrugteBogstaver(String token);

    @WebMethod
    String epGetSynligtOrd(String token);

    @WebMethod
    String epGetOrdet(String token);

    @WebMethod
    int epGetAntalForkerteBogstaver(String token);

    @WebMethod
    int epErSidsteBogstavKorrekt(String token);

    @WebMethod
    int epErSpilletVundet(String token);

    @WebMethod
    int epErSpilletTabt(String token);

    @WebMethod
    void epNulstil(String token);

    @WebMethod
    void epGætBogstav(String token, String bogstav);

  /*  @WebMethod
    void epLogStatus(String token);

    @WebMethod
    void hentOrdFraDR(String token);*/

    @WebMethod
    String epLogOn(String username, String password) throws UnirestException;

    @WebMethod
    void epLogOff(String token);
}
