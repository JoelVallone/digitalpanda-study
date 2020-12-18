
RxJS and webworker message passing ?


# References
Angular data flow and components hierarchy: https://blog.angular-university.io/angular-2-smart-components-vs-presentation-components-whats-the-difference-when-to-use-each-and-why/
## Websocket 
blog
- in angular: 
    - ~> (03.2020) https://javascript-conference.com/blog/real-time-in-angular-a-journey-into-websocket-and-rxjs/
    - ~> (05.2019) https://medium.com/briebug-blog/making-use-of-websockets-in-angular-way-easier-than-you-expected-25dd0061db1d
    - (04.2017) https://tutorialedge.net/typescript/angular/angular-websockets-tutorial/
- low-level browser javascript: 
    - ~>(03.2019) https://aravishack.medium.com/websocket-and-webworker-4d33b1d064fb

basic example
- ~>https://www.tutorialspoint.com/html5/html5_websocket.htm

wiki
- ~>https://en.wikipedia.org/wiki/WebSocket

playframework backend example
- https://github.com/playframework/play-samples/tree/2.8.x/play-java-websocket-example

## Webworker (scale usage of sockets)
blog
- in angular: 
 - ~>(05.2020) basic: https://medium.com/swlh/angular-and-web-workers-17cd3bf9acca
 - ~>(06.2019) +rxjs: https://dev.to/zakhenry/observable-webworkers-with-angular-8-4k6 ... need 3rd party library :-(
- low-level browser javascript: 
 - ~>(03.2019) https://aravishack.medium.com/websocket-and-webworker-4d33b1d064fb
 - (11.2019) scaling websocket with workers https://ayushgp.github.io/scaling-websockets-using-sharedworkers/
 - (07.2019) https://golb.hplar.ch/2019/07/web-workers.html

Official doc: 
 - Angular: https://angular.io/guide/web-worker
 - Mozzila: https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API



# Webworker basic example 1
Useful example to explore low-level constructs of the WebWorker entities in the browser

Ref: 
- https://aravishack.medium.com/websocket-and-webworker-4d33b1d064fb

In the following is an explanation on how we can use WebWorker for creating an offline page.

Step 1: Check Registers a new Service Worker
```javascript
if ("serviceWorker" in navigator) {
  navigator
   .serviceWorker   
   .register("service-worker.js")  // worker custom JavaScript code to execute.        
   .then(reg => console.log("Registred with scope: ",reg.scope))          
   .catch(err => console.log("ServiceWorker registration failed: ", err));
}
```

Step 2: On the service-worker.js, we can initialize the cache and add files to it for offline use.
```javascript
let cacheName = "myappCache";
var filesToCache = [
  "index.html",
  "js/site.js",
  "css/style.css",
  "images/favicon.png",
  "images/desktop-bg.jpg",
  "images/mobile-bg-copy.jpg",
  "images/og-image.jpg"
];
self.addEventListener("install", function(e) {
  e.waitUntil(
    caches.open(cacheName).then(function(cache) {
      return cache.addAll(filesToCache);
    })
  );
});
```

Step3: On the service-worker.js, we have an activate event. Once a new ServiceWorker has installed & a previous version isn’t being used, the new one activates, and you get an activate event. It can be used to clear out the old cache we don’t need anymore.
```javascript
self.addEventListener("activate", function(e) {
  e.waitUntil(
    caches.keys().then(function(keyList) {
      return Promise.all(
        keyList.map(function(key) {
          if (key !== cacheName) {
            return caches.delete(key);
          }
        })
      );
    })
  );
  return self.clients.claim();
});
```

Step4: On the service-worker.js, we have a fetch event, which fires every time an HTTP request is fired off from our app.
```javascript
self.addEventListener("fetch", function(event) {
  event.respondWith(
    caches.match(event.request).then(function(response) {
      if (response) {
        return response;
      } else {
        return fetch(event.request)
          .then(function(res) {
            return caches.open(cacheName).then(function(cache) {
              cache.put(event.request.url, res.clone());
              return res;
            });
          })
          .catch(function(err) {
            return caches.open(cacheName).then(function(cache) {
              return cache.match("/offline.html");
            });
          });
      }
    })
  );
});
```

# Websocket basic example
Ref:
- https://www.tutorialspoint.com/html5/html5_websocket.htm

1. Clone https://github.com/googlearchive/pywebsocket into a folder as pywebsocket
2. Copy html conent example of https://www.tutorialspoint.com/html5/html5_websocket.htm into client.html
3. Run and start the backend websocket server:
    ```sh
    cd pywebsocket;
    python setup.py build
    sudo python setup.py install
    cd mod_pywebsocket;
    sudo python standalone.py -p 9998 -w ../example/
    ```
4. Open `client.html` in a browser and observe the client/server interaction with the alert messages and the network traffic flow in the browser console

