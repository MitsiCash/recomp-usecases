package clinicaldata;
import org.provarules.service.EPService;
import org.provarules.service.ProvaService;
import org.provarules.service.impl.ProvaServiceImpl;

import java.util.HashMap;
import java.util.Map;

public class ClinicalData implements EPService {
    private final ProvaService service;

    final String patient_rulebase = "prova/clinicaldata/patient.prova";
    final String idp_rulebase = "prova/clinicaldata/idp.prova";
    final String dataController_rulebase = "prova/clinicaldata/dataController.prova";
    final String doctor_rulebase = "prova/clinicaldata/doctor.prova";
    final String researcher_rulebase = "prova/clinicaldata/researcher.prova";

    public ClinicalData() {
        this.service = new ProvaServiceImpl();
        this.service.init();
        this.service.register("responding", this);
    }

    @Override
    public void send(String xid, String dest, String agent, String verb, Object payload, EPService callback) {
        System.out.println(dest + " received " + verb + " from " + agent + " :" + payload);
    }

    public void initialize() {
        String patient = service.instance("patient", "");
        String doctor1 = service.instance("doctor1", "");
        String doctor2 = service.instance("doctor2", "");

        String idp = service.instance("idp", "");
        String dataController = service.instance("dataController", "");
        String researcher = service.instance("researcher", "");

        service.consult(patient, patient_rulebase, "patient");
        service.consult(dataController, dataController_rulebase, "dataController");
        service.consult(idp, idp_rulebase, "idp");
        service.consult(doctor1, doctor_rulebase, "doctor1");
        service.consult(doctor2, doctor_rulebase, "doctor2");
        service.consult(researcher, researcher_rulebase, "researcher");

        service.setGlobalConstant("idp", "$Service", this);
        service.setGlobalConstant("dataController", "$Service", this);
        service.setGlobalConstant("patient", "$Service", this);
        service.setGlobalConstant("doctor1", "$Service", this);
        service.setGlobalConstant("doctor2", "$Service", this);
    }

    public synchronized void resume() {
        notifyAll();
    }

