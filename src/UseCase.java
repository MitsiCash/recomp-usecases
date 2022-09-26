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
    final String dwp_rulebase = "prova/dwp.prova";
    final String bob_rulebase = "prova/bob.prova";
    final String searchApp_rulebase = "prova/searchApp.prova";

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
                wait(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initialize() {
        String alice = service.instance("alice", "");
        String idp = service.instance("idp", "");
        String bob = service.instance("bob", "");

        String searchApp = service.instance("searchApp", "");
//        String rp2 = service.instance("rp2", "");
        String dwp = service.instance("dwp", "");

        service.consult(alice, alice_rulebase, "alice");
        service.consult(idp, idp_rulebase, "idp");
        service.consult(bob, bob_rulebase, "bob");
        service.consult(dwp, dwp_rulebase, "dwp");
        service.consult(searchApp, searchApp_rulebase, "searchApp");
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
        service.send("xid1", "alice", "javaRunner", "request", payload, this);
        wait_();

        // Alice provides consent for 3rd party
        System.out.println("\nAlice consents for 3rd party:");
        payload=new HashMap<>();
        payload.put("operation", "consent");
        payload.put("agent", "idp");
        service.send("xid2", "alice", "javaRunner", "request", payload, this);
        wait_();

        //Alice registers with the DWP
        System.out.println("\nAlice attempts to register with the dwp:");
        payload=new HashMap<>();
        payload.put("operation","create_account");
        payload.put("agent", "dwp");
        payload.put("webID", "alice.dwpexample.com");
        service.send("xid3", "alice", "javaRunner", "request", payload, this);
        wait_();

        //Alice tries to re-register with the DWP, an error should appear
        System.out.println("\nAlice attempts to re-register with the dwp:");
        service.send("xid4", "alice", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nAlice attempts to upload an image to the dwp:");
        payload=new HashMap<>();
        payload.put("agent", "dwp");
        payload.put("operation","store");
        payload.put("webID", "alice.example.com");
        payload.put("object", "image.jpeg");
        service.send("xid5", "alice", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nAlice attempts to retrieve the image from the dwp:");
        payload=new HashMap<>();
        payload.put("agent", "dwp");
        payload.put("operation","retrieve");
        payload.put("webID", "alice.example.com");
        payload.put("object", "image.jpeg");
        service.send("xid6", "alice", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nBob attempts to retrieve the same image from the dwp:");
        payload=new HashMap<>();
        payload.put("agent", "dwp");
        payload.put("operation","retrieve");
        payload.put("webID", "bob.example.com");
        payload.put("object", "image.jpeg");
        service.send("xid7", "bob", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nAlice attempts to give permission to bob to read the image from the dwp");
        payload=new HashMap<>();
        payload.put("agent", "dwp");
        payload.put("operation","read_permission");
        payload.put("webID", "alice.example.com");
        payload.put("subjectWebID", "bob.example.com");
        payload.put("object", "image.jpeg");
        service.send("xid8", "alice", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nBob attempts again to retrieve the same image from the dwp:");
        payload=new HashMap<>();
        payload.put("agent", "dwp");
        payload.put("operation","retrieve");
        payload.put("webID", "bob.example.com");
        payload.put("object", "image.jpeg");
        service.send("xid9", "bob", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nAlice revokes permission to bob to read the image from the dwp,");
        System.out.println("but types the wrong image name:");
        payload=new HashMap<>();
        payload.put("agent", "dwp");
        payload.put("operation","revoke_read_permission");
        payload.put("webID", "alice.example.com");
        payload.put("subjectWebID", "bob.example.com");
        payload.put("object", "image2.jpeg");
        service.send("xid8", "alice", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nAlice revokes permission to bob to read the image from the dwp,");
        System.out.println("but types the correct image name:");
        payload=new HashMap<>();
        payload.put("agent", "dwp");
        payload.put("operation","revoke_read_permission");
        payload.put("webID", "alice.example.com");
        payload.put("subjectWebID", "bob.example.com");
        payload.put("object", "image.jpeg");
        service.send("xid9", "alice", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nBob attempts again to retrieve the same image from the dwp:");
        payload=new HashMap<>();
        payload.put("agent", "dwp");
        payload.put("operation","retrieve");
        payload.put("webID", "bob.example.com");
        payload.put("object", "image.jpeg");
        service.send("xid9", "bob", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nAlice uploads personal data to dwp");
        payload = new HashMap<>();
        payload.put("agent", "dwp");
        payload.put("operation","store");
        payload.put("webID", "alice.example.com");
        payload.put("object", "personaData.txt");

        service.send("xid10", "alice", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nAlice searches something with the Search app");
        payload = new HashMap<>();
        payload.put("agent", "searchApp");
        payload.put("operation","search");
        payload.put("dwp","dwp");
        payload.put("idp","idp");
        payload.put("webID", "alice.example.com");
        payload.put("query", "travel suggestions");

        service.send("xid11", "alice", "javaRunner", "request", payload, this);
        wait_();
    }

    public static void main(String[] args) {

        UseCase uc = new UseCase();
        uc.initialize();

        uc.runUseCase();

    }
}