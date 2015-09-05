Rules
=====

###Bible



###Hymns

 - The prepare file must be named in the format "hymns%s.txt" where "%s" is the name of the book eg: "hymns1962.txt"

 - The first line of the document must be "{#Hymns (%s)}" where "%s" is the name of the book eg: "{#Hymns (1962)}"

 - There should be no blank lines

 - Each hymn number is stored in the format "{%d}" where %d is the hymn number eg: a line reading "{3}" means that this is the start of hymn 3.

 - The next line after the number should be the information line (Even if there is no information)

  + This line MUST include a "," then a " " (space character) immediately after

  + The author/other information is written first then the metre in the format "%s1, %s2" where %s1 is the author and %s2 is the metre

  + eg: "W. COWPER (1731-1800) Adapted, C.M."

 - Each verse number is stored in the format "|%d|" where %d is the verse number eg: "|5|" is verse 5

 - All other lines are treated as verse text and so format is ignored (however line breaks will only be added in where there are line breaks in the prep document)

 - There is no special syntax for the end of a prep document

The first two hymns are shown from the 1962 hymn book as an example

```
	{#Hymns (1962)}
	{1}
	W. COWPER (1731-1800) Adapted, C.M.
	|1|
	Of all the Gifts Thy love bestows,
	Thou Giver of all good!
	E'en heav'n itself no richer knows
	Than the Redeemer's blood
	|2|
	Faith, too, that trusts the blood through grace,
	From that same love we gain;
	Else, sweetly as it suits our case,
	The gift had been in vain.
	|3|
	We praise Thee, and would praise Thee more;
	To Thee our all we owe;
	The precious Saviour, and the power
	That makes Him precious too.
	{2}
	J. G. DECK (1807-1884) Adapted, 8.7.8.7.4.7.
	|1|
	Father, 'twas Thy love that knew us
	Earth's foundation long before;
	That same love to Jesus drew us
	By its sweet constraining power,
	And hath made us
	Sons before Thee evermore.
	|2|
	Now that changeless love enfolds us,
	All its wealth on us bestows;
	While its power unchanging holds us
	In a holy calm repose.
	God and Father,
	Unto Thee our worship flows.
```

###Ministry