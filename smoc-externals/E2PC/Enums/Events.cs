namespace ReplicatedSM.Enums
{
    public enum Event
    {
        // Trivial redundant but NOT unnecessary events
        // Each individual trivial needs to be different because of the validation controls!
        TRIVIAL1,
        TRIVIAL2,
        TRIVIAL3,
        TRIVIAL4,
        TRIVIAL5,
        TRIVIAL6,
        TRIVIAL7,
        TRIVIAL8,
        TRIVIAL9,
        TRIVIAL10,
        TRIVIAL11,
        TRIVIAL12,
        TRIVIAL13,
        TRIVIAL14,
        TRIVIAL15,
        TRIVIAL_W,
        TRIVIAL_R,
        // Reads data from solid storage to local cache
        READ,
        // Checks the priority of other state machines
        CHECK,
        // All deposit-withdraw actions are combined to write operation
        WRITE,
        // Finish is used to wrap up a job so client can send the next stream
        FINISH,
        // Fail resets the state machine
        FAIL,
        // Other useful events between machines...
        COMMIT,
        NO_WRITE,
        DONE,
        // Client Confirmations
        GO_ON,
        // Server Confirmations
        WRITE_OK,
        WRITE_FAIL,
        WRITE_SUCCESS,
        CHECK_FAIL,
        CHECK_SAFE,
        FOREIGN_CHECK,
        // Other events for locks
        CONDITION_CHECK,
        NONE,
        GET_WRITING,
        FIRST_PHASE_2PC,
    }
}