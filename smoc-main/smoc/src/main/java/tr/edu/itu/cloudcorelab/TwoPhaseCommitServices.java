package tr.edu.itu.cloudcorelab.smoc;

import tr.edu.itu.cloudcorelab.smoc.grpc.Tpc.ConnectionRequest;
import tr.edu.itu.cloudcorelab.smoc.grpc.Tpc.ConnectionResponse;
import tr.edu.itu.cloudcorelab.smoc.grpc.Tpc.AllocationRequest;
import tr.edu.itu.cloudcorelab.smoc.grpc.Tpc.AllocationResponse;
import tr.edu.itu.cloudcorelab.smoc.grpc.Tpc.NotificationMessage;
import tr.edu.itu.cloudcorelab.smoc.grpc.Tpc.Empty;
import tr.edu.itu.cloudcorelab.smoc.grpc.tpcGrpc.tpcImplBase;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.locks.ReentrantLock;


public class TwoPhaseCommitServices extends tpcImplBase {
    private static final ReentrantLock reentrantLock = new ReentrantLock();
    private Integer timestamp; // Timestamp of the server
    private enum writeStatus {WRITING, NOT_WRITING};
    private final HashMap<String, AtomicStampedReference<writeStatus>> variableTable; // Map to check the variable status (isReading, isWriting)
    private final HashSet<String> clientMap; // Set to see which clients are connected to the server

    /**
     * Builds TwoPhaseCommitServices object
     */
    public TwoPhaseCommitServices() {
        this.timestamp = 0;
        this.variableTable = new HashMap<>();
        this.clientMap = new HashSet<>();
    }

    /**
     * Updates its timestamp according to the incoming message
     * @param timestamp incoming timestamp
     */
    public void updateTimestamp(Integer timestamp) {
        if (this.timestamp < timestamp) this.timestamp = timestamp + 1;
        else this.timestamp = this.timestamp + 1;
    }

    /**
     * Applies the logic for allocation, basically checks if any of the variables
     * is in-use by another process in the server
     * @param request incoming request
     * @return response, answer of the server on allocation request
     */
    public boolean allocationResponseLogic(AllocationRequest request) {
        boolean response = true; // initially true
        String readVariable;
        String writeVariable;

        /* Checking readFrom Variables */
        int numOfReadVariables = request.getReadFromCount();
        for (int index=0; index<numOfReadVariables; index++) {
            readVariable = request.getReadFrom(index);

            // If the server face with this variable for the first time, add it to the table
            TwoPhaseCommitServices.reentrantLock.lock();
            if (!this.variableTable.containsKey(readVariable)) {
                this.variableTable.put(readVariable, new AtomicStampedReference<>(writeStatus.NOT_WRITING, 1));
                TwoPhaseCommitServices.reentrantLock.unlock();
                continue;
            }
            TwoPhaseCommitServices.reentrantLock.unlock();

            // If the variable is being written, then server can not allocate read operation
            int numOfReaders = this.variableTable.get(readVariable).getStamp();
            if (!this.variableTable.get(readVariable).compareAndSet(writeStatus.NOT_WRITING, writeStatus.NOT_WRITING, numOfReaders, numOfReaders+1)) {
                response = false;
                break;
            }

        }

        /* Checking writeTo Variables */
        int numOfWriteVariables = request.getWriteToCount();
        for (int index=0; index<numOfWriteVariables; index++) {
            writeVariable = request.getWriteTo(index);

            // If the server face with this variable for the first time, add it to the table
            TwoPhaseCommitServices.reentrantLock.lock();
            if (!this.variableTable.containsKey(writeVariable)) {
                this.variableTable.put(writeVariable, new AtomicStampedReference<>(writeStatus.WRITING, 0));
                TwoPhaseCommitServices.reentrantLock.unlock();
                continue;
            }
            TwoPhaseCommitServices.reentrantLock.unlock();

            // If the variable is being read or written, then server can not allocate write operation
            if (!this.variableTable.get(writeVariable).compareAndSet(writeStatus.NOT_WRITING, writeStatus.WRITING, 0, 0)) {
                response = false;
                break;
            }
        }

        return response;
    }

