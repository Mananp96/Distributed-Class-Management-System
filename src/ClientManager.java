import java.io.IOException;
import java.util.Scanner;

public class ClientManager {
	
	public static void menu() {
		System.out.println("********************************");
		System.out.println("1. Add Teacher Record");
		System.out.println("2. Add Student Record");
		System.out.println("3. Get record counts");
		System.out.println("4. Edit Record");
		System.out.println("5. Exit");
		System.out.println("********************************");
	}
	
	public static String userInput(String var2) {
		System.out.print(var2);
		Scanner s = new Scanner(System.in);
		return s.nextLine();
	}
		
	public static void main(String[] args) throws IOException{
		String managerId = userInput("Enter Manager ID:");
		CenterServer centerServer = new CenterServer("MTL");
		
		menu();
		Location location;
		Status status;
		String choice = userInput("Enter Choice:");
		switch(choice){
			case "1":
				System.out.println("----------1>Add Teacher Record----------");
				String firstName = userInput("Enter First Name:");
				String lastName = userInput("Enter Last Name:");
				String address = userInput("Enter Address:");
				String phone = userInput("Enter Phone:");
				String spec = userInput("Enter Specialization:");
				String[] specialization=spec.split(",");
				String loc = userInput("Enter Server location:");
				
				if(loc == "MTL") {
					location = Location.MTL;}
				else if(loc == "LVL") {
					location = Location.LVL;}
				else{
					location = Location.DDO;}
			
			 	centerServer.createTRecord(firstName, lastName, address, phone, specialization, location, managerId);
			 	break;
			case "2":
				System.out.println("----------2>Add Student Record----------");
				String FirstName = userInput("Enter First Name:");
				String LastName = userInput("Enter Last Name:");
				String coursearray = userInput("Enter Registered Course:");
				String[] courseRegistered = coursearray.split(",");
				String stat = userInput("Enter Status");
				if(stat == "Active") {
					status = Status.ACTIVE;
				}
				else {
					status = Status.INACTIVE;
				}
				String statusDate = userInput("Enter Status Date:");
				centerServer.createSRecord(FirstName, LastName, courseRegistered, status, statusDate, managerId);
				break;
			case "3":
				System.out.println("----------3>>>Get Record Counts----------");
				centerServer.getRecordCount(managerId);
				break;
			case "4":
				System.out.println("----------4>Edit Record----------");
				String recordId = userInput("Enter Record ID:");
				String fieldName = userInput("Enter Field Name:");
				String newvalue = userInput("Enter New Value:");
				String[] newValue = newvalue.split(",");
				centerServer.editRecords(recordId, fieldName, newValue, managerId);
				break;
			case "5":
				System.out.println("Good Bye!");
				System.exit(0);
				break;
			default:
				System.out.println("Enter Correct Choice");
				main(args);
				
			
		}
		
		
		
		
	}

	
	
	
	
}