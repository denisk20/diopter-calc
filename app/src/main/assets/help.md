# Taking measurements

Pick the measuring mode (left eye, right eye or both eyes) at the bottom. Also, you can change the "
Focus" text color and background by clicking on it.

Take off your glasses (or contacts). If you have picked a single-eye mode, close or cover your other
eye. Look at the "Focus" text and slowly move away from the screen until the text becomes just a
little blurry:

![image](file:///android_asset/edge_of_blur.png)

After you find your edge of blur take the measurement by doing one of the following:

  * Tap on the camera button
  * Press any of Volume buttons
  * Cover your nose for couple seconds (this option can be disabled in Settings)

After the measurement is taken it is immediately persisted and can be found in the "Progress" menu
tab.

If you're unhappy with the measurement you can delete it immediately using a trash icon (or by
selecting the measurement in Progress and pressing a trash icon). You can also take another
measurement right away by pressing a "+" (plus) icon.

# Tracking progress

"Progress" menu tab shows all measurements taken. You can zoom in/out by pinching the chart with two
fingers. You can tap on the measurements to see the details or deleting them. You can swipe left to
right to switch to the Table mode.

# Export / Import progress
## Export

Go to Settings and select "Export" to save all your progress as a JSON file (this is really just a
text file). You can save it locally or upload to google drive, add to saved Telegram messages etc.

## Import

In order to import you progress you need to open the saved file (which has ".emjson" extension) on
your device. This should automatically open the app and prompt to import the progress. If for some
reason it doesn't work, try opening from a different source (Telegram saved messages, gmail
attachment etc.). **WARNING** this will overwrite all your existing measurements and can't be
undone.

# Settings

You can change the FOCUS text and size in Settings (it doesn't affect the measurements, just pick
the text and font size that you're comfortable working with). You can also disable the sound and "
cover your nose" gesture there.

# Troubleshooting

If you see the black screen instead of the measurement screen it's likely because your device doesn'
t support AR Core. Try running on another device if possible. Check the list of the devices which
are guaranteed to support AR
Core [here](https://developers.google.com/ar/discover/supported-devices).

# A note on astigmatism

Some people, including the app author, have pretty bad astigmatism which make it very difficult to
find the edge of blur. Here are some tips to solve this:

* Keep practicing. Eventually you should be able to distinguish between the edge of blur and the
  double vision
* You can try to use glasses or contacts which only correct the astigmatism (to certain degree)
  during the measurement.

# Known issues

If you import your progress and rotate the screen you'll be prompted to import again. To fix this,
force close the app and re-open.