# Taking measurements

Pick the measuring mode (left eye, right eye or both eyes) at the bottom. Also, you can change
the "Focus" text color and background by clicking on it.

Take off your glasses (or contacts). If you have picked single-eye mode, cover your other eye.
Look at the "Focus" text and slowly move away from the screen until the text becomes just a little
blurry:

![image](file:///android_asset/edge_of_blur.png)

After you find your edge of blur distance take the measurement by doing one of the following:

  * Tap on the camera button
  * Press any of Volume buttons
  * Cover your nose for couple seconds (this option can be disabled in Settings)

After the measurement is taken it is immediately persisted and can be found on "Progress" menu tab.

If you're unhappy with the measurement you can delete it immediately using a trash icon. You can
also take another measurement right away by pressing "+" (plus) icon.

# Tracking progress

"Progress" menu tab shows all measurements that have been taken. You can zoom in/out by pinching
the graph with two fingers. You can tap on measurements to see the details or deleting them.

# Export / Import progress
## Export

Go to Settings and select "Export" to save all your progress as an JSON file (this is really just
a text file). You can save it locally or upload to google drive, add to saved Telegram messages
etc.

## Import

In order to import you progress you need to open the saved file (which has "em.json" extension) on
your device. This should automatically open the app and prompt to import the progress. If for
some reason it doesn't work, try opening from a different source (Telegram saved messages, gmail
attachment etc. **WARNING** this will overwrite all your existing measurements and can't be undone.

# Settings
You can change the FOCUS text and size in Settings. You can also disable measuring sound and "cover
your nose" gesture there.

# Troubleshooting

If you see the black screen instead of the measurement screen it's likely because your device is
not in [this list](https://developers.google.com/ar/discover/supported-devices). The app uses ArCore
framework to measure the distance to the user's face, and for some reason this library doesn't work
on all android devices.

# Known issues

If you import your progress and rotate the screen you'll be prompted to import again. To fix this,
force close the app and re-open.