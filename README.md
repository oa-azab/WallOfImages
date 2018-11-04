

Wall Of Images
=============

Simple Application that fetches Images data from remote api and cache the response in memory and using library for downloading resources and cache these resources in memory too

Project consist of two modules

 - App
 - ResourceLoaderLibrary

## App Module
UI layer is consist of
 - Activity -> observes ui data from the ViewModel and update UI
   automatically
 - ViewModel -> holds a liveData of UI data (Images, refreshState)

Data Layer
- I am using Room for local data storage and Paging to load images in recyclerView as pages
- Retrofit for network request

## ResourceLoaderLibrary Module
This module is responsible for downloading resources and cache them locally in memory LruCache

All fetch resource requests are handled on HandlerThread while the actual resource downloading in performed on ThreadPoolExcetor 

## Libraries

 - Android architecture components -> Room, Paging and LiveData
 - Retrofit + Gson -> networking