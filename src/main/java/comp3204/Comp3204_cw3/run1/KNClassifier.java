package comp3204.Comp3204_cw3.run1;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

import org.openimaj.util.array.ArrayUtils;

public class KNClassifier {
	//variable for the K value
    private int K;

    //TinyImage feature size
	private final int img_size = 16;
	
	
	//Creating lists for feature vectors of both test and training images
    private List<DoubleFV> trainVectors,testVectors;
    
    //Lists for test image names and the category labels from the training set.
	private List<String> categories, testImgNames;

	
	
	//constructor to set up the K value
	public KNClassifier(int k) {
		this.K = k;
	}

	
	public void test(VFSListDataset<FImage> testImages) throws InterruptedException, IOException {
		
		//instantiating the Lists for test dataset
		testImgNames = new ArrayList<String>();
		testVectors = new ArrayList<DoubleFV>();
		
		//Configure class to create 1D normalized vectors with 0 mean.
		Configure config = new Configure();
		
		
		
		
		
		//for each image in test image set create a Feature Vector (Using Configure class) and save its name in a separate list.
		int count = 0;
		for(FImage img : testImages) {
			testVectors.add(config.extractFeature(img));
			testImgNames.add(testImages.getID(count));
			count++;
		}
		
		
		//List of class Result to save distance to each training image and the training image's category.
		List<Result> results;
		
		//Map which maps the name of the testing picture to the results against every test image.
		Map<String, List<Result>> resultsList = new HashMap<String, List<Result>>();
		
		
		
		
		//For every testing image vector in testVectors
		int testCount = 0;
		for(DoubleFV test : testVectors ) {
			count = 0;
			results = new ArrayList<Result>();
			//Calculate distance to every training image in trainVectors
			for(DoubleFV trained : trainVectors) {
							double distance = 0;
							
							//Euclidean Distance calculation for every point in test and training vectors
							for(int i = 0; i<test.length(); i++) {
								distance += Math.pow(test.get(i)-trained.get(i), 2);
							}
							distance =  Math.sqrt(distance);
							//Euclidean Distance END
							
							//add every results to a List<Result> saving training image's category and distanc towards it
							results.add(new Result(categories.get(count), distance));
							count++;
			}
			
			
			//Sorting all of the results for every test vector in testVectors using a self-defined comparator
			Collections.sort(results, new Comparator<Result>() {
				
				//comparing the distances to every test image and sorting them based on that
				public int compare(Result r1, Result r2) {

					return r1.getDistance() < r2.getDistance() ? -1 
						     : r1.getDistance() > r2.getDistance() ? 1 
						     : 0;


				}
			});
			
			//Mapping name of testing image to its Result
			resultsList.put(testImgNames.get(testCount), results);
			testCount++;
			
		}
		
		//Opening the bufferedWriter to write to file named run1.txt
			
			
			
			List<Result> sortingList = new ArrayList<Result>();
			//For every entry in the map, i.e. for every Tested Image name. 
			for(Entry<String, List<Result>> l : resultsList.entrySet()) {
				//save the name of the image to the list and pass the result list into a decideBest() method to classify the category
				sortingList.add(new Result(l.getKey(),decideBest(l.getValue())));
			}
			
			Collections.sort(sortingList, new Comparator<Result>() {
				//comparing the image names based on their integer parts and then sorting them
				public int compare(Result r1, Result r2) {
					return r1.getIntName() < r2.getIntName() ? -1 
						     : r1.getIntName() > r2.getIntName() ? 1 
						     : 0;
				}
				
			});
			BufferedWriter fw = new BufferedWriter(new FileWriter("run1.txt"));
			for(int i=0; i < sortingList.size(); i++) {
				
				//writing the image name and the best guess to the file
				fw.write(sortingList.get(i).getLabel() + "   " + sortingList.get(i).getBestGuess());
				fw.newLine();
			}
			
		//Closing the writing stream
		fw.close();
	}

