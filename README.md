TextABus
========

My first (and therefore probably awfully written) Android app. It allows you to save bus stops as list items, each with a name and number. Clicking on the list item will send a text to GRT (57555) for you, by default. You can change the number you want to send a text to by changing it in the settings.

## Features
- adding / editing / removing stops
- specifying what number to text
- importing and exporting data

## TODO
- ~~add in settings page so users can change the phone number that is texted (so people from different cities can use it)~~
- ~~update the design, it's pretty bland.~~
- ~~sort the data by name~~
- ~~CODE CLEANUP PLS~~
- ~~change name of app~~
- ~~add About section~~
- ~~change from using SharedPreferences to SQLite~~ (decided to use a stringified JSON object to hold all data)
- ~~add importing and exporting feature in settings~~
- ~~create a widget~~
- update button sizes. they're a bit too small, it seems.
- Pebble integration (that was actually the purpose in the beginning)

## Acknowledgements
Thanks to:
- [Pieter Stam](https://github.com/stampieter) for being on my ass about finishing this, suggesting the idea of adding a Toast when a text is sent, and for being a Beta tester
- [Imtiaz Hussain](https://github.com/imtizzle) for telling me that a stop number of only whitespace will cause a crash upon texting, and for being a Beta tester
- Duncan Forster and Joel Silva for being Beta testers
- [Christian de Angelis](https://github.com/cdeange), [Shane Creighton-Young](https://github.com/srcreigh) and [Moez Bhatti](https://github.com/moezbhatti) for helping me out with Android/Java stuff

## License
GPL License (http://www.gnu.org/licenses/gpl-2.0.txt)
