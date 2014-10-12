global with sharing class OldDealsHousekeeping implements Schedulable {
   global void execute(SchedulableContext SC) {
       delete [select Id from Deal_Message__c where CreatedDate < THIS_MONTH];
   }
}