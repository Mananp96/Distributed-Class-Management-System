package DistributedClassManagementSystem;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Objects;

import javax.xml.ws.Endpoint;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Server {

	private CenterServer mtlRef;
	private CenterServer lvlRef;
	private CenterServer ddoRef;

	@SuppressWarnings("unused")
	private void addStudentsToServer()
			throws IOException, ParseException, RequiredValueException, org.json.simple.parser.ParseException {

		JSONParser parser = new JSONParser();
		JSONArray jsonArray = (JSONArray) parser.parse(new FileReader("resources/studentData.json"));
		for (Object object : jsonArray) {
			String status = "";
			JSONObject student = (JSONObject) object;
			String firstName = (String) student.get("firstName");
			String lastName = (String) student.get("lastName");
			String[] courses = student.get("coursesRegistered").toString().replace("},{", " ,").split(" ");
			String statusDate = (String) student.get("statusDate");
			String stat = (String) student.get("status");
			if (Objects.equals(stat, "Active")) {
				status = "ACTIVE";
			} else {
				status = "INACTIVE";
			}

			if (student.get("region").toString().substring(0, 3).equalsIgnoreCase("MTL")) {
				this.mtlRef.createSRecord(firstName, lastName, courses, status, statusDate, "default");
			} else if (student.get("region").toString().substring(0, 3).equalsIgnoreCase("LVL")) {
				this.lvlRef.createSRecord(firstName, lastName, courses, status, statusDate, "default");
			} else if (student.get("region").toString().substring(0, 3).equalsIgnoreCase("DDO")) {
				this.ddoRef.createSRecord(firstName, lastName, courses, status, statusDate, "default");
			}
		}
	}

	@SuppressWarnings("unused")
	private void addTeachersToServer()
			throws IOException, ParseException, RequiredValueException, org.json.simple.parser.ParseException {

		JSONParser parser = new JSONParser();
		JSONArray jsonArray = (JSONArray) parser.parse(new FileReader("resources/teacherData.json"));
		for (Object object : jsonArray) {
			String location = "";
			JSONObject teacher = (JSONObject) object;
			String firstName = (String) teacher.get("firstName");
			String lastName = (String) teacher.get("lastName");
			String address = (String) teacher.get("address");
			String loc = (String) teacher.get("location");
			String phone = (String) teacher.get("phone");
			String specialization = (String) teacher.get("specialization");

			if (loc.equalsIgnoreCase("MLT")) {
				location = "MTL";
			} else if (loc.equalsIgnoreCase("LVL")) {
				location = "LVL";
			} else {
				location = "DDO";
			}

			if (teacher.get("location").toString().substring(0, 3).equalsIgnoreCase("MTL")) {
				this.mtlRef.createTRecord(firstName, lastName, address, phone, specialization, location, "default");
			} else if (teacher.get("location").toString().substring(0, 3).equalsIgnoreCase("LVL")) {
				this.lvlRef.createTRecord(firstName, lastName, address, phone, specialization, location, "default");
			} else if (teacher.get("location").toString().substring(0, 3).equalsIgnoreCase("DDO")) {
				this.ddoRef.createTRecord(firstName, lastName, address, phone, specialization, location, "default");
			}
		}
	}



	public static void main(String args[]) {
		
		HashMap<String, Integer> mtlPorts = new HashMap<String, Integer>() {
			{
				put("LVL", 6798);
				put("DDO", 6799);
			}
		};
		
		HashMap<String, Integer> lvlPorts = new HashMap<String, Integer>() {
			{
				put("MTL", 6797);
				put("DDO", 6799);
			}
		};
		
		HashMap<String, Integer> ddoPorts = new HashMap<String, Integer>() {
			{
				put("MTL", 6797);
				put("LVL", 6798);
			}
		};
		
	
		Endpoint mtlEndPoint = Endpoint.publish("http://localhost:8080/MTL", new CenterServerImpl("MTL",6797,mtlPorts));
        Endpoint lvlEndPoint = Endpoint.publish("http://localhost:8080/LVL", new CenterServerImpl("LVL",6798,lvlPorts));
        Endpoint ddoEndPoint = Endpoint.publish("http://localhost:8080/DDO", new CenterServerImpl("DDO",6799,ddoPorts));
        System.out.println("MTL service published: " + mtlEndPoint.isPublished());
        System.out.println("LVL service published: " + lvlEndPoint.isPublished());
        System.out.println("DDO service published: " + ddoEndPoint.isPublished());

        System.out.println("---Server started---");
	}
}
