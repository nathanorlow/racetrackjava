package org.nateorlow.racetrack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.AffineTransformModel;
import org.openimaj.math.model.fit.RANSAC;

public class RacetrackJava {

	public static final int NUM_CARDS=116;
	public static final int NUM_CUSTOM_CARDS=15;
	public static final int MATCHES_THRESHOLD=10;//or 25
	public static final String INPUT_FILENAME="C:\\Users\\Nate\\Desktop\\Race Images\\Test Photo 1.jpg";
	public static final String CARD_LOOKUP_FILENAME="C:\\Pyscript\\Race\\";
	private static final String[] CARD_NAMES={"Unused","Ancient Race","Earths Lost Colony","New Sparta","Doomed World","Alpha Centauri","Old Earth","Separatist Colony","Epsilon Eridani","Damaged Alien Factory","Investment Credits","Reptillian Uplift World","Terraforming Robots","Alien Robotic Factory","Lost Alien Warship","Space Mercenaries","Rebel Warrior Race","Star Nomad Lair","Space Marines","Deserted Alien Outpost","Alien Rosetta Stone World","Pre-Sentient Race","Expedition Force","Drop Ships","Interstellar Bank","New Galactic Order","Volcanic World","Rebel Homeworld","New Earth","Distant World","Hive World","Gambling World","Mining Conglomerate","Destroyed World","Terraformed World","Pan-Galactic League","Lost Alien Battle Fleet","Expanding Colony","Genetics Lab","Alien Toy Shop","Deserted Alien Library","Gem World","Diversified Economy","Export Duties","Contact Specialist","New Military Tactics","Public Works","Last of the Uplift Gnarssh","Plague World","Empath World","Secluded World","Rebel Sympathizers","Runaway Robots","Deserted Alien World","New Survivalists","Gambiling World","Smuggling Lair","Outlaw World","Smuggling Lair","Refugee World","Rebel Fuel Cache","Artist Colony","Pilgrimage World","Spice World","New Vinland","Aquatic Uplift Race","Former Penal Colony","Rebel Miners","Astroid Belt","Alien Robot Sentry","Avian Uplift World","Radioactive World","Space Port","Galactic Engineers","Clandestine Uplift Lab", "Galactic Bazaar","Mining World","Biohazard Mining World","Galactic Resort","Black Market Trading World","Rebel Underground","Pirate World","Comet Zone","Blaster Gem Mines","Prosperous World","Malevolent Life Forms","Merchant World","Imperium Armaments World","Tourist World","Alien Robot Scout Ship","Rebel Colony","Galactic Studios","Deserted Alien Colony","Rebel Outpost","Lost Species Ark World","Galactic Trendsetters","Rebel Base","Mining Robots","Colony Ship","Deficit Spending","Improved Logistics","Research Lab","Replicant Robots","Consumer Markets","Galactic Federation","Merchant Guild","Free Trade Association","Terraforming Guild","Imperium Lords","Galactic Genome Project","Galactic Renaissance","Mining League","Galactic Survey: SETI","New Economy","Alien Tech Institute","Galactic Imperium","Trade League"};
	private static final int[] CARD_POINTS={0,1,1,1,-1,0,2,1,1,2,1,2,2,5,3,0,2,1,1,3,3,1,1,2,1,0,1,7,3,2,2,1,2,0,5,0,4,1,1,1,5,1,2,1,1,1,1,0,0,1,1,1,1,2,1,1,1,1,1,1,1,1,2,1,1,2,1,1,1,2,2,1,1,1,2,2,2,2,2,2,4,2,2,2,2,2,2,2,2,2,4,3,4,5,3,3,6,1,1,1,2,2,2,3,0,0,0,0,0,0,0,0,0,0,0,0,0};
	private static final String[] CUSTOM_CARD_NUMS={"025","035","104","105","106","107","108","109","110","111","112","113","114","115","116"};
	private static final Set<String> CUSTOM_CARD_SET= new HashSet<String>(Arrays.asList(CUSTOM_CARD_NUMS));
	
	public static String makeThreeDigitString(int i){
		assert(i>=0 && i<1000):"Input needs to be a valid image number";
		if(i<10){
			return "00"+Integer.toString(i);
		}else if(i<100){
			return "0"+Integer.toString(i);
		}else{
			return Integer.toString(i);
		}
	}

