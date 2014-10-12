public with sharing class ManageDealsCtrl {
	public DealAlert__c currentDeal{get;set;}
	public List<DealAlert__c> deals {get;set;}
	public List<DealAlert__c> newDeals {get;set;}
	public List<Account> accounts {get;set;}
	
	
	
	public Map<Id, DealAlert__c> dealsById {get;set;}
	public Boolean noDealsFould {get;set;}
	
	public ManageDealsCtrl(){
		accounts = new List<Account>();
		currentDeal = new DealAlert__c();
		deals = new List<DealAlert__c>();
		newDeals = new List<DealAlert__c>();
		dealsById = new Map<Id, DealAlert__c>();
		deals = [select Address__c, radius__c, comment__c, Account__r.Id from DealAlert__c limit 100];
		
		noDealsFould = deals.isEmpty();
		for(DealAlert__c deal:deals){
			dealsById.put(deal.Id,deal);
		}
		
		List<Account> accounts = [select Id, Name from Account limit 100];
	}
	
	public void add(String address, String comment, Id acctId){
		DealAlert__c deal = new DealAlert__c();
		deal.Address__c = address;
		deal.comment__c = comment;
		deal.Id = acctId;
		upsert deal;
		dealsById.put(deal.Id, deal);
	}
	
	public void edit(Id id, String address, String comment, Id acctId){
		DealAlert__c deal = dealsById.get(Id);
		deal.Address__c = address;
		deal.comment__c = comment;
		deal.Id = acctId;
		upsert deal;
		dealsById.put(deal.Id, deal);
	}
	
	public void del(Id id){
		Database.delete(dealsById.get(id));
	}
	
	

}