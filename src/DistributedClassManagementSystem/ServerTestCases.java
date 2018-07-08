/**
 * 
 */
package DistributedClassManagementSystem;

import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

/**
 * @author Mihir
 *
 */
public class ServerTestCases {
	
	private CenterServer mtlServer; 
	private CenterServer lvlServer;
	private CenterServer ddoServer;
	
	public ServerTestCases() {
			try {
				this.mtlServer = this.ws_setup("MTL");
				this.lvlServer = this.ws_setup("LVL");
				this.ddoServer = this.ws_setup("DDO");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	}
	
	public CenterServer ws_setup(String serverRegion) throws Exception {
		 QName qname = new QName("http://DistributedClassManagementSystem/", "CenterServerImplService");
		 
		 
		URL url = null;
		if(serverRegion.equals("MTL")) {
			url = new URL("http://localhost:8080/DistributedClassManagementSystem/MTL?wsdl");
         
		}
		else if(serverRegion.equals("LVL")) {
			url = new URL("http://localhost:8080/DistributedClassManagementSystem/LVL?wsdl");
         
		}
		else if(serverRegion.equals("DDO")) {
			 url = new URL("http://localhost:8080/DistributedClassManagementSystem/DDO?wsdl");
	   
		}
		 Service service = Service.create(url, qname);
        return service.getPort(CenterServer.class);
	
		
	}
	
	
	public void testCreateStudentRecord() {
		final CountDownLatch latch = new CountDownLatch(3);
		Thread mtl = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=1;i<=10;i++) {
					if(mtlServer.createSRecord("MTLFNAME"+i, "lastName"+i, new String[] {"courseRegistered"+i }, "Active", "1/1/2014", "MTL0001")) {
						System.out.println("Record created successfully in MTL "+i);
					}
				}
				latch.countDown();
			}
		});
		Thread lvl = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=1;i<=10;i++) {
					if(lvlServer.createSRecord("LVLFNAME"+i, "lastName"+i, new String[] {"courseRegistered"+i }, "Active", "1/1/2014", "LVL0001")) {
						System.out.println("Record created successfully in LVL "+i);
					}
				}
				latch.countDown();
			}
		});
		
		Thread ddo = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=1;i<=10;i++) {
					if(ddoServer.createSRecord("DDOFNAME"+i, "lastName"+i, new String[] {"courseRegistered"+i }, "Active", "1/1/2014", "DDO0001")) {
						System.out.println("Record created successfully in DDO "+i);
					}
				}
				latch.countDown();
			}
		});
		
		mtl.start();
		ddo.start();
		lvl.start();
		
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
	
	public void testCreateTeacherRecord() {
		final CountDownLatch latch = new CountDownLatch(3);
		Thread mtl = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=1;i<=10;i++) {
					
					if(mtlServer.createTRecord("firstName"+i, "lastName"+i, "address"+i, "phone"+i, "specialization"+i, "MTL", "MTL0001")) {
						System.out.println("Record created successfully in MTL "+i);
					}
				}
				latch.countDown();
			}
		});
		Thread lvl = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=1;i<=10;i++) {
					if(lvlServer.createTRecord("firstName"+i, "lastName"+i, "address"+i, "phone"+i, "specialization"+i, "LVL", "LVL0001")) {
						System.out.println("Record created successfully in LVL "+i);
					}
				}
				latch.countDown();
			}
		});
		
		Thread ddo = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=1;i<=10;i++) {
					if(ddoServer.createTRecord("firstName"+i, "lastName"+i, "address"+i, "phone"+i, "specialization"+i, "DDO", "DDO0001")) {
						System.out.println("Record created successfully in DDO "+i);
					}
				}
				latch.countDown();
			}
		});
		
		mtl.start();
		ddo.start();
		lvl.start();
		
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
	
	public void testgetRecordCount() {
		System.out.println(this.mtlServer.getRecordCount("MTL0001"));
		System.out.println(this.ddoServer.getRecordCount("DDO0001"));
		System.out.println(this.lvlServer.getRecordCount("LVL0001"));
	}
	
	public void testTransferRecordConcurrency(final String recordId) {
		final CountDownLatch latch = new CountDownLatch(2);
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(mtlServer.transferRecord("MTL0001", recordId, "LVL")) {
					System.out.println("Record Transfer successfully");
				} else {
					System.out.println("Record not Transfered");
				}
				latch.countDown();
			}
		});
		Thread t2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=1;i<=5;i++) {
					if(mtlServer.transferRecord("MTL0001", recordId, "LVL")) {
						System.out.println("Record Transfer successfully");
					} else {
						System.out.println("Record not Transfered");
					}
				}
				latch.countDown();
			}
		});
		
		t1.start();
		t2.start();
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	
	public void testeditRecordConcurrency(final String recordId) { 
		final CountDownLatch latch = new CountDownLatch(2);
		
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(mtlServer.editRecords(recordId, "firstname", "testconcrrencttest", "MTL0001")) {
					System.out.println("Record is edited");
				} else {
					System.out.println("Record is not edited");
				}
				latch.countDown();
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=1;i<=5;i++) {
					if(mtlServer.editRecords(recordId, "firstname", "testconcrrencttest"+i, "MTL0001")) {
						System.out.println("Record is edited");
					} else {
						System.out.println("Record is not edited");
					}
				}
				latch.countDown();
			}
		});
		t1.start();
		t2.start();
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ServerTestCases s = new ServerTestCases();
		
		s.testCreateStudentRecord();
		s.testCreateTeacherRecord();
		s.testgetRecordCount();
		Scanner s1 = new Scanner(System.in);
		System.out.print("ENTER RrcordId to test Transfer Record Concurrecncy:");
		String recordId = s1.nextLine();
		s.testTransferRecordConcurrency(recordId);
		s.testgetRecordCount();
		System.out.print("ENTER RrcordId to test edit Record Concurrecncy:");
		recordId = s1.nextLine();
		s.testeditRecordConcurrency(recordId);

	}

}
