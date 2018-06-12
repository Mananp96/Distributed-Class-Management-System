package DistributedClassManagementSystem;

import org.omg.CORBA.ORB;

class CenterServerImpl extends CenterServerPOA {
	
	private ORB orb;
	
	public void setORB(ORB orb) {
		this.orb = orb;
	}

	@Override
	public boolean createTRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location, String managerId) {

		return false;
	}

	@Override
	public boolean createSRecord(String firstName, String lastName, String[] courseRegistered, String status,
			String statusDate, String managerId) {

		return false;
	}

	@Override
	public String getRecordCount(String managerId) {

		return null;
	}

	@Override
	public boolean editRecords(String recordId, String fieldName, String newValue, String managerId) {

		return false;
	}
	
	public void shutdown() {
		this.orb.shutdown(false);
	}

}

public class Server  {
	
	public static void main(String args[]) {
		
	}
}
