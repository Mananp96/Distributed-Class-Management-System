import java.util.UUID;

public interface ManagerClient {
	
	public boolean createTRecord(
		String firstName,
		String lastName,
		String address,
		String phone,
		String[] specialization,
		Location location
	);
	
	public boolean createSRecord(
		String firstName,
		String lastName,
		String[] coirseRegistered,
		Status status,
		String statusDate
	);
	
	public String getRecordCount();
	
	public boolean editRecords(
		UUID recordId,
		String fieldName,
		String[] newValue
	);
}
