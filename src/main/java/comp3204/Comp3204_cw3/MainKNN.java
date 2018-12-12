package comp3204.Comp3204_cw3;


import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import java.io.IOException;


public class MainKNN 
{
	private final static int K_Neighbours = 20;
    public static void main( String[] args ) throws InterruptedException, IOException
    {
    	VFSGroupDataset<FImage> groupedTrainImages = new VFSGroupDataset<FImage>("/home/somax/Desktop/University/comp3204/training", ImageUtilities.FIMAGE_READER);
    	
    	VFSListDataset<FImage> testImages = new VFSListDataset<FImage>("/home/somax/Desktop/University/comp3204/testing", ImageUtilities.FIMAGE_READER);
    	
        KNClassifier kNClassifier = new KNClassifier(K_Neighbours);
        
        kNClassifier.train(groupedTrainImages);
        kNClassifier.test(testImages);
    }
}
