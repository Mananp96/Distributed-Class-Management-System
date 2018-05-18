/**
 * 
 */

/**
 * @author mihir
 *
 */

public class StudentRecord extends Record {
	
	public String[] coursesRegistered;
	
	public Status status;
	
	public String statusDate;

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
