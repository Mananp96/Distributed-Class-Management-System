package records;

/**
 * This class extends the class Record, which will inherit all of it's members.
 * Students will have courses and status of the course.
 * Status date.
 * @author Team#2
 */

public class StudentRecord extends Record {

	private String[] coursesRegistered;

	private String status;

	private String statusDate;

	public StudentRecord(String recordId, String firstName, String lastName, String[] coursesRegistered, String status,
			String statusDate) {
		super(recordId, firstName, lastName);
		this.coursesRegistered = coursesRegistered;
		this.status = status;
		this.statusDate = statusDate;
	}

	public String[] getCoursesRegistered() {
		return coursesRegistered;
	}

	public void setCoursesRegistered(String[] coursesRegistered) {
		this.coursesRegistered = coursesRegistered;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(String statusDate) {
		this.statusDate = statusDate;
	}

	/**
	 * Over-ride method
	 * @return Formatted string 
	 */
	@Override
	public String toString() {
		return String.format("%s Status:%s StatusDate:%s coursesRegistered:%s", super.toString(),
				this.getStatus(), this.getStatusDate(), String.join(",", this.getCoursesRegistered()));
	}
	
	/**
	 * This function will return the string 
	 * @return String containing Status, Date and Courses in which student is registered. 
	 */
	public String toSplited() {
		return String.format("%s|%s|%s|%s", super.toSplited(),
				this.getStatus(), this.getStatusDate(), String.join(",", this.getCoursesRegistered()));
	}
	
	/**
	 * This method will create the student record obj from string.
	 * This is particularly used in transfer record where we just pass the record obj as a string 
	 * and other server will create the obj using the string with help of this method.
	 * @param String 
	 * @return StudentRecord object
	 */
	 public static StudentRecord fromString(String s){
    	String[] strs = s.split(";");
    	
    	String[] courses = strs[3].split(",");
    	StudentRecord  record = new StudentRecord(strs[0], strs[1], strs[2],  courses, strs[4], strs[5]);
    	
    	return record;
	 }
}
