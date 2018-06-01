import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CenterServerInterface extends Remote {
	
	public boolean createTRecord(
		String firstName,
		String lastName,
		String address,
		String phone,
		String specialization,
		Location location,
		String managerId
	) throws RemoteException,RequiredValueException;
	
	public boolean createSRecord(
		String firstName,
		String lastName,
		String[] courseRegistered,
		Status status,
		String statusDate,
		String managerId
	) throws RemoteException,RequiredValueException;
	
	public String getRecordCount(String managerId) throws RemoteException;
	
	public boolean editRecords(
		String recordId,
		String fieldName,
		String[] newValue,
		String managerId
	) throws RemoteException,RequiredValueException;
}
