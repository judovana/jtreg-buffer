import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class VarDeps implements Callable<Map<String, String>> {

    boolean checkVar(String var) {
        String varval = System.getenv(var);
        if (varval == null) { return false; }
        return varval.equalsIgnoreCase("true");
    }

    @Override
    public Map<String, String> call() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("var.rh.jdk", checkVar("RH_JDK") ? "true": "false");
        return map;
    }

    public static void main(String[] args) {
        for (Map.Entry<String,String> entry: new VarDeps().call().entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
