package comp3204.Comp3204_cw3.run2;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import comp3204.Comp3204_cw3.run1.Result;
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

			List<Result> sortingList = new ArrayList<Result>();

			for (String pred : classes) {
				sortingList.add(new Result(testImages.getID(counter), pred));
				counter++;
			}

			Collections.sort(sortingList, new Comparator<Result>() {
				//comparing the image names based on their integer parts and then sorting them
				public int compare(Result r1, Result r2) {
					return r1.getIntName() < r2.getIntName() ? -1 
							: r1.getIntName() > r2.getIntName() ? 1 
									: 0;
				}
			});
			
			for(int i = 0; i<sortingList.size(); i++) {
				fw.write(sortingList.get(i).getLabel()+ " " +sortingList.get(i).getBestGuess() + "\n");
			}
			fw.close();
		}
	}
}