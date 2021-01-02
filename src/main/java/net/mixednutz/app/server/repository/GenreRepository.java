package net.mixednutz.app.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.post.series.Genre;

@Repository
public interface GenreRepository extends CrudRepository<Genre, String> {

	Iterable<Genre> findByOrderByDisplayNameAsc();
	
}
