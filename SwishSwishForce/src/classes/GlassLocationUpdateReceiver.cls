@RestResource(urlMapping='/GlassUpdates/*')
global with sharing class GlassLocationUpdateReceiver {
	@HttpPost
    global static String updateLocationMoves(String coordMoves) {
    	try {
    		
    		//coordMoves=1413116219000|123456789123456|37.7847947|-122.4022345->1413116225000|123456789123456|37.7847943|-122.4022342
    		
	    	List<Location_Event__c> events = new List<Location_Event__c>();
	    	String[] eventStrings = coordMoves.split('->');
	    	for(String eventStr:eventStrings) {
	    		String[] parts = eventStr.split(' ');
	    		DateTime evtTime = DateTime.newInstance(Long.valueOf(parts[0].trim()));
	    		String clientGuid = parts[1];
	    		Decimal lat = Decimal.valueOf(parts[2]);
	    		Decimal lon = Decimal.valueOf(parts[3]);
	    		Location_Event__c event = new Location_Event__c(ClientGuid__c=clientGuid, EventTime__c=evtTime, loc__Latitude__s = lat, loc__Longitude__s = lon);
	    		events.add(event);
	    	}
	    	
	    	upsert events;
	    	return 'OK';
    	} catch (Exception e){
    		return 'FAIL:' + e.getMessage();
    	}
    }
    
}