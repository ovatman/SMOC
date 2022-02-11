/*
    All local locks are defined as the handshake channels, an agent in a server processes incoming 
    requests one by one.
    -----------------------------------------------------------------
    For LTL usage:

    spin -a  E2PC_final_version.pml
    gcc -DMEMLIM=4096 -O2 -DVECTORSZ=4000 -DXUSAFE -DCOLLAPSE -DSAFETY -w -o pan pan.c
    ./pan -m100000  -c1 -N p1

    For acceptance without LTL and only assertions:

    spin -a  E2PC_final_version.pml
    gcc -DMEMLIM=4096 -O2 -DVECTORSZ=4000 -DXUSAFE -DCOLLAPSE -DSAFETY -DNOCLAIM -w -o pan pan.c
    ./pan -m1000000  -c1

    Fastest
    gcc -DMEMLIM=4096 -O2 -DVECTORSZ=4000 -DXUSAFE -DSAFETY -DBITSTATE -DNOCLAIM -w -o pan pan.c 
*/
/*
    ---------------------------------------------------------------
    Current assertions:
    1)
        If a machine obtained its write flag, it cannot fail
    2)
        If a machine has its writing lock acquired, its current write flag should be true
    3)
        If I am writing I should have my reading and writing flags set to true
        AND
        My state should not be STATE0 (reset)
        AND
        my cached value should be equal to my store value
    3)
        All incoming events from other servers should have a priority larger than 0
    4)
        All events processed in the agent should be valid events
    5)
        If another machine has sent me a COMMIT event, i should not have my own writing lock
    6) 
        After I do a writing operation, all other servers stored values should be equal to mine
*/


// Number of client-server combinations
#define M 5
// Number of messages required for a process to end its life
#define N 50

// Enumerables
mtype:events = { 
    TRIVIAL1,TRIVIAL2,TRIVIAL3,TRIVIAL4,TRIVIAL5,TRIVIAL6,TRIVIAL_R,READ,CHECK,WRITE,FINISH,FAIL,COMMIT,
    DONE,GO_ON,WRITE_OK,WRITE_FAIL,WRITE_SUCCESS,CHECK_FAIL,CHECK_SAFE,CONDITION_CHECK,NONE,GET_WRITING,END
};

mtype:states = { 
    STATE0,STATE1,STATE2,STATE3,STATE4,STATE5,STATE6,
    STATE7,STATE8,STATE9,STATE10,STATE11,STATE12,STATE13
};

// Message structure which is used in channels
typedef Message
{
    int Value;
    int Priority;
    bool IsServer;
    mtype:events Event;
    int Sender;
    int ResponseNo;
}


// Main account variables
int agentAccounts[M];

// Cached account variables
int currentAccounts[M];

// These flags indicate the current status of an Agent
bool readFlags[M];
bool writingFlags[M];

// These indicate the current state of the services waiting for the responses from other machines
bool CheckStatus[M];
int checkResponseCounter[M];
int writeResponseCounter[M];

// This variable will make it possible for all loops to end
int allOperationsDone = 0;

// State Machine states
mtype:states serverStates[M];

// Communication channel for clients
chan chClient[M] = [0] of { int, int, bool, mtype:events, int, int };

// Communication channels between client - server, server - server
chan chServerReaderClient[M] = [0] of { int, int, bool, mtype:events, int, int };
chan chServerReaderServer[M] = [0] of { int, int, bool, mtype:events, int, int };

// Communication channels for service - agent couples
chan chServerServiceForClient[M] = [0] of { int, int, bool, mtype:events, int, int };
chan chServerServiceForServer[M] = [0] of { int, int, bool, mtype:events, int, int };

// Communication channels for agent - service couples
chan chServerAgent[M] = [0] of { int, int, bool, mtype:events, int, int };
chan chServerAgentResponseClient[M] = [0] of { int, int, bool, mtype:events, int, int };
chan chServerAgentResponseServer[M] = [0] of { int, int, bool, mtype:events, int, int };

// Process finishing parameter used by server processes
bool operationComplete[M];

/*
    Initialization
*/
init {
    int i = 0;
    for(i: i .. (M - 1)) {
        // init state variables
        agentAccounts[i] = 0;
        currentAccounts[i] = 0;
        serverStates[i] = STATE0;
        operationComplete[i] = false;
        readFlags[i] = false;
        writingFlags[i] = false;
    }

    int k = 0;
    // Run Servers
    for (k: k .. (M - 1)) {
        run ClientReader(k);
        run ServerServiceForClient(k);
        run ServerReader(k);
        run ServerServiceForServer(k);
        run Agent(k);
    }

    // Run Clients
    int j = 0;
    for (j: j .. (M - 1)) {
        run Client(j);
    }
}