	private static CardLookup importCard(String cardID){
		
		Scanner lookupScanner;
		List<Integer> tempones = new ArrayList<Integer>();  
		List<Integer> temptwos = new ArrayList<Integer>();  
		List<Integer> tempthrees = new ArrayList<Integer>();  
		List<Integer> tempneg = new ArrayList<Integer>();

		try {
			lookupScanner = new Scanner(new File(CARD_LOOKUP_FILENAME+cardID+".txt"));
			String[] tempArray;
			tempArray=lookupScanner.nextLine().trim().split(" ");
			for (String tempstr: tempArray){
				if(tempstr.length()<=0)
					continue;
				tempones.add(Integer.parseInt(tempstr));
			}
			tempArray=lookupScanner.nextLine().trim().split(" ");
			for (String tempstr: tempArray){
				if(tempstr.length()<=0)
					continue;
				temptwos.add(Integer.parseInt(tempstr));
			}
			tempArray=lookupScanner.nextLine().trim().split(" ");
			for (String tempstr: tempArray){
				if(tempstr.length()<=0)
					continue;
				tempthrees.add(Integer.parseInt(tempstr));
			}
			tempArray=lookupScanner.nextLine().trim().split(" ");
			for (String tempstr: tempArray){
				if(tempstr.length()<=0)
					continue;
				tempneg.add(Integer.parseInt(tempstr));
			}
			lookupScanner.close();
		} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		return(new CardLookup(tempones,temptwos,tempthrees,tempneg));
	}
	
	public static double getDot(Point2d pt1, Point2d pt2){
		return pt1.getX()*pt2.getX()+pt1.getY()*pt2.getY();
	}
	public static boolean findSkewAngle(Polygon input_polygon){
		List<Point2d> coords=input_polygon.getVertices();
		Point2d pt0=coords.get(0);
		Point2d pt1=coords.get(1);
		Point2d pt2=coords.get(2);
		Point2d vect1=pt0.minus(pt1);
		Point2d vect2=pt1.minus(pt2);
		//System.out.printf("vect1 is (%f,%f) and vect2 is (%f,%f)", vect1.getX(),vect1.getY(),vect2.getX(),vect2.getY());
		double cosAngle=getDot(vect1,vect2)/Math.sqrt(getDot(vect1,vect1)*getDot(vect2,vect2));
		//System.out.printf("Their cos(theta) is %f",cosAngle);
		return (Math.abs(cosAngle)>.1);
	}
	
