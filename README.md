MigratableProcess
=================

MigratableProcess is the first project for course 15640.

One classic problem in distributed systems is the migration of work. 
The common idea is moving in-progress work with as little disruption and 
wastage as possible. 

In our design, we use a classic master slave model and enable the user to 
migrate different in-progress processes.

There are two interesting features we implement:
1. Fault tolerant on worker server
2. Real-time information update such as worker status and process status
