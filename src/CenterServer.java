import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CenterServer extends UnicastRemoteObject implements CenterServerInterface {

	public CenterServer() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean createTRecord(String firstName, String lastName, String address, String phone,
			String[] specialization, Location location, String managerId) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createSRecord(String firstName, String lastName, String[] coirseRegistered, Status status,
			String statusDate, String managerId) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getRecordCount(String managerId) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean editRecords(String recordId, String fieldName, String[] newValue, String managerId)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

}
