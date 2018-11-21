# Dante - Book Tracker

Dante lets you manage all your books by simply scanning the ISBN barcode of the book. 
It will automatically grab all information from Googles book database. 
The app let's you arrange your books into 3 different categories, whether you 
have read the book, are currently reading the book or saved the book for later. So you 
can simply keep track of your progress of all your books and their current states.

## Versions

### Version 4.0 - ALL IN THE CLOUD
- [ ] Firebase as Online Backend

### Version 3.5 - CAMPING WITH FIREBASE
- [ ] Redesign login flow with Firebase login
- [ ] Use Firebase Data for book suggestions


### Version 3.4 - HISTORY OF MONEY
- [ ] Reading history in statistics
- [ ] Enable language selection for manual add
- [ ] In-app purchases

### Version 3.3 - DETAILS & DEBTS
- [ ] Improve details page
- [ ] Get rid off Kotterknife

### Version 3.2 - SMALL, STEADY IMPROVEMENTS
- [x] Include book description in Download
- [x] Improve dark mode
- [x] Improve search view (refactor with ViewModel)
- [x] Improve preferences UI
- [x] Fix layout bugs of MainActivity
- [ ] Replace ImagePicker library with https://github.com/qingmei2/RxImagePicker
- [ ] Italian language support

### Version 3.1 - DARK STATISTICS FIXES
* Dark mode
* Statistics fixes and Redesign
* Sort by pages
* Flatten UI
* Add books manually
* Abstract Glide usage with interface and object class
* Use Timber with Crashlytics Tree and increase logging
* Supporter's badge
* Integrate feature flagging and config platform

### Version 3.0 - FRESH FUN
* Fix 'wrong dates' bug
* Change dates after insertion
* Refactor detail view
* Sort book list
* General Architecture redesign (abstract Realm to exchange it)
* Fix backup mechanism
* Fresh and new UI
* Add current books to statistics (read pages to read, other pages to waiting)
* Change Analytics backend (Keen -> Google/Firebase)

### Version 2.8 - PAGES, POSITION, PROGUARD
* Include Proguard
* Show book page state as Overlay on cover in BookAdapter
* Switch position of books in category with drag and drop

### Version 2.7 - SEARCHY STATS
* Change publish date for book
* Improve statistics screen
* Search feature

### Version 2.6 - DETAILED DESIGN
* Rate books 
* 100% Kotlin Port if possible
* Enter book page count manually 
* Adding notes to books

### Version 2.5 - REFACTOR RAMPAGE

* Introduce utility classes (BaseFragment, BaseActivity, BackNavigableActivity)
* Introduce KotterKnife
* Update to newest ButterKnife version
* Improve backup api
* Introduce GoogleSignIn
* Add Crashlytics
* Code cleanup and Kotlin Port
* Introduction / Showcase View
* DownloadBook / QueryCapture Activity merging 
* ViewPagerAdapter
* Adaptive Icons
