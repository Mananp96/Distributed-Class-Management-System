package DistributedClassManagementSystem;

/**
 * This class extends the class Record, which will inherit all of it's members.
 * teachers will have address, phone, specialization and location.
 * Status date.
 * @author Team#2
 */
public class TeacherRecord extends Record {

    public String address;

    public String phone;

    public String specialization;

    public String location;

    public TeacherRecord(String recordId, String firstName, String lastName, String address, String phone,
                         String specialization, String location) {
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
		return String.format("%s Address:%s Phone:%s Specialization:%s Location:%s", super.toString(), this.getAddress(), this.getPhone(), this.getSpecialization(), this.getLocation());
    }
    
    public String toSplited(){
    	return String.format("%s;%s;%s;%s;%s", super.toSplited(), this.getAddress(), this.getPhone(), this.getSpecialization(), this.getLocation());
    }
    
    
    /**
	 * This method will create the student record obj from string.
	 * This is particularly used in transfer record where we just pass the record obj as a string 
	 * and other server will create the obj using the string with help of this method.
	 * @param String 
	 * @return TeacherRecord object
	 */
    public static TeacherRecord fromString(String s){   	
    	String[] strs = s.split(";");
    	TeacherRecord  record = new TeacherRecord(strs[0], strs[1], strs[2], strs[3], strs[4], strs[5], strs[6]);    	
    	return record;
    }
}
