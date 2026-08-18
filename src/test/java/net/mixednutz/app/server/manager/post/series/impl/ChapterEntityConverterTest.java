package net.mixednutz.app.server.manager.post.series.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.api.model.IAction;
import net.mixednutz.api.model.IImage;
import net.mixednutz.api.model.IUser;
import net.mixednutz.api.model.IUserProfile;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.manager.ApiManager;

public class ChapterEntityConverterTest {
	
	private static final String BASEURL = "https://mixednutz.net";
	
	ApiManager apiManager = mock(ApiManager.class);
	
	ChapterEntityConverter converter = new ChapterEntityConverter();
	{
		converter.setNetworkInfo(networkInfo());
		converter.setApiManager(apiManager);
	}
	
	
	private NetworkInfo networkInfo() {
		NetworkInfo networkInfo = new NetworkInfo();
		networkInfo.setBaseUrl(BASEURL);
		return networkInfo;
	}
	
	@Test
	public void canConvert() {
		Chapter chapter = new Chapter();
		assertTrue(converter.canConvert(chapter.getClass()));
	}
	
	@Test
	public void toTimelineElement() {
		when(apiManager.toUser(any(), anyString())).thenAnswer(inv->{
			User user = inv.getArgument(0, User.class);
			String basePath = inv.getArgument(1, String.class);
			return new UserWrapper(user, basePath);
		});
		
		User viewer = new User();
		User author = new User();
		author.setUserId(100L);
		author.setUsername("admin");
		User coauthor1 = new User();
		coauthor1.setUserId(101L);
		coauthor1.setUsername("guest1");
		User coauthor2 = new User();
		coauthor2.setUserId(102L);
		coauthor2.setUsername("guest2");
		
		Series series = new Series();
		series.setId(1000L);
		series.setTitle("Test Title");
		series.setAuthor(author);
		series.setCoAuthors(Set.of(coauthor1, coauthor2));
		Chapter chapter = new Chapter();
		chapter.setId(2000L);
		chapter.setSeries(series);
		chapter.setAuthor(author);
		
				
		InternalTimelineElement api = new InternalTimelineElement();
		converter.toTimelineElement(api, chapter, viewer, "http://localhost:8080");
		
		assertEquals("Test Title", api.getTitle());
		
		assertEquals(2, api.getContributedByUser().size());
		assertTrue(api.getContributedByUser().get(0).getUsername().startsWith("guest"));
		assertTrue(api.getContributedByUser().get(0).getUrl().startsWith("http://localhost:8080/guest"));
		assertTrue(api.getContributedByUser().get(1).getUsername().startsWith("guest"));
		assertTrue(api.getContributedByUser().get(1).getUrl().startsWith("http://localhost:8080/guest"));
		assertNotNull(api.getContributedByUser().get(1).getUrl());
		assertEquals("/series/id/1000/chapter/id/2000",api.getLatestSuburi());
		assertEquals("http://localhost:8080/series/id/1000/chapter/id/2000",api.getLatestSuburl());
		
	}
	
	public class UserWrapper implements IUser {

		final private User user;
		private String baseUrl;
		
		public UserWrapper(User user, String baseUrl) {
			super();
			this.user = user;
			this.baseUrl = baseUrl;
		}
			

		@Override
		public String getUsername() {
			return user.getUsername();
		}

		@Override
		public String getDisplayName() {
			return user.getDisplayName();
		}

		@Override
		public IImage getAvatar() {
			return null;
		}

		@Override
		public boolean isPrivate() {
			return user.isPrivate();
		}

		@Override
		public Serializable getProviderId() {
			return user.getProviderId();
		}

		@Override
		public String getUri() {
			return "/"+user.getUsername();
		}

		@Override
		public String getUrl() {
			return baseUrl+getUri();
		}

		@Override
		public List<? extends IAction> getActions() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IUserProfile getProfileData() {
			return null;
		}

		public LocalDate getMemberSince() {
			return user.getMemberSince().toLocalDate();
		}
		
	}

}
