trigger SendMessageTrigger on DealAlert__c (after insert, after update) {
	for (DealAlert__c a : trigger.new) {
        MessageCallouts.getLocation(a.id);
	}
}