# ITS330_Final_Project_Thread_Pool

## How to run

javac Task.java ThreadPool.java Client.java
java Client

### Result

I am a task result = 15
I am a task result = 2
I am a task result = 82
I am a task result = 15
I am a task result = 3
I am a task result = 6

## What this means

The output confirms the ThreadPool successfully managed the task queue, with worker threads removing and executing each Runnable task as they became available. The non-sequential order is a standard result of multiple threads processing these tasks concurrently.