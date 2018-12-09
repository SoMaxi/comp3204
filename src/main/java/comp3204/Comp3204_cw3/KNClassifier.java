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
import java.util.Map.Entry;
import java.util.Vector;

import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class KNClassifier {
	HashMap<String, List<Vector<Float>>> vectorList;

	public KNClassifier() {

	}

	public void test(VFSListDataset<FImage> testImages) throws InterruptedException {

		List<Vector<Float>> testVectors  = configure(testImages);
		List<Result> results = new ArrayList<Result>();
		List<List<Result>> resultsList = new ArrayList<List<Result>>();

		for(Vector<Float> b : testVectors ) {
			int count= 0;
			for(String label : vectorList.keySet()) {
				for(Vector<Float> v : vectorList.get(label)) {
					float distance = 0;
					for(int i = 0; i<b.size(); i++) {
						distance += Math.pow(b.get(i)-v.get(i), 2);
					}
					distance = (float) Math.sqrt(distance);
					results.add(new Result(label, count, distance));
				}
			}
			resultsList.add(results);
			count++;
		}

		System.out.println(resultsList.size());
		for(List<Result> l : resultsList) {
			Collections.sort(l, new ResultSorter());
		}

		for(List<Result> l : resultsList) {
			for(int i = 0; i < l.size(); i++) {
				DisplayUtilities.display(l.get(i).getLabel(), testImages.get(l.get(i).count));
				System.out.println(l.get(i).getLabel() + "    " + l.get(i).getDistance());
				Thread.sleep(15000);
			}
		}
	}

	public void train(VFSGroupDataset<FImage> groupedImages) {
		vectorList = configure(groupedImages);
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



	public List<Vector<Float>> configure(VFSListDataset<FImage> images){

		List<Vector<Float>> newVectorList = new ArrayList<Vector<Float>>();


		for(FImage img : images) {
			if(img.getHeight()>img.getWidth()) {
				img = img.extractCenter(img.getWidth(), img.getWidth());						
			}else if(img.getHeight()<img.getWidth()) {
				img = img.extractCenter(img.getHeight(), img.getHeight());
			}
			img.processInplace(new ResizeProcessor(16, 16, true));

			Vector<Float> vector = flatten(img);

			newVectorList.add(vector);
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

		return vector;
	}

	class ResultSorter implements Comparator<Result>{

		@Override
		public int compare(Result r1, Result r2) {

			return ((int)(r1.getDistance()) - (int)(r2.getDistance()));


		}

	}



	class Result{
		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public float getDistance() {
			return distance;
		}

		public void setDistance(float distance) {
			this.distance = distance;
		}

		String label;
		int count;
		float distance;

		public Result(String label, int count, float distance) {

			this.label = label;
			this.count = count;
			this.distance = distance;

		}




	}


}


