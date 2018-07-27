/**
 * 
 */
package rudp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.rudp.ReliableServerSocket;

/**
 * @author Mihir
 *
 */
public class UDPServer {

	private ReliableServerSocket serverSocket;

	private UDPServerListener serverListener;

	private boolean isRunning;

	private BlockingQueue<UDPRequest> requestQueue;

	private Thread requestThread;

	private Thread responseThread;

	public UDPServer(int port, UDPServerListener serverListener) throws IOException {
		this.serverSocket = new ReliableServerSocket(port);
		this.serverListener = serverListener;
		this.requestQueue = new LinkedBlockingQueue<UDPRequest>();

		this.requestThread = new Thread(new Runnable() {

			@Override
			public void run() {

				while (isRunning) {

					Socket socket;
					try {
						socket = serverSocket.accept();
						new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									while (socket.isConnected() && (!socket.isInputShutdown())) {
										DataInputStream input = new DataInputStream(socket.getInputStream());
										try {
											String requestMessage = input.readUTF();
											UDPRequest request = new UDPRequest(socket, requestMessage);
											requestQueue.put(request);

										} catch (EOFException e) {
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										input.close();
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}).start();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

				}
			}
		});

		this.responseThread = new Thread(new Runnable() {

			@Override
			public void run() {

				while (isRunning) {
					UDPRequest request;
					try {
						request = requestQueue.take();

						if (request != null) {
							String data = request.getData();
							Socket requestSocket = request.getSocket();
							InetAddress client = requestSocket.getInetAddress();
							String responseData = serverListener.respondRequest(data, client);
							if (requestSocket.isConnected() && (!requestSocket.isOutputShutdown())) {
								DataOutputStream outputStream;

								outputStream = new DataOutputStream(requestSocket.getOutputStream());
								outputStream.writeUTF(responseData);
								outputStream.close();
								requestSocket.close();

							}
						}

					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	public void start() {
		this.isRunning = true;
		this.requestThread.start();
		this.responseThread.start();
	}

	public void killServer() {
		this.isRunning = false;
		this.serverSocket.close();
	}

	public static void main(String[] args) {
		boolean running = true;
		ReliableServerSocket serverSocket;
		try {
			serverSocket = new ReliableServerSocket(9191);
			System.out.println(serverSocket.getLocalSocketAddress());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
