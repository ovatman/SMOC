package tr.edu.itu.cloudcorelab.twittersm.util;

import java.util.Collection;
import java.util.LinkedHashSet;

public class User
{
	public final int id;
	public final Collection<TweetActivity> timeline;
	public final Collection<User> following;
	public final Collection<User> followers;
	
	public User()
	{
		this(-1);
	}
	
	public User(int id)
	{
		this.id = id;
		timeline = new LinkedHashSet<>();
		following = new LinkedHashSet<>();
		followers = new LinkedHashSet<>();
		
		following.add(this);
		followers.add(this);
	}
	
	@Override
	public String toString()
	{
		return "" + id; // TODO details?
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(!(obj instanceof User))
			return false;
		User other = (User) obj;
		if(id != other.id)
			return false;
		return true;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
}
