package org.nateorlow.racetrack;

import org.openimaj.math.geometry.shape.Shape;

public class CardArea implements Comparable<CardArea>{
private Shape card_shape;
private int card_num;
private int card_matches;
public static final int YWEIGHT=6;

public CardArea(Shape new_shape, int new_num, int new_matches){
	if(new_shape==null){
		throw new NullPointerException();
	}
	card_shape=new_shape;
	card_num=new_num;
	card_matches=new_matches;
}

public Shape getCard_shape() {
	return card_shape;
}
public void setCard_shape(Shape card_shape) {
	this.card_shape = card_shape;
}
public int getCard_num() {
	return card_num;
}
public void setCard_num(int card_num) {
	this.card_num = card_num;
}
public int getCard_matches() {
	return card_matches;
}
public void setCard_matches(int card_matches) {
	this.card_matches = card_matches;
}

public String toString(){
	return "Card Area for id "+Integer.toString(card_num)+" and "+Integer.toString(card_matches)+" matches, covering coordinates "+card_shape.toString();
}

public int compareTo(CardArea compare_object){
	double compare_score=shapeToScore(compare_object.getCard_shape());
	double this_score=shapeToScore(this.getCard_shape());
	if(this_score==compare_score){
		return 0;
	}
	else if(this_score<compare_score)
		return -1;
	else{
		return 1;
	}
}

public int hashCode(){
	return card_shape.hashCode();
}

public boolean equals(Object compare_object){
	//following the docs example
    if (!(compare_object instanceof CardArea))
        return false;
    CardArea compare_area=(CardArea) compare_object;
    return(compare_area.card_shape==this.card_shape);
}

private double shapeToScore(Shape input_shape){
	return input_shape.minX()+YWEIGHT*input_shape.minY();
}

}
