package comp3204.Comp3204_cw3;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class KNClassifier {
		
	
	public KNClassifier() {
		
	}
	
	public void train(List<FImage> images) {
		 resize(images);
		
		
	}
	
	public List<FImage> resize(List<FImage> images){
		
		List<FImage> newImages = new ArrayList<FImage>();
		for(FImage img : images) {
			
			if(img.getHeight()>img.getWidth()) {
				img = img.extractCenter(img.getWidth(), img.getWidth());						
			}else if(img.getHeight()>img.getWidth()) {
				img = img.extractCenter(img.getWidth(), img.getWidth());
			}
			
			img.processInPlace(new ResizeProcessor());
			
		}
		
		
		
		return newImages;
		
		
	}
	

}
