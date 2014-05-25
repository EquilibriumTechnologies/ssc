ssc
===

Simple State Capture of AWS accounts.

This project was created with the purpose of providing a historical capture of the current state of one or more accounts being used within Amazon Web Services (AWS).  The goal really is not to be a realtime monitoring engine, checkout the fantastic Edda project for that at: https://github.com/Netflix/edda

#### Main goals: 
* Precice and accurate capture of each describe/get/list api call pertinant to the AWS account.
* Minimize storage by only storing captures when something pertinant has changed within the state.
* Support a simple pluggable capture API for enabling, disabling, and extending what is captured.
* Support monitoring of multiple accounts.

#### Getting started:

Clone this repo.
mvn clean package install

For a single account monitoring, create an AwsCredentials.properties file in /src/main/resources with appropriate accesses. (See xxx for the one we use)

Modify ssc.properties accordingly.

Run com.eqt.ssc.SimpleStateCollector
