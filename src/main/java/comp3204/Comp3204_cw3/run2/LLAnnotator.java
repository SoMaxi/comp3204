package comp3204.Comp3204_cw3.run2;
import java.util.ArrayList;
import java.util.List;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.LocalFeatureImpl;
import org.openimaj.feature.local.SpatialLocation;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.BlockSpatialAggregator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator.Mode;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;
import org.openimaj.util.pair.IntFloatPair;

import de.bwaldvogel.liblinear.SolverType;

public class LLAnnotator{

	private LiblinearAnnotator<FImage, String> annotator;
	/*Train method for the linear annotator
	 * begin by declaring the assigner and feature extractor
	 * continue to create the linear annotator and train it with the inbuilt train method
	 */
	public void train(VFSGroupDataset<FImage> trainingSet) {
		//taking 15 images of each class
		GroupedRandomSplitter<String, FImage> rand = new GroupedRandomSplitter<String, FImage>(trainingSet, 15, 0, 0);
		HardAssigner<float[], float[], IntFloatPair> assigner = trainQuantiser(rand.getTrainingDataset());
		FeatureExtractor<DoubleFV, FImage> extractor = new PatchClusterFeatureExtractor(assigner);

        // Create and train a linear classifier.
		annotator = new LiblinearAnnotator<FImage, String>(extractor, Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 1.0, 0.00001);
		annotator.train(trainingSet); 
	}
	//a classification method to be used after training
	public ClassificationResult<String> classify(FImage image) {
		
		return annotator.classify(image);
	}
	//the method to extract pixel patches
	public static List<LocalFeature<SpatialLocation, FloatFV>> extract(FImage image, float gap, float patchSize){
        List<LocalFeature<SpatialLocation, FloatFV>> patchesList = new ArrayList<LocalFeature<SpatialLocation, FloatFV>>();
        //normalising the image
        image.normalise();
        //variables for keeping track of the step position of the element in the vector and sum of the pixels
		int count=0;
		//declaring a vector of the correct size
		int size=(int) (patchSize*patchSize);
		//acquiring image dimensions
		int sizeHorizontal = image.pixels[0].length;
		int sizeVertical=image.pixels.length;
		int internalCount=0;
		//2 for loops going through the image pixels
		for(int i=0;i<sizeHorizontal;i++) 
			for(int j=0;j<sizeVertical;j++) {
				//conditions to be met to extract a patch
				if(internalCount%gap==0 && sizeHorizontal-i>=patchSize && sizeVertical-j>=patchSize) {
					float[] vector=new float[size];
					//extracting the patch
					for(int f=0;i<8;i++)
						for(int z=0;z<8;z++) {
							vector[count]=image.pixels[i+f][j+z];
							count++;
							
							
						}
					//converting the patches for convenience's sake
					SpatialLocation spatial = new SpatialLocation(i, j);
					FloatFV convVector= new FloatFV(vector);
					LocalFeature<SpatialLocation, FloatFV> lf = new LocalFeatureImpl<SpatialLocation, FloatFV>(spatial,convVector);
			        patchesList.add(lf);
			        count=0;
				}
				
			internalCount++;
			}
		
		return patchesList;
		
	}
	//creating a HardAssigner using K-means as per the cw spec
	static HardAssigner<float[], float[], IntFloatPair> trainQuantiser(Dataset<FImage> sample) {
		List<float[]> allkeys = new ArrayList<float[]>();
        //extract patches while setting the interval between them and the size in the extract method
		System.out.println("Beginning extraction");
		for (FImage image : sample) {
			List<LocalFeature<SpatialLocation, FloatFV>> sampleList = extract(image, 4, 8);
			for(LocalFeature<SpatialLocation, FloatFV> local : sampleList){
				allkeys.add(local.getFeatureVector().values);
			}
		}
		System.out.println("Extraction complete"); 

        // performing K-means clustering on the sample of patches
		System.out.println("Clustering"); 
		FloatKMeans km = FloatKMeans.createKDTreeEnsemble(500);
        float[][] data = allkeys.toArray(new float[][]{});  
		FloatCentroidsResult result = km.cluster(data);
		System.out.println("Clustering complete"); 
		return result.defaultHardAssigner();
		
	}
	//a patch feature extractor based on the assigner
	class PatchClusterFeatureExtractor implements FeatureExtractor<DoubleFV, FImage> {
		HardAssigner<float[], float[], IntFloatPair> assigner;

		public PatchClusterFeatureExtractor(HardAssigner<float[], float[], IntFloatPair> assigner) {
			this.assigner = assigner;
		}
		//extract the features of the image in accordance with the assigner returns a feature vector
		public DoubleFV extractFeature(FImage image) {
			BagOfVisualWords<float[]> bovw = new BagOfVisualWords<float[]>(assigner);
			BlockSpatialAggregator<float[], SparseIntFV> spatial = new BlockSpatialAggregator<float[], SparseIntFV>(bovw, 2, 2);
			return spatial.aggregate(extract(image, 4, 8), image.getBounds()).normaliseFV();
		}
	}
}
