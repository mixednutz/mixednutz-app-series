package net.mixednutz.app.server.controller.api;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import net.mixednutz.api.core.model.ApiList;
import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Genre;
import net.mixednutz.app.server.repository.GenreRepository;

@Controller
@RequestMapping({"/internal/genre"})
public class ApiGenreController {
	
	@Autowired
	private GenreRepository genreRepository;
	
	@RequestMapping(value="", method = RequestMethod.GET)
	public @ResponseBody GenreList getGenres() {
		GenreList list = new GenreList();
		for (Genre genre: genreRepository.findByOrderByDisplayNameAsc()) {
			list.add(genre);
		}
		return list;
	}
	
	@RolesAllowed("ROLE_ADMIN")
	@RequestMapping(value="", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody Genre addGenres(
			@RequestBody Genre genre, 
			@AuthenticationPrincipal final User currentUser) {
		return genreRepository.save(genre);
	}
	
	@RolesAllowed("ROLE_ADMIN")
	@RequestMapping(value="/{id}", method = RequestMethod.PUT)
	public @ResponseBody Genre editgenre(
			@PathVariable String id,
			@RequestBody Genre genre, 
			@AuthenticationPrincipal final User currentUser) {
		Genre existing = genreRepository.findById(id)
			.orElseThrow(()->{
				return new ResourceNotFoundException();
			});
		
		existing.setId(existing.getId());
		existing.setDisplayName(genre.getDisplayName());
		existing.setDescription(genre.getDescription());
		return genreRepository.save(existing);
	}
	
	public static class GenreList extends ApiList<Genre> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2314764021162994219L;

		public GenreList() {
			super("genres");
		}
		
	}

}
