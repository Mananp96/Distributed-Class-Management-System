package util;

import java.io.Serializable;

/**
* Class for representing a client Request and managing its attributes.
* @author Manan Prajapati
*/

public class FEMessage implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private	String message;
	
	public FEMessage(String msg) {
		this.message = msg;
	}
	
	public String getMessage() {
		return message;
	}
	
}
