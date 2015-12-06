# smartmuseum
Smart Museum Agent Framework

## About
Smart Museum is a simple Agent framework build on top of JADE. 
The main task of this homework is to identify the main Agents and their Behaviors. 
Also to use Directory Facilitator (DF) Agent in JADE to register and discover Agent's services.

## Technologies
 - JADE (http://jade.tilab.com/)
 - JSON Simple (https://code.google.com/p/json-simple/)
 - Maven (https://maven.apache.org/)
 
##Prerequisite
 - Maven 3.x as the build tool
 - Java 7
 
 
## Build the Application
 - mvn package
 
## Start Main Container
 - java -jar target/smartmuseum-1.0.0.jar -gui
 
## Start Profiler Agent
 - java -jar target/smartmuseum-1.0.0.jar -container -host localhost Profiler1:kth.id2209.profiler.ProfilerAgent

 
## Start TourGuide Agent
 - java -jar target/smartmuseum-1.0.0.jar -container -host localhost TourGuide:kth.id2209.tourguide.TourGuideAgent
 
## Start Artist Agent
java -jar target/smartmuseum-1.0.0.jar -container -host localhost Artist1:kth.id2209.artist.ArtistAgent

## Start Curator Agent
 - java -jar target/smartmuseum-1.0.0.jar -container -container-name HM -host localhost CuratorHM1:kth.id2209.curator.CuratorAgent
 

