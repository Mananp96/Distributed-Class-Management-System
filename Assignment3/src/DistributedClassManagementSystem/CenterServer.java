package DistributedClassManagementSystem;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface CenterServer 
{
	  boolean createTRecord (String firstName, String lastName, String address, String phone, String specialization, String location, String managerId);
	  boolean createSRecord (String firstName, String lastName, String[] courseRegistered, String status, String statusDate, String managerId);
	  String getRecordCount (String managerId);
	  boolean editRecords (String recordId, String fieldName, String newValue, String managerId);
	  boolean transferRecord (String managerID, String recordID, String remoteCenterServerName);
	
} // interface CenterServer
