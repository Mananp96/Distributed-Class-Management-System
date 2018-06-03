public class Manager {
    private String managerID;
    private String firstName;
    private String lastName;

    public Manager(String managerID, String firstName, String lastName) {
        this.managerID = managerID;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getManagerID() {
        return managerID;
    }

    public void setManagerID(String managerID) {
        this.managerID = managerID;
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
        return String.format("ManagerID:%s FirstName:%s LastName:%s", this.getManagerID(), this.getFirstName(), this.getLastName());
    }
}
