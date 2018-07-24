package dcms;
/**
 *
 */

/**
 * This is the superclass Record which has the common attributes that of 
 * teacher as well as Student. FName, LName and ID will be the common 
 * attributes for all two class.
 * @author mihir
 */
public class Record {


    private String recordId;

    private String firstName;

    private String lastName;

    private Object lock;

    public Record(String recordId, String firstName, String lastName) {
        this.recordId = recordId;
        this.firstName = firstName;
        this.lastName = lastName;

        this.lock = new Object();
    }

    public Object getLock() { return lock; }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public String toString() {
        return String.format("RecordID:%s FirstName:%s LastName:%s", this.getRecordId(), this.getFirstName(), this.getLastName());
    }
    
    public String toSplited()
    {
    	return String.format("%s;%s;%s", this.getRecordId(), this.getFirstName(), this.getLastName());
    }

}
