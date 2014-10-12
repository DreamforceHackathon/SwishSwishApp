public with sharing class ManageDealsCtrl {
	List<DealAlert__c> deals;
	Boolean noDealsFould = false;
	
	public ManageDealsCtrl(){
		deals = [select loc__c, Address__c, radius__c, comment__c, imageURL__c, Account__r.Id from DealAlert__c limit 100];
		noDealsFould = deals.isEmpty();
	}
	
	
	

}