package comp3204.Comp3204_cw3.run2;
import java.io.FileWriter;
import java.io.IOException;

import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

public class LiblinMain {
    public static void main( String[] args ) throws IOException {
    	VFSGroupDataset<FImage> groupedTrainImages = new VFSGroupDataset<FImage>("C:\\Users\\Ivan\\training", ImageUtilities.FIMAGE_READER);
    	
    	VFSListDataset<FImage> testImages = new VFSListDataset<FImage>("C:\\Users\\Ivan\\testing", ImageUtilities.FIMAGE_READER);
    	LLAnnotator run2=new LLAnnotator();
    	run2.train(groupedTrainImages);
    	FileWriter fw = null;
		fw = new FileWriter("run2.txt");
    	int counter=0;
    	for(FImage image: testImages) {
    		ClassificationResult<String> predicted = run2.classify(image);
            String[] classes = predicted.getPredictedClasses().toArray(new String[]{});
            for (String pred : classes) {
                fw.write(testImages.getID(counter)+ " " + pred+"\n");
                counter++;
            }
    	}
    	fw.close();
    }
}