	public static void main(String[] args) {
		DoGSIFTEngine engine = new DoGSIFTEngine();
		AffineTransformModel fittingModel = new AffineTransformModel(5);
		int i=0;
		int num_matches_found=0;
		RANSAC <Point2d , Point2d > ransac = new RANSAC <Point2d, Point2d >(fittingModel , 1500, new RANSAC.PercentageInliersStoppingCondition(0.5), true) ;
		ConsistentLocalFeatureMatcher2d<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d <Keypoint>(new FastBasicKeypointMatcher <Keypoint >(8), ransac);
		List<LocalFeatureList<Keypoint>> card_features = new ArrayList<LocalFeatureList<Keypoint>>(6);
		List<CardArea> card_areas= new ArrayList<CardArea>(NUM_CARDS);
		List<Integer> card_nums = new ArrayList<Integer>();
		
		int num_shape_groups=0;
		Shape current_shape;
		Polygon current_polygon;
		Shape group_shape;
		CardArea group_card_area;
		double current_area=0;
		Boolean shapematchfound=false;
		MBFImage original_target;
		Map<String,CardLookup> custom_card_list = new HashMap<String,CardLookup>(NUM_CUSTOM_CARDS);
		int score_result=0;

		// TODO Auto-generated method stub
		try {
			//for (i=1; i<NUM_CARDS; i++){
			//	System.out.printf("Card %s is worth %d points\n",CARD_NAMES[i],CARD_POINTS[i]);
			//}
			for (i=0; i<NUM_CUSTOM_CARDS; i++){
				custom_card_list.put(CUSTOM_CARD_NUMS[i],importCard(CUSTOM_CARD_NUMS[i]));
			}
			
			MBFImage target=ImageUtilities.readMBF(new File(INPUT_FILENAME));
			original_target=target.clone();
			//target.drawShape(test3, RGBColour.BLUE);
			//DisplayUtilities.display(target);
			
			LocalFeatureList<Keypoint> target_features = engine.findFeatures(target.flatten());
			System.out.printf("There are %d features in target image\n",target_features.size());
			System.out.printf("Using %d card names\n", CARD_NAMES.length-1); //0 is unused
			matcher.setModelFeatures((List<Keypoint>) target_features);
			for(i=1; i<=NUM_CARDS; i++){
				//String filename="C:\\Users\\Nate\\Desktop\\Race Images\\"+makeThreeDigitString(i)+".png";
				String generic_cardname="C:\\Users\\Nate\\Desktop\\Race Images\\100.png";
				String filenametext="C:\\Users\\Nate\\Desktop\\Race Images\\"+makeThreeDigitString(i-1)+"A.txt";

				MBFImage generic_image=ImageUtilities.readMBF(new File(generic_cardname));
				//MBFImage tempimage=ImageUtilities.readMBF(new File(filename));
				//card_features.add(engine.findFeatures(tempimage.flatten()));
				
				Class<Keypoint> featuretype = Keypoint.class;
				card_features.add(MemoryLocalFeatureList.read(new File(filenametext),featuretype));
				matcher.findMatches((List<Keypoint>) card_features.get(i-1));
				num_matches_found=matcher.getMatches().size();
				System.out.printf("There are %d matches for image %d (%s)\n",num_matches_found,i,CARD_NAMES[i]);
				
				if(num_matches_found>=MATCHES_THRESHOLD){

					//MBFImage temp_target=original_target.clone();
					current_shape=generic_image.getBounds().transform(fittingModel.getTransform());
					current_polygon=current_shape.asPolygon();
					//temp_target.drawShape(current_shape,1,RGBColour.BLUE);
					//temp_target.drawText(Integer.toString(i), 60, 60, HersheyFont.ASTROLOGY , 40, RGBColour.BLACK);
					//DisplayUtilities.display(temp_target);
					
					shapematchfound=false;
					current_area=current_shape.calculateArea();
					//if(current_area/current_shape.calculateRegularBoundingBox().calculateArea() <=.8){
					if(findSkewAngle(current_polygon)){
						System.out.print("Ignoring skew shape");
						continue;//if frame found is actually skew, ignore that choice
					}
					
					for (int j=0; j<num_shape_groups; j++){
						//System.out.printf("Current Shape (i=%d) is %s\n", i,current_shape.toString());
						double overlap_amount=4;
						group_card_area=card_areas.get(j);//these are past card areas
						group_shape=group_card_area.getCard_shape();
						overlap_amount=current_polygon.intersect(group_shape.asPolygon()).calculateArea();
						//System.out.printf("Area of group %d shape is %5.3f, area of current shape is %5.3f\n", j, group_shape.calculateArea(),current_area);
						//System.out.printf("Checking Shape from group %d %s\n", j, group_shape.toString());
						//System.out.printf("Overlaps %5.3f area with group %d\n", overlap_amount, j);
						overlap_amount=overlap_amount/current_area;
						//System.out.printf("Overlaps %5.3f with group %d\n", overlap_amount, j);
						if( overlap_amount>=.8){
							shapematchfound=true;
							if (num_matches_found>group_card_area.getCard_matches()){
								System.out.printf("Group %d has new image num %d (%s)\n",j,i,CARD_NAMES[i]);
								card_areas.get(j).setCard_matches(num_matches_found);
								card_areas.get(j).setCard_num(i);
								card_areas.get(j).setCard_shape(current_shape);
								target.drawShape(current_shape, 1,RGBColour.YELLOW);
								DisplayUtilities.display(target);
							}
							else {
								System.out.printf("Group %d is still image num %d (%s)\n",j,i,CARD_NAMES[i]);
								DisplayUtilities.display(target);								
							}
						}
					}
					if(!shapematchfound){
						card_areas.add(new CardArea(current_shape,i,num_matches_found));
						num_shape_groups+=1;
					}		
				}
			}
			Collections.sort(card_areas);
			
			for (int j=0; j<num_shape_groups; j++){
				Integer temp_cardnum;
				temp_cardnum=card_areas.get(j).getCard_num();
				card_nums.add(temp_cardnum);
				System.out.printf("%s, ", CARD_NAMES[temp_cardnum] );
				//System.out.printf("Found image %d (%s)\n", temp_cardnum ,CARD_NAMES[temp_cardnum] );
				target.drawShape(card_areas.get(j).getCard_shape(), 5,RGBColour.YELLOW);
			}
			for (Integer temp_cardnum:card_nums){
				String card_num_string=makeThreeDigitString(temp_cardnum);
				if (CUSTOM_CARD_SET.contains(card_num_string)){
					score_result+=custom_card_list.get(card_num_string).makeScore(card_nums);
				}else{
					score_result+=CARD_POINTS[temp_cardnum];
				}
			}
			System.out.printf("Has %d cards worth %d points", card_nums.size(), score_result);
			DisplayUtilities.display(target);
			
			/*for (i=0; i<NUM_CARDS; i++){
				String filenameASCII="C:\\Users\\Nate\\Desktop\\Race Images\\"+makeThreeDigitString(i)+"A.txt";
				PrintWriter print_writer;
				try{
					print_writer=new PrintWriter(filenameASCII);
					card_features.get(i).writeASCII(print_writer);
					print_writer.flush();
					print_writer.close();
				}catch(FileNotFoundException e){
					e.getStackTrace();
				}				
			}*/
			
		} catch (IOException e) {
			e.printStackTrace();
		}//end catch
	}//end main
}	

