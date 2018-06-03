import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class CenterServer extends UnicastRemoteObject implements CenterServerInterface, Runnable {

    private volatile HashMap<String, ArrayList<Record>> recordData;

    private String name;

    private ArrayList<Manager> serverManagerList;

    private int serverPort;

    private int[] nodePorts;

    private volatile boolean isServerRunning;

    public CenterServer(String name, int serverPort, int[] nodePorts) throws SecurityException, IOException {
        super();
        this.name = name;
        recordData = new HashMap<String, ArrayList<Record>>();
        serverManagerList = new ArrayList<Manager>();
        this.serverPort = serverPort;
        this.nodePorts = nodePorts;
        this.isServerRunning = true;
        Thread thread = new Thread(this);
        thread.start();

    }

    private String getIndividualRecordCount() {

        Set<String> keys = this.recordData.keySet();
        int count = 0;
        for (String key : keys) {
            if (this.recordData.get(key) != null) {
                count += this.recordData.get(key).size();
            }
        }

        return this.name + ": " + count;

    }

    private synchronized int generateNumber() {
        Random random = new Random(System.nanoTime());

        return 10000 + random.nextInt(89999);
    }

    private boolean addToRecordData(String firstCharacter, Record record) {
        if (this.recordData.containsKey(firstCharacter)) {
            ArrayList<Record> list = this.recordData.get(firstCharacter);
            if (list != null && list.size() > 0) {
                list.add(record);
                return true;
            } else {
                list.add(record);
                this.recordData.put(firstCharacter, list);
                return true;
            }
        } else {
            ArrayList<Record> list = new ArrayList<Record>();
            list.add(record);
            this.recordData.put(firstCharacter, list);
            return true;
        }
    }

    @Override
    public String getRecordCount(String managerId) throws RemoteException {
        LoggerFactory.LogServer("Received request for " + this.name + " server from " + managerId + " to get record counts.");
        String recordCountData = this.getIndividualRecordCount();
        LoggerFactory.LogServer("Total Records in " + this.name+" server are " + recordCountData);

        DatagramSocket socket = null;
        for (int port : this.nodePorts) {
            try {
                socket = new DatagramSocket();
                InetAddress host = InetAddress.getLocalHost();
                byte[] requestData = "GET_RECORD_COUNT".getBytes();
                DatagramPacket request = new DatagramPacket(requestData, requestData.length, host, port);
                socket.send(request);
                LoggerFactory.LogServer("Request sent to get record data from " + host.getHostName() + ":" + port);
                byte[] buffer = new byte[1000];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                socket.receive(reply);
                String replyData = new String(buffer).replaceAll("\u0000.*", "");
                LoggerFactory.LogServer("Received this response " + replyData + " from " + host.getHostName() + ":" + port);
                if (!replyData.equals("INVALID_REQUEST")) {
                    recordCountData += " " + replyData;
                }
            } catch (SocketException e) {
                System.out.println(e);
                LoggerFactory.LogServer("Error occur to connect another region server");
            } catch (UnknownHostException e) {
                System.out.println(e);
                LoggerFactory.LogServer("Invalid host");
            } catch (IOException e) {
                System.out.println(e);
                LoggerFactory.LogServer("Invalid data");
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }

        }


        return recordCountData;
    }

    @Override
    public boolean editRecords(String recordId, String fieldName, String newValue, String managerId)
            throws RemoteException, RequiredValueException {

        LoggerFactory.LogServer("Manager :" + managerId +" requested to edit a record." );
        LoggerFactory.LogServer(String.format("Editing record, RecordID:%s", recordId));

        if (recordId == null || recordId.isEmpty()) {
            LoggerFactory.LogServer("Record ID required");
            throw new RequiredValueException("Record ID required");
        }

        if (fieldName == null || fieldName.isEmpty()) {
            LoggerFactory.LogServer("FieldName required");
            throw new RequiredValueException("FieldName required");
        }

        if (newValue == null || newValue.isEmpty()) {
            LoggerFactory.LogServer("FieldValue required");
            throw new RequiredValueException("FieldValue required");
        }

        LoggerFactory.LogServer("Looking record id");
        Record record = null;
        for (ArrayList<Record> records : this.recordData.values()) {
            for (Record r : records) {
                if (r.getRecordId().equalsIgnoreCase(recordId)) {
                    LoggerFactory.LogServer(String.format("Record found, %s", r.toString()));
                    record = r;
                    break;
                }
            }

            if (record != null)
                break;
        }

        if ((record != null ? record.getClass() : null) == StudentRecord.class) {
            StudentRecord student = (StudentRecord) record;
            switch (fieldName.toLowerCase()) {
                case "firstname":
                    student.setFirstName(newValue);
                    break;
                case "lastname":
                    student.setLastName(newValue);
                    break;
                case "status":

                    if (!newValue.toLowerCase().equals("active") && !newValue.toLowerCase().equals("inactive")) {
                        LoggerFactory.LogServer("Status is invalid");
                        throw new RequiredValueException("Status is invalid");
                    }
                    student.setStatus(Status.valueOf(newValue));
                    break;
                case "statusdate":
                    try {
                        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                        Date today = df.parse(newValue);
                        student.setStatusDate(newValue);
                    } catch (ParseException e) {
                        LoggerFactory.LogServer("Date is invalid");
                        throw new RequiredValueException("Date is invalid");
                    }
                    break;
                case "coursesregistered":
                    student.setCoursesRegistered(newValue.split(","));
                    break;
                default:
                    LoggerFactory.LogServer("FieldName is invalid");
                    throw new RequiredValueException("FieldName is invalid");
            }

            LoggerFactory.LogServer(String.format("Student record edited, %s", student));

        } else {
            TeacherRecord teacher = (TeacherRecord) record;
            switch (fieldName.toLowerCase()) {
                case "firstname":
                    assert teacher != null;
                    teacher.setFirstName(newValue);
                    break;
                case "lastname":
                    assert teacher != null;
                    teacher.setLastName(newValue);
                    break;
                case "address":
                    assert teacher != null;
                    teacher.setAddress(newValue);
                    break;
                case "phone":
                    assert teacher != null;
                    teacher.setPhone(newValue);
                    break;
                case "specialization":
                    assert teacher != null;
                    teacher.setSpecialization(newValue);
                    break;

                case "location":

                    if (!newValue.toLowerCase().equals("mtl") && !newValue.toLowerCase().equals("lvl")
                            && !newValue.toLowerCase().equals("ddo")) {
                        LoggerFactory.LogServer("location is invalid");
                        throw new RequiredValueException("location is invalid");
                    }
                    assert teacher != null;
                    teacher.setLocation(Location.valueOf(newValue));
                    break;
                default:
                    LoggerFactory.LogServer("FieldName is invalid");
                    throw new RequiredValueException("FieldName is invalid");
            }

            LoggerFactory.LogServer(String.format("Teacher record edited, %s", teacher));
        }

        return true;
    }

    @Override
    public boolean createTRecord(String firstName, String lastName, String address, String phone, String specialization,
                                 Location location, String managerId) throws RemoteException, RequiredValueException {
        LoggerFactory.LogServer("Creating Teacher Record.");
        LoggerFactory.LogServer("Validating fields...");
        if (firstName == null || firstName.isEmpty()) {
            //LoggerFactory.LogServer("First name required");
            throw new RequiredValueException("First name required");
        }

        if (lastName == null || lastName.isEmpty()) {
            //LoggerFactory.LogServer("Last name required");
            throw new RequiredValueException("Last name required");
        }

        if (address == null || address.isEmpty()) {
            //LoggerFactory.LogServer("Address required");
            throw new RequiredValueException("Address required");
        }

        if (phone == null || phone.isEmpty()) {
            //LoggerFactory.LogServer("Phone required");
            throw new RequiredValueException("Phone required");
        }

        if (specialization == null || specialization.isEmpty()) {
            //LoggerFactory.LogServer("Specialization required");
            throw new RequiredValueException("Specialization required");
        }

        if (location == null) {
            // LoggerFactory.LogServer("Status required");
            throw new RequiredValueException("Status required");
        }
        LoggerFactory.LogServer("Validating fields complete...");
        Record record = new TeacherRecord("TR" + generateNumber(), firstName, lastName, address, phone, specialization, location);

        String firstCharacter = record.getLastName().substring(0, 1).toUpperCase();

        LoggerFactory.LogServer("Adding Record data to List...");
        Boolean result = addToRecordData(firstCharacter, record);

        if (result) {
            LoggerFactory.LogServer(String.format("Record added to the list :%s", record.toString()));
            LoggerFactory.LogServer(String.format("Teacher Record Successfully created by Manager:%s",(managerId)));
        }
        else {
            LoggerFactory.LogServer(String.format("Something went wrong when creating teacher record :%s \n by Manager: %s", record.toString(),(managerId)));
        }

        return result;
    }

    @Override
    public boolean createSRecord(String firstName, String lastName, String[] courseRegistered, Status status,
                                 String statusDate, String managerId) throws RemoteException, RequiredValueException {
        LoggerFactory.LogServer("Creating Student Record...");
        LoggerFactory.LogServer("Validating fields...");
        if (firstName == null || firstName.isEmpty()) {
            //LoggerFactory.LogServer("First name required");
            throw new RequiredValueException("First name required");
        }

        if (lastName == null || lastName.isEmpty()) {
            //LoggerFactory.LogServer("Last name required");
            throw new RequiredValueException("Last name required");
        }

        if (statusDate == null || statusDate.isEmpty()) {
            //LoggerFactory.LogServer("Status Date required");
            throw new RequiredValueException("Status Date required");
        }

        if (courseRegistered == null || courseRegistered.length < 1) {
            //LoggerFactory.LogServer("Registed Course required");
            throw new RequiredValueException("Registered Course required");
        }

        if (status == null) {
            //LoggerFactory.LogServer("Status required");
            throw new RequiredValueException("Status required");
        }
        LoggerFactory.LogServer("Validating fields complete...");


        Record record = new StudentRecord("SR" + generateNumber(), firstName, lastName, courseRegistered, status, statusDate);

        String firstCharacter = record.getLastName().substring(0, 1).toUpperCase();

        LoggerFactory.LogServer("Adding Record data to List...");
        boolean result = addToRecordData(firstCharacter, record);

        if (result) {
            LoggerFactory.LogServer(String.format("Record added to the list :%s", record.toString()));
            LoggerFactory.LogServer(String.format("Student Record Successfully created by Manager:%s",(managerId)));
        }
        else {
            LoggerFactory.LogServer(String.format("Something went wrong when creating student record :%s \n by Manager: %s", record.toString(),(managerId)));
        }
        return result;
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(this.serverPort);
            LoggerFactory.LogServer("UDP server Started in " + this.name + " region in this port " + this.serverPort);
            byte[] buffer = new byte[1000];
            while (this.isServerRunning) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                LoggerFactory.LogServer("Received request in " + this.name + " from " + request.getAddress() + ":" + request.getPort() + " with this data " + new String(request.getData()).replaceAll("\u0000.*", "") + "");
                String replyData = "";
                String requestData = new String(request.getData()).replaceAll("\u0000.*", "");
                if (requestData.equals("GET_RECORD_COUNT")) {
                    replyData = this.getIndividualRecordCount();
                } else {
                    replyData = "INVALID_REQUEST";
                }
                DatagramPacket reply = new DatagramPacket(replyData.getBytes(),
                        replyData.length(), request.getAddress(), request.getPort());
                socket.send(reply);
            }

        } catch (Exception e) {
            System.out.println(e);
            LoggerFactory.LogServer("Unable to start udp server in " + this.name + " region");
        }
    }

}
