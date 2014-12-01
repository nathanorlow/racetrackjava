package org.nateorlow.racetrack;

import java.util.ArrayList;
import java.util.List;

public class CardLookup {
	List<Integer> onepoint=new ArrayList<Integer>();
	List<Integer> twopoint=new ArrayList<Integer>();
	List<Integer> threepoint=new ArrayList<Integer>();
	List<Integer> negpoint=new ArrayList<Integer>();
	
	public CardLookup(List<Integer> newone){
		onepoint=newone;
	}
	public CardLookup(List<Integer> newone, List<Integer> newtwo){
		onepoint=newone;
		twopoint=newtwo;
	}

	public CardLookup(List<Integer> newone, List<Integer> newtwo, List<Integer> newthree){
		onepoint=newone;
		twopoint=newtwo;
		threepoint=newthree;
	}
	public CardLookup(List<Integer> newone, List<Integer> newtwo, List<Integer> newthree, List<Integer> newneg){
		onepoint=newone;
		twopoint=newtwo;
		threepoint=newthree;
		negpoint=newneg;
	}

	
	public int makeScore(List<Integer> cardIDlist){
		int score=0;
		for(Integer tempID: cardIDlist){
			if (threepoint.contains(tempID)){
				score+=3;
			} else if(twopoint.contains(tempID)){
				score+=2;
			} else if(onepoint.contains(tempID)){
				score+=1;
			}
		}
		return(score);
	}
}