/*
    GENERAL UTILITIES
*/

inline ClientMessageCheck()
{
    if
        :: clientMessageDone == N -> break;
        :: else -> skip;
    fi;    
}

inline AssignStructure(message, mainStructure)
{
    message.Event = mainStructure.Event;
    message.Value = mainStructure.Value;
    message.Priority = mainStructure.Priority;
    message.IsServer = mainStructure.IsServer;
}

inline InitStructure(message)
{
    message.Event = NONE;
    message.Value = 0;
    message.Priority = 0;
    message.IsServer = false;
}

/*
    STATE MACHINE
*/
// Processing for clients
inline ProcessStateMachine()
{
    if
        :: (clientState == STATE0 && sent.Event == TRIVIAL1)
            -> clientState = STATE1;
        :: (clientState == STATE1 && sent.Event == TRIVIAL2)
            -> clientState = STATE2;
        :: (clientState == STATE2 && sent.Event == READ)
            -> clientState = STATE3;
        :: (clientState == STATE3 && sent.Event == TRIVIAL3)
            -> clientState = STATE4;
        // Write Path
        :: (clientState == STATE4 && sent.Event == CHECK)
            -> clientState = STATE5;
        :: (clientState == STATE5 && sent.Event == TRIVIAL4)
            -> clientState = STATE6;
        :: (clientState == STATE6 && sent.Event == TRIVIAL5)
            -> clientState = STATE7;
        :: (clientState == STATE7 && sent.Event == WRITE)
            -> clientState = STATE8;
        :: (clientState == STATE8 && sent.Event == FINISH)
            -> clientState = STATE0;
        // Read Path
        :: (clientState == STATE4 && sent.Event == TRIVIAL_R)
            -> clientState = STATE10;
        :: (clientState == STATE10 && sent.Event == TRIVIAL4)
            -> clientState = STATE11;
        :: (clientState == STATE11 && sent.Event == TRIVIAL5)
            -> clientState = STATE12;
        :: (clientState == STATE12 && sent.Event == TRIVIAL6)
            -> clientState = STATE13;
        :: (clientState == STATE13 && sent.Event == FINISH)
            -> clientState = STATE0;
    fi
}

inline GetNextEvent(state, event, nextEvent, probability)
{
    if
        :: (state == STATE0 && event == TRIVIAL1)
            -> nextEvent = TRIVIAL2;
        :: (state == STATE1 && event == TRIVIAL2)
            -> nextEvent = READ;
        :: (state == STATE2 && event == READ)
            -> nextEvent = TRIVIAL3;
        // Probability Check
        :: (state == STATE3 && event == TRIVIAL3 && probability > 30)
            -> nextEvent = CHECK;
        :: (state == STATE3 && event == TRIVIAL3 && probability <= 30)
            -> nextEvent = TRIVIAL_R;
        // Write Path
        :: (state == STATE4 && event == CHECK)
            -> nextEvent = TRIVIAL4;
        :: (state == STATE5 && event == TRIVIAL4)
            -> nextEvent = TRIVIAL5;
        :: (state == STATE6 && event == TRIVIAL5)
            -> nextEvent = WRITE;
        :: (state == STATE7 && event == WRITE)
            -> nextEvent = FINISH;
        :: (state == STATE8 && event == FINISH)
            -> nextEvent = TRIVIAL1;
        // Read Path
        :: (state == STATE4 && event == TRIVIAL_R)
            -> nextEvent = TRIVIAL4;
        :: (state == STATE10 && event == TRIVIAL4)
            -> nextEvent = TRIVIAL5;
        :: (state == STATE11 && event == TRIVIAL5)
            -> nextEvent = TRIVIAL6;
        :: (state == STATE12 && event == TRIVIAL6)
            -> nextEvent = FINISH;
        :: (state == STATE13 && event == FINISH)
            -> nextEvent = TRIVIAL1;
    fi
}

/*
    SERVER SIDE
*/
proctype ClientReader(int id) {
    Message received;
    InitStructure(received);

    do
        // Processing messages from clients
        :: chServerReaderClient[id] ? received
            -> 
                if
                    :: (received.Event == END) 
                        -> operationComplete[id] = true 
                        -> break;
                    :: else -> skip;
                fi;        
            -> chServerServiceForClient[id] ! received
    od;
}