    public void runUseCase() {
        Map<String, String> payload = new HashMap<>();

        System.out.println("\nPatient authenticates with IDP, for dataController");
        payload.put("operation", "login");
        payload.put("idp", "idp");
        payload.put("dataController", "dataController");
        service.send("xid", "patient", "javaRunner", "request", payload, this);
        wait_();


        System.out.println("\nDoctor logs in to DataController");
        // payload is the same, dest is different
        service.send("xid", "doctor1", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nDoctor tries to login again to the DataController");
        service.send("xid", "doctor1", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nPatient is admitted to a clinic by the doctor");
        payload = new HashMap<>();
        payload.put("operation", "admit");
        payload.put("subject","patient");
        payload.put("dataController", "dataController");
        service.send("xid", "doctor1", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nPatient demonstrates consent for health data sharing with clinical doctors " +
                "for diagnostic/treatment purposes");
        payload = new HashMap<>();
        payload.put("dataController", "dataController");
        payload.put("operation", "demonstrateConsent");
        payload.put("purpose", "treatment");
        payload.put("parties", "clinical_doctor");
        service.send("xid", "patient", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nDoctor uploads clinical data of patient");
        payload = new HashMap<>();
        payload.put("dataController", "dataController");
        payload.put("operation", "assert");
        payload.put("data", "file1.txt");
        payload.put("subject", "patient");
        service.send("xid", "doctor1", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nDoctor uploads more clinical data of patient");
        payload = new HashMap<>();
        payload.put("dataController", "dataController");
        payload.put("operation", "assert");
        payload.put("data", "file2.txt");
        payload.put("subject", "patient");
        service.send("xid", "doctor1", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nDoctor requests all patient history");
        payload = new HashMap<>();
        payload.put("dataController", "dataController");
        payload.put("operation", "retrieveAllData");
        payload.put("subject", "patient");
        payload.put("purpose", "treatment");
        service.send("xid", "doctor1", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nPatient demonstrates consent for anonymized health data sharing with researchers " +
                "for research purposes");
        payload = new HashMap<>();
        payload.put("dataController", "dataController");
        payload.put("operation", "demonstrateConsent");
        payload.put("purpose", "research");
        payload.put("parties", "researcher");
        service.send("xid", "patient", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nResearcher logs in");
        payload = new HashMap<>();
        payload.put("operation", "login");
        payload.put("idp", "idp");
        payload.put("dataController", "dataController");
        service.send("xid", "researcher", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nResearcher requests all patient files");
        payload = new HashMap<>();
        payload.put("dataController", "dataController");
        payload.put("operation", "retrieveAllData");
        payload.put("purpose","research");
        service.send("xid", "researcher", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nResearcher realizes that no anonymized data exist so proceeds with requesting " +
                "anonymized data generation");
        payload = new HashMap<>();
        payload.put("dataController", "dataController");
        payload.put("operation", "generateAnonymizedData");
        service.send("xid", "researcher", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nResearcher requests again all patient files in his department");
        payload = new HashMap<>();
        payload.put("dataController", "dataController");
        payload.put("operation", "retrieveAllData");
        payload.put("purpose","research");
        service.send("xid", "researcher", "javaRunner", "request", payload, this);
        wait_();


        System.out.println("\nDoctor1 requests anonymized data");
        payload = new HashMap<>();
        payload.put("dataController", "dataController");
        payload.put("operation", "retrieveAllData");
        payload.put("purpose", "research");
        service.send("xid", "doctor1", "javaRunner", "request", payload, this);
        wait_();

        // @Ralph is that correct? The doctor should continue to being able to access the patient's data?
        System.out.println("\nPatient after hospitalization and following check-ups is now fully treated, and is discharged from the clinic by the doctor");
        payload = new HashMap<>();
        payload.put("operation", "discharge");
        payload.put("subject","patient");
        payload.put("dataController", "dataController");
        service.send("xid", "doctor1", "javaRunner", "request", payload, this);
        wait_();


        System.out.println("\nDoctor requests again all patient history");
        payload = new HashMap<>();
        payload.put("dataController", "dataController");
        payload.put("operation", "retrieveAllData");
        payload.put("subject", "patient");
        payload.put("purpose", "treatment");
        service.send("xid", "doctor1", "javaRunner", "request", payload, this);
        wait_();


        System.out.println("\nPatient discovers that has a severe case of Thripshaw's Disease and calls the emergency services");
        payload = new HashMap<>();
        payload.put("operation", "declareEmergency");
        payload.put("dataController", "dataController");
        payload.put("subject","patient");
        service.send("xid", "patient", "javaRunner", "request", payload, this);
        wait_();


        System.out.println("\nNearby researcher sees him in distress and requests all patient data for providing emergency assistance");
        payload = new HashMap<>();
        payload.put("dataController", "dataController");
        payload.put("operation", "retrieveAllData");
        payload.put("purpose","treatment");
        payload.put("subject","patient");
        service.send("xid", "researcher", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nEmergency doctor2 also comes in to help and logs in to DataController");
        payload = new HashMap<>();
        payload.put("operation", "login");
        payload.put("idp", "idp");
        payload.put("dataController", "dataController");
        service.send("xid", "doctor2", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nDoctor2 requests all patient history for treating the patient");
        payload = new HashMap<>();
        payload.put("dataController", "dataController");
        payload.put("operation", "retrieveAllData");
        payload.put("subject", "patient");
        payload.put("purpose", "treatment");
        service.send("xid", "doctor2", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nDoctor2 uploads new clinical data of patient");
        payload = new HashMap<>();
        payload.put("dataController", "dataController");
        payload.put("operation", "assert");
        payload.put("data", "file3.txt");
        payload.put("subject", "patient");
        service.send("xid", "doctor2", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nPatient is now treated and doctor 2 lifts the emergency status for patient");
        payload = new HashMap<>();
        payload.put("dataController", "dataController");
        payload.put("operation", "endEmergency");
        payload.put("subject","patient");
        service.send("xid", "doctor2", "javaRunner", "request", payload, this);
        wait_();


        System.out.println("\nThe researcher is curious and requests again all patient data");
        payload = new HashMap<>();
        payload.put("dataController", "dataController");
        payload.put("operation", "retrieveAllData");
        payload.put("purpose","treatment");
        payload.put("subject","patient");
        service.send("xid", "researcher", "javaRunner", "request", payload, this);
        wait_();

        System.out.println("\nEnd of use case");

        service.destroy();
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
    public static void main(String[] args) {

        ClinicalData cd = new ClinicalData();
        cd.initialize();
        cd.runUseCase();
    }



}
