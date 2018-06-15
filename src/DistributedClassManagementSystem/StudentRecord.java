package DistributedClassManagementSystem;
/**
 *
 */

/**
 * @author mihir
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

	@Override
	public String toString() {
		return String.format("%s Status:%s StatusDate:%s coursesRegistered:%s", super.toString(),
				this.getStatus(), this.getStatusDate(), String.join(",", this.getCoursesRegistered()));
	}
}
