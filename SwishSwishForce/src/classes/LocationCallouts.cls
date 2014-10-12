public class LocationCallouts {

 

     @future (callout=true)  // future method needed to run callouts from Triggers

      static public void getLocation(id dealId){

        // gather account info

        DealAlert__c a = [Select Address__c, radius__c, comment__c, Account__r.Id from DealAlert__c WHERE id =: dealId];

 

        // create an address string

        String address = EncodingUtil.urlEncode(a.Address__c, 'UTF-8');

 

        // build callout

        Http h = new Http();

        HttpRequest req = new HttpRequest();
        req.setEndpoint('http://maps.googleapis.com/maps/api/geocode/json?address= '+address+'&sensor=false');
        req.setMethod('GET');
        req.setTimeout(60000);

 

        try{

            // callout

            HttpResponse res = h.send(req);

            // parse coordinates from response

            JSONParser parser = JSON.createParser(res.getBody());

            double lat = null;

            double lon = null;

            while (parser.nextToken() != null) {

                if ((parser.getCurrentToken() == JSONToken.FIELD_NAME) &&
                    (parser.getText() == 'location')){
                       parser.nextToken(); // object start
                       while (parser.nextToken() != JSONToken.END_OBJECT){
                           String txt = parser.getText();
                           parser.nextToken();
                           if (txt == 'lat')
                               lat = parser.getDoubleValue();

                           else if (txt == 'lng')
                               lon = parser.getDoubleValue();

                       }
                }
            }

            // update coordinates if we get back

            if (lat != null){

                a.loc__Latitude__s = lat;

                a.loc__Longitude__s = lon;

                update a;

            }

        } catch (Exception e) {
        	a.addError(e.getMessage());
        }

    }

}