import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Objects;


/**
 * @author Mihir
 */
public class Server {

    private CenterServerInterface mtl;
    private CenterServerInterface lvl;
    private CenterServerInterface ddo;


    /**
     * @param args
     */
	public static void main(String[] args) throws Exception {
		Server server = new Server();
		server.runServer();
	}

    private void runServer() throws Exception {

	    LoggerFactory.LogServer("Staring Server");

        LoggerFactory.LogServer("Initialing Centers");

        this.mtl = new CenterServer("MTL",6797,new int[] {6798,6799});
        this.lvl = new CenterServer("LVL",6798,new int[] {6797,6799});
        this.ddo = new CenterServer("DDO",6799,new int[] {6797,6798});



        this.addManagersToServer();
        this.addTeachersToServer();
        this.addStudentsToServer();

        LoggerFactory.LogServer("Centers initialed");

        Registry registry = LocateRegistry.createRegistry(2964);
        registry.bind("MTL", this.mtl);
        registry.bind("LVL", this.lvl);
        registry.bind("DDL", this.ddo);

        System.out.println("Server Started");

        LoggerFactory.LogServer("Server Started");

    }

    private void addStudentsToServer() throws IOException, ParseException, RequiredValueException {
        LoggerFactory.LogServer("Start adding default students");

        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(new FileReader("resources/studentData.json"));
        for (Object object : jsonArray) {
            Status status;
            JSONObject student = (JSONObject) object;
            String firstName = (String) student.get("firstName");
            String lastName = (String) student.get("lastName");
            String recordId = (String) student.get("id");
            String[] courses = (String[]) student.get("coursesRegistered").toString().replace("},{", " ,").split(" ");;
            String statusDate = (String) student.get("statusDate");
            String stat = (String) student.get("status");
            if (Objects.equals(stat, "Active")) {
                status = Status.ACTIVE;
            } else {
                status = Status.INACTIVE;
            }

            if (student.get("region").toString().substring(0, 3).equalsIgnoreCase("MTL")) {
                ((CenterServer) this.mtl).createSRecord(firstName,lastName, courses, status,statusDate,"default",recordId);
            } else if (student.get("region").toString().substring(0, 3).equalsIgnoreCase("LVL")) {
                ((CenterServer) this.lvl).createSRecord(firstName,lastName, courses, status,statusDate,"default",recordId);
            } else if (student.get("region").toString().substring(0, 3).equalsIgnoreCase("DDO")) {
                ((CenterServer) this.ddo).createSRecord(firstName,lastName, courses, status,statusDate,"default",recordId);
            }
        }

        LoggerFactory.LogServer("Default students added");
    }

    private void addTeachersToServer() throws IOException, ParseException, RequiredValueException {

        LoggerFactory.LogServer("Start adding default teachers");

        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(new FileReader("resources/teacherData.json"));
        for (Object object : jsonArray) {
            Location location;
            JSONObject teacher = (JSONObject) object;
            String firstName = (String) teacher.get("firstName");
            String lastName = (String) teacher.get("lastName");
            String recordId = (String) teacher.get("id");
            String address = (String) teacher.get("address");
            String loc = (String) teacher.get("location");
            String phone = (String) teacher.get("phone");
            String specialization = (String) teacher.get("specialization");

            if(loc.equalsIgnoreCase("MLT")){
                location = Location.MTL;
            }else if(loc.equalsIgnoreCase("LVL")){
                location = Location.LVL;
            }else{
                location = Location.DDO;
            }

            if (teacher.get("location").toString().substring(0, 3).equalsIgnoreCase("MTL")) {
                ((CenterServer) this.mtl).createTRecord(firstName,lastName,address,phone,specialization,location,"default",recordId);
            } else if (teacher.get("location").toString().substring(0, 3).equalsIgnoreCase("LVL")) {
                ((CenterServer) this.lvl).createTRecord(firstName,lastName,address,phone,specialization,location,"default",recordId);
            } else if (teacher.get("location").toString().substring(0, 3).equalsIgnoreCase("DDO")) {
                ((CenterServer) this.ddo).createTRecord(firstName,lastName,address,phone,specialization,location,"default",recordId);
            }
        }

        LoggerFactory.LogServer("Default teachers added");

    }

    private void addManagersToServer() throws IOException, ParseException {

        LoggerFactory.LogServer("Start adding default managers");

        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(new FileReader("resources/managerData.json"));
        for (Object object : jsonArray) {
            JSONObject manager = (JSONObject) object;
            if (manager.get("managerID").toString().substring(0, 3).equalsIgnoreCase("MTL")) {
                ((CenterServer) this.mtl).addManagerToList(
                        new Manager(
                                manager.get("managerID").toString(),
                                manager.get("firstName").toString(),
                                manager.get("lastName").toString()));
            } else if (manager.get("managerID").toString().substring(0, 3).equalsIgnoreCase("LVL")) {
                ((CenterServer) this.lvl).addManagerToList(
                        new Manager(
                                manager.get("managerID").toString(),
                                manager.get("firstName").toString(),
                                manager.get("lastName").toString()));
            } else if (manager.get("managerID").toString().substring(0, 3).equalsIgnoreCase("DDO")) {
                ((CenterServer) this.ddo).addManagerToList(
                        new Manager(
                                manager.get("managerID").toString(),
                                manager.get("firstName").toString(),
                                manager.get("lastName").toString()));
            }
        }

        LoggerFactory.LogServer("Default managers added");

    }

}
