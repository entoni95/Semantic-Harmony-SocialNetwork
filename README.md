# Semantic Harmony Social Network

A social Network based on the users interests that exploits a P2P Network. The system collects the profiles of the users, and automatically creates friendships according to a matching startegy.



## Basic operations

- Get the user's nickname.

- Create a new user profile.

- Connect users who have the same profile.

- Get the user's friends list.


## Additional operations

- Chat with all connected users.

- Send a message to a friend in the list.

- Reset the user's profile so he can redo the test.


## Technologies 

- Java 8
- Tom P2P
- JUnit
- Apache Maven
- Docker
- Eclipse IDE

## Project structure

The program is structured in following Java classes:

1. ***Main***: main class of the project.
2. ***SemanticHarmonySN***: the API that define all the operations of the project.
3. ***SemanticHarmonySNImpl***: implementation of API.
4. ***MessageListener***: interface used for sending and receiving messages.


The project provide also the class ***SemanticHarmonySNImplUnitTest*** which is a JUnit test case.


## How to run Semantic Harmony Social Network
First of all run Docker, open your terminal and enter the SemanticHarmonySocialNetwork folder
`cd desktop/semanticharmonysocialnetwork`
#### Build docker container

So you must build your docker container:
`docker build --no-cache -t social .`

#### Start master peer
`docker run -i --name MASTER-PEER -e MASTERIP="127.0.0.1" -e ID=0 social`

The `MASTERIP` environment variable is the master peer *IP address* and the *ID* environment variable is the unique *ID* of peer. 

#### Start another peers

For run an other peer you must check the *IP address* of the container created:  `docker inspect <containerID>` [for check the *container ID*: `docker ps` ]

Now, you can start your peers varying the *peer ID* (the *ID* must be unique):

`docker run -i --name PEER-1 -e MASTERIP="172.17.0.2" -e ID=1 social`

`docker run -i --name PEER-2 -e MASTERIP="172.17.0.2" -e ID=2 social`

and so on.


### Developed by: 

Antonio Quirito 
Università degli Studi di Salerno
mat. 0522500577
