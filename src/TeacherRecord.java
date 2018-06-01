/**
 * 
 */

/**
 * @author mihir
 *
 */
public class TeacherRecord extends Record {
	
	public String address;
	
	public String phone;
	
	public String specialization;
	
	public Location location;

	public TeacherRecord(String recordId, String firstName, String lastName, String address, String phone,
			String specialization, Location location) {
		super(recordId, firstName, lastName);
		this.address = address;
		this.phone = phone;
		this.specialization = specialization;
		this.location = location;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}