proctype ServerReader(int id) {
    Message received;
    InitStructure(received);

    do
        :: operationComplete[id] == true
            -> break;
        // Processing messages from other servers
        :: chServerReaderServer[id] ? received
            -> 
            if
            :: (received.Event == WRITE_OK) -> writeResponseCounter[id]++;
            :: (received.Event == CHECK_SAFE) -> checkResponseCounter[id]++ -> skip;
            :: (received.Event == CHECK_FAIL) -> CheckStatus[id] = false -> checkResponseCounter[id]++;
            fi
    od;    
}

inline BroadcastMessage(message)
{
    message.Sender = id;
    int s = 0;
    for (s: s .. (M - 1)) {
        // start processes
        if
        :: (s != id) 
            -> message.IsServer = true 
            -> chServerServiceForServer[s] ! message;
        :: else -> skip;
        fi
    }   
    message.IsServer = false;
}

proctype ServerServiceForClient(int id)
{
    Message response;
    InitStructure(response);

    Message received;
    InitStructure(received);

    do
        :: operationComplete[id] == true
            -> break;
        :: chServerServiceForClient[id] ? received      
            if
            // Check can fail
            :: (received.Event == CHECK)
                -> CheckStatus[id] = true
                -> checkResponseCounter[id] = 0
                -> BroadcastMessage(received)
                -> checkResponseCounter[id] == (M - 1)
                -> 
                    if
                    :: (CheckStatus[id] == false) -> received.Event = FAIL;
                    :: else -> skip;
                    fi
                -> chServerAgent[id] ! received;  
            // Write should not fail          
            :: (received.Event == WRITE) 
                -> received.Event = GET_WRITING
                -> chServerAgent[id] ! received
                -> chServerAgentResponseClient[id] ? received
                -> 
                if
                :: (received.ResponseNo == 1) 
                    -> received.Event = FAIL;
                :: (received.ResponseNo == 5)
                    -> received.Event = WRITE
                    -> writeResponseCounter[id] = 0
                    -> BroadcastMessage(received)
                    -> writeResponseCounter[id] == (M - 1);
                fi
                -> chServerAgent[id] ! received;
            :: else 
                -> chServerAgent[id] ! received;
            fi
            // Prepare response for client according to the results
            -> chServerAgentResponseClient[id] ? received
            ->
            if
                :: received.ResponseNo == 1 -> response.Event = FAIL;
                :: received.ResponseNo == 0 || received.ResponseNo == 5 -> response.Event = GO_ON;
                :: received.ResponseNo == 6 -> response.Event = DONE;
            fi
            -> chClient[id] ! response;
    od;
}

proctype ServerServiceForServer(int id)
{
    Message response;
    InitStructure(response);

    Message received;
    InitStructure(received);

    do
        :: operationComplete[id] == true -> break;
        :: chServerServiceForServer[id] ? received
            -> int sender = received.Sender
            ->
            if
                :: (received.Event == WRITE) -> received.Event = COMMIT;
                :: (received.Event == CHECK) -> received.Event = CONDITION_CHECK;
            fi
            -> chServerAgent[id] ! received
            -> chServerAgentResponseServer[id] ? received
            if
                :: (received.ResponseNo == 2) -> received.Event = WRITE_OK
                :: (received.ResponseNo == 3) -> received.Event = CHECK_SAFE
                :: (received.ResponseNo == 4) -> received.Event = CHECK_FAIL
            fi
            -> chServerReaderServer[received.Sender] ! received;
    od;
}

/*
    AGENT SIDE
*/

inline AddToForeignDictionary(sender, prior)
{
    foreignPriorityDict[sender] = prior;
}

inline RemoveFromForeignDict(sender)
{
    foreignPriorityDict[sender] = -1;
}

inline ForeignPriorityDictControl(requestor, flag)
{
    int k = 0;
    int req = requestor;
    int myPriority = foreignPriorityDict[requestor];
    for(k: k .. (M - 1))
    {
        if  
            :: k != requestor && foreignPriorityDict[k] >= myPriority -> flag = true;
            :: else -> skip;
        fi
    }
    myPriority = 0;
}

inline ResetAgentStatus(id)
{
    currentAccounts[id] = -1;
    readFlags[id] = false;
    writingFlags[id] = false;
}

inline E2PCConditionControl(id, message, state)
{
    AddToForeignDictionary(message.Sender, message.Priority);
    int e2pcforeignhashigherprio = false;
    ForeignPriorityDictControl(message.Sender, e2pcforeignhashigherprio);
    if
        :: writingFlags[id] == true || e2pcforeignhashigherprio == true -> message.ResponseNo = 4;
        :: else -> 
            // FAIL
            -> assert(writingFlags[id] == false)
            if
            :: readFlags[id] == true 
                -> ResetAgentStatus(id)
                -> assert(state != STATE8)
                -> state = STATE0;
            :: else -> skip;
            fi     
            -> message.ResponseNo = 3;
    fi
}

