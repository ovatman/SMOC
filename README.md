# (S)tate (M)achines (O)n the (C)loud

Public repository for State Machines On the Cloud Project by İTÜ Cloud Core Lab. supported by TUBITAK 1003 program

SMOC is a joint library effort to increase the performance of replicated state machines (RSM). Main focus of the effort is on RSM execution in cloud and especially geographically distributed environemnts. Current efforts are gathered around consistency, checkpointing and caching of RSMs.

Currently, this library can be considered as under development until v1.0 is released. 

The following high level components are present in current version (0.9):

- ``smoc-main``: Contains code of the main components of smoc library
    - ``smoc``: Main integrating component for the smoc project.
    - ``smocclinet``: Client implementation for the main component
    - ``smoctest``: Test code for the main component 
    - ``smoccheckpointing``: Main component for the checkpointing approach developed specifically for the replicate state machines
    - ``smoccheckpointdriver``: Driver component for the checkpointing operation performed by the main component.
    - ``smocpathpredictor``: Methods developed for path prediction for a single state machine.
    - ``smoccachemanager``: Component that performs cache management by utilizing shared histroy approach for the state machines.
    - ``smocgeocoordinator``: Coordinator code that handles read write request management in geographically distributed replicated state machines
    - ``smocgeoclient``: Client implementation for coordination of geographically distributed replicate dstate machines
    - ``smocgeomessaging``: Proto message formats used in coordination of geographically distributed replicate dstate machines
    
- ``smoc-externals``: Extrnal and utility projects
    - ``E2PC``: C\# implementation of E2PC protocol developed for the smoc library.
    - ``Verification``: Promela code for formal verification of the smoc functionality

Main project contributors:
- Dr.Tolga Ovatman
- Dr.Mehmet Tahir Sandıkkaya
- Halit Uyanık
- Niyazi Özdinç Çelikel
- Onur Göksel
- James Akyüz
- Enes Bilgin
- Batuhan Can