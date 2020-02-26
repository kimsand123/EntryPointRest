package interfaces;

import io.javalin.http.Context;
import java.util.ArrayList;

public interface IEntryPointRest {
    ArrayList<String> restBrugteBogstaver(Context ctx);
    String restOrdet(Context ctx);
    int restAntalForkerteBogstaver(Context ctx);
    boolean restSidsteBogstavKorrekt(Context ctx);
    boolean restVundet(Context ctx);
    boolean restTabt(Context ctx);
    void restGaetBogstav(Context ctx);
    String estLogOn(Context ctx);
    void restLogOff (Context ctx);
}
