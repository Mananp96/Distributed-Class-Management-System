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

        this.mtl = new CenterServer("MTL");
        this.lvl = new CenterServer("LVL");
        this.ddo = new CenterServer("DDO");

        this.addManagersToServer();
        this.addTeachersToServer();
        this.addStudentsToServer();

        Registry registry = LocateRegistry.createRegistry(2964);
        registry.bind("MTL", this.mtl);
        registry.bind("LVL", this.lvl);
        registry.bind("DDL", this.ddo);

        System.out.println("Server Started");
    }

    private void addStudentsToServer() throws IOException, ParseException, RequiredValueException {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(new FileReader("resources/studentData.json"));
        for (Object object : jsonArray) {
            Status status;
            JSONObject student = (JSONObject) object;
            String firstName = (String) student.get("firstName");
            String lastName = (String) student.get("lastName");
            String recordId = (String) student.get("id");
            String[] courses = (String[]) student.get("coursesRegistered");
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
    }

    private void addTeachersToServer() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(new FileReader("resources/teacherData.json"));
        for (Object object : jsonArray) {
            JSONObject teacher = (JSONObject) object;
            String firstName = (String) teacher.get("firstName");
            String lastName = (String) teacher.get("lastName");
            String recordId = (String) teacher.get("id");
            String address = (String) teacher.get("address");
            String location = (String) teacher.get("location");
            String phone = (String) teacher.get("phone");
            String specialization = (String) teacher.get("specialization");


            if (teacher.get("location").toString().substring(0, 3).equalsIgnoreCase("MTL")) {
                ((CenterServer) this.mtl).createTRecord(firstName,lastName,address,phone,specialization,location,"default",recordId);
            } else if (teacher.get("location").toString().substring(0, 3).equalsIgnoreCase("LVL")) {
                ((CenterServer) this.lvl).createTRecord(firstName,lastName,address,phone,specialization,location,"default",recordId);
            } else if (teacher.get("location").toString().substring(0, 3).equalsIgnoreCase("DDO")) {
                ((CenterServer) this.ddo).createTRecord(firstName,lastName,address,phone,specialization,location,"default",recordId);
            }
        }
    }

    private void addManagersToServer() throws IOException, ParseException {
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
    }

}
