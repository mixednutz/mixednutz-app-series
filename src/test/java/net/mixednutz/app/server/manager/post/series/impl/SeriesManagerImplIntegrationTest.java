package net.mixednutz.app.server.manager.post.series.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import net.mixednutz.app.server.entity.post.series.Genre;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.post.series.SeriesViewManager;
import net.mixednutz.app.server.repository.GenreRepository;
import net.mixednutz.app.server.repository.SeriesRepository;

@RunWith(SpringRunner.class)
@Import({SeriesManagerImpl.class})
@DataJpaTest
public class SeriesManagerImplIntegrationTest {
		
	@PersistenceContext
	private EntityManager em;
	
	@Autowired 
	SeriesRepository seriesRepository;
	@Autowired 
	GenreRepository genreRepository;
	
	@MockBean
	ApiManager apiManager;
	@MockBean
	SeriesViewManager seriesViewManager;
	
	@Transactional
	@Test
	public void test() {
		//Setup Genre
		{
			Genre genre = new Genre();
			genre.setId("action");
			genreRepository.save(genre);
		}
		{
			Genre genre = new Genre();
			genre.setId("adventure");
			genreRepository.save(genre);
		}
		{
			Genre genre = new Genre();
			genre.setId("mystery");
			genreRepository.save(genre);
		}
		{
			Genre genre = new Genre();
			genre.setId("time-travel");
			genreRepository.save(genre);
		}
		{
			Genre genre = new Genre();
			genre.setId("romance");
			genreRepository.save(genre);
		}
		
		Series series = new Series();
		series.setTitle("Title");
		series.setDescription("Description");
		series.setGenre(genreRepository.findById("time-travel").get());
		series.setAdditionalGenres(new HashSet<>());
		series.getAdditionalGenres().add(genreRepository.findById("action").get());
		
		Series savedSeries = seriesRepository.save(series);
		
		//Force Transaction
		em.flush();
		em.clear();
		
		savedSeries = seriesRepository.findById(1L).get();
		assertEquals(1, savedSeries.getAdditionalGenres().size());
		
		//TEST ADDING NEW GENRE
		List<Genre> additionalGenresToSave = new ArrayList<>();
		additionalGenresToSave.add(genreRepository.findById("action").get());
		additionalGenresToSave.add(genreRepository.findById("romance").get());
		savedSeries.getAdditionalGenres().clear();
		savedSeries.getAdditionalGenres().addAll(additionalGenresToSave);
		
		savedSeries = seriesRepository.save(savedSeries);
		
		//Force Transaction
		em.flush();
		em.clear();
		
		savedSeries = seriesRepository.findById(1L).get();
		assertEquals(2, savedSeries.getAdditionalGenres().size());
		
		//TEST REMOVING GENRE
		additionalGenresToSave = new ArrayList<>();
		additionalGenresToSave.add(genreRepository.findById("mystery").get());
		additionalGenresToSave.add(genreRepository.findById("romance").get());
		savedSeries.getAdditionalGenres().clear();
		savedSeries.getAdditionalGenres().addAll(additionalGenresToSave);
		
		//Force Transaction
		em.flush();
		em.clear();
		
		savedSeries = seriesRepository.findById(1L).get();
		assertEquals(2, savedSeries.getAdditionalGenres().size());
	}

}
