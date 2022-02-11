package tr.edu.itu.cloudcorelab.geocoordinator.models;

import java.nio.ByteBuffer;

import tr.edu.itu.cloudcorelab.geocoordinator.queue.IElement;

public class RoomReservation  implements IElement{
    public int room_id;

    @Override
    public byte[] toByte() {
        ByteBuffer b = ByteBuffer.allocate(4);
        // b.order(ByteOrder.BIG_ENDIAN); // optional, the initial order of a byte
        // buffer is always BIG_ENDIAN.
        b.putInt(this.room_id);
        return b.array();
    }

    @Override
    public void fromByte(byte[] take) {
        ByteBuffer wrapped = ByteBuffer.wrap(take); // big-endian by default
        this.room_id = wrapped.getInt(); // 1
    }
}
