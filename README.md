# Ln4_Solutions
Android app that enables convenient mobile access to Ln4 Solutions web based applications

The Ln4 Solutions app prompts for a Customer ID, then authorizes with a Ln4 Solutions AB
corporate server over https and upon a valid ID returns and URL that refers to Web material
for that customer.

The internal WebView based browser is launced for navigating the HTML/Javascript content.
Alternatively the app can use an external installed browser unless the WebView component
runs Chrome browser technology (since android 4.4)

A corresponding iOS (Swift 2 based) app has been developed as well.