	public void train(VFSGroupDataset<FImage> groupedImages) {
		//Instantiating the lists for train dataset
		trainVectors = new ArrayList<DoubleFV>();
		categories = new ArrayList<String>();
	
		//Configure class to create 1D normalized vectors with 0 mean.
		Configure config = new Configure();
		
		
		//for every entry in the VFSGroupDataset
		for(Entry<String, VFSListDataset<FImage>>  e : groupedImages.entrySet()) {
			
			//for every image in the category, i.e. e.getKey(), save the category name to categories and create 1D vector, saving it to a list
			for(FImage img : groupedImages.get(e.getKey())) {
				categories.add(e.getKey());
				trainVectors.add(config.extractFeature(img));
			}
		}		
	}
	
	
	
	
	public String decideBest(List<Result> list) {
		
		//Map for storing categories and amounts of time they occur
		Map<String, Integer> countedCategories = new HashMap<String,Integer>();
		
		
		//from 0 to K, create a map with first K values and the amount of time they occur
		for(int i = 0; i<K; i++) {
			
			//get the value at the given key, if no value at the key return null
			Integer count = countedCategories.get(list.get(i).getLabel());
			
			//put a value on the given key, if the count equals null, this means that the value occurs the first time, so 1 is put into the map. 
			//Otherwise just increases the value already there by 1
		    countedCategories.put(list.get(i).getLabel(), count == null ? 1 : count +1 );
		}
		
		//create a to store the category and the amount of time it occurs
		Entry<String, Integer> nameSearch = null;
		
		//for every entry in the counted categories
		for( Entry<String,Integer> entry : countedCategories.entrySet()) {
			
			//if nameSearch is null, i.e. the for loop is in its first run, then set it to the current entry
			//or if the number of times the name occurs in the current entry, i.e. entry.getValue(), is bigger than the previous entry.getValue,
			//then make nameSearch equal to currently iterated entry
			if(nameSearch == null || entry.getValue()> nameSearch.getValue()){
				nameSearch = entry;
			}
			//goes through the whole map of countedCategories and selects the one with biggest value
			
		}
		//returns the name of the most occuring neighbour, thus decides on the category of an image
		return nameSearch.getKey();
	}

	

	//custom comparator class, sorts the list in ascending order



	
	//a custom class to store how far away is each training image from the test image. also saves the name of the training image (i.e. class/category)
	class Result{
		public String getBestGuess() {
			return bestGuess;
		}

		public void setBestGuess(String bestGuess) {
			this.bestGuess = bestGuess;
		}

		public int getIntName() {
			return intName;
		}

		public void setIntName(int intName) {
			this.intName = intName;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public double getDistance() {
			return distance;
		}

		public void setDistance(float distance) {
			this.distance = distance;
		}

		String label;
		double distance;
		String bestGuess;
		int intName;
		public Result(String label, double d) {

			this.label = label;
			this.distance = d;

		}
		
		
		public Result(String label, String bestGuess) {

			
			//saving the integer part of the image name
			this.intName = Integer.parseInt(label.substring(0, label.length() - 4));
			this.label = label;
			this.bestGuess = bestGuess;

		}
	}

	
	
	//configure class is a feature extractor which extracts a DoubleFV feature vector from a given image,
	// this returns a 16x16 tinyimage feature
	class Configure implements FeatureExtractor<DoubleFV,FImage>{
		
	
		public DoubleFV extractFeature(FImage img) {
			
			//crops the image to its smallest side being both sides
			if(img.getHeight()>img.getWidth()) {
				img = img.extractCenter(img.getWidth(), img.getWidth());						
			}else if(img.getHeight()<img.getWidth()) {
				img = img.extractCenter(img.getHeight(), img.getHeight());
			}
			
			//resizes the image to img_size (16)
			img = img.processInplace(new ResizeProcessor(img_size, img_size));
			
			
			//create a size variable to define the size of the vector
			int size = (ArrayUtils.reshape(ArrayUtils.convertToDouble(img.pixels))).length;	
			
			float sum = 0;
			//for every float value add it to the sum
			for(float f: ArrayUtils.reshape(img.pixels)) {
				sum += f;
			}
			//work out the mean of the vector
			float mean =sum/size;
			
			
			//zero mean the vector
			float[] v = new float[size];
			int count = 0;
			for(float f: ArrayUtils.reshape(img.pixels)) {
				f = f-mean;
				v[count] = f;
				count++;
			}
			//create a DoubleFV variable using the array of floats
			DoubleFV vector = new DoubleFV(ArrayUtils.convertToDouble(v));
			
			//make the vector a unit vector.
			vector.normaliseFV();
			
			return vector;
		}
		
	}

}

