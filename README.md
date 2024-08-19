# finance-demo

Playground Project to know how far a person is to achieving their retirement.

## Business

### Requirements
- Create, Delete and Update a retirement plan.
  - If Updated, recalculate the retirement progress.
- Have multiple bank account
- Sum funds added to the account.
- Calculate how far is from retiring.
  - NonFunction: choose an AI to perform data analytics.
- Take into account the inflation every month.
  - NonFunction: find a public API to fetch the inflation for a given Country. 

### Missing reqs
- Add docker
- Add Scheduler for calculating the retirement goal every day
- Find public API to fetch inflation
- Find AI to perform data analytic
- Add a Backend-For-Frontend API to create all the requirement data at once.

## Development
### Requirements
* IntelliJ IDE or similar
* Java SDK 17
* Apache Maven 3.6.3 or newer
* 
### Code Formatting
In order to format the code the following command can be used:
```bash
mvn spotless:apply
```