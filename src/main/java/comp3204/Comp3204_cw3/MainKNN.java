package comp3204.Comp3204_cw3;


import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import java.io.IOException;


public class MainKNN 
{
	
	//THe amount of K-nearest neighbours
	private final static int K_Neighbours = 20;
    public static void main( String[] args ) throws InterruptedException, IOException
    {
    	//loading the training images in a VFSGroupDataset in order to later get all of the categories
    	VFSGroupDataset<FImage> groupedTrainImages = new VFSGroupDataset<FImage>("/home/somax/Desktop/University/comp3204/training", ImageUtilities.FIMAGE_READER);
    	
    	//loading all of the test images in a VFSListDataset because categories here.
    	VFSListDataset<FImage> testImages = new VFSListDataset<FImage>("/home/somax/Desktop/University/comp3204/testing", ImageUtilities.FIMAGE_READER);
    	
    	//instantiating classifier with the given amount of K nearest neighbours to look for
    	KNClassifier kNClassifier = new KNClassifier(K_Neighbours);
    	
    	//train the classifier
        kNClassifier.train(groupedTrainImages);
        
        //test the classifier
        kNClassifier.test(testImages);
    }
}
