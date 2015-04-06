# Change Log
A brief summary of changes made in the app.

## V 0.5.2 (Beta)
- Prevent user from entering in a tab in the stop number, as well
- Exporting data works again

## [V 0.5.1 (Beta)](https://github.com/StDako/textabus/commit/1409c0e6017688c8044e655a726a11e76232b08e)
- Prevent user from editing a stop name to be a duplicate of another stop
- Change background color of settings menu
- Fix "bug" where background color flickers after keyboard disappears

## [V 0.5.0 (Beta)](https://github.com/StDako/textabus/commit/c9b44f70e9d02e12245156a1e7ea72bb404f5495)
- Add import and export feature
- Fix "bug" in adding keys as stop names
- Prevent user from entering in a tab in the stop name

## [V 0.4.5 (Beta)](https://github.com/StDako/textabus/commit/63081a1efb2f6e0d5b1272b6ef1e36ad6f4ba06c)
- Change "error" message in Add/Edit stop dialogs to a Toast notification
- Change limit on stop number to be a max of 48 characters

## [V 0.4.4 (Beta)](https://github.com/StDako/textabus/commit/d44db95f045d0636ea7fa2f8a238f14ede22966c)
- Add notification for click events to tell the user to do a long click instead

## [V 0.4.3 (Beta)](https://github.com/StDako/textabus/commit/cb5e0f711d10ca6945e3d3ff53ba86f4e2d2662e)
- Change duration for invalid phone number notification to 7 seconds
- Add "Version" and "License" sections to settings
- Change Settings background colour back to default (grey) because of a weird appearance bug
- Add Preference Categories "Options" (for SMS Number) and "About Textabus" (for About, License, etc.)

## [V 0.4.2 (Beta)](https://github.com/StDako/textabus/commit/6e1e029b38b2879c20cdd3a5b83aadc37d9f23f6)
- Prevent user from entering in an invalid phone number

## [V 0.4.1 (Beta)](https://github.com/StDako/textabus/commit/a00c425806020476384a804fb7484dbeca3e8218)
- Change the list item to use long clicks instead of clicks to prevent accidental texting

## [V 0.4.0 (Beta)](https://github.com/StDako/textabus/commit/4e97c6a1c58d00c982f89ca23f4a37bf8f65a6fd)
- Added a notification to tell the user that the SMS was sent

## [V 0.3.2 (Beta)](https://github.com/StDako/textabus/commit/ca09fc6c4c043f57b3c24b429ed8045e3c567de9)
- Changed colour of Settings menu

## [V 0.3.1 (Beta)](https://github.com/StDako/textabus/commit/0a2cf37435837e3c82db8da4879d23130c91fcb6)
- Added "About" and "Source Code" to Settings menu
- Removed "Preference Category" in Settings menu

## [V 0.3.0 (Beta)](https://github.com/StDako/textabus/commit/a4e2206dba0d5cf26a34747ad4fc4511b14e49a7)
- Added a real Settings menu using Fragments and everything! I'm a big boy now!
- Added additional regex checking to prevent user from entering in a stop number that is an empty string or only whitespace

## [V 0.2.1 (Beta)](https://github.com/StDako/textabus/commit/423c8e14a4b5256902b1cf99512d33042ba7ed16)
- Added "About" section to the "Settings" menu.

## [V 0.2.0 (Beta)](https://github.com/StDako/textabus/commit/dded14d59d9396190abab46bea73e279e0c1d3aa)
Fully functional, i.e. SMS messages can be sent to a specified number, stops can be added/edited/removed, etc.

## [V 0.1.0 (Alpha?)](https://github.com/StDako/textabus/commit/6ec79899098d04e1d2929fda36c073a6527e4218)
Almost entirely functional, except for a major flaw: initial use of the app would not properly define the SMS number. This wasn't obvious to me, because the app had already been installed.
