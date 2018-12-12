package comp3204.Comp3204_cw3;
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
import java.util.Vector;

import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

import org.openimaj.util.array.ArrayUtils;

public class KNClassifier {
    private int K;
	HashMap<String, List<Vector<Float>>> vectorList;
	private final int img_size = 16;
    private List<DoubleFV> vectors;
	private List<String> labels;
	private List<DoubleFV> testVectors;
	private List<String> testImgNames;
	VFSGroupDataset<FImage> groupedImages;
	public KNClassifier(int k) {
		this.K = k;
	}

	public void test(VFSListDataset<FImage> testImages) throws InterruptedException, IOException {
		testImgNames = new ArrayList<String>();
		testVectors = new ArrayList<DoubleFV>();
		Configure config = new Configure();
		int count = 0;
		for(FImage img : testImages) {
			testVectors.add(config.extractFeature(img));
			testImgNames.add(testImages.getID(count));
			count++;
		}
		
		
		List<Result> results;
		Map<String, List<Result>> resultsList = new HashMap<String, List<Result>>();
		int testCount = 0;
		
		
		
		
		for(DoubleFV test : testVectors ) {
			count = 0;
			results = new ArrayList<Result>();
			for(DoubleFV trained : vectors) {
							double distance = 0;
							for(int i = 0; i<test.length(); i++) {
								distance += Math.pow(test.get(i)-trained.get(i), 2);
							}
							distance =  Math.sqrt(distance);
							results.add(new Result(labels.get(count), distance));
							count++;
			}
			
			Collections.sort(results, new ResultSorter());
			resultsList.put(testImgNames.get(testCount), results);
			testCount++;
			
		}
		
		
	BufferedWriter fw = new BufferedWriter(new FileWriter("run1.txt"));
			for(Entry<String, List<Result>> l : resultsList.entrySet()) {
				fw.write(l.getKey() + "   " + decideBest(l.getValue()));
				fw.newLine();
			
			}

		fw.close();
	}

	public void train(VFSGroupDataset<FImage> groupedImages) {
		vectors = new ArrayList<DoubleFV>();
		labels = new ArrayList<String>();
		this.groupedImages = groupedImages;
		Configure config = new Configure();
		
		for(String label : groupedImages.keySet()) {
			
			for(FImage img : groupedImages.get(label)) {
				labels.add(label);
				vectors.add(config.extractFeature(img));
			}
		}		
	}
	
	
	
	
	public String decideBest(List<Result> list) {
		Map<String, Integer> names = new HashMap<String,Integer>();
		
		
		
		
		for(int i = 0; i<K; i++) {
			Integer count = names.get(list.get(i).getLabel());
		    names.put(list.get(i).getLabel(), count == null ? 1 : count +1 );
		}
				
		Entry<String, Integer> nameSearch = null;
		for( Entry<String,Integer> entry : names.entrySet()) {		
			if(nameSearch == null || entry.getValue()> nameSearch.getValue()){
				nameSearch = entry;
			}
		}
		return nameSearch.getKey();
	}

	

	class ResultSorter implements Comparator<Result>{

		@Override
		public int compare(Result r1, Result r2) {

			return r1.getDistance() < r2.getDistance() ? -1 
				     : r1.getDistance() > r2.getDistance() ? 1 
				     : 0;


		}

	}


	class Result{
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

		public Result(String label, double d) {

			this.label = label;
			this.distance = d;

		}
	}

	
	class Configure implements FeatureExtractor<DoubleFV,FImage>{
		
	
		public DoubleFV extractFeature(FImage img) {
			
			if(img.getHeight()>img.getWidth()) {
				img = img.extractCenter(img.getWidth(), img.getWidth());						
			}else if(img.getHeight()<img.getWidth()) {
				img = img.extractCenter(img.getHeight(), img.getHeight());
			}
			
			img = img.processInplace(new ResizeProcessor(img_size, img_size));
			
			
			DoubleFV vector = new DoubleFV(ArrayUtils.reshape(ArrayUtils.convertToDouble(img.pixels)));
			
			
			float sum = 0;
			for(float f: ArrayUtils.reshape(img.pixels)) {
				sum += f;
			}
			float mean =sum/vector.length();
			
			//zero mean the vector
			float[] v = new float[(vector.length())];
			int count = 0;
			for(float f: ArrayUtils.reshape(img.pixels)) {
				f = f-mean;
				v[count] = f;
				count++;
			}
			vector = new DoubleFV(ArrayUtils.convertToDouble(v));
			vector.normaliseFV();
			
			return vector;
		}
		
	}

}


