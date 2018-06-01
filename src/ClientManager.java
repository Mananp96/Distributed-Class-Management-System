import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClientManager {
	
	private CenterServerInterface server;
	
	private String region;
	
	private String managerId;

	public void menu() throws RemoteException, NotBoundException {
		while(true) {

			String managerId = userInput("Enter Manager ID:");
			
			String serverRegion = managerId.substring(0, 3);
			
			boolean lengthCheck = (managerId.length() == 7);
			
			boolean regionCheck = (serverRegion.equalsIgnoreCase("MTL") || serverRegion.equalsIgnoreCase("LVL") || serverRegion.equalsIgnoreCase("DDO"));
			
			boolean idCheck = false;
			
			try {
				if(lengthCheck) {
					String id = managerId.substring(3,7);
					int number = Integer.parseInt(id);
					idCheck = true;
				}
			} catch (Exception e) {}
			
			if(!(lengthCheck && regionCheck && idCheck)) {
				System.out.println("Invalid Manager ID \n Please try again...");
			} else { 
				region = serverRegion.toUpperCase();
				this.managerId = managerId;
				break; 
			}
		}
		
		if(! region.isEmpty()) {
			Registry registry = LocateRegistry.getRegistry(2964);
		
			server = (CenterServerInterface) registry.lookup(region);
			while (true) {
				System.out.println("********************************");
				System.out.println("1. Add Teacher Record");
				System.out.println("2. Add Student Record");
				System.out.println("3. Get record counts");
				System.out.println("4. Edit Record");
				System.out.println("5. Exit");
				System.out.println("********************************");
				String choice = userInput("Enter Choice:");
				switch (choice) {
				case "1":
					try {
						this.addTeacherRecord();
					} catch (RequiredValueException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Press any key to continue...");
					break;
				case "2":
					try {
						this.addStudentRecord();
					} catch (RequiredValueException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Press any key to continue...");
					break;
				case "3":
					System.out.println("----------3>>>Get Record Counts----------");
					System.out.println("Press any key to continue...");
					break;
				case "4":
					this.editRecord();
					System.out.println("Press any key to continue...");
					break;
				case "5":
					System.out.println("Good Bye!");
					break;
				default:
					System.out.println("Enter Correct Choice");
				}
				if(choice.equals("5")) {break;}
				userInput("");
			}
					
		}
		
	}


	private void addTeacherRecord() throws RemoteException, RequiredValueException {
		Location location;
		Status status;
		System.out.println("----------Add Teacher Record----------");
		String firstName = userInput("Enter First Name:");
		String lastName = userInput("Enter Last Name:");
		String address = userInput("Enter Address:");
		String phone = userInput("Enter Phone:");
		String spec = userInput("Enter Specialization:");
		String[] specialization = spec.split(",");
		String loc = userInput("Enter Server location:");

		if (loc == "MTL") {
			location = Location.MTL;
		} else if (loc == "LVL") {
			location = Location.LVL;
		} else {
			location = Location.DDO;
		}
		server.createTRecord(firstName, lastName, address, phone, specialization, location, managerId);
	}

	private void addStudentRecord() throws RemoteException, RequiredValueException {
		Status status;
		System.out.println("----------Add Student Record----------");
		String firstName = userInput("Enter First Name:");
		String lastName = userInput("Enter Last Name:");
		String coursearray = userInput("Enter Registered Course:");
		String[] courseRegistered = coursearray.split(",");
		String stat = userInput("Enter Status");
		if (stat == "Active") {
			status = Status.ACTIVE;
		} else {
			status = Status.INACTIVE;
		}
		String statusDate = userInput("Enter Status Date:");
		server.createSRecord(firstName, lastName, courseRegistered, status, statusDate, managerId);
	}

	private void editRecord() {
		System.out.println("----------Edit Record----------");
		String recordId = userInput("Enter Record ID:");
		String fieldName = userInput("Enter Field Name:");
		String newvalue = userInput("Enter New Value:");
		String[] newValue = newvalue.split(",");
	}

	private String userInput(String var2) {
		System.out.print(var2);
		Scanner s = new Scanner(System.in);
		return s.nextLine();
	}

	public static void main(String[] args) throws IOException, RequiredValueException {
		
		ClientManager client = new ClientManager();
		try {
			client.menu();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}