    /**
     * Generates connection response message for client
     * @param response boolean value, server's on greeting the client
     * @return ConnectionResponse message
     */
    public ConnectionResponse generateConnectionResponse(boolean response) {
        return ConnectionResponse
                .newBuilder()
                .setTimestamp(this.timestamp)
                .setResponse(response)
                .build();
    }

    /**
     * Generates allocation response message to send to client
     * @param response boolean value, server's answer on allocation request
     * @return AllocationResponse message
     */
    public AllocationResponse generateAllocationResponse(boolean response) {
        return AllocationResponse
                .newBuilder()
                .setTimestamp(this.timestamp)
                .setResponse(response)
                .build();
    }

    /**
     * Generates empty message (response of notifyingService)
     * @return Empty message
     */
    public Empty generateEmpty() {
        return Empty
                .newBuilder()
                .build();
    }

    /**
     * greetingService, used when a client first connects to a server
     * @param request, connection message includes clientID and timeStamp
     * @param responseObserver, sender of the response
     */
    @Override
    public void greetingService(ConnectionRequest request, StreamObserver<ConnectionResponse> responseObserver) {
        String clientID = request.getClientID();
        Integer timestamp = request.getTimestamp();

        /* Response Logic Of Greeting Service */
        boolean response;

        // If the client is already connected, ignore the request
        if (this.clientMap.contains(clientID)) {
            response = false;
        }

        // Otherwise, say hello
        else {
            this.clientMap.add(clientID);
            response = true;
        }

        /* Generating And Sending The Response */
        this.updateTimestamp(timestamp);
        ConnectionResponse connectionResponse = this.generateConnectionResponse(response);
        responseObserver.onNext(connectionResponse);
        responseObserver.onCompleted();
    }

    /**
     * allocationService, used when a client wants to allocate a process time from the server
     * @param request, allocation message includes clientID, timeStamp, readFrom and writeTo
     * @param responseObserver sender of the response
     */
    @Override
    public void allocationService(AllocationRequest request, StreamObserver<AllocationResponse> responseObserver) {
        String clientID = request.getClientID();
        Integer timestamp = request.getTimestamp();

        /* Response Logic Of Allocation Service */
        boolean response = allocationResponseLogic(request);

        /* Generating And Sending The Response */
        this.updateTimestamp(timestamp);
        AllocationResponse allocationResponse = this.generateAllocationResponse(response);
        responseObserver.onNext(allocationResponse);
        responseObserver.onCompleted();
    }

    /**
     * notifyingService, used when a client done with its process and release the allocation.
     * @param request, notifying message includes clientID, timeStamp, readFrom and writeTo
     * @param responseObserver, sender of the response
     */
    @Override
    public void notifyingService(NotificationMessage request, StreamObserver<Empty> responseObserver) {
        String clientID = request.getClientID();
        Integer timestamp = request.getTimestamp();

        /* Decrementing The Number Of Read Operations */
        for (String readVariable : request.getReadFromList()) {
            int numOfReaders = this.variableTable.get(readVariable).getStamp();
            while (!this.variableTable.get(readVariable).compareAndSet(writeStatus.NOT_WRITING, writeStatus.NOT_WRITING, numOfReaders, numOfReaders - 1)) {
                numOfReaders = this.variableTable.get(readVariable).getStamp();
            }
        }

        /* Clearing The Flag For Write Operation */
        for (String writeVariable : request.getWriteToList()) {
            if (!this.variableTable.get(writeVariable).compareAndSet(writeStatus.WRITING, writeStatus.NOT_WRITING, 0, 0)) {
                System.out.println("TWO WRITERS AT THE SAME TIME");
            }
        }

        /* Generating And Sending The Response */
        this.updateTimestamp(timestamp);
        Empty empty = this.generateEmpty();
        responseObserver.onNext(empty);
        responseObserver.onCompleted();
    }
}
