package Structures;

import java.util.ArrayList;

public class Relation {

	public String id = "";
	public String type = "";
	public boolean isBinaryRelation = true; // default
	public String arg1 = "";
	public String arg2 = "";
	public boolean isPositive = false;
	public ArrayList<String> listOfArgumentEntities = new ArrayList<String>();
	
	public String toString() {
		
		return this.type + " "
			+ this.arg1 + " " + this.arg2 + " " + (this.isPositive ? "true" : "false");
	}
	
	public Relation () {
		
	} 
	
	public Relation ( String typeArgsPolarity ) {
		String[] temp = typeArgsPolarity.split("\\s+");
		
		this.type = temp[0]; 
		this.arg1 = temp[1]; 
		this.arg2 = temp[2];
		this.isPositive = Boolean.valueOf(temp[3]);
	}
}
