package Structures;

import java.util.ArrayList;

import Utility.TextUtility;


public class Entity {
	
	public String id = "";
	public String name = "";
	public String type = "";
	public int startIndex = -1;
	public int endIndex = -1;
	public int[] boundaries = new int[2];
	public ArrayList<Integer> clauses = new ArrayList<Integer>();
	
	public String toString() {
		StringBuilder sbTemp = new StringBuilder();
		
		sbTemp.append( this.id + " "
						+ this.startIndex + " " + this.endIndex + " " + this.name + " | ");
				
		return sbTemp.toString();
	}
	
	public Entity() {
		
	} 
	
	public Entity ( String idAndBoundaries, String type, String name ) {
		String[] temp = idAndBoundaries.split("\\s+");
		
		this.id = temp[0]; // id
		this.startIndex = Integer.valueOf(temp[1]); // start index
		this.endIndex = Integer.valueOf(temp[2]); // end index
		this.type = type; // type
		this.name = name; // name
		this.boundaries = new int[] {startIndex, endIndex};
	}
	
	public boolean hasOverlap ( Entity otherEnt ) {
		return TextUtility.hasOverlap ( new int[] {startIndex, endIndex}, new int[] { otherEnt.startIndex, otherEnt.endIndex } );	
	}
}
