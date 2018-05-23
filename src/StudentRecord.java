/**
 * 
 */

/**
 * @author mihir
 *
 */

public class StudentRecord extends Record {
	
	private String[] coursesRegistered;
	
	private Status status;
	
	private String statusDate;
	
	public StudentRecord(String recordId, String firstName, String lastName, String[] coursesRegistered, Status status,
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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(String statusDate) {
		this.statusDate = statusDate;
	}
}
