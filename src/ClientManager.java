import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClientManager {

    private CenterServerInterface server;

    private String managerId;

    public static void main(String[] args) throws IOException, RequiredValueException {

        ClientManager client = new ClientManager();
        try {
            client.menu();
        } catch (NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void menu() throws RemoteException, NotBoundException {
        String region;
        while (true) {

            String managerId = userInput("Enter Manager ID:");

            String serverRegion = managerId.substring(0, 3);

            boolean lengthCheck = (managerId.length() == 7);

            boolean regionCheck = (serverRegion.equalsIgnoreCase("MTL") || serverRegion.equalsIgnoreCase("LVL") || serverRegion.equalsIgnoreCase("DDO"));

            boolean idCheck = false;


            try {
                if (lengthCheck) {
                    String id = managerId.substring(3, 7);
                    Integer.parseInt(id);
                    idCheck = true;
                }
            } catch (Exception ignored) {
            }

            if (!(lengthCheck && regionCheck && idCheck)) {
                System.out.println("Invalid Manager ID \n Please try again...");
            } else {
                region = serverRegion.toUpperCase();
                this.managerId = managerId;

                LoggerFactory.Log(this.managerId, "Registering manager");
                Registry registry = LocateRegistry.getRegistry(2964);

                LoggerFactory.Log(this.managerId, "Looking up server");
                server = (CenterServerInterface) registry.lookup(region);

                break;
            }
        }

        if (!region.isEmpty()) {

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
                        System.out.println("----------Get Record Counts----------");
                        System.out.println(server.getRecordCount(this.managerId));
                        System.out.println("Press any key to continue...");
                        break;
                    case "4":
                        try {
                            this.editRecord();
                        } catch (RequiredValueException e) {

                            e.printStackTrace();
                        }
                        System.out.println("Press any key to continue...");
                        break;
                    case "5":
                        System.out.println("Good Bye!");
                        break;
                    default:
                        System.out.println("Enter Correct Choice");
                }
                if (choice.equals("5")) {
                    break;
                }
                userInput("");
            }

        }

    }

    private void addTeacherRecord() throws RemoteException, RequiredValueException {

        LoggerFactory.Log(this.managerId, "Adding Teacher");

        Location location;
        System.out.println("----------Add Teacher Record----------");
        String firstName = userInput("Enter First Name:");
        String lastName = userInput("Enter Last Name:");
        String address = userInput("Enter Address:");
        String phone = userInput("Enter Phone:");
        String specialization = userInput("Enter Specialization:");
        String loc = userInput("Enter Server location:");

        if (loc == "MTL") {
            location = Location.MTL;
        } else if (loc == "LVL") {
            location = Location.LVL;
        } else {
            location = Location.DDO;
        }


        Boolean result = server.createTRecord(firstName, lastName, address, phone, specialization, location, managerId);

        String str = String.format("FirstName:%s LastName:%s Address:%s Phone:%s Specialization:%s Location:%s", firstName, lastName, address, phone, specialization, location.name());
        if (result)
            LoggerFactory.Log(this.managerId, String.format("Teacher record added:%s", str));
        else
            LoggerFactory.Log(this.managerId, String.format("Teacher record did not add:%s", str));

    }

    private void addStudentRecord() throws RemoteException, RequiredValueException {

        LoggerFactory.Log(this.managerId, "Adding Student");

        Status status;
        System.out.println("----------Add Student Record----------");
        String firstName = userInput("Enter First Name:");
        String lastName = userInput("Enter Last Name:");
        String courses = userInput("Enter Registered Course:");
        String[] courseRegistered = courses.split(",");
        String stat = userInput("Enter Status");
        if (stat == "Active") {
            status = Status.ACTIVE;
        } else {
            status = Status.INACTIVE;
        }
        String statusDate = userInput("Enter Status Date:");

        Boolean result = server.createSRecord(firstName, lastName, courseRegistered, status, statusDate, managerId);

        String str = String.format("FirstName:%s LastName:%s Status:%s StatusDate:%s coursesRegistered:%s", firstName, lastName, status.name(), String.join(",", courseRegistered));
        if (result)
            LoggerFactory.Log(this.managerId, String.format("Student record added:%s", str));
        else
            LoggerFactory.Log(this.managerId, String.format("Student record did not add:%s", str));

    }

    private void editRecord() throws RemoteException, RequiredValueException {
        System.out.println("----------Edit Record----------");
        String recordId = userInput("Enter Record ID:");
        String fieldName = userInput("Enter Field Name:");
        String newvalue = userInput("Enter New Value:");

        LoggerFactory.Log(this.managerId, String.format("Request to edit record: %s", recordId));

        Boolean result = server.editRecords(recordId, fieldName, newvalue, this.managerId);

        String str = String.format("RecordID:%s FieldName:%s Value:%s", recordId, fieldName, newvalue);
        if (result)
            LoggerFactory.Log(this.managerId, String.format("Record edited:%s", str));
        else
            LoggerFactory.Log(this.managerId, String.format("Record did not edit:%s", str));
    }

    private String userInput(String var2) {
        System.out.print(var2);
        Scanner s = new Scanner(System.in);
        return s.nextLine();
    }

}