package net.mixednutz.app.server.entity.post.series;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.NewCommentFactory;
import net.mixednutz.app.server.entity.post.NewPostFactory;
import net.mixednutz.app.server.manager.post.series.impl.SeriesSettingsManager;

@Component
public class SeriesFactory implements NewPostFactory<Series>, NewCommentFactory<SeriesReview> {

	public static final String MODEL_ATTRIBUTE = "newseries";
	public static final String MODEL_ATTRIBUTE_COMMENT = "newComment";
	
	@Autowired
	private SeriesSettingsManager seriesSettingsManager;
		
	@Override
	public Series newPostForm(Model model, User owner) {
		final Series series = new Series();
		model.addAttribute(MODEL_ATTRIBUTE, series);
		series.setOwnerId(owner!=null?owner.getUserId():null);
		
		//Reference data:
		addNewPostReferenceData(model);
		return series;
	}
	
	public void addNewPostReferenceData(Model model) {
		model.addAttribute("genres",seriesSettingsManager.genres());
		model.addAttribute("ratings",seriesSettingsManager.ratings());
	}
	
	public SeriesReview newCommentForm(Model model) {
		final SeriesReview seriesReview = new SeriesReview();
		model.addAttribute(MODEL_ATTRIBUTE_COMMENT, seriesReview);
		return seriesReview;
	}

}
