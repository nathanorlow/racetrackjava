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

public class RacesignJava {

	public static final int NUM_SIGNS=4;
	public static final int MATCHES_THRESHOLD=10;//or 25
	public static final String INPUT_FILENAME="C:\\Pyscript\\Sign\\Test Images\\Test Image 1.jpg";
	public static final String SIGN_LOOKUP_FILENAME="C:\\Pyscript\\Sign\\";
	private static final String[] SIGN_NAMES={"Speed Limit","Exact Stop Sign","Yield Sign","Speed Limit","Stop Sign"};
	public static final boolean MAKE_NEW_FEATURE_RECORDS=false;
	
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
	
	//public static double getDot(Point2d pt1, Point2d pt2){
	//	return pt1.getX()*pt2.getX()+pt1.getY()*pt2.getY();
	//}
	
	public static void main(String[] args) {
		DoGSIFTEngine engine = new DoGSIFTEngine();
		AffineTransformModel fittingModel = new AffineTransformModel(5);
		int i=0;
		int num_matches_found=0;
		RANSAC <Point2d , Point2d > ransac = new RANSAC <Point2d, Point2d >(fittingModel , 1500, new RANSAC.PercentageInliersStoppingCondition(0.5), true) ;
		ConsistentLocalFeatureMatcher2d<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d <Keypoint>(new FastBasicKeypointMatcher <Keypoint >(8), ransac);
		List<LocalFeatureList<Keypoint>> card_features = new ArrayList<LocalFeatureList<Keypoint>>(6);
		List<CardArea> sign_areas= new ArrayList<CardArea>(NUM_SIGNS);
		List<Integer> card_nums = new ArrayList<Integer>();
		
		int num_shape_groups=0;
		Shape current_shape;
		Polygon current_polygon;
		Shape group_shape;
		CardArea group_card_area;
		double current_area=0;
		Boolean shapematchfound=false;

		
		// TODO Auto-generated method stub
		try {
			
			MBFImage target=ImageUtilities.readMBF(new File(INPUT_FILENAME));
			//target.drawShape(test3, RGBColour.BLUE);
			//DisplayUtilities.display(target);

			LocalFeatureList<Keypoint> target_features = engine.findFeatures(target.flatten());
			System.out.printf("There are %d features in target image\n",target_features.size());
			System.out.printf("Using %d signs\n", SIGN_NAMES.length-1); //0 is unused
			matcher.setModelFeatures((List<Keypoint>) target_features);

			
			for(i=0; i<NUM_SIGNS; i++)
			{
				String filenametext="C:\\Pyscript\\Sign\\Info\\"+makeThreeDigitString(i)+"A.txt";

				String lookup_filename=SIGN_LOOKUP_FILENAME+makeThreeDigitString(i)+".jpg";
				MBFImage tempimage=ImageUtilities.readMBF(new File(lookup_filename));
				
				if (MAKE_NEW_FEATURE_RECORDS)
				{
					card_features.add(engine.findFeatures(tempimage.flatten()));
				}
				else
				{
					Class<Keypoint> featuretype = Keypoint.class;
					card_features.add(MemoryLocalFeatureList.read(new File(filenametext),featuretype));
				}
				matcher.findMatches((List<Keypoint>) card_features.get(i));
				num_matches_found=matcher.getMatches().size();
				System.out.printf("There are %d matches for image %d (%s)\n",num_matches_found,i,SIGN_NAMES[i]);
				
				if(num_matches_found>=MATCHES_THRESHOLD){

					//MBFImage temp_target=original_target.clone();
					current_shape=tempimage.getBounds().transform(fittingModel.getTransform());
					current_polygon=current_shape.asPolygon();
					//temp_target.drawShape(current_shape,1,RGBColour.BLUE);
					//temp_target.drawText(Integer.toString(i), 60, 60, HersheyFont.ASTROLOGY , 40, RGBColour.BLACK);
					//DisplayUtilities.display(temp_target);
					
					shapematchfound=false;
					
					for (int j=0; j<num_shape_groups; j++){
						//System.out.printf("Current Shape (i=%d) is %s\n", i,current_shape.toString());
						double overlap_amount=4;
						group_card_area=sign_areas.get(j);//these are past card areas
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
								System.out.printf("Group %d has new image num %d (%s)\n",j,i,SIGN_NAMES[i]);
								sign_areas.get(j).setCard_matches(num_matches_found);
								sign_areas.get(j).setCard_num(i);
								sign_areas.get(j).setCard_shape(current_shape);
								target.drawShape(current_shape, 1,RGBColour.YELLOW);
								DisplayUtilities.display(target);
							}
							else {
								System.out.printf("Group %d is still image num %d (%s)\n",j,i,SIGN_NAMES[i]);
								DisplayUtilities.display(target);								
							}
						}
					}
					if(!shapematchfound){
						sign_areas.add(new CardArea(current_shape,i,num_matches_found));
						num_shape_groups+=1;
					}		
				}
			}
			Collections.sort(sign_areas);
			
			for (int j=0; j<num_shape_groups; j++){
				Integer temp_signnum;
				temp_signnum=sign_areas.get(j).getCard_num();
				card_nums.add(temp_signnum);
				System.out.printf("%s, \n", SIGN_NAMES[temp_signnum] );
				//System.out.printf("Found image %d (%s)\n", temp_cardnum ,CARD_NAMES[temp_cardnum] );
				target.drawShape(sign_areas.get(j).getCard_shape(), 5,RGBColour.YELLOW);
			}
			DisplayUtilities.display(target);
			
			if (MAKE_NEW_FEATURE_RECORDS){
				for (i=0; i<NUM_SIGNS; i++){
					String filenameASCII="C:\\Pyscript\\Sign\\Info\\"+makeThreeDigitString(i)+"A.txt";
					PrintWriter print_writer;
					try{
						print_writer=new PrintWriter(filenameASCII);
						card_features.get(i).writeASCII(print_writer);
						print_writer.flush();
						print_writer.close();
					}catch(FileNotFoundException e){
						e.getStackTrace();
					}				
				}
				System.out.println("Wrote "+Integer.toString(NUM_SIGNS)+" sign files");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}//end catch
	}//end main
}	

