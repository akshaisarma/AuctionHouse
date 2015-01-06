package hw3;


import java.io.Serializable;
import javax.persistence.*;

@Entity
public class AuctionItem implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	private int id;
	private int currentValue;
	private String itemName;
	private String userName;

	public AuctionItem() {}
	
	public AuctionItem(int id, String name, int start, String userName) {
    	this.id = id;
    	this.itemName = name;
    	this.currentValue = start;
    	this.userName = userName;
    }

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(int currentValue) {
		this.currentValue = currentValue;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}	
	
    public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}

	