inline E2PCGetWriting(id, message, state)
{
    assert(writingFlags[id] == false);
    if
    :: currentAccounts[id] != agentAccounts[id] || serverStates[id] == STATE0 
        -> message.ResponseNo = 1;        
    :: else ->
        writingFlags[id] = true;
        message.ResponseNo = 5;
    fi
}

/*
    VALIDATIONS
*/
inline CheckAllAccounts()
{
    int a = 0;
    for(a: a .. (M - 1))
    {
        int b = 0;
        for(b: b .. (M - 1))
        {
            assert(agentAccounts[a] == agentAccounts[b])
        }
    }
}

inline WriteStateValidation(state, event)
{
    // Write should always be able to execute!
    assert(state == 7 && event == WRITE);
    assert((readFlags[id] == false || serverStates[id] == STATE0 || currentAccounts[id] != agentAccounts[id] || event == FAIL) == false);
}

inline WriteValidation(state, event)
{
    assert(state == 7 && event == WRITE);
    assert((writingFlags[id] == false || readFlags[id] == false || serverStates[id] == STATE0 || currentAccounts[id] != agentAccounts[id]) == false);
}

inline CommitValidation()
{
    assert(writingFlags[id] == false);
}
/*
    VALIDATIONS END
*/

/*
Response types
    0 default
    1 FAIL
    2 COMMIT success
    3 Outer CHECK success
    4 Outer Check fail
    5 get writing success
    6 finish
*/

inline ProcessStateMachineAgent(id, state, message)
{
    mtype:events event = message.Event;

    if
    :: (message.Event == CONDITION_CHECK || message.Event == GET_WRITING || message.Event == NONE)
        -> 
            if
            :: (message.Event == CONDITION_CHECK) 
                // Incoming Priority should be larger than 0
                -> assert(message.Priority > 0)
                -> E2PCConditionControl(id, message, state);
            :: (message.Event == GET_WRITING) 
                -> E2PCGetWriting(id, message, state);
            :: (message.Event == NONE) 
                // [ASSERT] No none event should exists in the model checker
                -> assert(message.Event != NONE);
            fi
    :: else ->
        if
        :: writingFlags[id] == true 
            -> WriteStateValidation(state, message.Event)
        :: else -> skip;
        fi
        ->
        if
        // failure
        :: (
            message.Event != COMMIT && message.Event != WRITE && 
            (
                event == FAIL || 
                (agentAccounts[id] != currentAccounts[id] && readFlags[id] == true) || 
                (serverStates[id] == STATE0 && message.Event != TRIVIAL1)
            )
            )
            // FAIL
            -> assert(writingFlags[id] == false)
            if
            :: readFlags[id] == true 
                -> ResetAgentStatus(id)
                -> assert(state != STATE8)
                -> state = STATE0;
            :: else -> skip;
            fi
            -> message.ResponseNo = 1;
        // Outside events
        :: (event == COMMIT)
            -> CommitValidation()
            -> agentAccounts[id] = agentAccounts[id] + message.Value
            -> message.ResponseNo = 2
            -> RemoveFromForeignDict(message.Sender);
        // Self executions
        :: else ->
            if
            :: (state == STATE0 && event == TRIVIAL1)
                -> state = STATE1;
            :: (state == STATE1 && event == TRIVIAL2)
                -> state = STATE2;
            :: (state == STATE2 && event == READ)
                -> currentAccounts[id] = agentAccounts[id]
                -> readFlags[id] = true
                -> message.Sender = id
                -> 
                    if
                        :: message.Priority != -1 -> AddToForeignDictionary(message.Sender, message.Priority);
                        :: else -> skip;
                    fi
                -> state = STATE3;
            :: (state == STATE3 && event == TRIVIAL3)
                -> state = STATE4;
            // Write Path
            :: (state == STATE4 && event == CHECK)
                -> state = STATE5;
            :: (state == STATE5 && event == TRIVIAL4)
                -> state = STATE6;
            :: (state == STATE6 && event == TRIVIAL5)
                -> state = STATE7;
            :: (state == STATE7 && event == WRITE)
                -> WriteValidation(state, event)
                // read and write flags should have already been set!
                -> assert(writingFlags[id] == true)
                -> assert(readFlags[id] == true)
                -> agentAccounts[id] = agentAccounts[id] + message.Value
                // Assertion that after self write all local values of servers should be equal!
                -> CheckAllAccounts()
                -> currentAccounts[id] = -1
                -> readFlags[id] = false
                -> writingFlags[id] = false
                -> message.Sender = id
                -> RemoveFromForeignDict(message.Sender)
                -> state = STATE8;
            :: (state == STATE8 && event == FINISH)
                -> assert(currentAccounts[id] == -1 && readFlags[id] == false && writingFlags[id] == false)
                -> message.ResponseNo = 6
                -> state = STATE0;
            // Read Path
            :: (state == STATE4 && event == TRIVIAL_R)
                -> state = STATE10;
            :: (state == STATE10 && event == TRIVIAL4)
                -> state = STATE11;
            :: (state == STATE11 && event == TRIVIAL5)
                -> state = STATE12;
            :: (state == STATE12 && event == TRIVIAL6)
                -> state = STATE13;
            :: (state == STATE13 && event == FINISH)
                -> message.ResponseNo = 6
                -> state = STATE0;
            fi
        fi
    fi

    
}

