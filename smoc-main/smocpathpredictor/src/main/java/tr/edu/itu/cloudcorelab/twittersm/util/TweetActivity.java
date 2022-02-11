package tr.edu.itu.cloudcorelab.twittersm.util;

public class TweetActivity
{
	public final User subject;
	public final User associated;
	public final long timestamp;
	public final Type type;
	
	public TweetActivity(User subject, /* Nullable */ User associated, long timestamp, Type type)
	{
		this.subject = subject;
		this.associated = associated;
		this.timestamp = timestamp;
		this.type = type;
	}
	
	@Override
	public String toString()
	{
		return String.format("{%d %d %d %s}", subject.id, associated.id, timestamp, type);
	}
	
	public static enum Type
	{
		RT, // retweet
		RE, // reply
		MT  // mention
	}
}
