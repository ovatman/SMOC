package tr.edu.itu.cloudcorelab.geocoordinator.queue;

public interface IElement {

	byte[] toByte();

	void fromByte(byte[] take);
}
