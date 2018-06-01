import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class CenterServer extends UnicastRemoteObject implements CenterServerInterface {
	
	
	private HashMap<String, ArrayList<Record>> recordData;

	private String name;

	private ArrayList<Manager> serverManagerList;

	public CenterServer(String name) throws SecurityException, IOException {
		super();
		this.name = name;
		recordData = new HashMap<String,ArrayList<Record>>();
		serverManagerList = new ArrayList<Manager>();
	}

	
	public boolean createTRecord(String firstName, String lastName, String address, String phone,
			String specialization, Location location, String managerId, String recordId) throws RemoteException,RequiredValueException {
		
		if(firstName == null || firstName.isEmpty()) {
			throw new RequiredValueException("First name required");
		}
		
		if(lastName == null || lastName.isEmpty()) {
			throw new RequiredValueException("Last name required");
		}
		
		if(address == null || address.isEmpty()) {
			throw new RequiredValueException("Address required");
		}
		
		if(phone == null || phone.isEmpty()) {
			throw new RequiredValueException("Phone required");
		}
		
		if(specialization == null || specialization.isEmpty()) {
			throw new RequiredValueException("Specialization required");
		}
		
		if(location == null) {
			throw new RequiredValueException("Status required");
		}

		Record record = new TeacherRecord(recordId,firstName,lastName,address,phone,specialization,location);
		
		String firstCharacter = record.getLastName().substring(0, 1).toUpperCase();
		
		return addToRecordData(firstCharacter, record);

	}

	
	public boolean createSRecord(String firstName, String lastName, String[] courseRegistered, Status status,
			String statusDate, String managerId, String recordId) throws RemoteException,RequiredValueException {
		
		if(firstName == null || firstName.isEmpty()) {
			throw new RequiredValueException("First name required");
		}
		
		if(lastName == null || lastName.isEmpty()) {
			throw new RequiredValueException("Last name required");
		}
		
		if(statusDate == null || statusDate.isEmpty()) {
			throw new RequiredValueException("Status Date required");
		}
		
		if(courseRegistered == null || courseRegistered.length < 1) {
			throw new RequiredValueException("Registed Course required");
		}
		
		if(status == null) {
			throw new RequiredValueException("Status required");
		}

		Record record = new StudentRecord(recordId,firstName,lastName,courseRegistered,status,statusDate);
		
		String firstCharacter = record.getLastName().substring(0, 1).toUpperCase();
		
		return addToRecordData(firstCharacter, record);
	}

	@Override
	public String getRecordCount(String managerId) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean editRecords(String recordId, String fieldName, String[] newValue, String managerId)
			throws RemoteException,RequiredValueException {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	private int generateNumber() {
		Random random = new Random(System.nanoTime());

		return 10000 + random.nextInt(89999);
	}

    private boolean addToRecordData(String firstCharacter, Record record) {
        if(this.recordData.containsKey(firstCharacter)) {
            ArrayList<Record> list = this.recordData.get(firstCharacter);
            if(list != null && list.size() > 0) {
                list.add(record);
                return true;
            } else {
                if (list != null) {
                    list.add(record);
                }
                this.recordData.put(firstCharacter, list);
                return true;
            }
        } else {
            ArrayList<Record> list = new ArrayList<Record>();
            list.add(record);
            this.recordData.put(firstCharacter, list);
            return true;
        }
    }


    public void addManagerToList(Manager manager){
	    if (manager !=null){
            serverManagerList.add(manager);
        }
    }

    @Override
    public boolean managerExists(String id) throws RemoteException{
        for (Manager manager: serverManagerList){
            if(manager.getManagerID().equals(id)){
                return true;
            }
        }
        return false;
    }


	@Override
	public boolean createTRecord(String firstName, String lastName, String address, String phone,
			String specialization, Location location, String managerId)
			throws RemoteException, RequiredValueException {
		return this.createTRecord(firstName, lastName, address, phone, specialization, location, managerId, "TR"+generateNumber());
	}


	@Override
	public boolean createSRecord(String firstName, String lastName, String[] courseRegistered, Status status,
			String statusDate, String managerId) throws RemoteException, RequiredValueException {
		return this.createSRecord(firstName, lastName, courseRegistered, status, statusDate, managerId, "SR"+generateNumber());
	}
}
