trigger SetGeolocation on DealAlert__c (after insert, after update) {
	for (DealAlert__c a : trigger.new) {
        if (a.loc__Latitude__s == null)
            LocationCallouts.getLocation(a.id);
	}
}