proctype Agent(int id)
{
    Message received;
    InitStructure(received);

    int foreignPriorityDict[M];

    do
        :: operationComplete[id] == true
            -> break;
        // can take status from here to send services!!
        :: chServerAgent[id] ? received
            -> ProcessStateMachineAgent(id, serverStates[id], received)
            ->
            if
            :: (received.IsServer == false) -> chServerAgentResponseClient[id] ! received;
            :: (received.IsServer == true) -> chServerAgentResponseServer[id] ! received;
            fi;
    od;
}

/*
    CLIENT SIDE
*/

inline ProcessClientEvent()
{
    if
        :: (received.Event == FAIL)
            -> clientState = STATE0;            
            -> assert(clientState == STATE0);
        :: (received.Event == GO_ON)
            -> ProcessStateMachine();
        :: (received.Event == DONE)
            -> assert(clientState == STATE8 || clientState == STATE13)
            -> ProcessStateMachine()
            -> assert(clientState == STATE0);
            -> clientMessageDone++;
    fi
}


inline PriorityGenerator(prior)
{
    if
        :: prior = prior + N;
    fi;
}

inline ValueGenerator(val)
{
    if
        :: (val > 10000) 
            ->
            if
            :: (1 == 1) -> val = val + 1;
            :: (2 == 2) -> val = val + 2;
            :: (3 == 3) -> val = val + 3;
            :: (4 == 4) -> val = val + 4;
            fi
        :: else ->
            if
                :: (1 == 1) -> val = val * 2;
                :: (1 == 1) -> val = val * 3;
            fi
    fi
}

// This is to force at least one write message to be done
inline ReadyNextMessage(message, previousState, received)
{
    if
    :: received.Event == FAIL -> nextEvent = TRIVIAL1
    :: else -> 
        if
        :: 0 == 0 -> GetNextEvent(previousState, message.Event, nextEvent, 50);
        :: 1 == 1 -> GetNextEvent(previousState, message.Event, nextEvent, 10);
        :: 2 == 2 -> GetNextEvent(previousState, message.Event, nextEvent, 50);
        :: 3 == 3 -> GetNextEvent(previousState, message.Event, nextEvent, 10);
        fi 
    fi
    // Set Event
    message.Event = nextEvent;
    // Set Priority -- if only the done event, which means a cycle finished
    if
    :: (received.Event == DONE) 
        -> ValueGenerator(message.Value)
        -> PriorityGenerator(message.Priority);
    :: else -> skip;
    fi
    // Set message server type
    message.IsServer = false;
    message.Sender = -1;
}

proctype Client(int id)
{
    int clientMessageDone = 0;
    mtype:states clientState = STATE0;
    
    // Prepare Structures
    Message sent;
    InitStructure(sent);

    Message received; 
    InitStructure(received);

    // Init first message
    sent.Event = TRIVIAL1;
    sent.Value = 1;
    sent.Priority = id+1;
    sent.IsServer = false;
    sent.Sender = -1;

    mtype:events nextEvent;
    // Previous state is used to get the next event in order
    mtype:states previousState;

    do
        :: ClientMessageCheck()
            -> chServerReaderClient[id] ! sent
            -> chClient[id] ? received
            -> previousState = clientState;
            -> ProcessClientEvent()
            -> ReadyNextMessage(sent, previousState, received);
    od;

    // After all messages are done send the END event
    allOperationsDone++;
    (allOperationsDone == M);
    sent.Event = END;
    chServerReaderClient[id] ! sent;
}

