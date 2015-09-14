Rules
=====

###Bible

 - The prepare file must be named in in the format "bible%d.txt" where "%d" is the number of the book eg: for Genesis it would be "bible1.txt"
 - Each chapter is a line with only the page number in curly braces eg: for chapter 5 "{5}"
 - Each verse is one line
   + A verse line begins with the verse number then two spaces eg: for verse 13 "13  "
   + The whole verse must be on the rest of that line (do not split verses over multiple lines)

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
 - There is no special syntax for the end of a prepare document

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

 - The prepare file must be named in the format "%s%d.txt" where:
   + "%s" is the initials of the author (or accepted abbreviation) in lower case eg: for John Darby "jnd"
   + "%d" is the volume number
   + eg: JND volume 5 would be "jnd5.txt"
 - The first line of the document must be "{#%s}" where "%s" is the title of the book eg: For ajg1 "{#Piety and other addresses}"
 - There should be no blank lines
 - The page number should be above the text of that page in the format "{%d}" where %d is the page number eg: page 5 "{5}"
 - A blank page should have a page number and then no text eg: if pages 307 and 308 were blank:
 
```
	{306}
	page 306 text
	{307}
	{308}
	{309}
	page 309 text
```

 - A header is determined by the line being all capital letters
 - Scripture references
   + Start with an @ symbol
   + Then the book name
   + Then the chapter
   + Then optionally the verse number in the format ":%d" where "%d" is the verse number
   + eg: @1 Timothy 2: 1
 - Paragraph breaks are denoted by line breaks in the prepare document therefore a single paragraph must be entirely on one line
 - A block of italics should be surrounded with "\*" characters eg: "some text \*This text is in Italics\* more text"
 - When a word has a footnote add the "¦" (broken bar) character to the end of the word then *at the end of the page* start the footnote line with another "¦" (broken bar) character. eg:
   + multiple footnotes will be assigned in the order they are found in the page.

```
	{4}
	This page has some text. The word footnote¦ has a footnote. A second footnote is on the word markdown.¦
	¦This is where the footnote is explained
	¦A footnote with information on the word markdown is added here
	{5}
	Page 5 text
```
 - Any html tags that are necessary can be added to the prepare file as long as they are valid eg: "</hr>" tags for breaks in topics
 - There is no special syntax for the end of a prepare document
 - The first three pages of AJG volume 1 are included as an example:

```
	{#Piety and other addresses}
	{1}
	PIETY
	@1 Timothy 2: 1 -- 4; @1 Timothy 3: 14 -- 16; @1 Timothy 4: 4 -- 10; @1 Timothy 6: 3 -- 9, 11
	I feel impressed, dear brethren, I trust by the Lord, to say a word as to godliness, or piety, believing that there is great need of our being reminded of the importance of it. The Lord is bringing before us in these days much that is connected with the greatest truths of Christianity, the highest privileges of the assembly, intending that we should have power to move into these things, and that requires spirituality; but I believe that before we can be spiritual, we must become marked by righteousness practically, and piety. You will remember that the  Scripture speaks of Simeon in the second chapter of Luke's gospel; how the Holy Spirit was upon him and that it had been revealed to him by the Spirit that he should not see death till he had seen the Lord's Christ; and ho he came in the Spirit into the temple. He was one who was evidently characterised in large measure by spirituality, by being spiritual. But before saying these things about him, it says that he was just and pious, meaning that he was characteristically righteous practically in all his ways and that he was pious; and I believe those two things, practical righteousness and piety, are essential foundations to spirituality. That is, unless we are concerned and helped of the Lord to maintain righteousness practically in every detail of life and to cultivate piety, we shall not be able to go very far in the way of spirituality. The Lord is stressing, dear brethren, that the recovery of the truth which we are now enjoying -- truth that the Lord has given to the assembly now for the past hundred years -- has in mind that all the thoughts of God, the best thoughts regarding the assembly, should be entered
	{2}
	upon by us at the close just before we are taken to be with the Lord. But I say again, and I believe it is incontrovertible, that we cannot in any real power touch spiritual things unless there is a foundation with us of practical righteousness and piety.
	Now this first epistle to Timothy says much about piety. If you read through the epistle, I think you will be perhaps surprised at the constant references to piety, or godliness as it is rendered in the Authorised Version. In chapter 2 the apostle exhorts first of all that supplications, prayers, intercessions, and thanksgiving should be made for all men, for kings and those who are in authority, that we may lead quiet and peaceable lives, in all piety and gravity. It was a prominent thought in the mind of the apostle, so that he gives exhortation *first of all*, and with a view to this, that we may lead quiet and peaceable lives, in all piety and gravity; for, he says, "this is good and acceptable in the sight of our Saviour God, who will have all men to be saved and come to the knowledge of the truth." I believe the force is, dear brethren, that not only are we ourselves to be marked by piety and the blessing that flows from it, but the result of it is to be a testimony to others that saints who are marked by piety are manifestly in the good of salvation. God wants all men to be saved and to come to the knowledge of the truth, enjoying all the truth, involving the knowledge of God; and there is nothing like the knowledge of God to save us practically from all the different features that are current in the world around. Take discontent for instance; a great feature of the present time is discontent and many things of that kind, that characterise men at the present time. The great power for salvation from things of that sort is the knowledge of God, and piety brings the knowledge of God into our everyday circumstances; so that we are encouraged to prayer and supplication,
	{3}
	intercession and thanksgiving. All these are things that belong to the knowledge of God; indeed intercession is a wonderful privilege; it is the exercise by those who know God of their privilege of drawing near to God, it is the privilege of exercising that power on behalf of others.
	Abraham, who is spoken of as the friend of God, took up the attitude of intercession with God in regard of Sodom. What a wonderful privilege, that one man outside of those wicked cities, should exercise the power he had with God, as God's friend, in intercession on behalf of Sodom and Gomorrah! and yet not doing it in any sentimental way, but doing it with a right sense of what was due to God, having the knowledge of God. So that he says, "There are perhaps fifty righteous within the city." And God says, "If I find in Sodom fifty righteous within the city, then I will spare all the place for their sakes." And then Abraham comes down to forty-five, forty, and thirty, speaking in a becoming way, saying he was but dust and ashes, thus showing what a low sense he had of himself; for, like Job who repented in dust and ashes, Abraham had to do with God. He himself was but dust and ashes, and yet what power he had with God. He came down to twenty and he came down to ten, but he did not go below ten. That is to say, while he was thoroughly with God as discerning that men should be saved, yet at the same time he had a right sense of what was due to God, that if a city was so wicked that there were not even ten righteous in it, then it was not suitable to pray for its salvation, it should be judged. Both Sodom and Gomorrah had to be judged, yet at the same time all that was due to God was maintained by Abraham in the way he interceded. There is great need for that at the present time, for there is no question but that evil is increasing by leaps and bounds in the world.
```
