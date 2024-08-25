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
~~- Find public API to fetch inflation~~
- ~~Scheduler to fetch the inflation every month~~
- Event to recalculate all accounts' net amount
- Find AI to perform data analytic
- Add a Backend-For-Frontend API to create all the requirement data at once.
- 

### IMF (International Monetary Fund) APIs
- The **datamapper API** is a simply API that provides the inflation rate and does not make any deep analysis.
  - [Documentation here](https://www.imf.org/external/datamapper/api/help)
  - Example: ` $ curl https://www.imf.org/external/datamapper/api/v1/PCPIPCH/ESP?periods=2024 `
- IMF also provides another robust API for more in-depth analysis.
  - [Documentation here](https://datahelp.imf.org/knowledgebase/articles/667681-json-restful-web-service)
  - Example: `$ curl http://dataservices.imf.org/REST/SDMX_JSON.svc/CompactData/IFS/M.ES.PCPI_IX.?startPeriod=2024&endPeriod=2024 `

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