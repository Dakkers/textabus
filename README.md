TextABus
========

My first (and therefore probably awfully written) Android app. It allows you to save bus stops as list items, each with a name and number. Clicking on the list item will send a text to GRT (57555) for you, by default. You can change the number you want to send a text to by changing it in the settings.

## Features
- adding / editing / removing stops
- specifying what number to text

## TODO
- ~~add in settings page so users can change the phone number that is texted (so people from different cities can use it)~~
- ~~update the design, it's pretty bland.~~
- ~~sort the data by name~~
- ~~CODE CLEANUP PLS~~
- ~~change name of app~~
- ~~add About section~~
- update button sizes. they're a bit too small, it seems.
- Pebble integration (that was actually the purpose in the beginning)
- add importing and exporting feature in settings
- change from using SharedPreferences to SQLite

## Acknowledgements
Thanks to:
- [Pieter Stam](https://github.com/stampieter) for being on my ass about finishing this, suggesting the idea of adding a Toast when a text is sent, and for being a Beta tester
- [Imtiaz Hussain](https://github.com/imtizzle) for telling me that a stop number of only whitespace will cause a crash upon texting, and for being a Beta tester
- Duncan Forster for being a Beta tester
- [Christian de Angelis](https://github.com/cdeange) and [Shane Creighton-Young](https://github.com/srcreigh) for helping me out with Android/Java stuff

## License
GPL License (http://www.gnu.org/licenses/gpl-2.0.txt)
