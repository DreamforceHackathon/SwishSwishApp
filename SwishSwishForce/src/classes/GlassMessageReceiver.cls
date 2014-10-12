@RestResource(urlMapping='/GlassMessages/*')
global with sharing class GlassMessageReceiver {
    @HttpPost
    global static String dealAlertFromHeroku(String address, String comment) {
    	try {
    		Deal_Message__c message = new Deal_Message__c(address__c=address, comment__c =comment);
	    	upsert message;
	    	return 'OK';
    	} catch (Exception e){
    		return 'FAIL:' + e.getMessage();
    	}
    }
}