import org.provarules.service.EPService;
import org.provarules.service.ProvaService;
import org.provarules.service.impl.ProvaServiceImpl;

import java.util.HashMap;
import java.util.Map;

public class UseCase implements EPService {
    //    static final String kAgent = "prova";
//    static final String kPort = null;
    final String alice_rulebase = "prova/alice.prova";
    final String idp_rulebase = "prova/idp.prova";

    private final ProvaService service;

    public UseCase() {
        this.service = new ProvaServiceImpl();
        this.service.init();
        this.service.register("responding", this);
    }

    @Override
    public void send(String xid, String dest, String agent, String verb, Object payload, EPService callback) {
        System.out.println(dest + " received " + verb + " from " + agent + " :" + payload);
    }

    private void wait_() {
        try {
            synchronized (this) {
                wait(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initialize() {
        String alice = service.instance("alice", "");
        String idp = service.instance("idp", "");
//        String bob = service.instance("bob", "");

//        String rp1 = service.instance("rp1", "");
//        String rp2 = service.instance("rp2", "");
//        String dwp = service.instance("dwp", "");
//        String gdpr = service.instance("gdpr", "");

        service.consult(alice, alice_rulebase, "alice");
        service.consult(idp, idp_rulebase, "idp");
////        service.consult(bob, bob_rulebase, "bob");
//        service.consult(dwp, dwp_rulebase, "dwp");
//        service.consult(gdpr, gdpr_rulebase, "gdpr");
//        service.consult(rp1, rp1_rulebase, "Search app");
    }

    public void runUseCase() {
        Map<String, String> payload = new HashMap<>();

        // Alice creates an account
        System.out.println("\nAlice attempts to register with the idp:");
        payload.put("operation", "create_account");
        payload.put("agent", "idp");
        service.send("xid", "alice", "javaRunner", "request", payload, this);
        wait_();

        // Alice tries to re-register, an error should appear
        System.out.println("\nAlice attempts to re-register:");
        service.send("xid", "alice", "javaRunner", "request", payload, this);
        wait_();

        // Alice provides consent for 3rd party
        System.out.println("\nAlice consents for 3rd party:");
        payload.clear();
        payload.put("operation", "consent");
        payload.put("agent", "idp");

        service.send("xid", "alice", "javaRunner", "request", payload, this);
        wait_();
    }

    public static void main(String[] args) {

        UseCase uc = new UseCase();
        uc.initialize();

        uc.runUseCase();

    }
}