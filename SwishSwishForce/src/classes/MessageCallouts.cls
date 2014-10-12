public with sharing class MessageCallouts {
	
	@future (callout=true)  // future method needed to run callouts from Triggers
      static public void getLocation(id dealId){
      	DealAlert__c a;
		try{
        // gather account info

        a = [Select Address__c, radius__c, comment__c, Account__r.Id from DealAlert__c WHERE id =: dealId];

 

        // create an address string

        String message = EncodingUtil.urlEncode(a.address__c+ ' ' + a.comment__c, 'UTF-8');

 

        // build callout

        Http h = new Http();

        HttpRequest req = new HttpRequest();
        req.setEndpoint('http://blooming-journey-3117.herokuapp.com/salesforce?message='+message);
        req.setMethod('GET');
        req.setTimeout(60000);

 

        

            // callout

            HttpResponse res = h.send(req);

        } catch (Exception e) {
        	a.addError(e.getMessage());
        }

    }

}