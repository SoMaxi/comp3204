package comp3204.Comp3204_cw3;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class KNClassifier {
	int k;
	HashMap<String, List<Vector<Float>>> vectorList;

	public KNClassifier(int k) {
		this.k = k;
	}

	public void test(VFSListDataset<FImage> testImages) throws InterruptedException, IOException {

		List<Vectors> testVectors  = configure(testImages);
		List<Result> results = new ArrayList<Result>();
		List<List<Result>> resultsList = new ArrayList<List<Result>>();
		for(Vectors b : testVectors ) {
			for(String label : vectorList.keySet()) {
				for(Vector<Float> v : vectorList.get(label)) {
					float distance = 0;
					for(int i = 0; i<b.getVector().size(); i++) {
						distance += Math.pow(b.getVector().get(i)-v.get(i), 2);
					}
					distance = (float) Math.sqrt(distance);
					results.add(new Result(label, b.getImgName(), distance));
				}
			}
			resultsList.add(results);
		}
		
		
		FileWriter fw = null;
		for(List<Result> l : resultsList) {
			Collections.sort(l, new ResultSorter());
			fw = new FileWriter("run1.txt");
			fw.write(l.get(0).getName() + "   " + decideBest(l));
			System.out.println(l.get(0).getName() + "   " + decideBest(l));
		}
		fw.close();
	}

	public void train(VFSGroupDataset<FImage> groupedImages) {
		vectorList = configure(groupedImages);
	}
	
	
	
	
	public String decideBest(List<Result> list) {
		Map<String, Integer> names = new HashMap<String,Integer>();
		
		for(int i = 0; i<k; i++) {
			Integer count = names.get(list.get(i).getLabel());;
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

	
	
	
	public HashMap<String, List<Vector<Float>>> configure(VFSGroupDataset<FImage> images){

		HashMap<String, List<Vector<Float>>> newVectorList = new HashMap<String, List<Vector<Float>>>();

		for(String label : images.keySet()) {
			List<Vector<Float>> newList = new ArrayList<Vector<Float>>();
			for(FImage img : images.get(label)) {
				if(img.getHeight()>img.getWidth()) {
					img = img.extractCenter(img.getWidth(), img.getWidth());						
				}else if(img.getHeight()<img.getWidth()) {
					img = img.extractCenter(img.getHeight(), img.getHeight());
				}
				img.processInplace(new ResizeProcessor(16, 16, true));

				Vector<Float> vector = flatten(img);

				newList.add(vector);
			}

			newVectorList.put(label, newList);

		}


		return newVectorList;


	}



	public List<Vectors> configure(VFSListDataset<FImage> images){

		List<Vectors> newVectorList = new ArrayList<Vectors>();


		for(FImage img : images) {
			int index = images.indexOf(img);
			if(img.getHeight()>img.getWidth()) {
				img = img.extractCenter(img.getWidth(), img.getWidth());						
			}else if(img.getHeight()<img.getWidth()) {
				img = img.extractCenter(img.getHeight(), img.getHeight());
			}
			img.processInplace(new ResizeProcessor(16, 16, true));

			Vector<Float> vector = flatten(img);
			
			newVectorList.add(new Vectors(vector, images.getID(index)));
		}

		return newVectorList;


	}

	public Vector<Float> flatten(FImage image) {

		Vector<Float> vector = new Vector<Float>();

		for(float[] p : image.pixels) {

			for(float f : p) {

				vector.add(f);
			}
		}
		float sum = 0;
		for(float f: vector) {
			
			sum += f;
			
		}
		
		float mean =sum/vector.size();
		
		//zero mean the vector
		for(float f: vector) {
			f = f-mean;
		}
		
		//make it a unit vector
		sum = 0;
		for(float f: vector) {
			sum+= Math.pow(f, 2);
		}
		
		for(float f: vector) {
			f = (float) (f/Math.sqrt(sum));
		}
		
		

		return vector;
	}

	class ResultSorter implements Comparator<Result>{

		@Override
		public int compare(Result r1, Result r2) {

			return r1.getDistance() < r2.getDistance() ? -1 
				     : r1.getDistance() > r2.getDistance() ? 1 
				     : 0;


		}

	}
	
	class Vectors{
		
		public Vector<Float> getVector() {
			return vector;
		}

		public void setVector(Vector<Float> vector) {
			this.vector = vector;
		}

		public String getImgName() {
			return imgName;
		}

		public void setImgName(String imgName) {
			this.imgName = imgName;
		}

		Vector<Float> vector;
		String imgName;

		public Vectors(Vector<Float> vector, String id) {
			this.vector =vector;
			this.imgName = id;
		}
		
	}



	class Result{
		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public float getDistance() {
			return distance;
		}

		public void setDistance(float distance) {
			this.distance = distance;
		}

		String label;
		String name;
		float distance;

		public Result(String label, String name, float distance) {

			this.label = label;
			this.name = name;
			this.distance = distance;

		}




	}


}


